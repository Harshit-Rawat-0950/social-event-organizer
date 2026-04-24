package ReportDashboardSubSystem;

import java.util.List;

import UserMangerSubSystem.ConfigLoader;

public class FriendsPage implements Page {

    private static FriendsPage instance;
    static List<TextElement> UIElements;
	private FriendsPage() {
		ConfigLoader configLoader = new ConfigLoader();
	    UIElements = TextElement.readFile(configLoader.getProperty("Files.friendpage"));
    }

    public static synchronized FriendsPage getInstance() {
        if (instance == null) {
            instance = new FriendsPage();
        }
        return instance;
    }	

	@Override
	public void displayPage() {
		// TODO Auto-generated method stub
		
	}

}
