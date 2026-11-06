package FriendSubSystem;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import InviteMangerSubSystem.InviteManagerFacade;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

// All 16 states two users can be in, checked from both sides
@RunWith(Parameterized.class)
public class FriendPairStatesTest {
	private static final int A = 1;
	private static final int B = 2;

	private enum Link { NONE, REQUEST_A_TO_B, REQUEST_B_TO_A, FRIENDS }

	@Parameters(name = "{0} aFollowsB={1} bFollowsA={2}")
	public static List<Object[]> cases() {
		boolean[] both = {false, true};
		List<Object[]> cases = new ArrayList<>();
		for (Link link : Link.values()) {
			for (boolean aFollowsB : both) {
				for (boolean bFollowsA : both) {
					cases.add(new Object[] {link, aFollowsB, bFollowsA});
				}
			}
		}
		return cases;
	}

	@Rule
	public TestEnvironment env = new TestEnvironment();

	private final Link link;
	private final boolean aFollowsB;
	private final boolean bFollowsA;

	public FriendPairStatesTest(Link link, boolean aFollowsB, boolean bFollowsA) {
		this.link = link;
		this.aFollowsB = aFollowsB;
		this.bFollowsA = bFollowsA;
	}

	@Before
	public void setUp() {
		Fixtures.user(A, "Alice");
		Fixtures.user(B, "Bob");
		if (link == Link.FRIENDS) {
			Fixtures.friends(A, B);
		} else if (link == Link.REQUEST_A_TO_B) {
			Fixtures.pendingRequest(A, B);
		} else if (link == Link.REQUEST_B_TO_A) {
			Fixtures.pendingRequest(B, A);
		}
		if (aFollowsB) {
			Fixtures.follows(A, B);
		}
		if (bFollowsA) {
			Fixtures.follows(B, A);
		}
	}

	private static Set<String> connections(int user) {
		Set<String> result = new TreeSet<>();
		for (FriendManagement connection : FriendFacade.getUserConnections(user)) {
			result.add(connection.getStatus() + ":" + connection.getTargetUserId());
		}
		return result;
	}

	private static Set<String> expected(boolean friend, boolean following, boolean follower, int other) {
		Set<String> result = new TreeSet<>();
		if (friend) {
			result.add("FRIEND:" + other);
		}
		if (following) {
			result.add("FOLLOWING:" + other);
		}
		if (follower) {
			result.add("FOLLOWER:" + other);
		}
		return result;
	}

	@Test
	public void bothUsersSeeTheCorrectConnections() {
		boolean friends = link == Link.FRIENDS;
		assertEquals(expected(friends, aFollowsB, bFollowsA, B), connections(A));
		assertEquals(expected(friends, bFollowsA, aFollowsB, A), connections(B));
	}

	@Test
	public void lookupsAgreeWithTheState() {
		assertEquals(link == Link.FRIENDS, FriendFacade.areFriends(A, B));
		assertEquals(link == Link.FRIENDS, FriendFacade.areFriends(B, A));
		assertEquals(aFollowsB, FriendFacade.isFollowing(A, B));
		assertEquals(bFollowsA, FriendFacade.isFollowing(B, A));
	}

	@Test
	public void pendingRequestAppearsOnlyForItsReceiver() {
		assertEquals(link == Link.REQUEST_B_TO_A ? 1 : 0, InviteManagerFacade.getInvites(A).length);
		assertEquals(link == Link.REQUEST_A_TO_B ? 1 : 0, InviteManagerFacade.getInvites(B).length);
	}

	@Test
	public void anotherRequestSucceedsOnlyFromTheNoneState() {
		boolean accepted = true;
		try {
			FriendFacade.sendFriendRequest(A, B);
		} catch (IllegalArgumentException e) {
			accepted = false;
		}
		assertEquals(link == Link.NONE, accepted);
	}

	@Test
	public void followSucceedsOnlyWhenNotAlreadyFollowing() {
		boolean accepted = true;
		try {
			FriendFacade.followUser(A, B);
		} catch (IllegalArgumentException e) {
			accepted = false;
		}
		assertEquals(!aFollowsB, accepted);
	}
}
