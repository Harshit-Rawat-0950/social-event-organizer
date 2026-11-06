package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;

import EventManagementSubSystem.BasicEvent;
import EventManagementSubSystem.CompositeEvent;
import EventManagementSubSystem.MusicEventDecorator;
import TestSupport.ConsoleDriver;
import TestSupport.TestEnvironment;

public class DashboardFacadeTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	private BasicEvent basic() {
		return new BasicEvent("Name", "D", "2099-01-01", "10:00", "L", "T");
	}

	@Test
	public void everyEventKindDisplaysThroughItsOwnDisplayMethod() {
		assertTrue(ConsoleDriver.call(() -> basic().display()).contains("Name"));
		CompositeEvent composite = new CompositeEvent("Top", "D", "2099-01-01", "10:00", "L", "T");
		composite.add(basic());
		String out = ConsoleDriver.call(composite::display);
		assertTrue(out, out.contains("Top"));
		assertTrue(out, out.contains("Name"));
		String decorated = ConsoleDriver.call(() -> new MusicEventDecorator(basic(), "Queen").display());
		assertTrue(decorated, decorated.contains("[Band: Queen]"));
	}

	@Test
	public void loginPageNamesTheAppAndShowsNoLogoOnPlainTerminals() {
		String out = ConsoleDriver.call(DashboardFacade::displayLoginPage);
		assertTrue(out, out.contains("Event Management System v1.0"));
		assertFalse(out, out.contains(Glyph.FULL_BLOCK.fancy()));
	}

	@Test
	public void loginPageShowsTheLogoOnFancyTerminals() {
		env.set("ui.unicode", "on");
		assertTrue(ConsoleDriver.call(DashboardFacade::displayLoginPage).contains(Glyph.FULL_BLOCK.fancy()));
	}

	@Test
	public void infoBoxShowsItsLines() {
		String out = ConsoleDriver.call(() -> DashboardFacade.displayInfoBox("Title", Arrays.asList("a line")));
		assertTrue(out, out.contains("Title"));
		assertTrue(out, out.contains("a line"));
	}

	@Test
	public void plainProgressPrintsOneFullBar() {
		String out = ConsoleDriver.call(() -> DashboardFacade.displayProgress("Loading", 50));
		assertTrue(out, out.contains("Loading ####################"));
		assertFalse(out, out.contains("\r "));
	}

	@Test
	public void colourProgressAnimatesInPlace() {
		env.set("ui.color", "on");
		String out = ConsoleDriver.call(() -> DashboardFacade.displayProgress("Loading", 20));
		assertEquals(21, out.split("\r ", -1).length - 1);
	}

	@Test
	public void colourProgressWithNoTimeJustPrintsTheBar() {
		env.set("ui.color", "on");
		assertFalse(ConsoleDriver.call(() -> DashboardFacade.displayProgress("Loading", 0)).contains("\r "));
	}

	@Test
	public void messageHelpersReachTheScreen() {
		String out = ConsoleDriver.call(() -> {
			DashboardFacade.showSuccess("S");
			DashboardFacade.showError("E");
			DashboardFacade.showInfo("I");
			DashboardFacade.flushMessages();
		});
		assertTrue(out, out.contains("[ok] S"));
		assertTrue(out, out.contains("[!] E"));
		assertTrue(out, out.contains("[i] I"));
	}
}
