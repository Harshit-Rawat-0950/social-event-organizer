package ReportDashboardSubSystem;

import java.util.List;

import EventManagementSubSystem.EventMangementFacade;
import EventManagementSubSystem.EventView;
import UserMangerSubSystem.ConfigLoader;
import UserMangerSubSystem.UserManagementFacade;

public class EventPage implements Page {

    private enum View { UPCOMING, ALL, MINE }

    private static EventPage instance;
    static List<TextElement> UIElements;
	private EventPage() {
		ConfigLoader configLoader = new ConfigLoader();
	    UIElements = TextElement.readFile(configLoader.getProperty("Files.eventpage"));
    }

    public static synchronized EventPage getInstance() {
        if (instance == null) {
            instance = new EventPage();
        }
        return instance;
    }
    public void displayPage()
    {
    	View view = View.UPCOMING;
    	int viewer = UserManagementFacade.getCurrUser().getId();
    	while(true)
    	{
    		Screen.open("Events", UIElements);
    		showEvents(view, viewer);
    		Screen.menu(UIElements);
    		try {
    			switch (ConsoleInput.getInstance().readChoice("> "))
    			{
    			case 'c':
    				EventMangementFacade.createEvent();
    				break;
    			case 's':
    				EventMangementFacade.addSubEvent();
    				break;
    			case 'd':
    				EventMangementFacade.decorateEvent();
    				break;
    			case 'e':
    				EventMangementFacade.editEvent();
    				break;
    			case 'x':
    				EventMangementFacade.cancelEvent();
    				break;
    			case 'i':
    				EventMangementFacade.inviteToEvent();
    				break;
    			case 'v':
    				EventMangementFacade.showResponses();
    				break;
    			case 'a':
    				view = view == View.ALL ? View.UPCOMING : View.ALL;
    				break;
    			case 'm':
    				view = view == View.MINE ? View.UPCOMING : View.MINE;
    				break;
    			case 'r':
    				return;
    			default:
    				Screen.error("Please choose one of the options listed.");
    			}
    		} catch (IllegalArgumentException e) {
    			Screen.error(e.getMessage());
    		}
    	}
    }

    private static void showEvents(View view, int viewer)
    {
    	List<EventView> events;
    	String label;
    	switch (view) {
    		case ALL:
    			events = EventMangementFacade.getAllEventViews(viewer);
    			label = "All events";
    			break;
    		case MINE:
    			events = EventMangementFacade.getMyEventViews(viewer);
    			label = "My events";
    			break;
    		default:
    			events = EventMangementFacade.getUpcomingEventViews(viewer);
    			label = "Upcoming events";
    	}
    	Screen.section(label + " (" + events.size() + ")");
    	if(events.isEmpty())
    		Screen.empty("(none)");
    	for(EventView event : events)
    		Screen.println(EventCardRenderer.render(event));
    }
}
