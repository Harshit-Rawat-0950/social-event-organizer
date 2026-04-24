package ReportDashboardSubSystem;

import java.util.List;

import UserMangerSubSystem.ConfigLoader;

public class InvitesPage implements Page {

    private static InvitesPage instance;
    static List<TextElement> UIElements;
	private InvitesPage() {
		ConfigLoader configLoader = new ConfigLoader();
	    UIElements = TextElement.readFile(configLoader.getProperty("Files.mainpage"));
    }

    public static synchronized InvitesPage getInstance() {
        if (instance == null) {
            instance = new InvitesPage();
        }
        return instance;
    }

	@Override
	public void displayPage() {
		// TODO Auto-generated method stub
		
	}	

}
