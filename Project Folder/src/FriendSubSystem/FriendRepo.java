package FriendSubSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import UserMangerSubSystem.User;

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
		try {
            Files.write(friendFilePath,(user.getId() + "," + targetuser.getId() + "," + closeness + "\n").getBytes(),StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void createRelationship(User user, User targetuser) //Follow
	{
		try {
            Files.write(friendFilePath,(user.getId() + "," + targetuser.getId() + "\n").getBytes(),StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public FriendManagement getRelationship(User user, User targetuser, String type)
	{
		if(type.equals("Friend"))
		{
			Path filePath = this.friendFilePath;
	        try {
	        	List<String> lines = Files.readAllLines(filePath);
	        	for (String line : lines) {
	                if (line.trim().isEmpty()) 
	                    continue;
	                String[] parts = line.split(",");
	                int id1 = Integer.parseInt(parts[0].trim());
	                int id2 = Integer.parseInt(parts[1].trim());
	                int closeness = Integer.parseInt(parts[2].trim());
	                if((id1 == user.getId() &&  id2 == targetuser.getId())||(id1 == targetuser.getId()&&id2 == user.getId()))
	                {
	                      	return new Friend(id1,id2,closeness);
	                }
	            }
	            
	        } catch (IOException e) {
				e.printStackTrace();
	        }
	    }
		else if(type.equals("Follower")||type.equals("Following"))
		{
			Path filePath = this.followFilePath;
	        try {
	        	List<String> lines = Files.readAllLines(filePath);
	        	for (String line : lines) {
	                if (line.trim().isEmpty()) 
	                    continue;
	                String[] parts = line.split(",");
	                int id1 = Integer.parseInt(parts[0].trim());
	                int id2 = Integer.parseInt(parts[1].trim());
	                LocalDateTime ldt = LocalDateTime.parse(parts[2].trim());
	                if(id1 == user.getId() &&  id2 == targetuser.getId() && type.equals("Follower"))
	                {
	                      	return new Follower(id1,id2,ldt);
	                }
	                else if(id2 == user.getId() &&  id1 == targetuser.getId() && type.equals("Following"))
	                {
	                	return new Follower(id2,id1,ldt);
	                }
	        	}
	        }
	        catch (IOException e) {
				e.printStackTrace();
	        }
	   }
		return null;
}
	
	
	public void deleteRelationship(int id1, int id2)
	{
		Path file1Path = this.friendFilePath; // Format: id1, id2
        Path file2Path = this.followFilePath; // Format: id1, id2

        // Process both files
        processAndRewrite(file1Path, id1, id2);
        processAndRewrite(file2Path, id1, id2);
	}
	private static void processAndRewrite(Path filePath, int targetId1, int targetId2) {
        if (!Files.exists(filePath)) {
            System.err.println("File not found: " + filePath.getFileName());
            return;
        }

        try {
            List<String> lines = new ArrayList<>(Files.readAllLines(filePath));
            List<String> new_lines = new ArrayList<>();
            for(String line : lines) {
                if (line.trim().isEmpty())
                	continue;
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                        int currentId1 = Integer.parseInt(parts[0].trim());
                        int currentId2 = Integer.parseInt(parts[1].trim());
                        if(currentId1 == targetId1 && currentId2 == targetId2)
                        	continue;
                        if(currentId2 == targetId1 && currentId1 == targetId2)
                        	continue;
                        new_lines.add(line);
                    }
                }
            Files.write(filePath, new_lines);
            System.out.println("Successfully updated: " + filePath.getFileName());
        } catch (IOException e) {
			e.printStackTrace();
        }
	}
	public List<FriendManagement> getallRelationship(User u)
	{ 
		List<FriendManagement> res = new ArrayList<>();
		try {
			List<String> lines = new ArrayList<>(Files.readAllLines(this.friendFilePath));
	        for(String line : lines) {
	        	if (line.trim().isEmpty())
                	continue;
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                        int currentId1 = Integer.parseInt(parts[0].trim());
                        int currentId2 = Integer.parseInt(parts[1].trim());
                        if(u.getId()==currentId1||u.getId()==currentId2)
                        	res.add(new Friend(currentId2, currentId1, 0));
                 }
	        }
	        lines = new ArrayList<>(Files.readAllLines(this.followFilePath));
	        for(String line : lines)
	        {
	        	if (line.trim().isEmpty())
                	continue;
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                        int currentId1 = Integer.parseInt(parts[0].trim());
                        int currentId2 = Integer.parseInt(parts[1].trim());
                        LocalDateTime ldt = LocalDateTime.parse(parts[2].trim());
                        if(u.getId()==currentId1||u.getId()==currentId2)
                        	res.add(new Follower(currentId2, currentId1, ldt));
                 }
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	public List<FriendManagement> getallRelationship(User u, String type)
	{
		List<FriendManagement> res = new ArrayList<>();
		if(type.equals("Friend"))
		try {
			List<String> lines = new ArrayList<>(Files.readAllLines(this.friendFilePath));
	        for(String line : lines) {
	        	if (line.trim().isEmpty())
                	continue;
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                        int currentId1 = Integer.parseInt(parts[0].trim());
                        int currentId2 = Integer.parseInt(parts[1].trim());
                        if(u.getId()==currentId1||u.getId()==currentId2)
                        	res.add(new Friend(currentId2, currentId1, 0));
                 }
	        }
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
		else
		try {
			List<String> lines = new ArrayList<>(Files.readAllLines(this.followFilePath));
	        for(String line : lines)
	        {
	        	if (line.trim().isEmpty())
                	continue;
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                        int currentId1 = Integer.parseInt(parts[0].trim());
                        int currentId2 = Integer.parseInt(parts[1].trim());
                        LocalDateTime ldt = LocalDateTime.parse(parts[2].trim());
                        if(u.getId()==currentId1||u.getId()==currentId2)
                        	res.add(new Follower(currentId2, currentId1, ldt));
                 }
	        }
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	public void createFriend(String s) {
		try {
			 String[] parts = s.split(",");
            Files.write(this.friendFilePath,(parts[0] + "," + parts[1] +  "," + "0" + "\n").getBytes(),StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	public void createFollower(String s) {
		try {
			 String[] parts = s.split(",");
           Files.write(this.friendFilePath,(parts[0] + "," + parts[1] + "," + "\n").getBytes(),StandardOpenOption.APPEND);
       } catch (IOException e) {
           e.printStackTrace();
       }
	}
}