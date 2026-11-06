package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import TestSupport.ConsoleDriver;
import TestSupport.TestEnvironment;

public class TextElementTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Test
	public void readFileAllowsCommasInContentAndSkipsBlankLines() throws Exception {
		Files.write(Paths.get(env.path("page.txt")), Arrays.asList("Hello, world,1,true,false", "", "(c) create,2,false,true"));
		List<TextElement> elements = TextElement.readFile(env.path("page.txt"));
		assertEquals(2, elements.size());
		assertEquals("Hello, world", elements.get(0).content);
		assertEquals(1, elements.get(0).style);
		assertTrue(elements.get(0).newLine);
		assertFalse(elements.get(0).devMode);
		assertFalse(elements.get(1).newLine);
		assertTrue(elements.get(1).devMode);
	}

	@Test
	public void everyRealPageFileLoadsAndStartsWithAHeading() {
		for (String page : new String[] {"mainpage", "eventpage", "invitepage", "notificationpage", "reportpage", "friendpage", "profilepage"}) {
			List<TextElement> elements = TextElement.readFile("Files/UI/" + page + ".txt");
			assertFalse(page, elements.isEmpty());
			assertEquals(page, TextElement.HEADING, elements.get(0).style);
		}
	}

	@Test
	public void stylesChangeTheRenderedText() {
		assertEquals("x", new TextElement("x", TextElement.PLAIN, true, false).stylise());
		assertEquals("   x", new TextElement("x", TextElement.LIST_ITEM, true, false).stylise());
		assertEquals(" x", new TextElement("x", TextElement.MUTED, true, false).stylise());
		assertEquals(" (c) go", new TextElement("(c) go", TextElement.MENU, true, false).stylise());
		assertTrue(new TextElement("x", TextElement.HEADING, true, false).stylise().contains("  X"));
	}

	@Test
	public void menuHighlightsHotkeysAndNumbersOnly() {
		env.set("ui.color", "on");
		String accent = "\033[1;38;5;" + Theme.NEON.colour(Theme.Role.ACCENT) + "m";
		String letters = TextElement.styleMenu("(c) create (e) edit");
		assertTrue(letters, letters.contains(accent + "(c)"));
		assertTrue(letters, letters.contains(accent + "(e)"));
		assertTrue(TextElement.styleMenu("1. Events").contains(accent + "1."));
		assertEquals("(words) stay plain", Style.strip(TextElement.styleMenu("(words) stay plain")));
		assertFalse(TextElement.styleMenu("(words) stay plain").contains(accent));
	}

	@Test
	public void displayHonoursTheNewLineSetting() {
		String with = ConsoleDriver.call(() -> new TextElement("a", 0, true, false).display());
		String without = ConsoleDriver.call(() -> new TextElement("a", 0, false, false).display());
		assertEquals("a" + System.lineSeparator(), with);
		assertEquals("a", without);
	}

	@Test
	public void devOnlyTextShowsOnlyInDevMode() {
		TextElement devOnly = new TextElement("d", 0, true, true);
		TextElement normal = new TextElement("n", 0, true, false);
		assertTrue(devOnly.isAllowed());
		env.set("app.mode", "production");
		assertFalse(devOnly.isAllowed());
		assertTrue(normal.isAllowed());
		String out = ConsoleDriver.call(() -> TextElement.displayList(Arrays.asList(devOnly, null, normal)));
		assertFalse(out, out.contains("d"));
		assertTrue(out, out.contains("n"));
	}

	@Test
	public void withContentKeepsTheStyle() {
		TextElement copy = new TextElement("a", TextElement.MENU, false, true).withContent("b");
		assertEquals("b", copy.getContent());
		assertEquals(TextElement.MENU, copy.style);
		assertFalse(copy.newLine);
		assertTrue(copy.devMode);
	}
}
