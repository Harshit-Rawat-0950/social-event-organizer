package FriendSubSystem;

public class Friend implements FriendManagement{
	int userid;
	int friendid;
	String status;
	int closeness;
	public Friend(int userid, int friendid, int c){
		this.userid = userid;
		this.friendid = friendid;
		this.status = "FRIEND";
		this.closeness = c;
	}
	@Override
	public int getUserId() { return userid; }
	@Override
	public int getTargetUserId() { return friendid; }
	@Override
	public String getStatus() { return status; }
	@Override
	public void updateStatus(String newStatus) { this.status = newStatus; }
	@Override
	public String getDetails() {
		return userid + " and " + friendid + " are friends, closeness " + closeness;
	}
}
