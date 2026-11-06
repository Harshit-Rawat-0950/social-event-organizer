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

public class UserRegistrationTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	private UserRepository repo;

	@Before
	public void setUp() {
		repo = new UserRepository(env.path("users.txt"));
	}

	private String register(String... keys) {
		return ConsoleDriver.call(() -> {
			try {
				new UserRegistration().showRegistration(repo);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, keys);
	}

	@Test
	public void registrationStoresNameAndEmailInTheRightColumns() {
		String out = register("5", "secret", "a@x", "Alice", "Dev");
		assertTrue(out, out.contains("User Registration Successful"));
		User user = repo.findUserById(5);
		assertEquals("Alice", user.getName());
		assertEquals("a@x", user.getEmail());
		assertEquals("Dev", user.getRole());
		assertEquals("ACTIVE", user.getStatus());
		assertEquals("secret", new PassWordManager(env.path("password.txt")).getPassWord(5));
	}

	@Test
	public void existingIdIsRefusedAndKeepsItsData() {
		Fixtures.user(5, "Alice");
		try {
			register("5");
			fail("Duplicate id was accepted");
		} catch (UncheckedIOException e) {
			assertEquals("User already exists", e.getCause().getMessage());
		}
		assertEquals(1, repo.countUsers());
		assertEquals(Fixtures.password(5), new PassWordManager(env.path("password.txt")).getPassWord(5));
	}

	@Test
	public void idsMustBePositive() {
		for (String id : new String[] {"0", "-3"}) {
			try {
				register(id);
				fail("Accepted id " + id);
			} catch (UncheckedIOException e) {
				assertTrue(e.getCause().getMessage().contains("positive"));
			}
		}
	}

	@Test
	public void separatorsAndBlanksInFieldsAskAgain() {
		String out = register("5", "pw", "a,b@x", "", "a@x", "Al;ice", "Alice", "D|ev", "Dev");
		assertTrue(out, out.contains("Please avoid these characters"));
		assertTrue(out, out.contains("This field cannot be empty."));
		assertEquals("Alice", repo.findUserById(5).getName());
	}

	@Test
	public void emptyPasswordAsksAgain() {
		String out = register("5", "", "pw", "a@x", "Alice", "Dev");
		assertTrue(out, out.contains("Password cannot be empty."));
	}

	@Test
	public void abandoningHalfWayStoresNothing() {
		ConsoleDriver.callUntilInputEnds(() -> {
			try {
				new UserRegistration().showRegistration(repo);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, "5", "pw", "a@x");
		assertNull(repo.findUserById(5));
		assertNull(new PassWordManager(env.path("password.txt")).getPassWord(5));
	}
}
