package ReportDashboardSubSystem;

import java.util.List;

import UserMangerSubSystem.ConfigLoader;

public class ReportPage implements Page {

    private static ReportPage instance;
    static List<TextElement> UIElements;
	private ReportPage() {
		ConfigLoader configLoader = new ConfigLoader();
	    UIElements = TextElement.readFile(configLoader.getProperty("Files.reportpage"));
    }

    public static synchronized ReportPage getInstance() {
        if (instance == null) {
            instance = new ReportPage();
        }
        return instance;
    }	

	@Override
	public void displayPage() {
		// TODO Auto-generated method stub
		
	}

}
