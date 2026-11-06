package InviteMangerSubSystem;

import EventManagementSubSystem.BasicEvent;
import EventManagementSubSystem.EventMangementFacade;
import FriendSubSystem.*;
import NotificationSubSystem.NotificationFacade;
import ReportDashboardSubSystem.DashboardFacade;
import UserMangerSubSystem.ConfigLoader;
import UserMangerSubSystem.UserManagementFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/*
 * Goal is to implement all interaction of other subsystem through this class
 * only creating a unified interface for the entire system. However this class
 * in itself must not perform any function other than delegation to the
 * respective class of any and all interactions possible from other
 * parts of project.
 **/


public class InviteManagerFacade {

	private static InviteRepo repo() {
		return new InviteRepositoryProxy(new ConfigLoader().getProperty("Files.invites"));
	}

	private static RsvpRepository rsvp() {
		return new RsvpRepository(new ConfigLoader().getProperty("Files.rsvp"));
	}

	public static String[] getInvites(int id) {
		List<String> invites = new ArrayList<>();
		for(String raw : repo().getAllRawInvites())
			if(Invite.fromString(raw).getReceiverId() == id)
				invites.add(raw);
		return invites.toArray(new String[0]);
	}

	public static void sendFriendInvite(int senderId, int receiverId) {
		InviteRepo repo = repo();
		for(String raw : repo.getAllRawInvites()) {
			Invite existing = Invite.fromString(raw);
			if(existing.isEventInvite())
				continue;
			if(existing.getSenderId() == senderId && existing.getReceiverId() == receiverId)
				throw new IllegalArgumentException("You have already sent this user a friend request.");
			if(existing.getSenderId() == receiverId && existing.getReceiverId() == senderId)
				throw new IllegalArgumentException("This user has already sent you a friend request. Check your invites.");
		}
		repo.saveInvite(new Invite(senderId, receiverId));
		NotificationFacade.sendNotification(receiverId, "New friend request from " + UserManagementFacade.getDisplayName(senderId) + ".");
	}

	public static void sendEventInvite(int senderId, int receiverId, int eventId) {
		BasicEvent event = EventMangementFacade.getEvent(eventId);
		if(event == null || event.isCancelled())
			throw new IllegalArgumentException("Event " + eventId + " is not open for invites.");
		if(event.getParentId() != 0)
			throw new IllegalArgumentException("Invite people to the main event, not a sub-event.");
		if(UserManagementFacade.findUser(receiverId) == null)
			throw new IllegalArgumentException("No user with id " + receiverId + ".");
		if(receiverId == event.getOwnerId())
			throw new IllegalArgumentException("The creator of an event does not need an invite.");
		RsvpRepository rsvp = rsvp();
		Map<Integer, RsvpStatus> responses = rsvp.getResponses(eventId);
		if(responses.containsKey(receiverId))
			throw new IllegalArgumentException(UserManagementFacade.getDisplayName(receiverId) + " has already been invited.");
		int attending = responses.size() - rsvp.getCounts(eventId).get(RsvpStatus.DECLINED);
		if(attending >= new ConfigLoader().getIntProperty("event.maxCapacity", Integer.MAX_VALUE))
			throw new IllegalArgumentException("This event has reached its maximum capacity.");
		repo().saveInvite(new Invite(senderId, receiverId, eventId));
		rsvp.setResponse(eventId, receiverId, RsvpStatus.PENDING);
		NotificationFacade.sendNotification(receiverId, UserManagementFacade.getDisplayName(senderId) + " invited you to " + event.getName() + ".");
	}

	// Pending, maybe and accepted invites count. Declining gives the invite up.
	public static boolean isInvited(int eventId, int userId) {
		RsvpStatus status = rsvp().getResponses(eventId).get(userId);
		return status != null && status != RsvpStatus.DECLINED;
	}

	public static Map<RsvpStatus, Integer> getRsvpCounts(int eventId) {
		return rsvp().getCounts(eventId);
	}

