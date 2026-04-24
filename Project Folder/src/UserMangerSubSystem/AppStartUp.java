package UserMangerSubSystem;

import java.io.IOException;
import java.util.Scanner;

class AppStartup {

    private static AppStartup instance;

    private ConfigLoader configLoader;
    private SystemDataService systemDataService;
    private UserRepository userRepository;

    private AppStartup() {
        configLoader = new ConfigLoader();
        systemDataService = new SystemDataService();
        String userFilePath = configLoader.getProperty("file.users");
        userRepository = new UserRepository(userFilePath);
        
    }

    public static synchronized AppStartup getInstance() {
        if (instance == null) {
            instance = new AppStartup();
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
            } catch (InterruptedException e) { // Not needed in this part as the application is single Threaded until this point but in realistic implementation is needed
                Thread.currentThread().interrupt();
                System.out.println("AppStartup has been Interuppted");
            } catch (NumberFormatException e) {
                System.out.println("Invalid values found in congifData");
            }
        }
        UserLogin login = new UserLogin();
        while(true) {
        try {
			login.showLogin(userRepository);
		} catch (IOException e) {
			if(e.getMessage().equals("User does not exists")) {
				System.out.println("User does not exists press y/n to register or try again");
				Scanner sc = new Scanner(System.in);
				if(sc.next().equals("y"))
				{
					UserRegistration userRegistration = new UserRegistration();
					try {
					userRegistration.showRegistration(userRepository);
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
				else if(sc.next().equals("n"))
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
}