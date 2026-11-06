package UserMangerSubSystem;

import java.util.ArrayList;
import java.util.List;

/*
 * Goal is to implement all interaction of other subsystem through this class
 * only creating a unified interface for the entire system. However this class
 * in itself must not perform any function other than delegation to the
 * respective class of any and all interactions possible from other
 * parts of project.
 **/

public class UserManagementFacade {

	private static UserRepository repo() {
		return new UserRepository(new ConfigLoader().getProperty("Files.users"));
	}

	public static User getCurrUser() {
		return CurrentApplicationUser.getInstance().getUser();
	}

	public static User findUser(int id) {
		return repo().findUserById(id);
	}

	public static String getDisplayName(int id) {
		User user = findUser(id);
		return user == null ? "User " + id : user.getName() + " (" + id + ")";
	}

	public static boolean isUserActive(int id) {
		User user = findUser(id);
		return user != null && "ACTIVE".equalsIgnoreCase(user.getStatus());
	}

	public static List<User> searchUsers(String query) {
		String needle = query.toLowerCase();
		List<User> matches = new ArrayList<>();
		for(User user : repo().getAllUsers()) {
			if(user.getName().toLowerCase().contains(needle) || user.getEmail().toLowerCase().contains(needle)) {
				matches.add(user);
			}
		}
		return matches;
	}

	public static void updateProfile(String name, String email) {
		User user = getCurrUser();
		user.setName(name);
		user.setEmail(email);
		repo().updateUser(user);
	}

	public static void changePassword(String currentPassword, String newPassword) {
		int id = getCurrUser().getId();
		PassWordManager pm = new PassWordManager(new ConfigLoader().getProperty("Files.password"));
		if(!currentPassword.equals(pm.getPassWord(id))) {
			throw new IllegalArgumentException("Current password is incorrect.");
		}
		pm.updatePassword(id, newPassword);
	}
}
