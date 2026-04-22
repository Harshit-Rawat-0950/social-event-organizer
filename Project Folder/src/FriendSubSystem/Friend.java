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
}
