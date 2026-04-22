package InviteMangerSubSystem;

import java.time.LocalDateTime;

public class FollowerRequest{

	private int senderId; 
    private int receiverId;
    private LocalDateTime expiryDate;
    
    public FollowerRequest(int senderId, int receiverId){
    	this.senderId = senderId;
    	this.receiverId = receiverId;
    	this.expiryDate = LocalDateTime.now().plusMonths(3);
    }
    
    public String toString() {
    	return senderId + "," + receiverId + "," + expiryDate; 
    }
}
