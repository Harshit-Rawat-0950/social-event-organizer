package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import EventManagementSubSystem.EventVisibility;
import NotificationSubSystem.NotificationFacade;
import TestSupport.ConsoleDriver;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

public class MainPageFlowTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Before
	public void setUp() {
		Fixtures.user(1, "Alice");
	}

	@Test
	public void menuListsEveryOption() {
		String out = ConsoleDriver.session(1);
		for (String item : new String[] {"1. Events", "2. Invites", "3. Notifications", "4. Reports", "5. Friends", "6. Profile", "0. Log out"}) {
			assertTrue(item, out.contains(item));
		}
	}

	@Test
	public void placeholdersAreNeverShown() {
		String out = ConsoleDriver.session(1);
		assertFalse(out, out.contains("{invites}"));
		assertFalse(out, out.contains("{notifications}"));
	}

	@Test
	public void noCountsWhenThereIsNothingNew() {
		String out = ConsoleDriver.session(1);
		assertFalse(out, out.contains("pending"));
		assertFalse(out, out.contains(" new"));
	}

	@Test
	public void countsShowLiveNumbers() {
		Fixtures.user(2, "Bob");
		Fixtures.pendingRequest(2, 1);
		Fixtures.invited(2, 1, Fixtures.event(2, "E", EventVisibility.PUBLIC), InviteMangerSubSystem.RsvpStatus.PENDING);
		NotificationFacade.sendNotification(1, "another");
		String out = ConsoleDriver.session(1);
		assertTrue(out, out.contains("2. Invites  - 2 pending"));
		assertTrue(out, out.contains("3. Notifications  - 2 new"));
	}

	@Test
	public void everyNumberOpensItsPageAndComesBack() {
		String[][] pages = {{"1", "r", "EVENTS"}, {"2", "r", "INVITES"}, {"3", "r", "NOTIFICATIONS"}, {"4", "", "REPORTS"}, {"5", "r", "FRIENDS"}, {"6", "r", "PROFILE"}};
		for (String[] page : pages) {
			String out = ConsoleDriver.session(1, page[0], page[1]);
			assertTrue(page[2], out.contains(page[2]));
			assertTrue("Back at the menu", out.lastIndexOf("MAIN MENU") > out.indexOf(page[2]));
		}
	}

	@Test
	public void invalidChoicesStayOnTheMenu() {
		String out = ConsoleDriver.session(1, "9", "abc", "-1");
		assertTrue(out, out.contains("Please choose one of the options listed."));
		assertTrue(out, out.contains("Please enter a whole number."));
	}

	@Test
	public void sameInstanceEverywhere() {
		assertSame(MainPage.getInstance(), MainPage.getInstance());
	}
}
