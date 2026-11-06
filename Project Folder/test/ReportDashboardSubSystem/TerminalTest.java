package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import TestSupport.ConsoleDriver;
import TestSupport.TestEnvironment;

public class TerminalTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Test
	public void settingsAreReadFromTheConfig() {
		env.set("ui.color", "on");
		env.set("ui.unicode", "on");
		env.set("ui.width", "90");
		env.set("ui.theme", "ocean");
		Terminal t = Terminal.get();
		assertTrue(t.isColor());
		assertTrue(t.isUnicode());
		assertEquals(90, t.getWidth());
		assertEquals(Theme.OCEAN, t.getTheme());
	}

	@Test
	public void onAndOffOverrideAuto() {
		assertTrue(Terminal.isOn("on", false));
		assertTrue(Terminal.isOn(" ON ", false));
		assertFalse(Terminal.isOn("off", true));
		assertTrue(Terminal.isOn("auto", true));
		assertFalse(Terminal.isOn("auto", false));
		assertTrue(Terminal.isOn("whatever", true));
	}

	// The tests have no real console, so auto must give the plain output
	@Test
	public void autoIsPlainWithoutARealConsole() {
		env.set("ui.color", "auto");
		env.set("ui.unicode", "auto");
		assertFalse(Terminal.get().isColor());
		assertFalse(Terminal.get().isUnicode());
	}

	@Test
	public void missingSettingsUseDefaults() {
		env.remove("ui.width");
		env.remove("ui.theme");
		assertEquals(78, Terminal.get().getWidth());
		assertEquals(Theme.NEON, Terminal.get().getTheme());
	}

	@Test
	public void widthIsKeptInASafeRange() {
		assertEquals(40, Terminal.clampWidth(5));
		assertEquals(300, Terminal.clampWidth(9000));
		assertEquals(78, Terminal.clampWidth(78));
	}

	@Test
	public void clearOnlyWritesWhenColourIsOn() {
		assertEquals("", ConsoleDriver.call(() -> Terminal.get().clear()));
		env.set("ui.color", "on");
		assertTrue(ConsoleDriver.call(() -> Terminal.get().clear()).startsWith("\033[2J"));
	}

	// Java cannot write some of the symbols in code page 437, so that console gets ASCII
	@Test
	public void glyphsAreOnlyUsedWhereEveryOneCanBeWritten() {
		assertTrue(Terminal.canShowGlyphs(java.nio.charset.StandardCharsets.UTF_8));
		assertFalse(Terminal.canShowGlyphs(java.nio.charset.Charset.forName("IBM437")));
		assertFalse(Terminal.canShowGlyphs(java.nio.charset.StandardCharsets.US_ASCII));
	}

	@Test
	public void resultIsRememberedUntilReset() {
		Terminal first = Terminal.get();
		assertSame(first, Terminal.get());
		Terminal.reset();
		assertNotSame(first, Terminal.get());
	}
}
