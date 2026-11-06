package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import EventManagementSubSystem.EventMangementFacade;
import EventManagementSubSystem.EventVisibility;
import InviteMangerSubSystem.InviteManagerFacade;
import InviteMangerSubSystem.RsvpStatus;
import TestSupport.ConsoleDriver;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

public class EventPageFlowTest {
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
		all[0] = "1";
		System.arraycopy(keys, 0, all, 1, keys.length);
		all[all.length - 1] = "r";
		return ConsoleDriver.session(user, all);
	}

	@Test
	public void emptyStateAndMenu() {
		String out = page(ALICE);
		assertTrue(out, out.contains("Upcoming events (0)"));
		assertTrue(out, out.contains("(none)"));
		for (String key : new String[] {"(c)", "(s)", "(d)", "(e)", "(x)", "(i)", "(v)", "(a)", "(m)", "(r)"}) {
			assertTrue(key, out.contains(key));
		}
	}

	@Test
	public void createThenSeeACard() {
		String out = page(ALICE, "c", "n", "Party", "Fun", "2099-01-01", "10:00", "Hall", "", "");
		assertTrue(out, out.contains("Created event #1."));
		assertTrue(out, out.contains("Upcoming events (1)"));
		assertTrue(out, out.contains("#1 Party"));
		assertTrue(out, out.contains("2099-01-01 10:00 @ Hall"));
	}

	@Test
	public void subEventAppearsUnderItsParent() {
		int top = Fixtures.composite(ALICE, "Fest", EventVisibility.PUBLIC);
		String out = page(ALICE, "s", String.valueOf(top), "n", "Talk", "D", "2099-01-01", "11:00", "Room", "Lecture");
		assertTrue(out, out.contains("`- #2 Talk"));
		assertTrue(out, out.contains("Upcoming events (1)"));
	}

	@Test
	public void decorationAppearsOnTheCard() {
		int id = Fixtures.event(ALICE, "Gig", EventVisibility.PUBLIC);
		String out = page(ALICE, "d", String.valueOf(id), "m", "Queen");
		assertTrue(out, out.contains("Event decorated."));
		assertTrue(out, out.contains("[Band: Queen]"));
	}

	@Test
	public void editShowsTheNewValue() {
		int id = Fixtures.event(ALICE, "Gig", EventVisibility.PUBLIC);
		String out = page(ALICE, "e", String.valueOf(id), "l", "Roof");
		assertTrue(out, out.contains("Event updated and attendees notified."));
		assertTrue(out, out.contains("@ Roof"));
	}

	@Test
	public void cancelMovesTheEventOutOfUpcomingAndIntoAll() {
		int id = Fixtures.event(ALICE, "Gig", EventVisibility.PUBLIC);
		String out = page(ALICE, "x", String.valueOf(id), "a");
		assertTrue(out, out.contains("Event cancelled and attendees notified."));
		assertTrue(out, out.contains("Upcoming events (0)"));
		assertTrue(out, out.contains("All events (1)"));
		assertTrue(out, out.contains("CANCELLED"));
	}

	@Test
	public void viewKeysSwitchBetweenLists() {
		Fixtures.eventOn(ALICE, "Old", EventVisibility.PUBLIC, "2000-01-01");
		Fixtures.event(ALICE, "New", EventVisibility.PUBLIC);
		Fixtures.event(BOB, "Bobs", EventVisibility.PUBLIC);
		String out = page(ALICE, "a", "a", "m", "m");
		assertTrue(out, out.contains("Upcoming events (2)"));
		assertTrue(out, out.contains("All events (3)"));
		assertTrue(out, out.contains("My events (2)"));
	}

	@Test
	public void inviteAndResponses() {
		int id = Fixtures.event(ALICE, "Gig", EventVisibility.PUBLIC);
		String out = page(ALICE, "i", String.valueOf(id), String.valueOf(BOB), "v", String.valueOf(id));
		assertTrue(out, out.contains("Invite sent."));
		assertTrue(out, out.contains("Pending: 1"));
		assertTrue(out, out.contains("Bob (2): PENDING"));
		assertEquals(1, InviteManagerFacade.getInvites(BOB).length);
	}

	@Test
	public void ownerCardsShowResponsesAndGuestCardsDoNot() {
		int id = Fixtures.event(ALICE, "Gig", EventVisibility.PUBLIC);
		Fixtures.invited(ALICE, BOB, id, RsvpStatus.ACCEPTED);
		assertTrue(page(ALICE).contains("1 going - 0 maybe - 0 waiting - 0 declined"));
		assertFalse(page(BOB).contains("going"));
	}

	@Test
	public void errorsAreShownAndThePageCarriesOn() {
		int id = Fixtures.event(ALICE, "Gig", EventVisibility.PUBLIC);
		String out = page(BOB, "e", String.valueOf(id), "e", "99", "z");
		assertTrue(out, out.contains("Only the creator of an event can change it."));
		assertTrue(out, out.contains("No event with id 99."));
		assertTrue(out, out.contains("Please choose one of the options listed."));
	}

	@Test
	public void hiddenEventsDoNotAppear() {
		Fixtures.event(ALICE, "Secret", EventVisibility.PRIVATE);
		String out = page(BOB, "a");
		assertFalse(out, out.contains("Secret"));
		assertTrue(out, out.contains("All events (0)"));
		assertEquals(0, EventMangementFacade.getAllEventViews(BOB).size());
	}

	@Test
	public void cardsAreNotPrintedAsRawCsv() {
		Fixtures.event(ALICE, "Gig", EventVisibility.PUBLIC);
		assertFalse(page(ALICE).contains("ACTIVE,BASIC"));
	}

	@Test
	public void sameInstanceEverywhere() {
		assertSame(EventPage.getInstance(), EventPage.getInstance());
	}
}