	// Invites every friend of the event creator who has not been invited yet
	public static void getInvitedToEvent(BasicEvent event) {
		if(event.getParentId() != 0)
			throw new IllegalArgumentException("Invite people to the main event, not a sub-event.");
		int sent = 0;
		for(int friendId : FriendFacade.getFriendIds(event.getOwnerId())) {
			if(!rsvp().getResponses(event.getId()).containsKey(friendId)) {
				sendEventInvite(event.getOwnerId(), friendId, event.getId());
				sent++;
			}
		}
		DashboardFacade.showSuccess("Sent " + sent + " invite(s).");
	}

	public static void acceptInvite(String invite) {
		Invite parsed = requirePending(invite);
		repo().removeInvite(invite);
		if(parsed.isEventInvite()) {
			respond(parsed, RsvpStatus.ACCEPTED);
		} else {
			FriendFacade.createFriend(invite);
			NotificationFacade.sendNotification(parsed.getSenderId(),
					UserManagementFacade.getDisplayName(parsed.getReceiverId()) + " accepted your friend request.");
		}
	}

	public static void rejectInvite(String invite) {
		Invite parsed = requirePending(invite);
		repo().removeInvite(invite);
		if(parsed.isEventInvite())
			respond(parsed, RsvpStatus.DECLINED);
	}

	// The invite stays in the list so a firm answer can still be given later
	public static void maybeInvite(String invite) {
		Invite parsed = requirePending(invite);
		if(!parsed.isEventInvite())
			throw new IllegalArgumentException("Maybe is only available for event invites.");
		respond(parsed, RsvpStatus.MAYBE);
	}

	private static Invite requirePending(String invite) {
		if(!repo().getAllRawInvites().contains(invite))
			throw new IllegalArgumentException("This invite is no longer available.");
		return Invite.fromString(invite);
	}

	private static void respond(Invite invite, RsvpStatus status) {
		rsvp().setResponse(invite.getEventId(), invite.getReceiverId(), status);
		BasicEvent event = EventMangementFacade.getEvent(invite.getEventId());
		NotificationFacade.sendNotification(invite.getSenderId(), UserManagementFacade.getDisplayName(invite.getReceiverId())
				+ " responded " + status + " to " + (event == null ? "your event" : event.getName()) + ".");
	}

	public static void removeEventInvites(int eventId) {
		InviteRepo repo = repo();
		for(String raw : repo.getAllRawInvites())
			if(Invite.fromString(raw).getEventId() == eventId)
				repo.removeInvite(raw);
	}

	// Everyone invited to the event hears about changes, unless they declined
	public static void notifyAttendees(int eventId, String message) {
		for(Map.Entry<Integer, RsvpStatus> response : rsvp().getResponses(eventId).entrySet())
			if(response.getValue() != RsvpStatus.DECLINED)
				NotificationFacade.sendNotification(response.getKey(), message);
	}

	public static String getRsvpSummary(int eventId) {
		Map<RsvpStatus, Integer> counts = rsvp().getCounts(eventId);
		return "Accepted: " + counts.get(RsvpStatus.ACCEPTED) + ", Declined: " + counts.get(RsvpStatus.DECLINED)
				+ ", Maybe: " + counts.get(RsvpStatus.MAYBE) + ", Pending: " + counts.get(RsvpStatus.PENDING);
	}

	public static List<String> getRsvpResponses(int eventId) {
		List<String> lines = new ArrayList<>();
		for(Map.Entry<Integer, RsvpStatus> response : rsvp().getResponses(eventId).entrySet())
			lines.add(UserManagementFacade.getDisplayName(response.getKey()) + ": " + response.getValue());
		return lines;
	}

	public static String describeInvite(String raw) {
		Invite invite = Invite.fromString(raw);
		String sender = UserManagementFacade.getDisplayName(invite.getSenderId());
		if(!invite.isEventInvite())
			return "Friend request from " + sender;
		BasicEvent event = EventMangementFacade.getEvent(invite.getEventId());
		RsvpStatus current = rsvp().getResponses(invite.getEventId()).get(invite.getReceiverId());
		return "Event invite from " + sender + ": " + (event == null ? "#" + invite.getEventId() : event.getDetails())
				+ (current == RsvpStatus.MAYBE ? " (you said maybe)" : "");
	}
}
