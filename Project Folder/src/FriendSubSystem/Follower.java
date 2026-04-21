package FriendSubSystem;

import java.time.LocalDateTime;

public class Follower implements FriendManagement {
	String userid;
	String friendid;
	String status;
	LocalDateTime establishedDateTime;
	public Follower(String userid, String friendid){
		this.userid = userid;
		this.friendid = friendid; 
		this.status = "FOLLOWER";
		establishedDateTime = LocalDateTime.now();
	}
}
