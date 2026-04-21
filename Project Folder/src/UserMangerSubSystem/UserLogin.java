package UserMangerSubSystem;

import java.io.IOException;
import java.util.Scanner;

import ReportDashboardSubSystem.DashboardFacade;

public class UserLogin {
	
	public void showLogin(UserRepository Repo) throws IOException {
		DashboardFacade.displayLoginPage();//if needed
		Scanner sc = new Scanner(System.in);
		System.out.println("===== User Login =====");
        System.out.print("Enter id: ");
        int id = sc.nextInt();
        if(Repo.findUserById(id)==null)
        {
    		sc.close();
        	throw new IOException("User does not exists");
        }
        PassWordManager pm = new PassWordManager(new ConfigLoader().getProperty("Files.password"));
        System.out.print("Enter password :");
        for(int i=1; i < Integer.parseInt((new ConfigLoader()).getProperty("login.maxAttempts"));i++)
        if (pm.getPassWord(id)==sc.nextLine()) {
            System.out.println("Login successful!");
            System.out.println("Welcome, " + Repo.getName());
    		sc.close();
            try {
				Thread.sleep(Integer.parseInt((new ConfigLoader()).getProperty("app.delay=500")));
			} catch (NumberFormatException e) {
			} catch (InterruptedException e) {
			}
            DashboardFacade.loadMainPage();
        } else {
            System.out.println("Invalid login details.");
        }
        throw new IOException("Too many attempts");
	}

}
