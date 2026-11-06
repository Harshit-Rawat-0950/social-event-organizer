package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

import ReportDashboardSubSystem.Theme.Role;

public class ThemeTest {

	@Test
	public void everyThemeDefinesAValidColourForEveryRole() {
		for (Theme theme : Theme.values()) {
			for (Role role : Role.values()) {
				int colour = theme.colour(role);
				assertTrue(theme + " " + role, colour >= 0 && colour <= 255);
			}
		}
	}

	@Test
	public void everyThemeHasALogoGradientForEachLine() {
		for (Theme theme : Theme.values()) {
			assertEquals(6, theme.getLogoGradient().length);
		}
	}

	@Test
	public void gradientCannotBeChangedFromOutside() {
		Theme.NEON.getLogoGradient()[0] = -1;
		assertNotEquals(-1, Theme.NEON.getLogoGradient()[0]);
	}

	@Test
	public void themesAreFoundByNameInAnyCase() {
		assertEquals(Theme.AMBER, Theme.fromName("amber"));
		assertEquals(Theme.OCEAN, Theme.fromName(" OCEAN "));
		assertEquals(Theme.NEON, Theme.fromName("neon"));
	}

	@Test
	public void unknownThemeFallsBackToNeon() {
		assertEquals(Theme.NEON, Theme.fromName("disco"));
		assertEquals(Theme.NEON, Theme.fromName(null));
	}

	// Written out so that adding a symbol the console fonts lack needs a conscious decision
	@Test
	public void everyFancyGlyphIsOnTheListOfSymbolsTheConsoleFontsDraw() {
		String drawable = "\u2554\u2557\u255A\u255D\u2550\u2551\u250C\u2510\u2514\u2518\u2500\u2502\u251C\u2588\u2591\u25BA\u2022\u221A\u203C\u263B\u00BB\u266B\u00A7\u263C\u2666\u25B2 ";
		for (Glyph glyph : Glyph.values()) {
			for (char c : glyph.fancy().toCharArray()) {
				assertTrue(glyph.name(), drawable.indexOf(c) >= 0);
			}
		}
	}

	@Test
	public void everyPlainGlyphIsAscii() {
		for (Glyph glyph : Glyph.values()) {
			assertTrue(glyph.name(), StandardCharsets.US_ASCII.newEncoder().canEncode(glyph.plain()));
		}
	}
}
