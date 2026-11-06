package InviteMangerSubSystem;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import EventManagementSubSystem.EventMangementFacade;
import EventManagementSubSystem.EventVisibility;
import FriendSubSystem.FriendFacade;
import TestSupport.ConsoleDriver;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

public class InviteFacadeTest {
	private static final int OWNER = 1;
	private static final int GUEST = 2;
	private static final int OTHER = 3;

	@Rule
	public TestEnvironment env = new TestEnvironment();

	private int event;

	@Before
	public void setUp() {
		Fixtures.user(OWNER, "Owner");
		Fixtures.user(GUEST, "Guest");
		Fixtures.user(OTHER, "Other");
		event = Fixtures.event(OWNER, "Party", EventVisibility.PRIVATE);
	}

	private void assertRefused(String message, Runnable action) {
		try {
			action.run();
			fail("Expected refusal: " + message);
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage(), e.getMessage().contains(message));
		}
	}

	private String pending() {
		return InviteManagerFacade.getInvites(GUEST)[0];
	}

	@Test
	public void invitesReachOnlyTheirReceiver() {
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, event);
		assertEquals(1, InviteManagerFacade.getInvites(GUEST).length);
		assertEquals(0, InviteManagerFacade.getInvites(OTHER).length);
		assertEquals(0, InviteManagerFacade.getInvites(OWNER).length);
	}

	@Test
	public void sendingNotifiesTheGuestAndRecordsPending() {
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, event);
		assertTrue(Fixtures.texts(GUEST).contains("Owner (1) invited you to Party."));
		assertTrue(InviteManagerFacade.getRsvpSummary(event).contains("Pending: 1"));
		assertTrue(InviteManagerFacade.isInvited(event, GUEST));
	}

	@Test
	public void acceptRemovesTheInviteAndTellsTheSender() {
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, event);
		InviteManagerFacade.acceptInvite(pending());
		assertEquals(0, InviteManagerFacade.getInvites(GUEST).length);
		assertEquals("Accepted: 1, Declined: 0, Maybe: 0, Pending: 0", InviteManagerFacade.getRsvpSummary(event));
		assertTrue(Fixtures.texts(OWNER).contains("Guest (2) responded ACCEPTED to Party."));
		assertTrue(InviteManagerFacade.isInvited(event, GUEST));
	}

	@Test
	public void declineRemovesTheInviteAndGivesUpAccess() {
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, event);
		InviteManagerFacade.rejectInvite(pending());
		assertEquals(0, InviteManagerFacade.getInvites(GUEST).length);
		assertEquals("Accepted: 0, Declined: 1, Maybe: 0, Pending: 0", InviteManagerFacade.getRsvpSummary(event));
		assertTrue(Fixtures.texts(OWNER).contains("Guest (2) responded DECLINED to Party."));
		assertFalse(InviteManagerFacade.isInvited(event, GUEST));
	}

	@Test
	public void maybeKeepsTheInviteSoItCanBeAnsweredLater() {
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, event);
		InviteManagerFacade.maybeInvite(pending());
		assertEquals(1, InviteManagerFacade.getInvites(GUEST).length);
		assertTrue(InviteManagerFacade.getRsvpSummary(event).contains("Maybe: 1"));
		assertTrue(Fixtures.texts(OWNER).contains("Guest (2) responded MAYBE to Party."));
		InviteManagerFacade.acceptInvite(pending());
		assertEquals("Accepted: 1, Declined: 0, Maybe: 0, Pending: 0", InviteManagerFacade.getRsvpSummary(event));
	}

	@Test
	public void maybeThenDeclineCountsOnlyTheDecline() {
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, event);
		InviteManagerFacade.maybeInvite(pending());
		InviteManagerFacade.rejectInvite(pending());
		assertEquals("Accepted: 0, Declined: 1, Maybe: 0, Pending: 0", InviteManagerFacade.getRsvpSummary(event));
	}

	@Test
	public void friendRequestsCannotBeMaybe() {
		Fixtures.pendingRequest(OTHER, GUEST);
		assertRefused("only available for event invites", () -> InviteManagerFacade.maybeInvite(pending()));
	}

	@Test
	public void decliningAFriendRequestNotifiesNobody() {
		Fixtures.pendingRequest(OTHER, GUEST);
		InviteManagerFacade.rejectInvite(pending());
		assertTrue(Fixtures.texts(OTHER).isEmpty());
		assertFalse(FriendFacade.areFriends(GUEST, OTHER));
	}

	@Test
	public void answeringAnInviteThatIsGoneIsRefused() {
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, event);
		String raw = pending();
		InviteManagerFacade.acceptInvite(raw);
		assertRefused("no longer available", () -> InviteManagerFacade.acceptInvite(raw));
		assertRefused("no longer available", () -> InviteManagerFacade.rejectInvite(raw));
		assertRefused("no longer available", () -> InviteManagerFacade.maybeInvite(raw));
	}

	@Test
	public void invalidInvitesAreRefused() {
		assertRefused("not open for invites", () -> InviteManagerFacade.sendEventInvite(OWNER, GUEST, 99));
		assertRefused("No user with id", () -> InviteManagerFacade.sendEventInvite(OWNER, 99, event));
		assertRefused("does not need an invite", () -> InviteManagerFacade.sendEventInvite(OWNER, OWNER, event));
	}

	@Test
	public void sameUserCannotBeInvitedTwice() {
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, event);
		assertRefused("already been invited", () -> InviteManagerFacade.sendEventInvite(OWNER, GUEST, event));
	}

	@Test
	public void declinedUsersCannotBeInvitedAgain() {
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, event);
		InviteManagerFacade.rejectInvite(pending());
		assertRefused("already been invited", () -> InviteManagerFacade.sendEventInvite(OWNER, GUEST, event));
	}

	@Test
	public void cancelledEventsCannotBeInvitedTo() {
		Fixtures.login(OWNER);
		ConsoleDriver.call(EventMangementFacade::cancelEvent, String.valueOf(event));
		assertRefused("not open for invites", () -> InviteManagerFacade.sendEventInvite(OWNER, GUEST, event));
	}

	@Test
	public void subEventsCannotBeInvitedTo() {
		int top = Fixtures.composite(OWNER, "Fest", EventVisibility.PUBLIC);
		int child = Fixtures.subEvent(top, OWNER, "Talk");
		assertRefused("main event", () -> InviteManagerFacade.sendEventInvite(OWNER, GUEST, child));
		assertRefused("main event", () -> EventMangementFacade.getEvent(child).getInvited());
	}

	@Test
	public void capacityCountsEveryoneExceptDecliners() {
		env.set("event.maxCapacity", "1");
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, event);
		assertRefused("maximum capacity", () -> InviteManagerFacade.sendEventInvite(OWNER, OTHER, event));
		InviteManagerFacade.rejectInvite(pending());
		InviteManagerFacade.sendEventInvite(OWNER, OTHER, event);
		assertEquals(1, InviteManagerFacade.getInvites(OTHER).length);
	}

	@Test
	public void expiredInvitesAreNotShownOrAnswerable() {
		Invite expired = new Invite(OTHER, GUEST, LocalDateTime.now().minusDays(1), 0);
		new InviteRepository(env.path("invites.txt")).saveInvite(expired);
		assertEquals(0, InviteManagerFacade.getInvites(GUEST).length);
		assertRefused("no longer available", () -> InviteManagerFacade.acceptInvite(expired.toString()));
	}

	@Test
	public void cancellingRemovesOnlyThatEventsInvites() {
		int second = Fixtures.event(OWNER, "Second", EventVisibility.PUBLIC);
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, event);
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, second);
		InviteManagerFacade.removeEventInvites(event);
		assertEquals(1, InviteManagerFacade.getInvites(GUEST).length);
		assertEquals(second, Invite.fromString(pending()).getEventId());
	}

	@Test
	public void notifyAttendeesSkipsDecliners() {
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, event);
		InviteManagerFacade.sendEventInvite(OWNER, OTHER, event);
		InviteManagerFacade.rejectInvite(InviteManagerFacade.getInvites(OTHER)[0]);
		InviteManagerFacade.notifyAttendees(event, "Hello");
		assertTrue(Fixtures.texts(GUEST).contains("Hello"));
		assertFalse(Fixtures.texts(OTHER).contains("Hello"));
	}

	@Test
	public void responsesListNames() {
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, event);
		assertEquals(Arrays.asList("Guest (2): PENDING"), InviteManagerFacade.getRsvpResponses(event));
	}

	@Test
	public void descriptionsDependOnTheKindOfInvite() {
		Fixtures.pendingRequest(OTHER, GUEST);
		assertEquals("Friend request from Other (3)", InviteManagerFacade.describeInvite(pending()));
		InviteManagerFacade.rejectInvite(pending());
		InviteManagerFacade.sendEventInvite(OWNER, GUEST, event);
		assertTrue(InviteManagerFacade.describeInvite(pending()).startsWith("Event invite from Owner (1): #1 Party"));
		InviteManagerFacade.maybeInvite(pending());
		assertTrue(InviteManagerFacade.describeInvite(pending()).endsWith("(you said maybe)"));
	}

	@Test
	public void descriptionSurvivesAMissingEvent() {
		String raw = new Invite(OWNER, GUEST, 77).toString();
		assertTrue(InviteManagerFacade.describeInvite(raw).contains("#77"));
	}
}
