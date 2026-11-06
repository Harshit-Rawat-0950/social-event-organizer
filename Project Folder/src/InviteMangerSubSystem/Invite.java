package InviteMangerSubSystem;

import java.time.LocalDateTime;

public class Invite {

	private int senderId;
    private int receiverId;
    private LocalDateTime expiryDate;
    private int eventId; // 0 means this is a friend request

    public Invite(int senderId, int receiverId){
    	this(senderId, receiverId, 0);
    }

    public Invite(int senderId, int receiverId, int eventId){
    	this(senderId, receiverId, LocalDateTime.now().plusMonths(3), eventId);
    }

    public Invite(int senderId, int receiverId, LocalDateTime expiryDate, int eventId){
    	this.senderId = senderId;
    	this.receiverId = receiverId;
    	this.expiryDate = expiryDate;
    	this.eventId = eventId;
    }

    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public int getEventId() { return eventId; }
    public boolean isEventInvite() { return eventId != 0; }
    public boolean isExpired() { return expiryDate.isBefore(LocalDateTime.now()); }

    public String toString() {
    	return senderId + "," + receiverId + "," + expiryDate + "," + eventId;
    }

    public static Invite fromString(String line) {
    	String[] parts = line.split(",");
    	return new Invite(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()),
    			LocalDateTime.parse(parts[2].trim()), parts.length > 3 ? Integer.parseInt(parts[3].trim()) : 0);
    }
}
