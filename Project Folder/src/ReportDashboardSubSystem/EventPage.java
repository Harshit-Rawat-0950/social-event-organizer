package ReportDashboardSubSystem;

import java.util.List;
import java.util.Scanner;

import UserMangerSubSystem.ConfigLoader;

public class EventPage implements Page {

    private static EventPage instance;
    static List<TextElement> UIElements;
	private EventPage() {
		ConfigLoader configLoader = new ConfigLoader();
	    UIElements = TextElement.readFile(configLoader.getProperty("Files.mainpage"));
    }

    public static synchronized EventPage getInstance() {
        if (instance == null) {
            instance = new EventPage();
        }
        return instance;
    }	
    public void displayPage()
    {
    	TextElement.displayList(UIElements);
    	Scanner sc = new Scanner(System.in);
    	int choice = sc.nextInt();
    	switch (choice)
    	{
    	case 1:
    		
    	}
    }
}
