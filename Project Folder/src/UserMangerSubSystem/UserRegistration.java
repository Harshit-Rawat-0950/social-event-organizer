package UserMangerSubSystem;

import java.io.IOException;
import java.util.Scanner;

import ReportDashboardSubSystem.DashboardFacade;

public class UserRegistration {

	public void showRegistration(UserRepository Repo) throws IOException {
		DashboardFacade.displayLoginPage();//if needed
		Scanner sc = new Scanner(System.in);
		System.out.println("===== User Registration =====");
        System.out.print("Enter id: ");
        int id = sc.nextInt();
        if(Repo.findUserById(id)!=null)
        {
        	throw new IOException("User already exists");
        }
        PassWordManager pm = new PassWordManager(new ConfigLoader().getProperty("Files.password"));
        System.out.print("Enter password :");
        String ps = sc.nextLine();
        pm.setPassword(id,ps);

        System.out.println("Enter User information");
        System.out.print("Enter email: ");
        String email = sc.nextLine();
        System.out.print("Enter name: ");
        String name = sc.nextLine();
        System.out.print("Enter role: ");
        String role = sc.nextLine();
        User u = new User(id,email,name,role,"ACTIVE");
        Repo.addUser(u);
	}

}
