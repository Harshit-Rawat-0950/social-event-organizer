package EventManagementSubSystem;

import static org.junit.Assert.*;

import org.junit.Test;

public class BasicEventTest {

	private BasicEvent event() {
		return new BasicEvent("Name", "Desc", "2099-05-06", "07:08", "Place", "Kind");
	}

	@Test
	public void newEventsArePublicActiveAndTopLevel() {
		BasicEvent e = event();
		assertEquals(EventVisibility.PUBLIC, e.getVisibility());
		assertFalse(e.isCancelled());
		assertEquals(0, e.getParentId());
		assertEquals(0, e.getId());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void leafCannotAddChildren() {
		event().add(event());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void leafCannotRemoveChildren() {
		event().remove(event());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void leafHasNoChildren() {
		event().getChild(0);
	}

	@Test
	public void detailsListEveryField() {
		assertEquals("#0 Name (Kind) - 2099-05-06 07:08 @ Place - Desc", event().getDetails());
	}

	@Test
	public void storedFormRoundTripsEveryField() {
		BasicEvent e = event();
		e.id = 4;
		e.ownerId = 9;
		e.parentId = 2;
		e.decorations = "MUSIC|Queen;URGENT|2";
		e.setVisibility(EventVisibility.FRIENDS);
		e.status = BasicEvent.CANCELLED;
		BasicEvent copy = BasicEvent.fromString(BasicEvent.toString(e));
		assertEquals(BasicEvent.toString(e), BasicEvent.toString(copy));
		assertEquals(4, copy.getId());
		assertEquals(9, copy.getOwnerId());
		assertEquals(2, copy.getParentId());
		assertEquals(EventVisibility.FRIENDS, copy.getVisibility());
		assertTrue(copy.isCancelled());
		assertFalse(copy instanceof CompositeEvent);
	}

	@Test
	public void compositeKindSurvivesTheStoredForm() {
		CompositeEvent e = new CompositeEvent("N", "D", "2099-01-01", "10:00", "L", "T");
		assertTrue(BasicEvent.fromString(BasicEvent.toString(e)) instanceof CompositeEvent);
	}

	// A real row saved before visibility existed
	@Test
	public void rowsWithoutVisibilityLoadAsPublic() {
		BasicEvent old = BasicEvent.fromString("1,101,0,ACTIVE,BASIC,Happy,Make me happy,2025-11-20,10:20,Bits goa,public,");
		assertEquals(EventVisibility.PUBLIC, old.getVisibility());
		assertEquals("Happy", old.getName());
		assertEquals(101, old.getOwnerId());
	}

	@Test
	public void unknownVisibilityLoadsAsPublic() {
		assertEquals(EventVisibility.PUBLIC, EventVisibility.fromStored("nonsense"));
		assertEquals(EventVisibility.PRIVATE, EventVisibility.fromStored(" private "));
	}

	@Test
	public void viewCarriesEveryField() {
		BasicEvent e = event();
		e.id = 3;
		e.ownerId = 8;
		EventView view = e.describe();
		assertEquals(3, view.id);
		assertEquals(8, view.ownerId);
		assertEquals("Name", view.name);
		assertEquals("Desc", view.description);
		assertEquals("2099-05-06", view.date);
		assertEquals("07:08", view.time);
		assertEquals("Place", view.location);
		assertEquals("Kind", view.type);
		assertEquals(EventVisibility.PUBLIC, view.visibility);
		assertFalse(view.cancelled);
		assertTrue(view.tags.isEmpty());
		assertTrue(view.children.isEmpty());
		assertFalse(view.hasRsvp());
	}
}
