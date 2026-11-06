package UserMangerSubSystem;

import java.io.IOException;

import ReportDashboardSubSystem.ConsoleInput;
import ReportDashboardSubSystem.DashboardFacade;

public class UserLogin {

	public void showLogin(UserRepository Repo) throws IOException {
	    DashboardFacade.displayLoginPage(); //if needed
	    ConsoleInput in = ConsoleInput.getInstance();

	    DashboardFacade.displayHeading("User Login");
	    int id = in.readInt("Enter id (0 to exit): ");
	    if (id == 0) {
	        throw new IOException("Exit requested");
	    }
	    User currentUser = Repo.findUserById(id);
	    if (currentUser == null) {
	        throw new IOException("User does not exist");
	    }

	    PassWordManager pm = new PassWordManager(new ConfigLoader().getProperty("Files.password"));
	    int maxAttempts = new ConfigLoader().getIntProperty("login.maxAttempts", 3);


	    for (int i = 1; i <= maxAttempts; i++) {
	        String password = in.readSecret("Enter password : ");
	        if (password.equals(pm.getPassWord(id))) {
	            DashboardFacade.showSuccess("Login successful! Welcome, " + currentUser.getName());
	            try {
	                Thread.sleep(new ConfigLoader().getIntProperty("app.delay", 500));
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	            }
	            CurrentApplicationUser.getInstance().login(currentUser);
	            return;
	          } else {
	            DashboardFacade.showError("Invalid login details. Attempts remaining: " + (maxAttempts - i));
	        }
	    }
	    throw new IOException("Too many attempts");
	}
	public static void main(String[] args)
	{
		UserLogin ul = new UserLogin();
		UserRepository ur = new UserRepository(new ConfigLoader().getProperty("Files.users"));
		try {
			ul.showLogin(ur);
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}
