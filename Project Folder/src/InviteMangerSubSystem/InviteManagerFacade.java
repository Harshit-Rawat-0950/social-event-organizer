package InviteMangerSubSystem;

import FriendSubSystem.*;
import UserMangerSubSystem.ConfigLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;



/*
 * Goal is to implement all interaction of other subsystem through this class 
 * only creating a unified interface for the entire system. However this class
 * in itself must not perform any function other than delegation to the 
 * respective class of any and all interactions possible from other
 * parts of project.
 **/


public class InviteManagerFacade {
    
	
	

	public static String[] getInvites(int id) {
		ConfigLoader configLoader = new ConfigLoader();
		String infp = configLoader.getProperty("Files.invites");
		InviteRepository inre = new InviteRepository(infp);
		return inre.getAllRawInvites().toArray(new String[inre.getAllRawInvites().size()]);
		
	} 
  


	public static void acceptInvite(String invite) {
	    ConfigLoader configLoader = new ConfigLoader();
	    String infp = configLoader.getProperty("Files.invites");
	    final Path inviteFilePath = Paths.get(infp);
	    
	    // Declare the list here so it's available outside the try block if needed
	    List<String> updatedLines = new ArrayList<>();
	    
	    try {
	        // 1. MOVED INSIDE THE TRY BLOCK:
	        List<String> lines = Files.readAllLines(inviteFilePath);
	         
	        for (String line : lines) {
	            // 2. BONUS FIX: Use !invite.equals(line) instead of invite != line
	            if (!invite.equals(line)) {
	                updatedLines.add(line);
	            } else {
	                FriendRepo.createFriend(invite);
	            }
	        }
	         
	        Files.write(inviteFilePath, updatedLines, StandardOpenOption.TRUNCATE_EXISTING);

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}



	public static void rejectInvite(String invite) {
	    ConfigLoader configLoader = new ConfigLoader();
	    String infp = configLoader.getProperty("Files.invites");
	    final Path inviteFilePath = Paths.get(infp);
	    
	    // Declare the list here so it's available outside the try block if needed
	    List<String> updatedLines = new ArrayList<>();
	    
	    try {
	        // 1. MOVED INSIDE THE TRY BLOCK:
	        List<String> lines = Files.readAllLines(inviteFilePath);
	        
	        for (String line : lines) {
	            // 2. BONUS FIX: Use !invite.equals(line) instead of invite != line
	            if (!invite.equals(line)) {
	                updatedLines.add(line);
	            }
	        }
	        
	        Files.write(inviteFilePath, updatedLines, StandardOpenOption.TRUNCATE_EXISTING);

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}


}
