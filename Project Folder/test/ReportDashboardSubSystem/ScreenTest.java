package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;

import TestSupport.ConsoleDriver;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

public class ScreenTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Test
	public void messagesWaitUntilFlushed() {
		String out = ConsoleDriver.call(() -> {
			Screen.success("Yes");
			System.out.print("first");
		});
		assertTrue(out, out.indexOf("first") < out.indexOf("Yes"));
	}

	@Test
	public void eachMessageKindHasItsMark() {
		String out = ConsoleDriver.call(() -> {
			Screen.success("A");
			Screen.error("B");
			Screen.info("C");
		});
		assertTrue(out, out.contains("[ok] A"));
		assertTrue(out, out.contains("[!] B"));
		assertTrue(out, out.contains("[i] C"));
	}

	@Test
	public void flushingTwiceShowsMessagesOnce() {
		String out = ConsoleDriver.call(() -> {
			Screen.info("Once");
			Screen.flush();
			Screen.flush();
		});
		assertEquals(out.indexOf("Once"), out.lastIndexOf("Once"));
	}

	@Test
	public void resetForgetsMessages() {
		String out = ConsoleDriver.call(() -> {
			Screen.info("Lost");
			Screen.reset();
		});
		assertFalse(out, out.contains("Lost"));
	}

	@Test
	public void headerShowsTheUserAndWhereTheyAre() {
		Fixtures.user(1, "Alice");
		Fixtures.login(1);
		String out = ConsoleDriver.call(() -> Screen.open("Events", Arrays.asList(new TextElement("Title", 1, true, false))));
		assertTrue(out, out.contains("* Alice (1) - Tester"));
		assertTrue(out, out.contains("Home > Events"));
		assertTrue(out, out.contains("TITLE"));
	}

	@Test
	public void headerWorksWithNobodyLoggedInAndNoText() {
		String out = ConsoleDriver.call(() -> Screen.open("", Arrays.asList()));
		assertTrue(out, out.contains("Home"));
	}

	@Test
	public void menuShowsAllButTheFirstElement() {
		String out = ConsoleDriver.call(() -> Screen.menu(Arrays.asList(new TextElement("first", 0, true, false), new TextElement("second", 0, true, false))));
		assertFalse(out, out.contains("first"));
		assertTrue(out, out.contains("second"));
	}

	@Test
	public void bareArrowPromptBecomesThePromptSymbol() {
		assertEquals(" > ", Screen.stylePrompt("> "));
		env.set("ui.unicode", "on");
		assertEquals(" " + Glyph.PROMPT.fancy() + " ", Screen.stylePrompt("> "));
		assertEquals(" Name: ", Screen.stylePrompt("Name: "));
	}

	@Test
	public void clearingHappensOnlyInColourMode() {
		env.set("ui.color", "on");
		assertTrue(ConsoleDriver.call(() -> Screen.open("", Arrays.asList())).startsWith("\033[2J"));
	}

	@Test
	public void helpersPrint() {
		String out = ConsoleDriver.call(() -> {
			Screen.section("Sec");
			Screen.empty("(none)");
			Screen.println(Arrays.asList("l1", "l2"));
		});
		for (String part : new String[] {"Sec", "(none)", "l1", "l2"}) {
			assertTrue(out, out.contains(part));
		}
	}
}
