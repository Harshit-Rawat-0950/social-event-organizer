package FriendSubSystem;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import UserMangerSubSystem.User;

public class FriendRepoTest {
	private Path friends;
	private Path follows;
	private FriendRepo repo;
	private final User alice = new User(1, "Alice", "a@x", "r", "ACTIVE");
	private final User bob = new User(2, "Bob", "b@x", "r", "ACTIVE");
	private final User carol = new User(3, "Carol", "c@x", "r", "ACTIVE");

	@Before
	public void setUp() throws Exception {
		friends = Files.createTempFile("friends", ".txt");
		follows = Files.createTempFile("follows", ".txt");
		repo = new FriendRepo(friends.toString(), follows.toString());
	}

	@After
	public void tearDown() throws Exception {
		Files.deleteIfExists(friends);
		Files.deleteIfExists(follows);
	}

	@Test
	public void missingFilesAreCreated() throws Exception {
		Files.delete(friends);
		Files.delete(follows);
		new FriendRepo(friends.toString(), follows.toString());
		assertTrue(Files.exists(friends));
		assertTrue(Files.exists(follows));
	}

	@Test
	public void friendshipIsMutualAndKeepsCloseness() {
		repo.createRelationship(alice, bob, 3);
		assertEquals(2, repo.getRelationship(alice, bob, "Friend").getTargetUserId());
		assertEquals(1, repo.getRelationship(bob, alice, "Friend").getTargetUserId());
		assertTrue(repo.getallRelationship(bob, "Friend").get(0).getDetails().contains("closeness 3"));
	}

	@Test
	public void followGoesToTheFollowFileAndIsOneWay() throws Exception {
		repo.createRelationship(alice, bob);
		assertEquals(0, Files.readAllLines(friends).size());
		assertEquals(1, Files.readAllLines(follows).size());
		assertEquals("FOLLOWING", repo.getRelationship(alice, bob, "Following").getStatus());
		assertEquals("FOLLOWER", repo.getRelationship(bob, alice, "Follower").getStatus());
		assertNull(repo.getRelationship(bob, alice, "Following"));
		assertNull(repo.getRelationship(alice, bob, "Follower"));
	}

	@Test
	public void noRelationshipGivesNullForEveryType() {
		assertNull(repo.getRelationship(alice, bob, "Friend"));
		assertNull(repo.getRelationship(alice, bob, "Following"));
		assertNull(repo.getRelationship(alice, bob, "Follower"));
		assertNull(repo.getRelationship(alice, bob, "Enemy"));
	}

	@Test
	public void allRelationshipsCombinesEveryType() {
		repo.createRelationship(alice, bob, 0);
		repo.createRelationship(alice, bob);
		repo.createRelationship(bob, alice);
		assertEquals(3, repo.getallRelationship(alice).size());
	}

	@Test
	public void relationshipsOfOthersAreNotIncluded() {
		repo.createRelationship(bob, carol, 0);
		repo.createRelationship(bob, carol);
		assertTrue(repo.getallRelationship(alice).isEmpty());
	}

	@Test
	public void unfollowKeepsTheFriendshipAndTheReverseFollow() {
		repo.createRelationship(alice, bob, 0);
		repo.createRelationship(alice, bob);
		repo.createRelationship(bob, alice);
		repo.deleteFollow(alice.getId(), bob.getId());
		assertNull(repo.getRelationship(alice, bob, "Following"));
		assertNotNull(repo.getRelationship(bob, alice, "Following"));
		assertNotNull(repo.getRelationship(alice, bob, "Friend"));
	}

	@Test
	public void removingAFriendWorksFromEitherSide() {
		repo.createRelationship(alice, bob, 0);
		repo.deleteFriend(bob.getId(), alice.getId());
		assertNull(repo.getRelationship(alice, bob, "Friend"));
	}

	@Test
	public void removingAFriendLeavesOtherFriendsAndFollows() {
		repo.createRelationship(alice, bob, 0);
		repo.createRelationship(alice, carol, 0);
		repo.createRelationship(alice, bob);
		repo.deleteFriend(alice.getId(), bob.getId());
		assertNotNull(repo.getRelationship(alice, carol, "Friend"));
		assertNotNull(repo.getRelationship(alice, bob, "Following"));
	}

	@Test
	public void deleteRelationshipClearsEverything() {
		repo.createRelationship(alice, bob, 0);
		repo.createRelationship(alice, bob);
		repo.createRelationship(bob, alice);
		repo.deleteRelationship(alice.getId(), bob.getId());
		assertTrue(repo.getallRelationship(alice).isEmpty());
	}

	@Test
	public void rawCreatorsWriteTheRightFiles() throws Exception {
		repo.createFriend("1,2,2099-01-01T10:00");
		repo.createFollower("2,1");
		assertEquals("1,2,0", Files.readAllLines(friends).get(0));
		assertNotNull(repo.getRelationship(bob, alice, "Following"));
	}

	@Test
	public void relationshipObjectsDescribeThemselves() {
		Friend friend = new Friend(1, 2, 7);
		Follower follower = new Follower(1, 2);
		Following following = new Following(1, 2);
		assertEquals("1 and 2 are friends, closeness 7", friend.getDetails());
		assertTrue(follower.getDetails().startsWith("2 follows 1"));
		assertTrue(following.getDetails().startsWith("1 follows 2"));
		for (FriendManagement relation : new FriendManagement[] {friend, follower, following}) {
			assertEquals(1, relation.getUserId());
			assertEquals(2, relation.getTargetUserId());
			relation.updateStatus("X");
			assertEquals("X", relation.getStatus());
		}
	}
}
