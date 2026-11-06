package UserMangerSubSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ReportDashboardSubSystem.ConsoleInput;
import ReportDashboardSubSystem.DashboardFacade;
import ReportDashboardSubSystem.InputClosedException;

public class AppStartUp {

    private static AppStartUp instance;

    private ConfigLoader configLoader;
    private SystemDataService systemDataService;
    private UserRepository userRepository;

    private AppStartUp() {
    }

    public static synchronized AppStartUp getInstance() {
        if (instance == null) {
            instance = new AppStartUp();
        }
        return instance;
    }



    public void startApplication() {
        configLoader = new ConfigLoader();
        systemDataService = new SystemDataService();
        userRepository = new UserRepository(configLoader.getProperty("Files.users"));
        UserLogin login = new UserLogin();

        while(true) {
        try {
			login.showLogin(userRepository);
			break;
		} catch (IOException e) {
			if(e.getMessage().equals("Exit requested")) {
				DashboardFacade.showInfo("Goodbye!");
				DashboardFacade.flushMessages();
				return;
			}
			else if(e.getMessage().equals("User does not exist")) {
				DashboardFacade.showInfo("User does not exist. Press y to register or n to try again");
				String choice = ConsoleInput.getInstance().readLine("> ").trim();
				if(choice.equals("y"))
				{
					UserRegistration userRegistration = new UserRegistration();
					try {
					userRegistration.showRegistration(userRepository);
					DashboardFacade.showInfo("You can log in now.");
					pause(configLoader.getIntProperty("app.registerDelayMs", 1500));
					}
					catch(IOException e1){
						DashboardFacade.showError(e1.getMessage());
					}
				}
				else if(!choice.equals("n"))
				{
					DashboardFacade.showError("Please answer y or n. Returning to login.");
				}
			}
			else {
				DashboardFacade.showError(e.getMessage());
			}
		}
    }
        DashboardFacade.flushMessages();
        if ("dev".equalsIgnoreCase(configLoader.getProperty("app.mode"))) {
            List<String> startupInfo = new ArrayList<>();
            startupInfo.add("Config: " + configLoader.loadConfig());
            startupInfo.add("Data:   " + systemDataService.loadInitialData());
            startupInfo.add("Users:  " + userRepository.getUserData());
            DashboardFacade.displayInfoBox("Development mode", startupInfo);
            DashboardFacade.displayProgress("Starting application", configLoader.getIntProperty("app.devStartupDelayMs", 0));
        }
        DashboardFacade.loadMainPage();
        CurrentApplicationUser.getInstance().logout();
        DashboardFacade.showSuccess("Logged out. Goodbye!");
        DashboardFacade.flushMessages();
    }

    private void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args)
	{
		try {
			AppStartUp.getInstance().startApplication();
		} catch (InputClosedException e) {
			DashboardFacade.flushMessages();
			System.out.println();
			System.out.println("Input closed. Exiting.");
		}
	}
}
