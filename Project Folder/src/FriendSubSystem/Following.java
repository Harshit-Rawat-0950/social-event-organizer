package FriendSubSystem;

import java.time.LocalDateTime;

public class Following implements FriendManagement{
	int userid;
	int followingid;
	String status;
	LocalDateTime establishedDateTime;
	public Following(int userid, int followingid){
		this.userid = userid;
		this.followingid = followingid; 
		this.status = "FOLLOWING";
		establishedDateTime = LocalDateTime.now();
	}
}
