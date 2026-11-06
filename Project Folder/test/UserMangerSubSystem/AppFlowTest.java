package UserMangerSubSystem;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import ReportDashboardSubSystem.ConsoleInput;
import TestSupport.ConsoleDriver;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

public class AppFlowTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Test
	public void zeroExitsFromTheLoginScreen() {
		String out = ConsoleDriver.app("0");
		assertTrue(out, out.contains("USER LOGIN"));
		assertTrue(out, out.contains("Goodbye!"));
	}

	@Test
	public void unknownIdOffersRegistrationThenLoginAndLogout() {
		String out = ConsoleDriver.app("5", "y", "5", "pw", "a@x", "Alice", "Dev", "5", "pw", "0");
		assertTrue(out, out.contains("User does not exist."));
		assertTrue(out, out.contains("USER REGISTRATION"));
		assertTrue(out, out.contains("User Registration Successful"));
		assertTrue(out, out.contains("Login successful! Welcome, Alice"));
		assertTrue(out, out.contains("MAIN MENU"));
		assertTrue(out, out.contains("Logged out. Goodbye!"));
		assertNull(UserManagementFacade.getCurrUser());
	}

	@Test
	public void decliningRegistrationReturnsToLogin() {
		String out = ConsoleDriver.app("5", "n", "0");
		assertFalse(out, out.contains("USER REGISTRATION"));
		assertNull(UserManagementFacade.findUser(5));
	}

	@Test
	public void anythingElseAtTheRegistrationQuestionReturnsToLogin() {
		String out = ConsoleDriver.app("5", "what", "0");
		assertTrue(out, out.contains("Please answer y or n."));
	}

	@Test
	public void registeringATakenIdShowsTheErrorAndReturnsToLogin() {
		Fixtures.user(1, "Alice");
		String out = ConsoleDriver.app("5", "y", "1", "0");
		assertTrue(out, out.contains("User already exists"));
	}

	@Test
	public void tooManyAttemptsReturnsToLogin() {
		Fixtures.user(1, "Alice");
		String out = ConsoleDriver.app("1", "a", "b", "c", "0");
		assertTrue(out, out.contains("Too many attempts"));
		assertFalse(out, out.contains("MAIN MENU"));
	}

	@Test
	public void devModeShowsStartupInformation() {
		Fixtures.user(1, "Alice");
		String out = ConsoleDriver.session(1);
		assertTrue(out, out.contains("Development mode"));
		assertTrue(out, out.contains("Event Management System v1.0, mode=dev"));
		assertTrue(out, out.contains("1 registered users"));
	}

	@Test
	public void otherModesHideStartupInformation() {
		env.set("app.mode", "production");
		Fixtures.user(1, "Alice");
		assertFalse(ConsoleDriver.session(1).contains("Development mode"));
	}

	@Test
	public void dataPersistsBetweenSessions() {
		Fixtures.user(1, "Alice");
		ConsoleDriver.session(1, "1", "c", "n", "Party", "Fun", "2099-01-01", "10:00", "Hall", "", "", "r");
		String second = ConsoleDriver.session(1, "1", "r");
		assertTrue(second, second.contains("#1 Party"));
	}

	@Test
	public void anotherUserCanLogInAfterLogout() {
		Fixtures.user(1, "Alice");
		Fixtures.user(2, "Bob");
		ConsoleDriver.session(1);
		String out = ConsoleDriver.session(2);
		assertTrue(out, out.contains("Welcome, Bob"));
	}

	@Test
	public void runningOutOfInputEndsTheAppCleanly() {
		ConsoleInput.getInstance().useInput(new java.io.ByteArrayInputStream(new byte[0]));
		java.io.ByteArrayOutputStream captured = new java.io.ByteArrayOutputStream();
		java.io.PrintStream original = System.out;
		System.setOut(new java.io.PrintStream(captured));
		try {
			AppStartUp.main(new String[0]);
		} finally {
			System.setOut(original);
		}
		assertTrue(captured.toString().contains("Input closed. Exiting."));
	}

	@Test
	public void getInstanceAlwaysGivesTheSameObject() {
		assertSame(AppStartUp.getInstance(), AppStartUp.getInstance());
	}
}
