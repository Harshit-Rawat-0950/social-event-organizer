package EventManagementSubSystem;

// The rule for who may see an event, given what is known about the viewer
public final class EventAccessPolicy {

	private EventAccessPolicy() {
	}

	/*
	 * isInvited: holds a pending, maybe or accepted invite (declining gives it up).
	 * isFriend: is friends with the creator.
	 * isFollower: follows the creator. The creator following the viewer grants nothing.
	 */
	public static boolean canView(EventVisibility visibility, boolean isOwner, boolean isInvited, boolean isFriend, boolean isFollower) {
		if (isOwner || isInvited) {
			return true;
		}
		switch (visibility) {
			case PUBLIC:
				return true;
			case FOLLOWERS:
				return isFriend || isFollower;
			case FRIENDS:
				return isFriend;
			default:
				return false;
		}
	}
}
