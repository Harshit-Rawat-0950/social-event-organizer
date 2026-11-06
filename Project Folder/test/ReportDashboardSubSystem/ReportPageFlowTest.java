package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import EventManagementSubSystem.EventVisibility;
import InviteMangerSubSystem.RsvpStatus;
import NotificationSubSystem.NotificationFacade;
import TestSupport.ConsoleDriver;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

public class ReportPageFlowTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Before
	public void setUp() {
		Fixtures.user(1, "Alice");
		Fixtures.user(2, "Bob");
		Fixtures.user(3, "Carol");
	}

	private String report(int user) {
		return ConsoleDriver.session(user, "4", "");
	}

	private void assertStat(String out, String label, int value) {
		assertTrue(label + "=" + value + " in\n" + out, Pattern.compile(label + "\\s+" + value + "\\s").matcher(out).find());
	}

	@Test
	public void emptyReport() {
		String out = report(1);
		assertTrue(out, out.contains("Report for Alice"));
		assertTrue(out, out.contains("(you have not created any events)"));
		for (String label : new String[] {"Events created", "Pending invites", "Notifications", "Friends", "Followers", "Following"}) {
			assertStat(out, label, 0);
		}
	}

	@Test
	public void everyNumberIsCounted() {
		Fixtures.friends(1, 2);
		Fixtures.follows(1, 2);
		Fixtures.follows(1, 3);
		Fixtures.follows(3, 1);
		Fixtures.event(1, "A", EventVisibility.PUBLIC);
		Fixtures.event(1, "B", EventVisibility.PUBLIC);
		Fixtures.pendingRequest(3, 1);
		NotificationFacade.sendNotification(1, "x");
		String out = report(1);
		assertStat(out, "Events created", 2);
		assertStat(out, "Pending invites", 1);
		assertStat(out, "Notifications", 1);
		assertStat(out, "Friends", 1);
		assertStat(out, "Followers", 1);
		assertStat(out, "Following", 2);
	}

	@Test
	public void responsesAreShownPerMainEvent() {
		int top = Fixtures.composite(1, "Fest", EventVisibility.PUBLIC);
		Fixtures.subEvent(top, 1, "Talk");
		Fixtures.invited(1, 2, top, RsvpStatus.ACCEPTED);
		Fixtures.invited(1, 3, top, RsvpStatus.DECLINED);
		String out = report(1);
		assertTrue(out, out.contains("#1 Fest"));
		assertTrue(out, out.contains("1 going - 0 maybe - 0 waiting - 1 declined"));
		assertFalse("Sub-events are not listed", out.contains("#2 Talk"));
	}

	@Test
	public void othersEventsAreNotInMyReport() {
		Fixtures.event(2, "Theirs", EventVisibility.PUBLIC);
		assertFalse(report(1).contains("Theirs"));
	}

	@Test
	public void sameInstanceEverywhere() {
		assertSame(ReportPage.getInstance(), ReportPage.getInstance());
	}
}
