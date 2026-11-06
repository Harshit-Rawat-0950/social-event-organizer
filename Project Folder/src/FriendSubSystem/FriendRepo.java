package FriendSubSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import UserMangerSubSystem.User;

/*
 * Friend file:  id1,id2,closeness   (mutual, either id may come first)
 * Follow file:  followerId,followedId,timestamp   (one way)
 */
public class FriendRepo{
	private final Path friendFilePath;
	private final Path followFilePath;
	public FriendRepo(String friend_filePath, String follow_filePath) {
        friendFilePath = Paths.get(friend_filePath);
        this.followFilePath = Paths.get(follow_filePath);
        createFileIfNotExists_friend();
        createFileIfNotExists_follow();
    }

    public void createFileIfNotExists_friend() {
        try {
            if (friendFilePath.getParent() != null) {
                Files.createDirectories(friendFilePath.getParent());
            }
            if (!Files.exists(friendFilePath)) {
                Files.createFile(friendFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createFileIfNotExists_follow() {
        try {
            if (followFilePath.getParent() != null) {
                Files.createDirectories(followFilePath.getParent());
            }
            if (!Files.exists(followFilePath)) {
                Files.createFile(followFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void createRelationship(User user, User targetuser, int closeness) //Friend
	{
		append(friendFilePath, user.getId() + "," + targetuser.getId() + "," + closeness);
	}

	public void createRelationship(User user, User targetuser) //user follows targetuser
	{
		append(followFilePath, user.getId() + "," + targetuser.getId() + "," + LocalDateTime.now());
	}

	/*
	 * "Friend": user and targetuser are friends.
	 * "Following": user follows targetuser.
	 * "Follower": targetuser follows user.
	 */
	public FriendManagement getRelationship(User user, User targetuser, String type)
	{
		if(type.equals("Friend"))
		{
			for(FriendManagement friend : getallRelationship(user, "Friend"))
				if(friend.getTargetUserId() == targetuser.getId())
					return friend;
		}
		else if(type.equals("Follower")||type.equals("Following"))
		{
			for(FriendManagement follow : getallRelationship(user, type))
				if(follow.getTargetUserId() == targetuser.getId())
					return follow;
		}
		return null;
	}

	// Removes friendship and follows in both directions between the two users
	public void deleteRelationship(int id1, int id2)
	{
		deleteFriend(id1, id2);
		deleteFollow(id1, id2);
		deleteFollow(id2, id1);
	}

	public void deleteFriend(int id1, int id2)
	{
		processAndRewrite(this.friendFilePath, id1, id2);
		processAndRewrite(this.friendFilePath, id2, id1);
	}

	public void deleteFollow(int followerId, int followedId)
	{
		processAndRewrite(this.followFilePath, followerId, followedId);
	}

	private static void processAndRewrite(Path filePath, int targetId1, int targetId2) {
        try {
            List<String> new_lines = new ArrayList<>();
            for(String line : Files.readAllLines(filePath)) {
                if (line.trim().isEmpty())
                	continue;
                String[] parts = line.split(",");
                if (Integer.parseInt(parts[0].trim()) == targetId1 && Integer.parseInt(parts[1].trim()) == targetId2)
                	continue;
                new_lines.add(line);
            }
            Files.write(filePath, new_lines);
        } catch (IOException e) {
			e.printStackTrace();
        }
	}

	public List<FriendManagement> getallRelationship(User u)
	{
		List<FriendManagement> res = getallRelationship(u, "Friend");
		res.addAll(getallRelationship(u, "Follower"));
		res.addAll(getallRelationship(u, "Following"));
		return res;
	}

	// Every result is expressed from the point of view of u
	public List<FriendManagement> getallRelationship(User u, String type)
	{
		List<FriendManagement> res = new ArrayList<>();
		for(String[] parts : readRecords(type.equals("Friend") ? this.friendFilePath : this.followFilePath))
		{
			int id1 = Integer.parseInt(parts[0].trim());
			int id2 = Integer.parseInt(parts[1].trim());
			if(type.equals("Friend"))
			{
				int closeness = Integer.parseInt(parts[2].trim());
				if(id1 == u.getId())
					res.add(new Friend(id1, id2, closeness));
				else if(id2 == u.getId())
					res.add(new Friend(id2, id1, closeness));
			}
			else if(type.equals("Follower") && id2 == u.getId())
				res.add(new Follower(id2, id1, LocalDateTime.parse(parts[2].trim())));
			else if(type.equals("Following") && id1 == u.getId())
				res.add(new Following(id1, id2, LocalDateTime.parse(parts[2].trim())));
		}
		return res;
	}

	private static List<String[]> readRecords(Path filePath)
	{
		List<String[]> records = new ArrayList<>();
		try {
			for(String line : Files.readAllLines(filePath))
				if(!line.trim().isEmpty())
					records.add(line.split(","));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return records;
	}

	private static void append(Path filePath, String record)
	{
		try {
            Files.write(filePath,(record + "\n").getBytes(),StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public void createFriend(String s) {
		String[] parts = s.split(",");
		append(friendFilePath, parts[0] + "," + parts[1] + ",0");
	}
	public void createFollower(String s) {
		String[] parts = s.split(",");
		append(followFilePath, parts[0] + "," + parts[1] + "," + LocalDateTime.now());
	}
}
