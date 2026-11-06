package InviteMangerSubSystem;

import java.util.List;

public interface InviteRepo {
	void saveInvite(Invite invite);
	List<String> getAllRawInvites();
	boolean removeInvite(String rawInvite);
}
