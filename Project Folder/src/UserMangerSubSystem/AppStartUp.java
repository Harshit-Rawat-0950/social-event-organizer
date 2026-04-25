package UserMangerSubSystem;

import java.io.IOException;
import java.util.Scanner;

public class AppStartUp {

    private static AppStartUp instance;

    private ConfigLoader configLoader;
    private SystemDataService systemDataService;
    private UserRepository userRepository;

    private AppStartUp() {
        configLoader = new ConfigLoader();
        systemDataService = new SystemDataService();
        String userFilePath = configLoader.getProperty("Files.users");
        userRepository = new UserRepository(userFilePath);
        
    }

    public static synchronized AppStartUp getInstance() {
        if (instance == null) {
            instance = new AppStartUp();
        }
        return instance;
    }
    
    
    
    public void startApplication() {
        System.out.println("Starting application...");

        String config = configLoader.loadConfig();
        String appData = systemDataService.loadInitialData();
        String userData = userRepository.getUserData();

        System.out.println("Displaying Loaded Config data: " + config);
        System.out.println("Displaying Loaded App Data: " + appData);
        System.out.println("Displaying Loaded User Data: " + userData);
        
        String mode = configLoader.getProperty("app.mode");
        String delayValue = configLoader.getProperty("app.devStartupDelayMs");

        int delay = Integer.parseInt(delayValue);
        
        if ("dev".equalsIgnoreCase(mode)) {
            try {
                System.out.print("Development mode detected. Waiting for " + delay + " ms.");
                for(int i =0 ; i <10; i++) {
                	System.out.print(".");
                Thread.sleep(delay/10);  
                }
                System.out.print("\n");
            } catch (InterruptedException e) { // Not needed in this part as the application is single Threaded until this point but in realistic implementation is needed
                Thread.currentThread().interrupt();
                System.out.println("AppStartup has been Interuppted");
            } catch (NumberFormatException e) {
                System.out.println("Invalid values found in configData");
            }
        }
        UserLogin login = new UserLogin();
        while(true) {
        try {
			login.showLogin(userRepository);
			break;
		} catch (IOException e) {
			if(e.getMessage().equals("User does not exist")) {
				System.out.println("User does not exists press y/n to register or try again");
				Scanner sc = new Scanner(System.in);
				String choice = sc.next();
				if(choice.equals("y"))
				{
					UserRegistration userRegistration = new UserRegistration();
					try {
					userRegistration.showRegistration(userRepository);
					System.out.println("Returning to login screen...");
			        try {
			            Thread.sleep(1500); 
			        } catch (InterruptedException ie) {
			            Thread.currentThread().interrupt();
			        }
			        clearConsole();
					}
					catch(IOException e1){
						if(e1.getMessage().equals("User already exists"))
							System.out.println("User with this id already exists try again");
							try {
								Thread.sleep(delay);
							} catch (InterruptedException e2) {
								e2.printStackTrace();
							}
					}
				}
				else if(choice.equals("n"))
				{
					continue;
				}
				else
				{
					System.out.println("Its a yes or no how u messing that up"); //to be removed
				}
				
			}
		}
		}
    }
    
    private void clearConsole() {
        try {
            // This triggers the native Windows clear screen command
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            // Fallback to blank lines just in case it fails or runs in a weird environment
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }  
    
    public static void main(String[] args)
	{
		AppStartUp as = new AppStartUp();
		as.startApplication();
		
	}
}