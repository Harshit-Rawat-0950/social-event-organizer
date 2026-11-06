package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import FriendSubSystem.FriendFacade;
import InviteMangerSubSystem.InviteManagerFacade;
import TestSupport.ConsoleDriver;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

public class FriendsPageFlowTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Before
	public void setUp() {
		Fixtures.user(1, "Alice");
		Fixtures.user(2, "Bob");
	}

	private String page(String... keys) {
		String[] all = new String[keys.length + 2];
		all[0] = "5";
		System.arraycopy(keys, 0, all, 1, keys.length);
		all[all.length - 1] = "r";
		return ConsoleDriver.session(1, all);
	}

	@Test
	public void emptyState() {
		assertTrue(page().contains("(no connections yet)"));
	}

	@Test
	public void tableShowsEveryRelation() {
		Fixtures.friends(1, 2);
		Fixtures.follows(1, 2);
		Fixtures.follows(2, 1);
		String out = page();
		assertTrue(out, out.contains("Relation"));
		assertTrue(out, out.contains("[FRIEND]"));
		assertTrue(out, out.contains("[FOLLOWER]"));
		assertTrue(out, out.contains("[FOLLOWING]"));
		assertTrue(out, out.contains("Bob"));
	}

	@Test
	public void searchFindsAndReportsNoMatch() {
		String out = page("s", "bob", "s", "zzz");
		assertTrue(out, out.contains("Found 1 user(s):"));
		assertTrue(out, out.contains("Bob (2) - bob@x"));
		assertTrue(out, out.contains("No users found."));
	}

	@Test
	public void addFriendSendsARequest() {
		String out = page("a", "2");
		assertTrue(out, out.contains("Friend request sent."));
		assertEquals(1, InviteManagerFacade.getInvites(2).length);
	}

	@Test
	public void removeFriend() {
		Fixtures.friends(1, 2);
		assertTrue(page("d", "2").contains("Friend removed."));
		assertFalse(FriendFacade.areFriends(1, 2));
	}

	@Test
	public void followAndUnfollow() {
		String out = page("f", "2", "u", "2");
		assertTrue(out, out.contains("Now following."));
		assertTrue(out, out.contains("[FOLLOWING]"));
		assertTrue(out, out.contains("Unfollowed."));
		assertFalse(FriendFacade.isFollowing(1, 2));
	}

	@Test
	public void refusalsAreShownAndThePageCarriesOn() {
		String out = page("a", "1", "a", "99", "d", "2", "f", "1", "u", "2", "z");
		assertTrue(out, out.contains("You cannot send a friend request to yourself."));
		assertTrue(out, out.contains("No user with id 99."));
		assertTrue(out, out.contains("You are not friends with user 2."));
		assertTrue(out, out.contains("You cannot follow yourself."));
		assertTrue(out, out.contains("You do not follow user 2."));
		assertTrue(out, out.contains("Please choose one of the options listed."));
	}

	@Test
	public void sameInstanceEverywhere() {
		assertSame(FriendsPage.getInstance(), FriendsPage.getInstance());
	}
}
