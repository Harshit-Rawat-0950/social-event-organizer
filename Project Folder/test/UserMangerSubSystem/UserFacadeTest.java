package UserMangerSubSystem;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import EventManagementSubSystem.EventVisibility;
import InviteMangerSubSystem.InviteManagerFacade;
import NotificationSubSystem.NotificationFacade;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

public class UserFacadeTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Before
	public void setUp() {
		Fixtures.user(1, "Alice");
		Fixtures.user(2, "Bob");
		Fixtures.user(3, "Alicia");
	}

	@Test
	public void nobodyIsLoggedInAtFirst() {
		assertNull(UserManagementFacade.getCurrUser());
		assertNull(CurrentApplicationUser.getInstance().getUserData());
	}

	@Test
	public void loginAndLogoutChangeTheCurrentUser() {
		Fixtures.login(1);
		assertEquals(1, UserManagementFacade.getCurrUser().getId());
		CurrentApplicationUser.getInstance().logout();
		assertNull(UserManagementFacade.getCurrUser());
	}

	@Test
	public void userDataIsGatheredAtLogin() {
		Fixtures.friends(1, 2);
		Fixtures.follows(1, 2);
		Fixtures.event(1, "Mine", EventVisibility.PUBLIC);
		int other = Fixtures.event(2, "Theirs", EventVisibility.PUBLIC);
		InviteManagerFacade.sendEventInvite(2, 1, other);
		NotificationFacade.sendNotification(1, "extra");
		Fixtures.login(1);
		User.UserData data = CurrentApplicationUser.getInstance().getUserData();
		assertEquals(2, data.getFriends().size());
		assertEquals(1, data.getCreatedEvents().size());
		assertEquals(1, data.getInvites().size());
		assertEquals(2, data.getNotifications().size());
	}

	@Test
	public void findUser() {
		assertEquals("Bob", UserManagementFacade.findUser(2).getName());
		assertNull(UserManagementFacade.findUser(9));
	}

	@Test
	public void displayNameFallsBackForUnknownUsers() {
		assertEquals("Bob (2)", UserManagementFacade.getDisplayName(2));
		assertEquals("User 9", UserManagementFacade.getDisplayName(9));
	}

	@Test
	public void activeMeansAnActiveStatusAndAnExistingUser() {
		assertTrue(UserManagementFacade.isUserActive(1));
		assertFalse(UserManagementFacade.isUserActive(9));
	}

	@Test
	public void searchMatchesNameOrEmailIgnoringCase() {
		assertEquals(2, UserManagementFacade.searchUsers("ALI").size());
		assertEquals(1, UserManagementFacade.searchUsers("bob@").size());
		assertTrue(UserManagementFacade.searchUsers("zzz").isEmpty());
	}

	@Test
	public void updateProfileChangesTheFileAndTheSession() {
		Fixtures.login(1);
		UserManagementFacade.updateProfile("Allie", "allie@x");
		assertEquals("Allie", UserManagementFacade.getCurrUser().getName());
		assertEquals("Allie", UserManagementFacade.findUser(1).getName());
		assertEquals("allie@x", UserManagementFacade.findUser(1).getEmail());
		assertEquals("Bob", UserManagementFacade.findUser(2).getName());
	}

	@Test
	public void changePasswordChecksTheCurrentOne() {
		Fixtures.login(1);
		try {
			UserManagementFacade.changePassword("wrong", "new");
			fail("Wrong current password was accepted");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("incorrect"));
		}
		PassWordManager pm = new PassWordManager(env.path("password.txt"));
		assertEquals(Fixtures.password(1), pm.getPassWord(1));
		UserManagementFacade.changePassword(Fixtures.password(1), "new");
		assertEquals("new", pm.getPassWord(1));
	}
}
