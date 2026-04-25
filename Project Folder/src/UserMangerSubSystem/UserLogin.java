package UserMangerSubSystem;

import java.io.IOException;
import java.util.Scanner;

import ReportDashboardSubSystem.DashboardFacade;

public class UserLogin {
	
	public void showLogin(UserRepository Repo) throws IOException {
	    DashboardFacade.displayLoginPage(); //if needed
	    Scanner sc = new Scanner(System.in);
	    
	    System.out.println("===== User Login =====");
	    System.out.print("Enter id: ");
	    int id = sc.nextInt();
	    sc.nextLine();
	    User currentUser = Repo.findUserById(id); 
	    if (currentUser == null) {
	        throw new IOException("User does not exist");
	    }

	    PassWordManager pm = new PassWordManager(new ConfigLoader().getProperty("Files.password"));
	    int maxAttempts = Integer.parseInt((new ConfigLoader()).getProperty("login.maxAttempts"));

	   
	    for (int i = 1; i <= maxAttempts; i++) {
	        System.out.print("Enter password : ");
	        String password = sc.nextLine();
	        if (pm.getPassWord(id).equals(password)) {
	            System.out.println("Login successful!");
	            System.out.println("Welcome, " + currentUser.getName());
	            try {
	                Thread.sleep(Integer.parseInt((new ConfigLoader()).getProperty("app.delay=500")));
	            } catch (NumberFormatException | InterruptedException e) {
	            }
	            
	            DashboardFacade.loadMainPage();
	            return; 
	          } else {
	            System.out.println("Invalid login details. Attempts remaining: " + (maxAttempts - i));
	        }
	    }
        sc.close();
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
