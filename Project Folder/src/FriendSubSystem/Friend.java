package FriendSubSystem;

public class Friend implements FriendManagement{
	String userid;
	String friendid;
	String status;
	int closeness;
	public Friend(String userid, String friendid){
		this.userid = userid;
		this.friendid = friendid;
		this.status = "FRIEND";
		this.closeness = 0;
	}
}
