package EventManagementSubSystem;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import InviteMangerSubSystem.InviteManagerFacade;
import InviteMangerSubSystem.RsvpStatus;
import TestSupport.ConsoleDriver;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

public class EventFacadeTest {
	private static final int OWNER = 1;
	private static final int GUEST = 2;
	private static final int OTHER = 3;

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Before
	public void setUp() {
		Fixtures.user(OWNER, "Owner");
		Fixtures.user(GUEST, "Guest");
		Fixtures.user(OTHER, "Other");
		Fixtures.login(OWNER);
	}

	private static Set<String> set(String... values) {
		return new HashSet<>(Arrays.asList(values));
	}

	@Test
	public void createBasicEventThroughTheConsole() {
		String out = ConsoleDriver.call(EventMangementFacade::createEvent,
				"n", "Party", "Fun", "2099-01-01", "10:00", "Hall", "Birthday", "i");
		assertTrue(out, out.contains("Created event #1."));
		BasicEvent event = EventMangementFacade.getEvent(1);
		assertEquals("Party", event.getName());
		assertEquals(OWNER, event.getOwnerId());
		assertEquals(EventVisibility.FRIENDS, event.getVisibility());
		assertFalse(event instanceof CompositeEvent);
	}

	@Test
	public void compositeIsCreatedWhenSubEventsAreWanted() {
		ConsoleDriver.call(EventMangementFacade::createEvent, "y", "Fest", "Fun", "2099-01-01", "10:00", "Hall", "", "");
		assertTrue(EventMangementFacade.getEvent(1) instanceof CompositeEvent);
	}

	@Test
	public void blankTypeUsesTheConfiguredDefault() {
		env.set("event.defaultType", "General");
		ConsoleDriver.call(EventMangementFacade::createEvent, "n", "Party", "Fun", "2099-01-01", "10:00", "Hall", "", "");
		assertTrue(EventMangementFacade.getEvent(1).getDetails().contains("(General)"));
	}

	@Test
	public void everyVisibilityKeyWorks() {
		String[][] keys = {{"p", "PUBLIC"}, {"f", "FOLLOWERS"}, {"i", "FRIENDS"}, {"v", "PRIVATE"}, {"", "PUBLIC"}};
		int id = 1;
		for (String[] key : keys) {
			ConsoleDriver.call(EventMangementFacade::createEvent, "n", "E", "D", "2099-01-01", "10:00", "L", "T", key[0]);
			assertEquals(EventVisibility.valueOf(key[1]), EventMangementFacade.getEvent(id++).getVisibility());
		}
	}

	@Test
	public void badVisibilityKeyAsksAgain() {
		String out = ConsoleDriver.call(EventMangementFacade::createEvent, "n", "E", "D", "2099-01-01", "10:00", "L", "T", "z", "v");
		assertTrue(out, out.contains("Please choose p, f, i or v."));
		assertEquals(EventVisibility.PRIVATE, EventMangementFacade.getEvent(1).getVisibility());
	}

	@Test
	public void pastDatesAreRefusedOnCreate() {
		String yesterday = LocalDate.now().minusDays(1).toString();
		String out = ConsoleDriver.call(EventMangementFacade::createEvent, "n", "E", "D", yesterday, "2099-01-01", "10:00", "L", "T", "");
		assertTrue(out, out.contains("The date cannot be in the past."));
		assertEquals("2099-01-01", EventMangementFacade.getEvent(1).getDate());
	}

	@Test
	public void todayIsAcceptedOnCreate() {
		String today = LocalDate.now().toString();
		ConsoleDriver.call(EventMangementFacade::createEvent, "n", "E", "D", today, "10:00", "L", "T", "");
		assertEquals(today, EventMangementFacade.getEvent(1).getDate());
	}

	@Test
	public void pastDatesAreRefusedOnEdit() {
		int id = Fixtures.event(OWNER, "E", EventVisibility.PUBLIC);
		String out = ConsoleDriver.call(EventMangementFacade::editEvent, String.valueOf(id), "d", "2000-01-01", "2099-03-03");
		assertTrue(out, out.contains("The date cannot be in the past."));
		assertEquals("2099-03-03", EventMangementFacade.getEvent(id).getDate());
	}

