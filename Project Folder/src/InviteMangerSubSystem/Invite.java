package InviteMangerSubSystem;

import java.time.LocalDateTime;

public class Invite {

	private int senderId; 
    private int receiverId;
    private LocalDateTime expiryDate;
    
    public Invite(int senderId, int receiverId){
    	this.senderId = senderId;
    	this.receiverId = receiverId;
    	this.expiryDate = LocalDateTime.now().plusMonths(3);
    }
    
    public String toString() {
    	return senderId + "," + receiverId + "," + expiryDate; 
    }
}
