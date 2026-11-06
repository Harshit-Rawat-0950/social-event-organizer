package FriendSubSystem;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import InviteMangerSubSystem.InviteManagerFacade;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

public class FriendFacadeTest {
	private static final int A = 1;
	private static final int B = 2;
	private static final int MISSING = 99;

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Before
	public void setUp() {
		Fixtures.user(A, "Alice");
		Fixtures.user(B, "Bob");
	}

	private void assertRefused(String message, Runnable action) {
		try {
			action.run();
			fail("Expected refusal: " + message);
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage(), e.getMessage().contains(message));
		}
	}

	@Test
	public void friendRequestCreatesAnInviteAndNotifies() {
		FriendFacade.sendFriendRequest(A, B);
		assertEquals(1, InviteManagerFacade.getInvites(B).length);
		assertEquals(0, InviteManagerFacade.getInvites(A).length);
		assertTrue(Fixtures.texts(B).contains("New friend request from Alice (1)."));
		assertFalse(FriendFacade.areFriends(A, B));
	}

	@Test
	public void acceptingMakesThemFriendsBothWays() {
		FriendFacade.sendFriendRequest(A, B);
		InviteManagerFacade.acceptInvite(InviteManagerFacade.getInvites(B)[0]);
		assertTrue(FriendFacade.areFriends(A, B));
		assertTrue(FriendFacade.areFriends(B, A));
		assertEquals(Arrays.asList(B), FriendFacade.getFriendIds(A));
		assertEquals(Arrays.asList(A), FriendFacade.getFriendIds(B));
		assertTrue(Fixtures.texts(A).contains("Bob (2) accepted your friend request."));
	}

	@Test
	public void decliningLeavesThemStrangersAndAllowsAskingAgain() {
		FriendFacade.sendFriendRequest(A, B);
		InviteManagerFacade.rejectInvite(InviteManagerFacade.getInvites(B)[0]);
		assertFalse(FriendFacade.areFriends(A, B));
		FriendFacade.sendFriendRequest(A, B);
		assertEquals(1, InviteManagerFacade.getInvites(B).length);
	}

	@Test
	public void requestsAreRefusedWhenTheyMakeNoSense() {
		assertRefused("yourself", () -> FriendFacade.sendFriendRequest(A, A));
		assertRefused("No user with id", () -> FriendFacade.sendFriendRequest(A, MISSING));
		FriendFacade.sendFriendRequest(A, B);
		assertRefused("already sent", () -> FriendFacade.sendFriendRequest(A, B));
		assertRefused("Check your invites", () -> FriendFacade.sendFriendRequest(B, A));
		InviteManagerFacade.acceptInvite(InviteManagerFacade.getInvites(B)[0]);
		assertRefused("already friends", () -> FriendFacade.sendFriendRequest(A, B));
	}

	@Test
	public void acceptingTwiceDoesNotDuplicateTheFriendship() throws Exception {
		FriendFacade.createFriend("1,2,x");
		FriendFacade.createFriend("1,2,x");
		assertEquals(1, env.lines("friends.txt").size());
	}

	@Test
	public void removeFriendWorksFromEitherSide() {
		Fixtures.friends(A, B);
		FriendFacade.removeFriend(B, A);
		assertFalse(FriendFacade.areFriends(A, B));
		assertRefused("not friends", () -> FriendFacade.removeFriend(A, B));
	}

	@Test
	public void followIsOneWayAndNotifies() {
		FriendFacade.followUser(A, B);
		assertTrue(FriendFacade.isFollowing(A, B));
		assertFalse(FriendFacade.isFollowing(B, A));
		assertTrue(Fixtures.texts(B).contains("Alice started following you."));
		assertTrue(Fixtures.texts(A).isEmpty());
	}

	@Test
	public void followIsRefusedWhenItMakesNoSense() {
		assertRefused("yourself", () -> FriendFacade.followUser(A, A));
		assertRefused("No user with id", () -> FriendFacade.followUser(A, MISSING));
		FriendFacade.followUser(A, B);
		assertRefused("already follow", () -> FriendFacade.followUser(A, B));
	}

	@Test
	public void unfollowRemovesOnlyThatDirection() {
		Fixtures.follows(A, B);
		Fixtures.follows(B, A);
		FriendFacade.unfollowUser(A, B);
		assertFalse(FriendFacade.isFollowing(A, B));
		assertTrue(FriendFacade.isFollowing(B, A));
		assertRefused("do not follow", () -> FriendFacade.unfollowUser(A, B));
		assertRefused("No user with id", () -> FriendFacade.unfollowUser(A, MISSING));
	}

	@Test
	public void unfollowAndUnfriendAreIndependent() {
		Fixtures.friends(A, B);
		Fixtures.follows(A, B);
		FriendFacade.unfollowUser(A, B);
		assertTrue(FriendFacade.areFriends(A, B));
		Fixtures.follows(A, B);
		FriendFacade.removeFriend(A, B);
		assertTrue(FriendFacade.isFollowing(A, B));
	}

	@Test
	public void lookupsWithUnknownUsersAreSafe() {
		assertFalse(FriendFacade.areFriends(A, MISSING));
		assertFalse(FriendFacade.isFollowing(MISSING, A));
		assertRefused("No user with id", () -> FriendFacade.getUserConnections(MISSING));
	}

	@Test
	public void connectionsListEveryKind() {
		Fixtures.friends(A, B);
		Fixtures.follows(A, B);
		Fixtures.follows(B, A);
		assertEquals(3, FriendFacade.getUserConnections(A).size());
		assertEquals(1, FriendFacade.getConnections(A, "Friend").size());
		assertEquals(1, FriendFacade.getConnections(A, "Follower").size());
		assertEquals(1, FriendFacade.getConnections(A, "Following").size());
	}
}
