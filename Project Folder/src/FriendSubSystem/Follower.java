package FriendSubSystem;

import java.time.LocalDateTime;

public class Follower implements FriendManagement {
	int userid;
	int followerid;
	String status;
	LocalDateTime establishedDateTime;
	public Follower(int userid, int followerid){
		this.userid = userid;
		this.followerid = followerid;
		this.status = "FOLLOWER";
		establishedDateTime = LocalDateTime.now();
	}
	public Follower(int userid, int followerid, LocalDateTime establishedDateTime){
		this.userid = userid;
		this.followerid = followerid;
		this.status = "FOLLOWER";
		this.establishedDateTime = establishedDateTime;
	}
	@Override
	public int getUserId() { return userid; }
	@Override
	public int getTargetUserId() { return followerid; }
	@Override
	public String getStatus() { return status; }
	@Override
	public void updateStatus(String newStatus) { this.status = newStatus; }
	@Override
	public String getDetails() {
		return followerid + " follows " + userid + " since " + establishedDateTime;
	}
}
