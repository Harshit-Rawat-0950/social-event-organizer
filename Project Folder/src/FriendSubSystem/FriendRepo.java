package FriendSubSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import UserMangerSubSystem.User;

class FriendRepo{
	private final Path friendFilePath;
	private final Path followFilePath;
	public FriendRepo(String friend_filePath, String follow_filePath) {
        this.friendFilePath = Paths.get(friend_filePath);
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
	
	public List<FriendManagement> getRelationship(User u, String type)
	{
		List<FriendManagement> lif = new ArrayList<>();
		if(type.equals("Friend"))
		{
			Path filePath = this.friendFilePath;

	        try (Stream<String> lines = Files.lines(filePath)) {
	            
	            lines.forEach(line -> {
	                // 1. Skip any accidentally blank lines
	                if (line.trim().isEmpty()) {
	                    return; 
	                }

	                // 2. Split the line at the commas
	                String[] parts = line.split(",");

	             
	                    
	                        // 4. Parse the strings into integers.
	                        // Note: .trim() is highly recommended here to remove any hidden 
	                        // spaces (e.g., if the file has "1, 2, 3" instead of "1,2,3")
	                        int id1 = Integer.parseInt(parts[0].trim());
	                        int id2 = Integer.parseInt(parts[1].trim());
	                        int closeness   = Integer.parseInt(parts[2].trim());
	                        if(id1 == u.getId() || id2 == u.getId())
	                        {
	                        	lif.add(new Friend(id1,id2,closeness));
	                        }
	               
	            });
	            
	        } catch (IOException e) {
	            System.err.println("Failed to get relationship: " + e.getMessage());
	        }
	    }
		else if(type.equals("Follower"))
		{
			Path filePath = this.followFilePath;

	        try (Stream<String> lines = Files.lines(filePath)) {
	            
	            lines.forEach(line -> {
	                // 1. Skip any accidentally blank lines
	                if (line.trim().isEmpty()) {
	                    return; 
	                }

	                // 2. Split the line at the commas
	                String[] parts = line.split(",");

	             
	                    
	                        // 4. Parse the strings into integers.
	                  
	                       
	                        int id1 = Integer.parseInt(parts[0].trim());
	                        int id2 = Integer.parseInt(parts[1].trim());
	                       
	                        if(id2 == u.getId())
	                        {
	                        	lif.add(new Follower(id2,id1));
	                        }
	               
	            });
	            
	        } catch (IOException e) {
	            System.err.println("Failed to get relationship: " + e.getMessage());
	        }
	    }
		else if(type.equals("Following"))
		{
			Path filePath = this.followFilePath;

	        try (Stream<String> lines = Files.lines(filePath)) {
	            
	            lines.forEach(line -> {
	                // 1. Skip any accidentally blank lines
	                if (line.trim().isEmpty()) {
	                    return; 
	                }

	                // 2. Split the line at the commas
	                String[] parts = line.split(",");

	             
	                    
	                        // 4. Parse the strings into integers.
	                  
	                       
	                        int id1 = Integer.parseInt(parts[0].trim());
	                        int id2 = Integer.parseInt(parts[1].trim());
	                       
	                        if(id1 == u.getId())
	                        {
	                        	lif.add(new Follower(id1,id2));
	                        }
	               
	            });
	            
	        } catch (IOException e) {
	            System.err.println("Failed to get relationship: " + e.getMessage());
	        }
	    }

        return lif;
		}
	
	
	public void deleteRelationship(int id1, int id2)
	{
		Path file1Path = this.friendFilePath; // Format: id1, id2, x
        Path file2Path = this.followFilePath; // Format: id1, id2

        // Process both files
        processAndRewrite(file1Path, id1, id2);
        processAndRewrite(file2Path, id1, id2);
	}
	private static void processAndRewrite(Path filePath, int targetId1, int targetId2) {
        // Check if file exists first to avoid crashing
        if (!Files.exists(filePath)) {
            System.err.println("File not found: " + filePath.getFileName());
            return;
        }

        try {
            // 1. Read all lines into a mutable list
            List<String> lines = new ArrayList<>(Files.readAllLines(filePath));

            // 2. Remove the line if the first two IDs match our targets
            lines.removeIf(line -> {
                // Skip empty lines to prevent crashes
                if (line.trim().isEmpty()) return false; 

                String[] parts = line.split(",");
                
                // Ensure the line has at least 2 parts (works for both file formats)
                if (parts.length >= 2) {
                    try {
                        int currentId1 = Integer.parseInt(parts[0].trim());
                        int currentId2 = Integer.parseInt(parts[1].trim());
                        
                        // If both IDs match the parameters, return true to DELETE this line
                        return (currentId1 == targetId1 && currentId2 == targetId2) || (currentId1 == targetId2 && currentId2 == targetId1);
                        
                    } catch (NumberFormatException e) {
                        // If it's text instead of a number, don't delete it, just skip
                        return false; 
                    }
                }
                return false; 
            });

            // 3. Overwrite the file with the surviving lines
            Files.write(filePath, lines);
            System.out.println("Successfully updated: " + filePath.getFileName());

        } catch (IOException e) {
            System.err.println("Error processing " + filePath.getFileName() + ": " + e.getMessage());
        }
	}
	public List<FriendManagement> getallRelationship(User u)
	{
		List<FriendManagement> li = new ArrayList<>();
		li.addAll(getRelationship(u, "Friend"));
		li.addAll(getRelationship(u, "Follower"));
		li.addAll(getRelationship(u, "Following"));
		return li;
	}
}