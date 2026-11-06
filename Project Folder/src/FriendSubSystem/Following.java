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
	public Following(int userid, int followingid, LocalDateTime establishedDateTime){
		this.userid = userid;
		this.followingid = followingid;
		this.status = "FOLLOWING";
		this.establishedDateTime = establishedDateTime;
	}
	@Override
	public int getUserId() { return userid; }
	@Override
	public int getTargetUserId() { return followingid; }
	@Override
	public String getStatus() { return status; }
	@Override
	public void updateStatus(String newStatus) { this.status = newStatus; }
	@Override
	public String getDetails() {
		return userid + " follows " + followingid + " since " + establishedDateTime;
	}
}
