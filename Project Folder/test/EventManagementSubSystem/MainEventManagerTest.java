package EventManagementSubSystem;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import TestSupport.TestEnvironment;

public class MainEventManagerTest {
	private static final int OWNER = 7;

	@Rule
	public TestEnvironment env = new TestEnvironment();

	private MainEventManeger manager;

	@Before
	public void setUp() {
		manager = new MainEventManeger(env.path("events.txt"));
	}

	private BasicEvent event(String name, String date) {
		BasicEvent e = new BasicEvent(name, "desc", date, "10:00", "Hall", "Public");
		e.ownerId = OWNER;
		return e;
	}

	private CompositeEvent composite(String name) {
		CompositeEvent e = new CompositeEvent(name, "desc", "2099-01-01", "10:00", "Hall", "Public");
		e.ownerId = OWNER;
		return e;
	}

	@Test
	public void missingFoldersAndFileAreCreated() {
		new MainEventManeger(env.path("a/b/events.txt"));
		assertTrue(Files.exists(Paths.get(env.path("a/b/events.txt"))));
	}

	@Test
	public void eventsAreStoredOnSeparateLinesWithUniqueIds() throws Exception {
		assertEquals(1, manager.addEvent(event("A", "2099-01-01")));
		assertEquals(2, manager.addEvent(event("B", "2099-01-01")));
		assertEquals(2, env.lines("events.txt").size());
		assertEquals("B", manager.findEvent(2).getName());
	}

	@Test
	public void idsContinueAfterTheHighestExisting() {
		manager.addEvent(event("A", "2099-01-01"));
		assertEquals(2, new MainEventManeger(env.path("events.txt")).addEvent(event("B", "2099-01-01")));
	}

	@Test
	public void findingAMissingEventGivesNull() {
		assertNull(manager.findEvent(99));
	}

