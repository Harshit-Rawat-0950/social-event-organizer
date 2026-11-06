package EventManagementSubSystem;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class EventComponentTest{

	
	//Tests constructor of Basic Event
	@Test 
	public void testBasicEventConstructor() {
		BasicEvent event = new BasicEvent("Mosic Night","Live mosic event","2026-03-10","19:00","MoSOC","Public");
		assertNotNull(event);
        assertEquals("Mosic Night", event.name);
        assertEquals("Live mosic event", event.description);
        assertEquals("2026-03-10", event.date);
        assertEquals("19:00", event.time);
        assertEquals("MoSOC", event.location);
        assertEquals("Public", event.type);
	}
	//Tests constructor of Composite Event
	@Test 
	public void testCompositeEventConstructorandGetter() {
		List<EventComponent> children = new ArrayList<>();
		children.addLast(new BasicEvent("A","B","C","D","E","F"));
		children.addLast(new CompositeEvent("a","b","c","d","e","f"));
		children.addLast(new CompositeEvent(new ArrayList<>()));
		CompositeEvent C = new CompositeEvent(children);
		assertNotNull(C);
		assertEquals(((BasicEvent)(C.getChild(0))).name,"A");
		assertEquals(((CompositeEvent)(C.getChild(1))).name,"a");
		assertEquals(((CompositeEvent)(C.getChild(2))).name,"");
	}
	@Test 
	public void testCompositeEventAddAndRemoveEvent() {
		BasicEvent evet = new BasicEvent("Mosic Night","Live mosic event","2026-03-10","19:00","MoSOC","Public");
		List<EventComponent> children = new ArrayList<>();
		children.addLast(evet);
		CompositeEvent C = new CompositeEvent(children);
		assertEquals(C.getChildren().size(),1);
		C.remove(evet);
		assertEquals(C.getChildren().size(),0);
	}
	@Test 
	public void testEventDecorators() {
		EventDecorator deco = new NewsEventDecorator(new BasicEvent("A","B","C","D","E","F"),"Shreyas is awesomne","He like OOP");
	}
}
