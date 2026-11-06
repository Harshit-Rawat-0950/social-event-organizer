package UserMangerSubSystem;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import EventManagementSubSystem.EventVisibility;
import NotificationSubSystem.NotificationFacade;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

public class SystemDataServiceTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Test
	public void missingFilesCountAsZero() {
		assertEquals("events=0 invites=0 friends=0 follow=0 notification=0 rsvp=0", new SystemDataService().loadInitialData());
	}

	@Test
	public void recordsAreCounted() {
		Fixtures.user(1, "Alice");
		Fixtures.user(2, "Bob");
		Fixtures.event(1, "E", EventVisibility.PUBLIC);
		Fixtures.friends(1, 2);
		NotificationFacade.sendNotification(1, "hello");
		assertEquals("events=1 invites=0 friends=1 follow=0 notification=1 rsvp=0", new SystemDataService().loadInitialData());
	}
}
