package ReportDashboardSubSystem;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import TestSupport.ConsoleDriver;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;
import UserMangerSubSystem.UserManagementFacade;

public class ProfilePageFlowTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Before
	public void setUp() {
		Fixtures.user(1, "Alice");
	}

	private String page(String... keys) {
		String[] all = new String[keys.length + 2];
		all[0] = "6";
		System.arraycopy(keys, 0, all, 1, keys.length);
		all[all.length - 1] = "r";
		return ConsoleDriver.session(1, all);
	}

	@Test
	public void showsEveryDetail() {
		String out = page();
		for (String part : new String[] {"Id:", "1", "Name:", "Alice", "Email:", "alice@x", "Role:", "Tester", "Status:", "ACTIVE"}) {
			assertTrue(part, out.contains(part));
		}
	}

	@Test
	public void changeName() {
		String out = page("n", "Alicia");
		assertTrue(out, out.contains("Name updated."));
		assertTrue(out, out.contains("Alicia"));
		assertEquals("Alicia", UserManagementFacade.findUser(1).getName());
		assertEquals("alice@x", UserManagementFacade.findUser(1).getEmail());
	}

	@Test
	public void changeEmail() {
		assertTrue(page("e", "new@x").contains("Email updated."));
		assertEquals("new@x", UserManagementFacade.findUser(1).getEmail());
		assertEquals("Alice", UserManagementFacade.findUser(1).getName());
	}

	@Test
	public void separatorsInNamesAskAgain() {
		assertTrue(page("n", "A,B", "AB").contains("Please avoid these characters"));
		assertEquals("AB", UserManagementFacade.findUser(1).getName());
	}

	@Test
	public void changePasswordThenLogInWithIt() {
		assertTrue(page("p", Fixtures.password(1), "brandnew").contains("Password changed."));
		String out = ConsoleDriver.app("1", "brandnew", "0");
		assertTrue(out, out.contains("Login successful!"));
	}

	@Test
	public void wrongCurrentPasswordChangesNothing() {
		assertTrue(page("p", "wrong", "brandnew").contains("Current password is incorrect."));
		assertTrue(ConsoleDriver.session(1).contains("Login successful!"));
	}

	@Test
	public void invalidKeyIsExplained() {
		assertTrue(page("z").contains("Please choose one of the options listed."));
	}

	@Test
	public void sameInstanceEverywhere() {
		assertSame(ProfilePage.getInstance(), ProfilePage.getInstance());
	}
}
