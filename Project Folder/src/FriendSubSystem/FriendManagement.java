package FriendSubSystem;

public interface FriendManagement {
	int getUserId();
	int getTargetUserId();
	String getStatus();
	void updateStatus(String newStatus);
	String getDetails();
}
