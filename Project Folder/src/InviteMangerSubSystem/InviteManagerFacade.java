package InviteMangerSubSystem;

import EventManagementSubSystem.BasicEvent;

import java.time.LocalDateTime;



/*
 * Goal is to implement all interaction of other subsystem through this class 
 * only creating a unified interface for the entire system. However this class
 * in itself must not perform any function other than delegation to the 
 * respective class of any and all interactions possible from other
 * parts of project.
 **/


public class InviteManagerFacade {

	private int senderId; 
    private int receiverId;
    private LocalDateTime expiryDate;
    
    public InviteManagerFacade(int senderId, int receiverId){
    	this.senderId = senderId;
    	this.receiverId = receiverId;
    	this.expiryDate = LocalDateTime.now().plusMonths(3);
    }
    
    public String toString() {
    	return senderId + "," + receiverId + "," + expiryDate; 
    }
	
	
	public static void getInvitedToEvent(BasicEvent basicEvent) {
		// TODO Auto-generated method stub	
	}
	
	
	
	public void processFriendRequest(int senderId, int receiverId) {
		
		
		
	}

}
