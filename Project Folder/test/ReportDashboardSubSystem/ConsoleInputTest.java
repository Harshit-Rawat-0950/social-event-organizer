package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.Test;

import TestSupport.ConsoleDriver;
import TestSupport.TestEnvironment;

public class ConsoleInputTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	private final ConsoleInput in = ConsoleInput.getInstance();

	private <T> T read(Supplier<T> reader, String... keys) {
		AtomicReference<T> result = new AtomicReference<>();
		ConsoleDriver.call(() -> result.set(reader.get()), keys);
		return result.get();
	}

	private String output(Supplier<?> reader, String... keys) {
		return ConsoleDriver.call(reader::get, keys);
	}

	@Test
	public void lineComesBackExactlyAsTyped() {
		assertEquals("  spaced  ", read(() -> in.readLine("p"), "  spaced  "));
	}

	@Test
	public void promptIsShown() {
		assertTrue(output(() -> in.readLine("Say something: "), "x").contains("Say something: "));
	}

	@Test
	public void waitingMessagesAppearBeforeThePrompt() {
		String out = ConsoleDriver.call(() -> {
			Screen.error("Oops");
			in.readLine("Prompt");
		}, "x");
		assertTrue(out, out.indexOf("Oops") < out.indexOf("Prompt"));
	}

	@Test(expected = InputClosedException.class)
	public void noInputThrowsInsteadOfKillingTheProgram() {
		in.useInput(new java.io.ByteArrayInputStream(new byte[0]));
		java.io.PrintStream original = System.out;
		System.setOut(new java.io.PrintStream(new java.io.ByteArrayOutputStream()));
		try {
			in.readLine("p");
		} finally {
			System.setOut(original);
		}
	}

	@Test
	public void intRetriesUntilValid() {
		assertEquals(Integer.valueOf(7), read(() -> in.readInt("p"), "abc", "", "1.5", " 7 "));
		assertTrue(output(() -> in.readInt("p"), "abc", "7").contains("Please enter a whole number."));
	}

	@Test
	public void intInRangeIncludesBothEnds() {
		assertEquals(Integer.valueOf(1), read(() -> in.readIntInRange("p", 1, 5), "0", "1"));
		assertEquals(Integer.valueOf(5), read(() -> in.readIntInRange("p", 1, 5), "6", "5"));
		assertTrue(output(() -> in.readIntInRange("p", 1, 5), "9", "2").contains("from 1 to 5"));
	}

	@Test
	public void choiceIsTheFirstLetterInLowerCase() {
		assertEquals(Character.valueOf('y'), read(() -> in.readChoice("p"), "  YES "));
		assertEquals(Character.valueOf(' '), read(() -> in.readChoice("p"), ""));
	}

	@Test
	public void fieldRefusesBlanksAndEverySeparator() {
		assertEquals("ok", read(() -> in.readField("p"), "", "   ", "a,b", "a;b", "a|b", " ok "));
		String out = output(() -> in.readField("p"), "", "a,b", "ok");
		assertTrue(out, out.contains("cannot be empty"));
		assertTrue(out, out.contains("avoid these characters"));
	}

	@Test
	public void fieldOrDefaultUsesTheDefaultWhenBlank() {
		assertEquals("Public", read(() -> in.readFieldOrDefault("Type", "Public"), ""));
		assertEquals("Mine", read(() -> in.readFieldOrDefault("Type", "Public"), "a,b", "Mine"));
		assertTrue(output(() -> in.readFieldOrDefault("Type", "Public"), "").contains("Type [Public]: "));
	}

	@Test
	public void passwordRefusesEmptyAndKeepsSpaces() {
		assertEquals(" a b ", read(() -> in.readPassword("p"), "", " a b "));
		assertTrue(output(() -> in.readPassword("p"), "", "x").contains("cannot be empty"));
	}

	@Test
	public void secretAllowsEmpty() {
		assertEquals("", read(() -> in.readSecret("p"), ""));
	}

	@Test
	public void dateMustBeReal() {
		assertEquals("2026-12-31", read(() -> in.readDate("p"), "soon", "31-12-2026", "2026-02-30", "2026-12-31"));
		assertTrue(output(() -> in.readDate("p"), "soon", "2026-12-31").contains("yyyy-MM-dd"));
	}

	@Test
	public void futureDateRefusesThePastButAllowsToday() {
		String today = LocalDate.now().toString();
		String yesterday = LocalDate.now().minusDays(1).toString();
		assertEquals(today, read(() -> in.readFutureDate("p"), yesterday, today));
		assertTrue(output(() -> in.readFutureDate("p"), yesterday, today).contains("cannot be in the past"));
	}

	@Test
	public void timeMustBeReal() {
		assertEquals("19:30", read(() -> in.readTime("p"), "7pm", "25:00", "19:30"));
		assertTrue(output(() -> in.readTime("p"), "7pm", "19:30").contains("HH:mm"));
	}

	@Test
	public void sameInstanceEverywhere() {
		assertSame(ConsoleInput.getInstance(), ConsoleInput.getInstance());
	}
}
