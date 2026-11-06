package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import ReportDashboardSubSystem.Theme.Role;
import TestSupport.TestEnvironment;

public class StyleTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Test
	public void plainModeAddsNoCodes() {
		assertEquals("x", Style.color(Role.TITLE, "x"));
		assertEquals("x", Style.bold(Role.TITLE, "x"));
		assertEquals("x", Style.strike(Role.TITLE, "x"));
		assertEquals("x", Style.colorCode(5, "x"));
	}

	@Test
	public void colourModeWrapsAndAlwaysResets() {
		env.set("ui.color", "on");
		for (String styled : new String[] {Style.color(Role.TITLE, "x"), Style.bold(Role.TITLE, "x"), Style.strike(Role.TITLE, "x"), Style.colorCode(5, "x")}) {
			assertTrue(styled, styled.startsWith("\033["));
			assertTrue(styled, styled.endsWith("\033[0m"));
			assertEquals("x", Style.strip(styled));
			assertEquals(1, Style.visibleLength(styled));
		}
	}

	@Test
	public void boldAndStrikeUseTheirOwnCodes() {
		env.set("ui.color", "on");
		assertTrue(Style.bold(Role.TITLE, "x").startsWith("\033[1;38;5;"));
		assertTrue(Style.strike(Role.TITLE, "x").startsWith("\033[9;38;5;"));
	}

	@Test
	public void colourComesFromTheActiveTheme() {
		env.set("ui.color", "on");
		env.set("ui.theme", "amber");
		assertTrue(Style.color(Role.TITLE, "x").contains("38;5;" + Theme.AMBER.colour(Role.TITLE) + "m"));
	}

	@Test
	public void emptyTextStaysEmpty() {
		env.set("ui.color", "on");
		assertEquals("", Style.color(Role.TITLE, ""));
	}

	@Test
	public void padIgnoresColourCodes() {
		env.set("ui.color", "on");
		assertEquals(5, Style.visibleLength(Style.pad(Style.color(Role.TITLE, "ab"), 5)));
		assertEquals("abcdef", Style.pad("abcdef", 3));
	}

	@Test
	public void truncateLeavesShortTextAlone() {
		assertEquals("abc", Style.truncate("abc", 3));
	}

	@Test
	public void truncateCutsToWidthWithAMarker() {
		assertEquals("abc..", Style.truncate("abcdefgh", 5));
		assertEquals("..", Style.truncate("abcdefgh", 2));
		assertEquals(".", Style.truncate("abcdefgh", 1));
	}

	@Test
	public void truncateKeepsColourAndWidth() {
		env.set("ui.color", "on");
		String cut = Style.truncate(Style.color(Role.TITLE, "abcdefgh") + Style.color(Role.ACCENT, "ijkl"), 6);
		assertEquals(6, Style.visibleLength(cut));
		assertEquals("abcd..", Style.strip(cut));
		assertTrue(cut.endsWith("\033[0m"));
	}

	@Test
	public void wrapBreaksBetweenWords() {
		assertEquals(Arrays.asList("one two", "three"), Style.wrap("one two three", 7));
	}

	@Test
	public void wrapSplitsVeryLongWords() {
		assertEquals(Arrays.asList("abcd", "efgh", "ij"), Style.wrap("abcdefghij", 4));
	}

	@Test
	public void wrapNeverExceedsTheWidth() {
		List<String> lines = Style.wrap("a bb ccc dddddddddddddd e ff", 5);
		for (String line : lines) {
			assertTrue(line, line.length() <= 5);
		}
		assertEquals("a bb ccc dddddddddddddd e ff".replace(" ", ""), String.join("", lines).replace(" ", ""));
	}

	@Test
	public void wrapOfNothingGivesOneEmptyLine() {
		assertEquals(Arrays.asList(""), Style.wrap("   ", 5));
	}
}
