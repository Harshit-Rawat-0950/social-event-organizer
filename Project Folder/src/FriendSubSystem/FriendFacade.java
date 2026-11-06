package FriendSubSystem;

import java.util.ArrayList;
import java.util.List;

import InviteMangerSubSystem.InviteManagerFacade;
import NotificationSubSystem.NotificationFacade;
import UserMangerSubSystem.ConfigLoader;
import UserMangerSubSystem.User;
import UserMangerSubSystem.UserManagementFacade;

/*
 * Goal is to implement all interaction of other subsystem through this class
 * only creating a unified interface for the entire system. However this class
 * in itself must not perform any function other than delegation to the
 * respective class of any and all interactions possible from other
 * parts of project.
 **/
public class FriendFacade {

	private static FriendRepo repo() {
		ConfigLoader configLoader = new ConfigLoader();
		return new FriendRepo(configLoader.getProperty("Files.friends"), configLoader.getProperty("Files.follow"));
	}

	private static User requireUser(int id) {
		User user = UserManagementFacade.findUser(id);
		if(user == null)
			throw new IllegalArgumentException("No user with id " + id + ".");
		return user;
	}

	// Called with a raw "senderId,receiverId,..." invite once it has been accepted
	public static void createFriend(String invite) {
		String[] parts = invite.split(",");
		if(!areFriends(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])))
			repo().createFriend(invite);
	}

	public static void sendFriendRequest(int fromId, int toId) {
		requireUser(toId);
		if(fromId == toId)
			throw new IllegalArgumentException("You cannot send a friend request to yourself.");
		if(areFriends(fromId, toId))
			throw new IllegalArgumentException("You are already friends.");
		InviteManagerFacade.sendFriendInvite(fromId, toId);
	}

	public static void removeFriend(int userId, int friendId) {
		if(!areFriends(userId, friendId))
			throw new IllegalArgumentException("You are not friends with user " + friendId + ".");
		repo().deleteFriend(userId, friendId);
	}

	public static void followUser(int userId, int targetId) {
		User user = requireUser(userId);
		User target = requireUser(targetId);
		if(userId == targetId)
			throw new IllegalArgumentException("You cannot follow yourself.");
		FriendRepo repo = repo();
		if(repo.getRelationship(user, target, "Following") != null)
			throw new IllegalArgumentException("You already follow this user.");
		repo.createRelationship(user, target);
		NotificationFacade.sendNotification(targetId, user.getName() + " started following you.");
	}

	public static void unfollowUser(int userId, int targetId) {
		FriendRepo repo = repo();
		if(repo.getRelationship(requireUser(userId), requireUser(targetId), "Following") == null)
			throw new IllegalArgumentException("You do not follow user " + targetId + ".");
		repo.deleteFollow(userId, targetId);
	}

	public static boolean areFriends(int id1, int id2) {
		User user = UserManagementFacade.findUser(id1);
		User target = UserManagementFacade.findUser(id2);
		return user != null && target != null && repo().getRelationship(user, target, "Friend") != null;
	}

	// Does followerId follow targetId? One way only.
	public static boolean isFollowing(int followerId, int targetId) {
		User follower = UserManagementFacade.findUser(followerId);
		User target = UserManagementFacade.findUser(targetId);
		return follower != null && target != null && repo().getRelationship(follower, target, "Following") != null;
	}

	public static List<FriendManagement> getUserConnections(int userId) {
		return repo().getallRelationship(requireUser(userId));
	}

	public static List<FriendManagement> getConnections(int userId, String type) {
		return repo().getallRelationship(requireUser(userId), type);
	}

	public static List<Integer> getFriendIds(int userId) {
		List<Integer> ids = new ArrayList<>();
		for(FriendManagement friend : getConnections(userId, "Friend"))
			ids.add(friend.getTargetUserId());
		return ids;
	}
}
