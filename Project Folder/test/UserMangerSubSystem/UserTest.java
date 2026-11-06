package UserMangerSubSystem;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import TestSupport.TestEnvironment;

public class UserTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	private UserRepository repo;
	private final User shreyas = new User(21, "Shreyas", "shreyas@bits.com", "Log-Ops Head", "Active");
	private final User harshit = new User(10, "Harshit", "harshit@bits.com", "Chief Coordi", "Actively - Active");

	@Before
	public void setUp() {
		repo = new UserRepository(env.path("users.txt"));
	}

	@Test
	public void constructorAndGettersKeepEveryField() {
		assertEquals(21, shreyas.getId());
		assertEquals("Shreyas", shreyas.getName());
		assertEquals("shreyas@bits.com", shreyas.getEmail());
		assertEquals("Log-Ops Head", shreyas.getRole());
		assertEquals("Active", shreyas.getStatus());
	}

	@Test
	public void settersChangeEveryField() {
		User u = new User(1, "a", "b", "c", "d");
		u.setName("A");
		u.setEmail("B");
		u.setRole("C");
		u.setStatus("D");
		assertEquals("1,A,B,C,D", u.toString());
	}

	@Test
	public void storedOrderIsIdNameEmailRoleStatus() {
		assertEquals("21,Shreyas,shreyas@bits.com,Log-Ops Head,Active", shreyas.toString());
		User copy = User.fromString(shreyas.toString());
		assertEquals(shreyas.toString(), copy.toString());
		assertEquals("Shreyas", copy.getName());
		assertEquals("shreyas@bits.com", copy.getEmail());
	}

	@Test
	public void missingFileIsCreatedWithItsFolders() {
		new UserRepository(env.path("deep/down/users.txt"));
		assertTrue(Files.exists(Paths.get(env.path("deep/down/users.txt"))));
	}

	@Test
	public void addFindAndCount() {
		repo.addUser(shreyas);
		repo.addUser(harshit);
		assertEquals(harshit.toString(), repo.findUserById(10).toString());
		assertNull(repo.findUserById(99));
		assertEquals(2, repo.countUsers());
		assertEquals("2 registered users", repo.getUserData());
	}

	@Test
	public void updateReplacesOnlyThatUser() {
		repo.addUser(shreyas);
		repo.addUser(harshit);
		repo.updateUser(new User(10, "New", "new@x", "r", "s"));
		assertEquals("New", repo.findUserById(10).getName());
		assertEquals("Shreyas", repo.findUserById(21).getName());
		assertEquals(2, repo.countUsers());
	}

	@Test
	public void updatingAnUnknownUserAddsNothing() {
		repo.addUser(shreyas);
		repo.updateUser(new User(99, "Ghost", "g@x", "r", "s"));
		assertEquals(1, repo.countUsers());
	}

	@Test
	public void deleteRemovesOnlyThatUser() {
		repo.addUser(shreyas);
		repo.addUser(harshit);
		repo.deleteUserById(10);
		assertNull(repo.findUserById(10));
		assertEquals(1, repo.countUsers());
		repo.deleteUserById(21);
		assertEquals(0, repo.countUsers());
	}

	@Test
	public void blankLinesAreIgnored() throws Exception {
		Files.write(Paths.get(env.path("users.txt")), "\n1,a,b,c,d\n\n".getBytes());
		List<User> users = repo.getAllUsers();
		assertEquals(1, users.size());
	}
}
