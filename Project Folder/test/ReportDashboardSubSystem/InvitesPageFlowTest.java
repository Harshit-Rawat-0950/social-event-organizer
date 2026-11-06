package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import EventManagementSubSystem.EventVisibility;
import FriendSubSystem.FriendFacade;
import InviteMangerSubSystem.InviteManagerFacade;
import InviteMangerSubSystem.RsvpStatus;
import TestSupport.ConsoleDriver;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

public class InvitesPageFlowTest {
	private static final int ALICE = 1;
	private static final int BOB = 2;

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Before
	public void setUp() {
		Fixtures.user(ALICE, "Alice");
		Fixtures.user(BOB, "Bob");
	}

	private String page(int user, String... keys) {
		String[] all = new String[keys.length + 2];
		all[0] = "2";
		System.arraycopy(keys, 0, all, 1, keys.length);
		all[all.length - 1] = "r";
		return ConsoleDriver.session(user, all);
	}

	@Test
	public void emptyState() {
		String out = page(ALICE);
		assertTrue(out, out.contains("(no pending invites)"));
	}

	@Test
	public void answeringWithNothingPendingIsExplained() {
		String out = page(ALICE, "y", "n", "m");
		assertTrue(out, out.contains("There are no invites to answer."));
	}

	@Test
	public void invalidKeyIsExplained() {
		assertTrue(page(ALICE, "z").contains("Please choose one of the options listed."));
	}

	@Test
	public void friendRequestCanBeAccepted() {
		Fixtures.pendingRequest(BOB, ALICE);
		String out = page(ALICE, "y");
		assertTrue(out, out.contains("Friend request from Bob (2)"));
		assertTrue(out, out.contains("Invite accepted."));
		assertTrue(FriendFacade.areFriends(ALICE, BOB));
	}

	@Test
	public void friendRequestCanBeDeclined() {
		Fixtures.pendingRequest(BOB, ALICE);
		assertTrue(page(ALICE, "n").contains("Invite declined."));
		assertFalse(FriendFacade.areFriends(ALICE, BOB));
	}

	@Test
	public void friendRequestCannotBeMaybe() {
		Fixtures.pendingRequest(BOB, ALICE);
		assertTrue(page(ALICE, "m").contains("only available for event invites"));
		assertEquals(1, InviteManagerFacade.getInvites(ALICE).length);
	}

	@Test
	public void eventInviteAnswersRecordTheResponse() {
		int id = Fixtures.event(BOB, "Gig", EventVisibility.PRIVATE);
		Fixtures.invited(BOB, ALICE, id, RsvpStatus.PENDING);
		String out = page(ALICE, "m", "y");
		assertTrue(out, out.contains("Marked as maybe."));
		assertTrue(out, out.contains("(you said maybe)"));
		assertTrue(out, out.contains("Invite accepted."));
		assertEquals("Accepted: 1, Declined: 0, Maybe: 0, Pending: 0", InviteManagerFacade.getRsvpSummary(id));
	}

	@Test
	public void onlyTheFirstInviteIsAnsweredAndTheRestWait() {
		Fixtures.pendingRequest(BOB, ALICE);
		int id = Fixtures.event(BOB, "Gig", EventVisibility.PUBLIC);
		Fixtures.invited(BOB, ALICE, id, RsvpStatus.PENDING);
		String out = page(ALICE, "n");
		assertTrue(out, out.contains("Answering this one"));
		assertTrue(out, out.contains("Next"));
		assertEquals(1, InviteManagerFacade.getInvites(ALICE).length);
		assertFalse(FriendFacade.areFriends(ALICE, BOB));
	}

	@Test
	public void onlyYourOwnInvitesAppear() {
		Fixtures.pendingRequest(ALICE, BOB);
		assertTrue(page(ALICE).contains("(no pending invites)"));
	}

	@Test
	public void sameInstanceEverywhere() {
		assertSame(InvitesPage.getInstance(), InvitesPage.getInstance());
	}
}
