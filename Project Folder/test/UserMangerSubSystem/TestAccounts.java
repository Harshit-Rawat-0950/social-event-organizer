package UserMangerSubSystem;

// Lets test code outside this package store a password
public final class TestAccounts {

	private TestAccounts() {
	}

	public static void setPassword(int id, String password) {
		new PassWordManager(new ConfigLoader().getProperty("Files.password")).setPassword(id, password);
	}
}
