package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import EventManagementSubSystem.BasicEvent;
import EventManagementSubSystem.BusinessEventDecorator;
import EventManagementSubSystem.CompositeEvent;
import EventManagementSubSystem.EventComponent;
import EventManagementSubSystem.EventView;
import EventManagementSubSystem.EventVisibility;
import EventManagementSubSystem.MusicEventDecorator;
import EventManagementSubSystem.NewsEventDecorator;
import EventManagementSubSystem.OutingEventDecorator;
import EventManagementSubSystem.UrgentEventDecorator;
import TestSupport.TestEnvironment;

public class EventCardRendererTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	private BasicEvent basic(int id, String name) {
		return BasicEvent.fromString(id + ",1,0,ACTIVE,BASIC," + name + ",Desc,2099-01-01,10:00,Hall,Party,,PUBLIC");
	}

	private CompositeEvent composite(int id, String name) {
		return (CompositeEvent) BasicEvent.fromString(id + ",1,0,ACTIVE,COMPOSITE," + name + ",Desc,2099-01-01,10:00,Hall,Party,,PUBLIC");
	}

	private String card(EventComponent event) {
		return String.join("\n", EventCardRenderer.render(event.describe()));
	}

	private void assertAllLinesWide(List<String> lines, int width) {
		for (String line : lines) {
			assertEquals("[" + Style.strip(line) + "]", width, Style.visibleLength(line));
		}
	}

	@Test
	public void showsEveryField() {
		String out = card(basic(3, "Party"));
		for (String part : new String[] {"#3 Party", "Public", "2099-01-01 10:00 @ Hall", "Party", "Desc"}) {
			assertTrue(part + " in " + out, out.contains(part));
		}
	}

	@Test
	public void showsTheVisibility() {
		BasicEvent event = basic(1, "E");
		for (EventVisibility visibility : EventVisibility.values()) {
			event.setVisibility(visibility);
			assertTrue(card(event).contains(visibility.label()));
		}
	}

	@Test
	public void plainTagsAreSpelledOut() {
		EventComponent e = new BusinessEventDecorator(new UrgentEventDecorator(new OutingEventDecorator(new NewsEventDecorator(new MusicEventDecorator(basic(1, "E"), "Queen"), "Big", "Very big"), 4, "Taj"), 3), "Sales");
		String out = card(e);
		for (String tag : new String[] {"[Band: Queen]", "[News: Big - Very big]", "[Outing: Taj - 4 star]", "[Urgency: 3/5]", "[Business: Sales]"}) {
			assertTrue(tag + " in " + out, out.contains(tag));
		}
	}

	@Test
	public void fancyTagsUseSymbolsAndLevels() {
		env.set("ui.unicode", "on");
		EventComponent e = new BusinessEventDecorator(new UrgentEventDecorator(new OutingEventDecorator(new NewsEventDecorator(new MusicEventDecorator(basic(1, "E"), "Queen"), "Big", "Very big"), 4, "Taj"), 3), "Sales");
		String out = card(e);
		assertTrue(out, out.contains(Glyph.MUSIC.fancy() + " Queen"));
		assertTrue(out, out.contains(Glyph.NEWS.fancy() + " Big - Very big"));
		assertTrue(out, out.contains(Glyph.OUTING.fancy() + " Taj ****"));
		assertTrue(out, out.contains(Glyph.BUSINESS.fancy() + " Sales"));
		String u = Glyph.URGENT.fancy();
		String l = Glyph.LIGHT_BLOCK.fancy();
		assertTrue(out, out.contains(u + u + u + l + l + " Urgent"));
	}

	@Test
	public void subEventsFormATreeInOrder() {
		CompositeEvent top = composite(1, "Top");
		CompositeEvent middle = composite(2, "Middle");
		middle.add(basic(3, "Deep"));
		top.add(middle);
		top.add(basic(4, "Last"));
		String[] lines = card(top).split("\n");
		String out = String.join("\n", lines);
		assertTrue(out, out.contains("|- #2 Middle"));
		assertTrue(out, out.contains("|  `- #3 Deep"));
		assertTrue(out, out.contains("`- #4 Last"));
		assertTrue(out, out.indexOf("#2 Middle") < out.indexOf("#3 Deep"));
		assertTrue(out, out.indexOf("#3 Deep") < out.indexOf("#4 Last"));
	}

	@Test
	public void lastBranchUsesBlankIndentForItsChildren() {
		CompositeEvent top = composite(1, "Top");
		CompositeEvent only = composite(2, "Only");
		only.add(basic(3, "Deep"));
		top.add(only);
		assertTrue(card(top).contains("`- #2 Only"));
		assertTrue(card(top).contains("   `- #3 Deep"));
	}

	@Test
	public void subEventTagsStayWithTheSubEvent() {
		CompositeEvent top = composite(1, "Top");
		top.add(new MusicEventDecorator(basic(2, "Gig"), "Queen"));
		String out = card(top);
		assertTrue(out, out.indexOf("[Band: Queen]") > out.indexOf("#2 Gig"));
	}

	@Test
	public void cancelledEventsAreMarked() {
		BasicEvent off = BasicEvent.fromString("1,1,0,CANCELLED,BASIC,Off,D,2099-01-01,10:00,H,T,,PUBLIC");
		String out = card(off);
		assertTrue(out, out.contains("CANCELLED"));
		assertFalse(out, out.contains(" days"));
	}

	@Test
	public void cancelledEventsAreStruckThroughInColour() {
		env.set("ui.color", "on");
		BasicEvent off = BasicEvent.fromString("1,1,0,CANCELLED,BASIC,Off,D,2099-01-01,10:00,H,T,,PUBLIC");
		assertTrue(card(off).contains("\033[9;38;5;"));
	}

	@Test
	public void cancelledSubEventsAreMarked() {
		CompositeEvent top = composite(1, "Top");
		top.add(BasicEvent.fromString("2,1,1,CANCELLED,BASIC,Off,D,2099-01-01,10:00,H,T,,PUBLIC"));
		assertTrue(card(top).contains("#2 Off [CANCELLED]"));
	}

	@Test
	public void responsesShowOnlyWhenThereAreSome() {
		EventView view = basic(1, "E").describe();
		assertFalse(String.join("\n", EventCardRenderer.render(view)).contains("going"));
		view.going = 3;
		view.maybe = 1;
		view.waiting = 2;
		view.declined = 4;
		assertTrue(String.join("\n", EventCardRenderer.render(view)).contains("3 going - 1 maybe - 2 waiting - 4 declined"));
	}

	@Test
	public void countdownCoversEveryCase() {
		LocalDate today = LocalDate.now();
		assertEquals("today", EventCardRenderer.countdown(today.toString()));
		assertEquals("tomorrow", EventCardRenderer.countdown(today.plusDays(1).toString()));
		assertEquals("in 2 days", EventCardRenderer.countdown(today.plusDays(2).toString()));
		assertEquals("in 30 days", EventCardRenderer.countdown(today.plusDays(30).toString()));
		assertEquals("yesterday", EventCardRenderer.countdown(today.minusDays(1).toString()));
		assertEquals("5 days ago", EventCardRenderer.countdown(today.minusDays(5).toString()));
		assertEquals("", EventCardRenderer.countdown("not a date"));
	}

	@Test
	public void cardKeepsItsShapeWithWideContentInEveryMode() {
		String longText = "Word ".repeat(60).trim();
		CompositeEvent top = (CompositeEvent) BasicEvent.fromString("1,1,0,ACTIVE,COMPOSITE," + longText + "," + longText + ",2099-01-01,10:00," + longText + "," + longText + ",,FRIENDS");
		CompositeEvent middle = composite(2, longText);
		middle.add(new MusicEventDecorator(BasicEvent.fromString("3,1,2,ACTIVE,BASIC,Deep,D,2099-01-01,10:00," + longText + ",T,,PUBLIC"), longText));
		top.add(middle);
		EventComponent decorated = new NewsEventDecorator(new UrgentEventDecorator(top, 5), longText, longText);
		for (String color : new String[] {"off", "on"}) {
			for (String unicode : new String[] {"off", "on"}) {
				for (int width : new int[] {40, 78, 120}) {
					env.set("ui.color", color);
					env.set("ui.unicode", unicode);
					env.set("ui.width", String.valueOf(width));
					EventView view = decorated.describe();
					view.going = 100;
					view.declined = 1;
					assertAllLinesWide(EventCardRenderer.render(view), width);
				}
			}
		}
	}

	@Test
	public void longTextIsWrappedRatherThanLost() {
		env.set("ui.width", "40");
		BasicEvent e = BasicEvent.fromString("1,1,0,ACTIVE,BASIC,E,alpha beta gamma delta epsilon zeta eta theta iota kappa lambda mu,2099-01-01,10:00,H,T,,PUBLIC");
		String out = card(e);
		assertTrue(out, out.contains("lambda"));
		assertTrue(out, out.contains("alpha"));
	}

	@Test
	public void plainModeHasNoEscapeCodes() {
		assertFalse(card(new MusicEventDecorator(basic(1, "E"), "Queen")).contains("\033"));
	}
}
