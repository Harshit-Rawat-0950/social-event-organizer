package FriendSubSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class FriendRepo{
	private final Path friendFilePath;
	
	public FriendRepo(String filePath) {
        this.friendFilePath = Paths.get(filePath);
        createFileIfNotExists();
    }

    public void createFileIfNotExists() {
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
    
	public void saveRelationship(int userid, int targetid)
	{
		
	}
	
	public FriendManagement getRelationship(int userid, int targetid)
	{
		
	}
	
	public void deleteRelationship(int userid, int targetid)
	{
		
	}
	
	public List<FriendManagement> getallRelationship(int userid)
	{
		
	}
}