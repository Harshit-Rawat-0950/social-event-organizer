package EventManagementSubSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import FriendSubSystem.FriendFacade;
import InviteMangerSubSystem.InviteManagerFacade;
import InviteMangerSubSystem.RsvpStatus;
import ReportDashboardSubSystem.ConsoleInput;
import ReportDashboardSubSystem.DashboardFacade;
import UserMangerSubSystem.ConfigLoader;
import UserMangerSubSystem.UserManagementFacade;

/*
 * Goal is to implement all interaction of other subsystem through this class
 * only creating a unified interface for the entire system. However this class
 * in itself must not perform any function other than delegation to the
 * respective class of any and all interactions possible from other
 * parts of project.
 **/
public class EventMangementFacade {

	private static MainEventManeger manager() {
		return new MainEventManeger(new ConfigLoader().getProperty("Files.events"));
	}

	private static String defaultType() {
		return new ConfigLoader().getProperty("event.defaultType", "Public");
	}

	private static int currentUserId() {
		return UserManagementFacade.getCurrUser().getId();
	}

	/* ---------- Who can see what ---------- */

	// Sub-events follow their top level event, so that is the one to check
	public static boolean canView(BasicEvent event, int viewerId) {
		BasicEvent root = manager().getRootEvent(event.getId());
		if(root == null)
			root = event;
		return EventAccessPolicy.canView(root.getVisibility(),
				root.getOwnerId() == viewerId,
				InviteManagerFacade.isInvited(root.getId(), viewerId),
				FriendFacade.areFriends(root.getOwnerId(), viewerId),
				FriendFacade.isFollowing(viewerId, root.getOwnerId()));
	}

	private static Predicate<BasicEvent> visibleTo(int viewerId) {
		return event -> canView(event, viewerId);
	}

	// Asks for an id. Events the user may not see look exactly like ones that do not exist.
	private static BasicEvent readVisibleEvent(String prompt) {
		int id = ConsoleInput.getInstance().readInt(prompt);
		BasicEvent event = manager().findEvent(id);
		if(event == null || !canView(event, currentUserId()))
			throw new IllegalArgumentException("No event with id " + id + ".");
		return event;
	}

	/* ---------- Actions ---------- */

	public static void createEvent() {
		int id = manager().createEvent(defaultType());
		DashboardFacade.showSuccess("Created event #" + id + ".");
	}

	public static void addSubEvent() {
		BasicEvent parent = readVisibleEvent("Parent event id: ");
		int id = manager().addSubEvent(parent.getId(), currentUserId(), defaultType());
		DashboardFacade.showSuccess("Created sub-event #" + id + ".");
	}

	public static void decorateEvent() {
		BasicEvent event = readVisibleEvent("Event id: ");
		manager().decorateEvent(event.getId(), currentUserId());
		DashboardFacade.showSuccess("Event decorated.");
	}

	public static void editEvent() {
		BasicEvent event = readVisibleEvent("Event id: ");
		MainEventManeger manager = manager();
		manager.editEvent(event.getId(), currentUserId());
		int rootId = manager.getRootEvent(event.getId()).getId();
		InviteManagerFacade.notifyAttendees(rootId, "Event updated: " + manager.findEvent(event.getId()).getDetails());
		DashboardFacade.showSuccess("Event updated and attendees notified.");
	}

	public static void cancelEvent() {
		if(!new ConfigLoader().getBooleanProperty("event.allowCancellation", true))
			throw new IllegalArgumentException("Cancelling events is turned off.");
		BasicEvent event = readVisibleEvent("Event id: ");
		MainEventManeger manager = manager();
		int rootId = manager.getRootEvent(event.getId()).getId();
		manager.cancelEvent(event.getId(), currentUserId());
		InviteManagerFacade.notifyAttendees(rootId, "Event cancelled: " + event.getName() + " (#" + event.getId() + ")");
		if(rootId == event.getId())
			InviteManagerFacade.removeEventInvites(rootId);
		DashboardFacade.showSuccess("Event cancelled and attendees notified.");
	}

	public static void inviteToEvent() {
		BasicEvent event = readVisibleEvent("Event id: ");
		if(event.getOwnerId() != currentUserId())
			throw new IllegalArgumentException("You can only invite people to your own events.");
		if(event.getParentId() != 0)
			throw new IllegalArgumentException("Invite people to the main event, #" + manager().getRootEvent(event.getId()).getId() + ".");
		int receiver = ConsoleInput.getInstance().readInt("User id to invite (0 for all friends): ");
		if(receiver == 0)
			event.getInvited();
		else {
			InviteManagerFacade.sendEventInvite(event.getOwnerId(), receiver, event.getId());
			DashboardFacade.showSuccess("Invite sent.");
		}
	}

	// Counts for anyone who can see the event, the names only for its creator
	public static void showResponses() {
		BasicEvent event = readVisibleEvent("Event id: ");
		BasicEvent root = manager().getRootEvent(event.getId());
		DashboardFacade.showInfo(root.getName() + " - " + InviteManagerFacade.getRsvpSummary(root.getId()));
		if(root.getOwnerId() == currentUserId())
			for(String response : InviteManagerFacade.getRsvpResponses(root.getId()))
				DashboardFacade.showInfo(response);
	}

	/* ---------- Queries ---------- */

	public static BasicEvent getEvent(int id) {
		return manager().findEvent(id);
	}

	public static List<EventView> getUpcomingEventViews(int viewerId) {
		return views(manager().getUpcomingEvents(visibleTo(viewerId)), viewerId);
	}

	public static List<EventView> getAllEventViews(int viewerId) {
		return views(manager().getAllEvents(visibleTo(viewerId)), viewerId);
	}

	public static List<EventView> getMyEventViews(int viewerId) {
		return views(manager().getEventsByOwner(viewerId), viewerId);
	}

	public static List<EventComponent> getEventsCreatedBy(int userId) {
		return manager().getEventsByOwner(userId);
	}

	public static List<BasicEvent> getEventRecordsCreatedBy(int userId) {
		return manager().getRecordsByOwner(userId);
	}

	// Response numbers appear on the card only for the creator of the event
	private static List<EventView> views(List<EventComponent> events, int viewerId) {
		List<EventView> result = new ArrayList<>();
		for(EventComponent event : events) {
			EventView view = event.describe();
			if(view.ownerId == viewerId) {
				Map<RsvpStatus, Integer> counts = InviteManagerFacade.getRsvpCounts(view.id);
				view.going = counts.get(RsvpStatus.ACCEPTED);
				view.maybe = counts.get(RsvpStatus.MAYBE);
				view.waiting = counts.get(RsvpStatus.PENDING);
				view.declined = counts.get(RsvpStatus.DECLINED);
			}
			result.add(view);
		}
		return result;
	}
}
