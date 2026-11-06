package NotificationSubSystem;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import TestSupport.TestEnvironment;

public class NotificationFacadeTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Test
	public void sentNotificationsReachOnlyTheirRecipientInOrder() {
		NotificationFacade.sendNotification(1, "first");
		NotificationFacade.sendNotification(2, "other");
		NotificationFacade.sendNotification(1, "second");
		assertEquals(2, NotificationFacade.getNotifications(1).size());
		assertEquals("first", NotificationFacade.getNotifications(1).get(0).text);
		assertEquals("second", NotificationFacade.getNotifications(1).get(1).text);
	}

	@Test
	public void clearingLeavesOtherUsersAlone() {
		NotificationFacade.sendNotification(1, "mine");
		NotificationFacade.sendNotification(2, "theirs");
		NotificationFacade.clearNotifications(1);
		assertTrue(NotificationFacade.getNotifications(1).isEmpty());
		assertEquals(1, NotificationFacade.getNotifications(2).size());
	}

	@Test
	public void noNotificationsGivesAnEmptyList() {
		assertTrue(NotificationFacade.getNotifications(5).isEmpty());
	}
}
