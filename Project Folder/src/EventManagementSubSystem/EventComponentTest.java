package EventManagementSubSystem;

import static org.junit.Assert.*;

import org.junit.Test;

public class EventComponentTest{

	
	//Tests constructor of Basic Event
	@Test 
	public void testBasicEventConstructor() {
		BasicEvent event = new CompositeEvent("Mosic Night","Live mosic event","2026-03-10","19:00","MoSOC","Public");
		assertNotNull(event);
        assertEquals("Mousic Night", event.name);
        assertEquals("Live mosic event", event.description);
        assertEquals("2026-03-10", event.date);
        assertEquals("19:00", event.time);
        assertEquals("MoSOC", event.location);
        assertEquals("Public", event.type);
	}
	//Tests constructor of Composite Event
	@Test 
	public void testCompositeEventConstructor() {
	}
}
