package UserMangerSubSystem;

import static org.junit.Assert.*;

import org.junit.Test;

public class UserTest{

	
	//Tests constructor of User
	@Test 
	public void testUserConstructor() {
		User u1 = new User(21,"Shreyas","shreyas@bits.com","Log-Ops Head","Active");
		assertNotNull(u1);
        assertEquals(21, u1.getId());
        assertEquals("Shreyas", u1.getName());
        assertEquals("shreyas@bits.com", u1.getEmail());
        assertEquals("Log-Ops Head", u1.getRole());
        assertEquals("Active", u1.getStatus());
	}
	//Tests UserRepo
	@Test 
	public void testUserRepo_add_find_update() {
		UserRepository ur = new UserRepository("UUUserdemo.txt");
		assertNotNull(ur);
		User u1 = new User(21,"Shreyas","shreyas@bits.com","Log-Ops Head","Active");
		User u2 = new User(10,"Harshit","harshit@bits.com","Chief Coordi","Actively - Active");
		User u3 = new User(2,"Yashvardhan","yashvardhan@rmit.com","no-role","Actively - Inactive");
		User u4 = new User(2,"Ishan","ishan@bits.com","no-role","Actively - Inactive");
		ur.addUser(u1);
		ur.addUser(u2);
		ur.addUser(u3);
		assertEquals(u2.toString(),ur.findUserById(10).toString());
		ur.updateUser(u4);
		assertEquals(u4.toString(),ur.findUserById(2).toString());
		assertEquals(3,ur.countUsers());
		ur.deleteUserById(21);
		ur.deleteUserById(10);
		ur.deleteUserById(2);
	}
	@Test 
	public void testUserRepo_delete() {
		UserRepository ur = new UserRepository("Userdemo_del.txt");
		User u1 = new User(21,"Shreyas","shreyas@bits.com","Log-Ops Head","Active");
		User u2 = new User(10,"Harshit","harshit@bits.com","Chief Coordi","Actively - Active");
		User u3 = new User(2,"Yashvardhan","yashvardhan@rmit.com","no-role","Actively - Inactive");
		ur.addUser(u1);
		ur.addUser(u2);
		ur.addUser(u3);
		ur.deleteUserById(10);
		assertEquals(2,ur.countUsers());
		ur.deleteUserById(21);
		ur.deleteUserById(2);
	}
}
