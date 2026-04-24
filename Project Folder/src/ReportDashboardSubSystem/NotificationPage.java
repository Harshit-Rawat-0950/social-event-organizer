package ReportDashboardSubSystem;

import java.util.List;

import UserMangerSubSystem.ConfigLoader;

public class NotificationPage implements Page {
	
    private static NotificationPage instance;
    static List<TextElement> UIElements;
	private NotificationPage() {
		ConfigLoader configLoader = new ConfigLoader();
	    UIElements = TextElement.readFile(configLoader.getProperty("Files.notificationpage"));
    }

    public static synchronized NotificationPage getInstance() {
        if (instance == null) {
            instance = new NotificationPage();
        }
        return instance;
    }	

	@Override
	public void displayPage() {
		// TODO Auto-generated method stub
		
	}


}