	@Test
	public void subEventTakesItsParentsVisibilityWithoutAsking() {
		int top = Fixtures.composite(OWNER, "Fest", EventVisibility.FRIENDS);
		ConsoleDriver.call(EventMangementFacade::addSubEvent, String.valueOf(top), "n", "Talk", "D", "2099-01-01", "11:00", "L", "T");
		assertEquals(EventVisibility.FRIENDS, EventMangementFacade.getEvent(2).getVisibility());
		assertEquals(top, EventMangementFacade.getEvent(2).getParentId());
	}

	@Test
	public void nestedCompositeCanBeCreated() {
		int top = Fixtures.composite(OWNER, "Fest", EventVisibility.PUBLIC);
		ConsoleDriver.call(EventMangementFacade::addSubEvent, String.valueOf(top), "y", "Day", "D", "2099-01-01", "11:00", "L", "T");
		assertTrue(EventMangementFacade.getEvent(2) instanceof CompositeEvent);
	}

	@Test
	public void parentMustHoldSubEvents() {
		int id = Fixtures.event(OWNER, "E", EventVisibility.PUBLIC);
		try {
			ConsoleDriver.call(EventMangementFacade::addSubEvent, String.valueOf(id));
			fail("Accepted a basic event as parent");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("not created to hold sub-events"));
		}
	}

	@Test
	public void everyDecorationKeyWorks() {
		int id = Fixtures.event(OWNER, "E", EventVisibility.PUBLIC);
		String key = String.valueOf(id);
		ConsoleDriver.call(EventMangementFacade::decorateEvent, key, "m", "Queen");
		ConsoleDriver.call(EventMangementFacade::decorateEvent, key, "n", "Big", "Very big");
		ConsoleDriver.call(EventMangementFacade::decorateEvent, key, "o", "4", "Taj");
		ConsoleDriver.call(EventMangementFacade::decorateEvent, key, "u", "3");
		ConsoleDriver.call(EventMangementFacade::decorateEvent, key, "b", "Sales");
		EventView view = EventMangementFacade.getUpcomingEventViews(OWNER).get(0);
		assertEquals(5, view.tags.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void unknownDecorationKeyIsRefused() {
		int id = Fixtures.event(OWNER, "E", EventVisibility.PUBLIC);
		ConsoleDriver.call(EventMangementFacade::decorateEvent, String.valueOf(id), "z");
	}

	@Test
	public void urgencyAndStarsMustBeOneToFive() {
		int id = Fixtures.event(OWNER, "E", EventVisibility.PUBLIC);
		String out = ConsoleDriver.call(EventMangementFacade::decorateEvent, String.valueOf(id), "u", "0", "6", "5");
		assertTrue(out, out.contains("Please enter a number from 1 to 5."));
	}

	@Test
	public void everyEditKeyWorks() {
		int id = Fixtures.event(OWNER, "E", EventVisibility.PUBLIC);
		String key = String.valueOf(id);
		ConsoleDriver.call(EventMangementFacade::editEvent, key, "n", "Newname");
		ConsoleDriver.call(EventMangementFacade::editEvent, key, "e", "Newdesc");
		ConsoleDriver.call(EventMangementFacade::editEvent, key, "d", "2099-09-09");
		ConsoleDriver.call(EventMangementFacade::editEvent, key, "t", "09:09");
		ConsoleDriver.call(EventMangementFacade::editEvent, key, "l", "Roof");
		ConsoleDriver.call(EventMangementFacade::editEvent, key, "y", "Gala");
		ConsoleDriver.call(EventMangementFacade::editEvent, key, "v", "v");
		assertEquals("#1 Newname (Gala) - 2099-09-09 09:09 @ Roof - Newdesc", EventMangementFacade.getEvent(id).getDetails());
		assertEquals(EventVisibility.PRIVATE, EventMangementFacade.getEvent(id).getVisibility());
	}

	@Test(expected = IllegalArgumentException.class)
	public void unknownEditKeyIsRefused() {
		int id = Fixtures.event(OWNER, "E", EventVisibility.PUBLIC);
		ConsoleDriver.call(EventMangementFacade::editEvent, String.valueOf(id), "z");
	}

	@Test
	public void subEventVisibilityCannotBeEdited() {
		int top = Fixtures.composite(OWNER, "Fest", EventVisibility.PUBLIC);
		int child = Fixtures.subEvent(top, OWNER, "Talk");
		try {
			ConsoleDriver.call(EventMangementFacade::editEvent, String.valueOf(child), "v");
			fail("Allowed changing a sub-event's visibility");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("follow the visibility"));
		}
	}

	@Test
	public void changingVisibilityOfTheMainEventCoversItsSubEvents() {
		int top = Fixtures.composite(OWNER, "Fest", EventVisibility.PUBLIC);
		int child = Fixtures.subEvent(top, OWNER, "Talk");
		ConsoleDriver.call(EventMangementFacade::editEvent, String.valueOf(top), "v", "v");
		assertFalse(EventMangementFacade.canView(EventMangementFacade.getEvent(child), OTHER));
	}

	@Test
	public void editNotifiesEveryoneExceptDecliners() {
		int id = Fixtures.event(OWNER, "E", EventVisibility.PUBLIC);
		Fixtures.invited(OWNER, GUEST, id, RsvpStatus.ACCEPTED);
		Fixtures.invited(OWNER, OTHER, id, RsvpStatus.DECLINED);
		ConsoleDriver.call(EventMangementFacade::editEvent, String.valueOf(id), "l", "Roof");
		assertTrue(Fixtures.texts(GUEST).toString(), Fixtures.texts(GUEST).stream().anyMatch(t -> t.startsWith("Event updated:") && t.contains("Roof")));
		assertFalse(Fixtures.texts(OTHER).stream().anyMatch(t -> t.startsWith("Event updated:")));
	}

	@Test
	public void editingASubEventNotifiesTheMainEventsAttendees() {
		int top = Fixtures.composite(OWNER, "Fest", EventVisibility.PUBLIC);
		int child = Fixtures.subEvent(top, OWNER, "Talk");
		Fixtures.invited(OWNER, GUEST, top, RsvpStatus.ACCEPTED);
		ConsoleDriver.call(EventMangementFacade::editEvent, String.valueOf(child), "l", "Roof");
		assertTrue(Fixtures.texts(GUEST).stream().anyMatch(t -> t.startsWith("Event updated:") && t.contains("Talk")));
	}

	@Test
	public void cancelNotifiesAndRemovesPendingInvites() {
		int id = Fixtures.event(OWNER, "E", EventVisibility.PUBLIC);
		Fixtures.invited(OWNER, GUEST, id, RsvpStatus.PENDING);
		String out = ConsoleDriver.call(EventMangementFacade::cancelEvent, String.valueOf(id));
		assertTrue(out, out.contains("Event cancelled"));
		assertTrue(Fixtures.texts(GUEST).contains("Event cancelled: E (#1)"));
		assertEquals(0, InviteManagerFacade.getInvites(GUEST).length);
		assertTrue(EventMangementFacade.getEvent(id).isCancelled());
	}

	@Test
	public void cancellingASubEventNotifiesButKeepsInvitesToTheMainEvent() {
		int top = Fixtures.composite(OWNER, "Fest", EventVisibility.PUBLIC);
		int child = Fixtures.subEvent(top, OWNER, "Talk");
		Fixtures.invited(OWNER, GUEST, top, RsvpStatus.PENDING);
		ConsoleDriver.call(EventMangementFacade::cancelEvent, String.valueOf(child));
		assertTrue(Fixtures.texts(GUEST).contains("Event cancelled: Talk (#2)"));
		assertEquals(1, InviteManagerFacade.getInvites(GUEST).length);
	}

	@Test
	public void cancellingCanBeTurnedOff() {
		env.set("event.allowCancellation", "false");
		int id = Fixtures.event(OWNER, "E", EventVisibility.PUBLIC);
		try {
			ConsoleDriver.call(EventMangementFacade::cancelEvent);
			fail("Cancelling was allowed");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("turned off"));
		}
		assertFalse(EventMangementFacade.getEvent(id).isCancelled());
	}

	@Test
	public void hiddenEventsLookLikeMissingOnesForEveryAction() {
		int hidden = Fixtures.event(OWNER, "Secret", EventVisibility.PRIVATE);
		Fixtures.login(OTHER);
		Runnable[] actions = {EventMangementFacade::addSubEvent, EventMangementFacade::decorateEvent, EventMangementFacade::editEvent,
				EventMangementFacade::cancelEvent, EventMangementFacade::inviteToEvent, EventMangementFacade::showResponses};
		for (Runnable action : actions) {
			try {
				ConsoleDriver.call(action, String.valueOf(hidden));
				fail("Hidden event was reachable");
			} catch (IllegalArgumentException e) {
				assertEquals("No event with id " + hidden + ".", e.getMessage());
			}
		}
		assertFalse(EventMangementFacade.getEvent(hidden).isCancelled());
	}

	@Test
	public void visibleEventsTellNonOwnersTheyCannotChangeThem() {
		int id = Fixtures.composite(OWNER, "Open", EventVisibility.PUBLIC);
		Fixtures.login(OTHER);
		Runnable[] actions = {EventMangementFacade::decorateEvent, EventMangementFacade::editEvent, EventMangementFacade::cancelEvent, EventMangementFacade::addSubEvent};
		for (Runnable action : actions) {
			try {
				ConsoleDriver.call(action, String.valueOf(id));
				fail("Non-owner was allowed");
			} catch (IllegalArgumentException e) {
				assertTrue(e.getMessage(), e.getMessage().contains("Only the creator"));
			}
		}
	}

	@Test
	public void nonOwnersCannotInvite() {
		int id = Fixtures.event(OWNER, "Open", EventVisibility.PUBLIC);
		Fixtures.login(OTHER);
		try {
			ConsoleDriver.call(EventMangementFacade::inviteToEvent, String.valueOf(id));
			fail("Non-owner invited");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("your own events"));
		}
	}

	@Test
	public void invitesToSubEventsPointToTheMainEvent() {
		int top = Fixtures.composite(OWNER, "Fest", EventVisibility.PUBLIC);
		int child = Fixtures.subEvent(top, OWNER, "Talk");
		try {
			ConsoleDriver.call(EventMangementFacade::inviteToEvent, String.valueOf(child));
			fail("Invited to a sub-event");
		} catch (IllegalArgumentException e) {
			assertEquals("Invite people to the main event, #" + top + ".", e.getMessage());
		}
	}

	@Test
	public void inviteOneUser() {
		int id = Fixtures.event(OWNER, "E", EventVisibility.PRIVATE);
		String out = ConsoleDriver.call(EventMangementFacade::inviteToEvent, String.valueOf(id), String.valueOf(GUEST));
		assertTrue(out, out.contains("Invite sent."));
		assertEquals(1, InviteManagerFacade.getInvites(GUEST).length);
	}

	@Test
	public void inviteAllFriendsSkipsThoseAlreadyInvited() {
		Fixtures.friends(OWNER, GUEST);
		Fixtures.friends(OTHER, OWNER);
		int id = Fixtures.event(OWNER, "E", EventVisibility.PRIVATE);
		Fixtures.invited(OWNER, GUEST, id, RsvpStatus.PENDING);
		String out = ConsoleDriver.call(EventMangementFacade::inviteToEvent, String.valueOf(id), "0");
		assertTrue(out, out.contains("Sent 1 invite(s)."));
		assertEquals(1, InviteManagerFacade.getInvites(OTHER).length);
		assertEquals(1, InviteManagerFacade.getInvites(GUEST).length);
	}

	@Test
	public void responsesShowNamesOnlyToTheCreator() {
		int id = Fixtures.event(OWNER, "E", EventVisibility.PUBLIC);
		Fixtures.invited(OWNER, GUEST, id, RsvpStatus.ACCEPTED);
		String owner = ConsoleDriver.call(EventMangementFacade::showResponses, String.valueOf(id));
		assertTrue(owner, owner.contains("Accepted: 1, Declined: 0, Maybe: 0, Pending: 0"));
		assertTrue(owner, owner.contains("Guest (2): ACCEPTED"));
		Fixtures.login(OTHER);
		String other = ConsoleDriver.call(EventMangementFacade::showResponses, String.valueOf(id));
		assertTrue(other, other.contains("Accepted: 1"));
		assertFalse(other, other.contains("Guest"));
	}

	@Test
	public void responsesForASubEventShowThoseOfTheMainEvent() {
		int top = Fixtures.composite(OWNER, "Fest", EventVisibility.PUBLIC);
		int child = Fixtures.subEvent(top, OWNER, "Talk");
		Fixtures.invited(OWNER, GUEST, top, RsvpStatus.MAYBE);
		String out = ConsoleDriver.call(EventMangementFacade::showResponses, String.valueOf(child));
		assertTrue(out, out.contains("Fest - Accepted: 0, Declined: 0, Maybe: 1, Pending: 0"));
	}

	@Test
	public void cardsCarryResponseNumbersOnlyForTheCreator() {
		int id = Fixtures.event(OWNER, "E", EventVisibility.PUBLIC);
		Fixtures.invited(OWNER, GUEST, id, RsvpStatus.ACCEPTED);
		Fixtures.invited(OWNER, OTHER, id, RsvpStatus.MAYBE);
		EventView owner = EventMangementFacade.getUpcomingEventViews(OWNER).get(0);
		assertEquals(1, owner.going);
		assertEquals(1, owner.maybe);
		assertFalse(EventMangementFacade.getUpcomingEventViews(GUEST).get(0).hasRsvp());
	}

	@Test
	public void listingsDifferByView() {
		Fixtures.eventOn(OWNER, "Past", EventVisibility.PUBLIC, "2000-01-01");
		Fixtures.event(OWNER, "Future", EventVisibility.PUBLIC);
		int off = Fixtures.event(OWNER, "Off", EventVisibility.PUBLIC);
		ConsoleDriver.call(EventMangementFacade::cancelEvent, String.valueOf(off));
		Fixtures.event(GUEST, "GuestsOwn", EventVisibility.PUBLIC);
		assertEquals(set("Future", "GuestsOwn"), Fixtures.names(EventMangementFacade.getUpcomingEventViews(OWNER)));
		assertEquals(set("Past", "Future", "Off", "GuestsOwn"), Fixtures.names(EventMangementFacade.getAllEventViews(OWNER)));
		assertEquals(set("Past", "Future", "Off"), Fixtures.names(EventMangementFacade.getMyEventViews(OWNER)));
	}

	@Test
	public void unfriendingRemovesAccessStraightAway() {
		Fixtures.friends(OWNER, GUEST);
		Fixtures.event(OWNER, "Close", EventVisibility.FRIENDS);
		assertEquals(Collections.singleton("Close"), Fixtures.names(EventMangementFacade.getUpcomingEventViews(GUEST)));
		FriendSubSystem.FriendFacade.removeFriend(GUEST, OWNER);
		assertTrue(EventMangementFacade.getUpcomingEventViews(GUEST).isEmpty());
	}

	@Test
	public void unfollowingRemovesAccessStraightAway() {
		Fixtures.follows(GUEST, OWNER);
		Fixtures.event(OWNER, "Fans", EventVisibility.FOLLOWERS);
		assertEquals(1, EventMangementFacade.getUpcomingEventViews(GUEST).size());
		FriendSubSystem.FriendFacade.unfollowUser(GUEST, OWNER);
		assertTrue(EventMangementFacade.getUpcomingEventViews(GUEST).isEmpty());
	}

	@Test
	public void decliningRemovesAccessGrantedByTheInvite() {
		int id = Fixtures.event(OWNER, "Secret", EventVisibility.PRIVATE);
		Fixtures.invited(OWNER, GUEST, id, RsvpStatus.PENDING);
		assertEquals(1, EventMangementFacade.getUpcomingEventViews(GUEST).size());
		InviteManagerFacade.rejectInvite(Fixtures.findEventInvite(GUEST, id));
		assertTrue(EventMangementFacade.getUpcomingEventViews(GUEST).isEmpty());
	}

	@Test
	public void decliningDoesNotOverrideFriendship() {
		Fixtures.friends(OWNER, GUEST);
		int id = Fixtures.event(OWNER, "Close", EventVisibility.FRIENDS);
		Fixtures.invited(OWNER, GUEST, id, RsvpStatus.DECLINED);
		assertEquals(1, EventMangementFacade.getUpcomingEventViews(GUEST).size());
	}

	@Test
	public void beingFollowedByTheCreatorGrantsNothing() {
		Fixtures.follows(OWNER, GUEST);
		Fixtures.event(OWNER, "Fans", EventVisibility.FOLLOWERS);
		assertTrue(EventMangementFacade.getUpcomingEventViews(GUEST).isEmpty());
	}

	@Test
	public void eventsCreatedByGivesTheirTopLevelEvents() {
		int top = Fixtures.composite(OWNER, "Fest", EventVisibility.PUBLIC);
		Fixtures.subEvent(top, OWNER, "Talk");
		assertEquals(1, EventMangementFacade.getEventsCreatedBy(OWNER).size());
		assertEquals(2, EventMangementFacade.getEventRecordsCreatedBy(OWNER).size());
	}

	@Test
	public void missingEventGivesNull() {
		assertNull(EventMangementFacade.getEvent(99));
	}
}
