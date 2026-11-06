package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import NotificationSubSystem.NotificationFacade;
import TestSupport.ConsoleDriver;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

public class NotificationPageFlowTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Before
	public void setUp() {
		Fixtures.user(1, "Alice");
		Fixtures.user(2, "Bob");
	}

	private String page(int user, String... keys) {
		String[] all = new String[keys.length + 2];
		all[0] = "3";
		System.arraycopy(keys, 0, all, 1, keys.length);
		all[all.length - 1] = "r";
		return ConsoleDriver.session(user, all);
	}

	@Test
	public void emptyState() {
		assertTrue(page(1).contains("(no notifications)"));
	}

	@Test
	public void showsTextAndAge() {
		NotificationFacade.sendNotification(1, "Hello there");
		String out = page(1);
		assertTrue(out, out.contains("Hello there"));
		assertTrue(out, out.contains("just now"));
	}

	@Test
	public void onlyYourOwnAppear() {
		NotificationFacade.sendNotification(2, "For Bob");
		assertFalse(page(1).contains("For Bob"));
	}

	@Test
	public void clearEmptiesTheList() {
		NotificationFacade.sendNotification(1, "Hello");
		String out = page(1, "c");
		assertTrue(out, out.contains("Notifications cleared."));
		assertTrue(out, out.contains("(no notifications)"));
		assertTrue(NotificationFacade.getNotifications(1).isEmpty());
	}

	@Test
	public void longMessagesWrapOnNarrowScreens() {
		env.set("ui.width", "40");
		NotificationFacade.sendNotification(1, "A very long message that really cannot fit on one narrow line at all");
		String out = page(1);
		assertTrue(out, out.contains("cannot fit on one narrow"));
		for (String line : out.split("\\R")) {
			if (line.contains("very long") || line.contains("narrow")) {
				assertTrue(line, line.length() <= 40);
			}
		}
	}

	@Test
	public void invalidKeyIsExplained() {
		assertTrue(page(1, "z").contains("Please choose one of the options listed."));
	}

	@Test
	public void sameInstanceEverywhere() {
		assertSame(NotificationPage.getInstance(), NotificationPage.getInstance());
	}
}
