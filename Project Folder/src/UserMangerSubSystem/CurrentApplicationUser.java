package UserMangerSubSystem;

import UserMangerSubSystem.User.UserData;

// Singleton: remembers who is logged in for the rest of the application
public class CurrentApplicationUser {

	private static CurrentApplicationUser instance;
	private User user;
	private UserData userData;

	private CurrentApplicationUser() {
	}

	public static synchronized CurrentApplicationUser getInstance() {
		if (instance == null) {
			instance = new CurrentApplicationUser();
		}
		return instance;
	}

	public void login(User user) {
		this.user = user;
		this.userData = new UserData(user);
	}

	public void logout() {
		this.user = null;
		this.userData = null;
	}

	public User getUser() {
		return user;
	}

	public UserData getUserData() {
		return userData;
	}
}
