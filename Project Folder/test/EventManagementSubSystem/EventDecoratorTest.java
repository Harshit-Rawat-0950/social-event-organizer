package EventManagementSubSystem;

import static org.junit.Assert.*;

import org.junit.Test;

public class EventDecoratorTest {

	private BasicEvent base() {
		return new BasicEvent("A", "B", "2099-01-01", "10:00", "E", "F");
	}

	@Test
	public void musicAddsItsBand() {
		MusicEventDecorator d = new MusicEventDecorator(base(), "Queen");
		assertEquals("Queen", d.getBandName());
		d.setBandName("Abba");
		assertTrue(d.getDetails().endsWith("[Band: Abba]"));
		assertEquals(EventView.TagKind.MUSIC, d.describe().tags.get(0).kind);
		assertEquals("Abba", d.describe().tags.get(0).value);
	}

	@Test
	public void newsKeepsHeadlineAndSummary() {
		NewsEventDecorator d = new NewsEventDecorator(base(), "Big", "Very big");
		assertEquals("Big", d.getHeadline());
		assertEquals("Very big", d.getDescription());
		d.setHeadline("Huge");
		d.setDescription("Very huge");
		assertTrue(d.getDetails().endsWith("[News: Huge - Very huge]"));
		assertEquals(EventView.TagKind.NEWS, d.describe().tags.get(0).kind);
	}

	@Test
	public void outingAddsHotelAndStars() {
		OutingEventDecorator d = new OutingEventDecorator(base(), 4, "Taj");
		assertEquals(4, d.getStarRating());
		assertEquals("Taj", d.getHotel());
		d.setStarRating(5);
		d.setHotel("Oberoi");
		assertTrue(d.getDetails().endsWith("[Outing: Oberoi - 5 star]"));
		EventView.Tag tag = d.describe().tags.get(0);
		assertEquals(EventView.TagKind.OUTING, tag.kind);
		assertEquals(5, tag.level);
		assertEquals("Oberoi", tag.value);
	}

	@Test
	public void urgentAddsItsLevel() {
		UrgentEventDecorator d = new UrgentEventDecorator(base(), 2);
		assertEquals(2, d.getUrgencyLevel());
		d.setUrgencyLevel(5);
		assertTrue(d.getDetails().endsWith("[Urgency: 5/5]"));
		assertEquals(5, d.describe().tags.get(0).level);
	}

	@Test
	public void businessAddsItsType() {
		BusinessEventDecorator d = new BusinessEventDecorator(base(), "Sales");
		assertEquals("Sales", d.getType());
		d.setType("Tech");
		assertTrue(d.getDetails().endsWith("[Business: Tech]"));
		assertEquals(EventView.TagKind.BUSINESS, d.describe().tags.get(0).kind);
	}

	@Test
	public void stackedDecoratorsAllShowInOrder() {
		EventComponent stacked = new UrgentEventDecorator(new MusicEventDecorator(base(), "Queen"), 3);
		assertTrue(stacked.getDetails().endsWith("[Band: Queen] [Urgency: 3/5]"));
		EventView view = stacked.describe();
		assertEquals(EventView.TagKind.MUSIC, view.tags.get(0).kind);
		assertEquals(EventView.TagKind.URGENT, view.tags.get(1).kind);
	}

	@Test
	public void decoratingACompositeKeepsChildrenBelowTheTag() {
		CompositeEvent c = new CompositeEvent("Top", "d", "2099-01-01", "10:00", "l", "t");
		c.add(base());
		String[] lines = new MusicEventDecorator(c, "Queen").getDetails().split("\n");
		assertEquals(2, lines.length);
		assertTrue(lines[0].endsWith("[Band: Queen]"));
		assertTrue(lines[1].startsWith("    #0 A"));
	}

	@Test
	public void structureCallsReachTheWrappedEvent() {
		CompositeEvent c = new CompositeEvent("Top", "d", "2099-01-01", "10:00", "l", "t");
		EventComponent d = new MusicEventDecorator(c, "Queen");
		BasicEvent child = base();
		d.add(child);
		assertSame(child, d.getChild(0));
		d.remove(child);
		assertTrue(c.getChildren().isEmpty());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void aDecoratedLeafIsStillALeaf() {
		new MusicEventDecorator(base(), "Queen").add(base());
	}

	@Test
	public void everyStoredDecorationRebuildsTheRightClass() {
		assertTrue(EventDecorator.fromSpec(base(), "MUSIC|Queen") instanceof MusicEventDecorator);
		assertTrue(EventDecorator.fromSpec(base(), "NEWS|H|D") instanceof NewsEventDecorator);
		assertTrue(EventDecorator.fromSpec(base(), "OUTING|4|Taj") instanceof OutingEventDecorator);
		assertTrue(EventDecorator.fromSpec(base(), "URGENT|2") instanceof UrgentEventDecorator);
		assertTrue(EventDecorator.fromSpec(base(), "BUSINESS|Sales") instanceof BusinessEventDecorator);
	}

	@Test(expected = IllegalArgumentException.class)
	public void unknownStoredDecorationIsRejected() {
		EventDecorator.fromSpec(base(), "DANCE|Salsa");
	}
}
