package UserMangerSubSystem;

import java.io.IOException;
import java.util.Scanner;

import ReportDashboardSubSystem.DashboardFacade;

public class UserRegistration {

	public void showRegistration(UserRepository Repo) {
		DashboardFacade.displayLoginPage();//if needed
		Scanner sc = new Scanner(System.in);
		System.out.println("===== User Registration =====");
        System.out.print("Enter id: ");
        int id = sc.nextInt();
        PassWordManager pm = new PassWordManager();
        System.out.print("Enter password :");
	}

}
