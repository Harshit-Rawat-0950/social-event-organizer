package InviteMangerSubSystem;

import java.util.ArrayList;
import java.util.List;

/*
 * Proxy pattern: stands in for InviteRepository. The real repository (and its
 * file) is only touched when first needed, and callers never see an invite
 * that has passed its expiry date.
 */
public class InviteRepositoryProxy implements InviteRepo {
	private final String filePath;
	private InviteRepository realRepository;

	public InviteRepositoryProxy(String filePath) {
		this.filePath = filePath;
	}

	private InviteRepository real() {
		if (realRepository == null) {
			realRepository = new InviteRepository(filePath);
		}
		return realRepository;
	}

	@Override
	public void saveInvite(Invite invite) {
		real().saveInvite(invite);
	}

	@Override
	public List<String> getAllRawInvites() {
		List<String> valid = new ArrayList<>();
		for (String line : real().getAllRawInvites()) {
			if (!Invite.fromString(line).isExpired()) {
				valid.add(line);
			}
		}
		return valid;
	}

	@Override
	public boolean removeInvite(String rawInvite) {
		return real().removeInvite(rawInvite);
	}
}
