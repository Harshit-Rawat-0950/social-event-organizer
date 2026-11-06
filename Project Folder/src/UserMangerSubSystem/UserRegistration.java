package UserMangerSubSystem;

import java.io.IOException;

import ReportDashboardSubSystem.ConsoleInput;
import ReportDashboardSubSystem.DashboardFacade;

public class UserRegistration {

	public void showRegistration(UserRepository Repo) throws IOException {
		DashboardFacade.displayLoginPage();//if needed
		ConsoleInput in = ConsoleInput.getInstance();
		DashboardFacade.displayHeading("User Registration");
        int id = in.readInt("Enter id: ");
        if(id <= 0)
        {
        	throw new IOException("User id must be a positive number");
        }
        if(Repo.findUserById(id)!=null)
        {
        	throw new IOException("User already exists");
        }
        String ps = in.readPassword("Enter password : ");

        DashboardFacade.showInfo("Enter User information");
        String email = in.readField("Enter email: ");
        String name = in.readField("Enter name: ");
        String role = in.readField("Enter role: ");
        PassWordManager pm = new PassWordManager(new ConfigLoader().getProperty("Files.password"));
        pm.setPassword(id,ps);
        User u = new User(id,name,email,role,"ACTIVE");
        Repo.addUser(u);
        DashboardFacade.showSuccess("User Registration Successful :) :)");
	}

	public static void main(String[] args)
	{
		UserRegistration ureg = new UserRegistration();
		UserRepository ur = new UserRepository(new ConfigLoader().getProperty("Files.users"));
		try {
			ureg.showRegistration(ur);
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}
