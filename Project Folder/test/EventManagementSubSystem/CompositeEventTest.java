package EventManagementSubSystem;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

public class CompositeEventTest {

	private BasicEvent leaf(String name) {
		return new BasicEvent(name, "d", "2099-01-01", "10:00", "l", "t");
	}

	private CompositeEvent composite(String name) {
		return new CompositeEvent(name, "d", "2099-01-01", "10:00", "l", "t");
	}

	@Test
	public void addAndGetKeepOrder() {
		CompositeEvent c = composite("top");
		c.add(leaf("a"));
		c.add(leaf("b"));
		assertEquals("a", ((BasicEvent) c.getChild(0)).getName());
		assertEquals("b", ((BasicEvent) c.getChild(1)).getName());
	}

	@Test
	public void removeOnlyRemovesThatChild() {
		CompositeEvent c = composite("top");
		BasicEvent a = leaf("a");
		c.add(a);
		c.add(leaf("b"));
		c.remove(a);
		assertEquals(1, c.getChildren().size());
		assertEquals("b", ((BasicEvent) c.getChild(0)).getName());
	}

	@Test
	public void removingAnUnknownChildChangesNothing() {
		CompositeEvent c = composite("top");
		c.add(leaf("a"));
		c.remove(leaf("a"));
		assertEquals(1, c.getChildren().size());
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void gettingAMissingChildFails() {
		composite("top").getChild(0);
	}

	@Test
	public void listConstructorUsesGivenChildren() {
		CompositeEvent c = new CompositeEvent(new ArrayList<>(Arrays.asList(leaf("a"), leaf("b"))));
		assertEquals(2, c.getChildren().size());
		assertEquals("", c.getName());
	}

	@Test
	public void detailsIndentOneLevelPerNesting() {
		CompositeEvent top = composite("top");
		CompositeEvent middle = composite("middle");
		middle.add(leaf("bottom"));
		top.add(middle);
		String[] lines = top.getDetails().split("\n");
		assertEquals(3, lines.length);
		assertTrue(lines[0].startsWith("#0 top"));
		assertTrue(lines[1].startsWith("    #0 middle"));
		assertTrue(lines[2].startsWith("        #0 bottom"));
	}

	@Test
	public void viewContainsNestedChildren() {
		CompositeEvent top = composite("top");
		CompositeEvent middle = composite("middle");
		middle.add(leaf("bottom"));
		top.add(middle);
		EventView view = top.describe();
		assertEquals("middle", view.children.get(0).name);
		assertEquals("bottom", view.children.get(0).children.get(0).name);
	}
}
