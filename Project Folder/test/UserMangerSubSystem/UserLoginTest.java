package UserMangerSubSystem;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import TestSupport.ConsoleDriver;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

public class UserLoginTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	private UserRepository repo;

	@Before
	public void setUp() {
		Fixtures.user(1, "Alice");
		repo = new UserRepository(env.path("users.txt"));
	}

	private String login(String... keys) {
		return ConsoleDriver.call(() -> {
			try {
				new UserLogin().showLogin(repo);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, keys);
	}

	private void assertLoginFails(String message, String... keys) {
		try {
			login(keys);
			fail("Login should have failed");
		} catch (UncheckedIOException e) {
			assertEquals(message, e.getCause().getMessage());
		}
	}

	@Test
	public void rightPasswordLogsIn() {
		String out = login("1", Fixtures.password(1));
		assertTrue(out, out.contains("Login successful! Welcome, Alice"));
		assertEquals(1, UserManagementFacade.getCurrUser().getId());
	}

	@Test
	public void wrongPasswordThenRightPasswordLogsIn() {
		String out = login("1", "bad", Fixtures.password(1));
		assertTrue(out, out.contains("Attempts remaining: 2"));
		assertEquals(1, UserManagementFacade.getCurrUser().getId());
	}

	@Test
	public void tooManyAttemptsIsRefused() {
		assertLoginFails("Too many attempts", "1", "a", "b", "c");
		assertNull(UserManagementFacade.getCurrUser());
	}

	@Test
	public void attemptLimitComesFromTheConfig() {
		env.set("login.maxAttempts", "1");
		assertLoginFails("Too many attempts", "1", "a");
	}

	@Test
	public void unknownUserIsReported() {
		assertLoginFails("User does not exist", "9");
	}

	@Test
	public void zeroMeansExit() {
		assertLoginFails("Exit requested", "0");
	}

	@Test
	public void userWithoutAStoredPasswordCannotLogIn() {
		repo.addUser(new User(5, "NoPass", "n@x", "r", "ACTIVE"));
		assertLoginFails("Too many attempts", "5", "a", "b", "c");
	}

	@Test
	public void nonNumericIdAsksAgain() {
		String out = login("abc", "1", Fixtures.password(1));
		assertTrue(out, out.contains("Please enter a whole number."));
	}

	@Test
	public void passwordsAreCaseAndSpaceSensitive() {
		assertLoginFails("Too many attempts", "1", Fixtures.password(1).toUpperCase(), " " + Fixtures.password(1), Fixtures.password(1) + " ");
	}
}
