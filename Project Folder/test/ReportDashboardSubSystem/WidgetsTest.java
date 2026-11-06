package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import ReportDashboardSubSystem.Theme.Role;
import TestSupport.TestEnvironment;

public class WidgetsTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	private static final String LONG = "This text is much much longer than any box could possibly be drawn on a terminal screen, even a wide one, and then it goes on some more";

	private void assertAllLinesWide(List<String> lines, int width) {
		for (String line : lines) {
			assertEquals("[" + Style.strip(line) + "]", width, Style.visibleLength(line));
		}
	}

	private void forEveryMode(java.util.function.IntConsumer check) {
		for (String color : new String[] {"off", "on"}) {
			for (String unicode : new String[] {"off", "on"}) {
				for (int width : new int[] {40, 78, 120}) {
					env.set("ui.color", color);
					env.set("ui.unicode", unicode);
					env.set("ui.width", String.valueOf(width));
					check.accept(width);
				}
			}
		}
	}

	@Test
	public void titleBarLinesAreAllTheSameWidth() {
		forEveryMode(width -> {
			assertAllLinesWide(Widgets.titleBar("Events"), width);
			assertAllLinesWide(Widgets.titleBar(LONG), width);
		});
	}

	@Test
	public void boxLinesAreAllTheSameWidthWhateverTheContent() {
		forEveryMode(width -> {
			assertAllLinesWide(Widgets.box("Title", "Right", Arrays.asList("short"), Role.FRAME), width);
			assertAllLinesWide(Widgets.box(LONG, LONG, Arrays.asList(LONG), Role.FRAME), width);
			assertAllLinesWide(Widgets.box(null, null, new ArrayList<>(), Role.FRAME), width);
			assertAllLinesWide(Widgets.box(Style.bold(Role.TITLE, LONG), Style.color(Role.ACCENT, "right"), Arrays.asList(Style.color(Role.MUSIC, LONG), "a\nb"), Role.FRAME), width);
			assertAllLinesWide(Widgets.box("Only title", null, Arrays.asList(""), Role.FRAME), width);
			assertAllLinesWide(Widgets.box(null, "Only right", Arrays.asList(""), Role.FRAME), width);
		});
	}

	@Test
	public void boxUsesTheRightBorderCharacters() {
		env.set("ui.width", "40");
		List<String> ascii = Widgets.box("T", null, Arrays.asList("x"), Role.FRAME);
		assertTrue(ascii.get(0), ascii.get(0).startsWith("+ T -"));
		assertTrue(ascii.get(1), ascii.get(1).startsWith("| x"));
		assertTrue(ascii.get(2), ascii.get(2).startsWith("+--"));
		env.set("ui.unicode", "on");
		List<String> fancy = Widgets.box("T", null, Arrays.asList("x"), Role.FRAME);
		assertTrue(fancy.get(0), fancy.get(0).startsWith("\u250C T \u2500"));
		assertTrue(fancy.get(1), fancy.get(1).startsWith("\u2502 x"));
		assertTrue(fancy.get(2), fancy.get(2).startsWith("\u2514\u2500"));
	}

	@Test
	public void newLinesInBoxTextMakeSeparateRows() {
		assertEquals(4, Widgets.box(null, null, Arrays.asList("a\nb"), Role.FRAME).size());
	}

	@Test
	public void titleBarIsUpperCase() {
		assertTrue(Widgets.titleBar("Events").get(1).contains("EVENTS"));
	}

	@Test
	public void plainTerminalsHaveNoLogo() {
		assertTrue(Widgets.logo().isEmpty());
	}

	@Test
	public void fancyLogoHasSixLinesOfEqualWidthWithAGradient() {
		env.set("ui.unicode", "on");
		env.set("ui.color", "on");
		List<String> logo = Widgets.logo();
		assertEquals(6, logo.size());
		int width = Style.visibleLength(logo.get(0));
		int[] gradient = Theme.NEON.getLogoGradient();
		for (int i = 0; i < logo.size(); i++) {
			assertEquals(width, Style.visibleLength(logo.get(i)));
			assertTrue(logo.get(i).startsWith("\033[38;5;" + gradient[i] + "m"));
			assertFalse("Stand-in characters must be replaced", Style.strip(logo.get(i)).matches(".*[X{}\\[\\]=!].*"));
		}
	}

	@Test
	public void tableLinesUpColumnsEvenWithColour() {
		env.set("ui.color", "on");
		List<String[]> rows = new ArrayList<>();
		rows.add(new String[] {Style.color(Role.SUCCESS, "FRIEND"), "Alice", "1"});
		rows.add(new String[] {"X", "Bob", "22"});
		List<String> table = Widgets.table(new String[] {"Relation", "Name", "Id"}, rows);
		assertEquals(4, table.size());
		int nameColumn = Style.strip(table.get(0)).indexOf("Name");
		assertEquals(nameColumn, Style.strip(table.get(2)).indexOf("Alice"));
		assertEquals(nameColumn, Style.strip(table.get(3)).indexOf("Bob"));
	}

	@Test
	public void tableIsCutToTheScreenWidth() {
		env.set("ui.width", "40");
		List<String[]> rows = new ArrayList<>();
		rows.add(new String[] {LONG, "x"});
		for (String line : Widgets.table(new String[] {"A", "B"}, rows)) {
			assertTrue(Style.visibleLength(line) <= 40);
		}
	}

	@Test
	public void meterFillsInProportion() {
		assertEquals("##########", Widgets.meter(10, 10, 10, Role.ACCENT));
		assertEquals("#####-----", Widgets.meter(5, 10, 10, Role.ACCENT));
		assertEquals("----------", Widgets.meter(0, 10, 10, Role.ACCENT));
	}

	@Test
	public void meterHandlesEdgeCases() {
		assertEquals("Any value shows", "#---------", Widgets.meter(1, 1000, 10, Role.ACCENT));
		assertEquals("----------", Widgets.meter(5, 0, 10, Role.ACCENT));
		assertEquals("Never overflows", "##########", Widgets.meter(50, 10, 10, Role.ACCENT));
	}

	@Test
	public void stackedBarIsAlwaysExactlyItsWidth() {
		Role[] roles = {Role.SUCCESS, Role.WARNING, Role.INFO, Role.ERROR};
		int[][] cases = {{0, 0, 0, 0}, {1, 0, 0, 0}, {1, 1, 1, 1}, {100, 1, 1, 1}, {1, 1, 1, 100}, {3, 3, 3, 1}, {1, 2, 3, 4}};
		for (String color : new String[] {"off", "on"}) {
			env.set("ui.color", color);
			for (int[] values : cases) {
				for (int width : new int[] {4, 7, 16}) {
					assertEquals(Arrays.toString(values), width, Style.visibleLength(Widgets.stackedBar(values, roles, width)));
				}
			}
		}
	}

	@Test
	public void stackedBarWithNoValuesIsEmpty() {
		assertEquals("----", Widgets.stackedBar(new int[] {0, 0}, new Role[] {Role.SUCCESS, Role.ERROR}, 4));
	}

	@Test
	public void badgeShowsItsText() {
		assertEquals("[FRIEND]", Widgets.badge("FRIEND", Role.SUCCESS));
	}
}
