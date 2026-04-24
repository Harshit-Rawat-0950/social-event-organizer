package ReportDashboardSubSystem;

import java.util.List;
import java.util.Scanner;

import EventManagementSubSystem.EventMangementFacade;
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
    	char choice = sc.next().charAt(0);
    	sc.close();
    	String[] events = EventMangementFacade.getAllEvents();
    	for(String event:events)
    	{
    		new TextElement(event, 0, true, false).display();// Style to choose
    	}
    	
    	switch (choice)
    	{
    	case 'c':
    		EventMangementFacade.createEvent();
    		break;
    	case 'e':
    		EventMangementFacade.editEvent();
    		break;
    	case 'r':
    		MainPage.getInstance().displayPage();
    		break;
    	default:
    		
    	}
    }
}