	@Test
	public void varargsCreateKeepsEveryField() {
		int id = manager.createBasicEvent("Name", "Desc", "2099-05-06", "07:08", "Place", "Kind");
		String details = manager.findEvent(id).getDetails();
		assertTrue(details, details.contains("Name (Kind) - 2099-05-06 07:08 @ Place - Desc"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void varargsCreateRejectsMissingFields() {
		manager.createBasicEvent("Name", "Desc");
	}

	@Test
	public void everyFieldRejectsEverySeparatorAndBlanks() {
		for (int field = 0; field < 6; field++) {
			for (String bad : new String[] {"a,b", "a;b", "a|b", "", "   "}) {
				String[] values = {"N", "D", "2099-01-01", "10:00", "L", "T"};
				values[field] = bad;
				try {
					manager.createBasicEvent(values);
					fail("Accepted '" + bad + "' in field " + field);
				} catch (IllegalArgumentException expected) {
					assertNotNull(expected.getMessage());
				}
			}
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidDatesAreRejected() {
		manager.addEvent(event("A", "tomorrow"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void impossibleDatesAreRejected() {
		manager.addEvent(event("A", "2099-02-30"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidTimesAreRejected() {
		BasicEvent e = event("A", "2099-01-01");
		e.time = "25:00";
		manager.addEvent(e);
	}

	@Test
	public void pastDatesAreAcceptedHereBecauseOldDataHasThem() {
		assertEquals(1, manager.addEvent(event("Old", "2000-01-01")));
	}

	@Test
	public void subEventsNestAndSurviveReload() {
		int top = manager.addEvent(composite("Fest"));
		int middle = manager.addSubEvent(top, OWNER, composite("Day One"));
		manager.addSubEvent(middle, OWNER, event("Keynote", "2099-01-01"));
		String details = new MainEventManeger(env.path("events.txt")).getAllEvents().get(0).getDetails();
		assertTrue(details, details.contains("Fest"));
		assertTrue(details, details.contains("\n    #2 Day One"));
		assertTrue(details, details.contains("\n        #3 Keynote"));
		assertEquals("Only top level events are listed", 1, manager.getAllEvents().size());
	}

	@Test
	public void subEventsTakeTheVisibilityOfTheRoot() {
		CompositeEvent top = composite("Fest");
		top.setVisibility(EventVisibility.PRIVATE);
		int topId = manager.addEvent(top);
		int middle = manager.addSubEvent(topId, OWNER, composite("Day"));
		int leaf = manager.addSubEvent(middle, OWNER, event("Talk", "2099-01-01"));
		assertEquals(EventVisibility.PRIVATE, manager.findEvent(leaf).getVisibility());
	}

	@Test
	public void rootOfAnEventIsFoundThroughAnyDepth() {
		int top = manager.addEvent(composite("Fest"));
		int middle = manager.addSubEvent(top, OWNER, composite("Day"));
		int leaf = manager.addSubEvent(middle, OWNER, event("Talk", "2099-01-01"));
		assertEquals(top, manager.getRootEvent(leaf).getId());
		assertEquals(top, manager.getRootEvent(top).getId());
		assertNull(manager.getRootEvent(99));
	}

	@Test(expected = IllegalArgumentException.class)
	public void basicEventsCannotHoldSubEvents() {
		int id = manager.addEvent(event("A", "2099-01-01"));
		manager.addSubEvent(id, OWNER, event("B", "2099-01-01"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void onlyTheOwnerCanAddSubEvents() {
		int id = manager.addEvent(composite("A"));
		manager.addSubEvent(id, OWNER + 1, event("B", "2099-01-01"));
	}

	@Test
	public void decorationsAreStoredAndAllShown() {
		int id = manager.addEvent(event("Gig", "2099-01-01"));
		manager.addDecoration(id, OWNER, "MUSIC|Queen");
		manager.addDecoration(id, OWNER, "URGENT|5");
		manager.addDecoration(id, OWNER, "OUTING|4|Taj");
		manager.addDecoration(id, OWNER, "NEWS|Big|Very big");
		manager.addDecoration(id, OWNER, "BUSINESS|Sales");
		String details = manager.getAllEvents().get(0).getDetails();
		for (String tag : new String[] {"[Band: Queen]", "[Urgency: 5/5]", "[Outing: Taj - 4 star]", "[News: Big - Very big]", "[Business: Sales]"}) {
			assertTrue(details, details.contains(tag));
		}
		assertEquals(5, manager.getAllEvents().get(0).describe().tags.size());
	}

	@Test
	public void decorationsOnSubEventsStayWithThem() {
		int top = manager.addEvent(composite("Fest"));
		int child = manager.addSubEvent(top, OWNER, event("Keynote", "2099-01-01"));
		manager.addDecoration(child, OWNER, "URGENT|2");
		EventView view = manager.getAllEvents().get(0).describe();
		assertTrue(view.tags.isEmpty());
		assertEquals(EventView.TagKind.URGENT, view.children.get(0).tags.get(0).kind);
	}

	@Test
	public void decorationsOnCompositeStayOnItsOwnLine() {
		int top = manager.addEvent(composite("Fest"));
		manager.addSubEvent(top, OWNER, event("Keynote", "2099-01-01"));
		manager.addDecoration(top, OWNER, "URGENT|2");
		String[] lines = manager.getAllEvents().get(0).getDetails().split("\n");
		assertEquals(2, lines.length);
		assertTrue(lines[0], lines[0].endsWith("[Urgency: 2/5]"));
	}

	@Test
	public void upcomingSkipsPastAndCancelledAndSortsBySoonest() {
		manager.addEvent(event("Past", "2000-01-01"));
		manager.addEvent(event("Later", "2099-12-31"));
		manager.addEvent(event("Sooner", "2099-01-01"));
		int cancelled = manager.addEvent(event("Off", "2099-06-01"));
		manager.cancelEvent(cancelled, OWNER);
		List<EventComponent> upcoming = manager.getUpcomingEvents();
		assertEquals(2, upcoming.size());
		assertTrue(upcoming.get(0).getDetails().contains("Sooner"));
		assertTrue(upcoming.get(1).getDetails().contains("Later"));
	}

	@Test
	public void upcomingBreaksDateTiesByTime() {
		BasicEvent late = event("Late", "2099-01-01");
		late.time = "22:00";
		BasicEvent early = event("Early", "2099-01-01");
		early.time = "08:00";
		manager.addEvent(late);
		manager.addEvent(early);
		assertTrue(manager.getUpcomingEvents().get(0).getDetails().contains("Early"));
	}

	@Test
	public void todayIsUpcomingAndYesterdayIsNot() {
		manager.addEvent(event("Today", LocalDate.now().toString()));
		manager.addEvent(event("Yesterday", LocalDate.now().minusDays(1).toString()));
		List<EventComponent> upcoming = manager.getUpcomingEvents();
		assertEquals(1, upcoming.size());
		assertTrue(upcoming.get(0).getDetails().contains("Today"));
	}

	@Test
	public void subEventsAreNotListedSeparatelyAsUpcoming() {
		int top = manager.addEvent(composite("Fest"));
		manager.addSubEvent(top, OWNER, event("Keynote", "2099-01-01"));
		assertEquals(1, manager.getUpcomingEvents().size());
	}

	@Test
	public void filtersApplyToListings() {
		manager.addEvent(event("Keep", "2099-01-01"));
		manager.addEvent(event("Drop", "2099-01-01"));
		assertEquals(1, manager.getAllEvents(e -> e.getName().equals("Keep")).size());
		assertEquals(1, manager.getUpcomingEvents(e -> e.getName().equals("Keep")).size());
	}

	@Test
	public void cancelCascadesToSubEvents() {
		int top = manager.addEvent(composite("Fest"));
		int child = manager.addSubEvent(top, OWNER, composite("Day"));
		int leaf = manager.addSubEvent(child, OWNER, event("Keynote", "2099-01-01"));
		assertEquals(3, manager.cancelEvent(top, OWNER).size());
		assertTrue(manager.findEvent(child).isCancelled());
		assertTrue(manager.findEvent(leaf).isCancelled());
	}

	@Test
	public void cancellingASubEventLeavesItsParentActive() {
		int top = manager.addEvent(composite("Fest"));
		int child = manager.addSubEvent(top, OWNER, event("Keynote", "2099-01-01"));
		manager.cancelEvent(child, OWNER);
		assertFalse(manager.findEvent(top).isCancelled());
		assertTrue(manager.getAllEvents().get(0).getDetails().contains("[CANCELLED]"));
	}

	@Test
	public void cancellingReportsEachEventOnce() {
		int top = manager.addEvent(composite("Fest"));
		int child = manager.addSubEvent(top, OWNER, event("Keynote", "2099-01-01"));
		manager.cancelEvent(child, OWNER);
		assertEquals("Already cancelled children are not repeated", 1, manager.cancelEvent(top, OWNER).size());
	}

	@Test
	public void everyFieldCanBeEditedAlone() {
		int id = manager.addEvent(event("A", "2099-01-01"));
		manager.updateEventField(id, OWNER, "name", "B");
		manager.updateEventField(id, OWNER, "description", "Newdesc");
		manager.updateEventField(id, OWNER, "date", "2099-02-02");
		manager.updateEventField(id, OWNER, "time", "12:30");
		manager.updateEventField(id, OWNER, "location", "Roof");
		manager.updateEventField(id, OWNER, "type", "Gala");
		manager.updateEventField(id, OWNER, "visibility", "FRIENDS");
		assertEquals("#1 B (Gala) - 2099-02-02 12:30 @ Roof - Newdesc", manager.findEvent(id).getDetails());
		assertEquals(EventVisibility.FRIENDS, manager.findEvent(id).getVisibility());
	}

	@Test(expected = IllegalArgumentException.class)
	public void unknownFieldIsRejected() {
		int id = manager.addEvent(event("A", "2099-01-01"));
		manager.updateEventField(id, OWNER, "colour", "red");
	}

	@Test
	public void invalidEditLeavesTheFileUnchanged() throws Exception {
		int id = manager.addEvent(event("A", "2099-01-01"));
		List<String> before = env.lines("events.txt");
		try {
			manager.updateEventField(id, OWNER, "date", "someday");
			fail("Accepted a bad date");
		} catch (IllegalArgumentException expected) {
			assertEquals(before, env.lines("events.txt"));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void onlyTheOwnerCanEdit() {
		int id = manager.addEvent(event("A", "2099-01-01"));
		manager.updateEventField(id, OWNER + 1, "name", "Hacked");
	}

	@Test(expected = IllegalArgumentException.class)
	public void onlyTheOwnerCanCancel() {
		int id = manager.addEvent(event("A", "2099-01-01"));
		manager.cancelEvent(id, OWNER + 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void onlyTheOwnerCanDecorate() {
		int id = manager.addEvent(event("A", "2099-01-01"));
		manager.addDecoration(id, OWNER + 1, "URGENT|1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void cancelledEventsCannotBeEdited() {
		int id = manager.addEvent(event("A", "2099-01-01"));
		manager.cancelEvent(id, OWNER);
		manager.updateEventField(id, OWNER, "name", "Back");
	}

	@Test(expected = IllegalArgumentException.class)
	public void cancelledEventsCannotBeCancelledAgain() {
		int id = manager.addEvent(event("A", "2099-01-01"));
		manager.cancelEvent(id, OWNER);
		manager.cancelEvent(id, OWNER);
	}

	@Test(expected = IllegalArgumentException.class)
	public void missingEventsCannotBeChanged() {
		manager.updateEventField(99, OWNER, "name", "X");
	}

	@Test
	public void ownerListingsExcludeOthersAndSubEventsAtTopLevel() {
		int top = manager.addEvent(composite("Mine"));
		manager.addSubEvent(top, OWNER, event("MineChild", "2099-01-01"));
		BasicEvent other = event("Theirs", "2099-01-01");
		other.ownerId = OWNER + 1;
		manager.addEvent(other);
		assertEquals(1, manager.getEventsByOwner(OWNER).size());
		assertEquals("Records include sub-events", 2, manager.getRecordsByOwner(OWNER).size());
		assertTrue(manager.getEventsByOwner(99).isEmpty());
	}
}
