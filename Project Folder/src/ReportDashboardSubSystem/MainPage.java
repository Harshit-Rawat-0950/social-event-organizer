package ReportDashboardSubSystem;

import java.util.ArrayList;
import java.util.List;

import InviteMangerSubSystem.InviteManagerFacade;
import NotificationSubSystem.NotificationFacade;
import ReportDashboardSubSystem.Theme.Role;
import UserMangerSubSystem.ConfigLoader;
import UserMangerSubSystem.UserManagementFacade;

public class MainPage implements Page{

    private static MainPage instance;
    static List<TextElement> UIElements;
	private MainPage() {
		ConfigLoader configLoader = new ConfigLoader();
	    UIElements = TextElement.readFile(configLoader.getProperty("Files.mainpage"));
    }

    public static synchronized MainPage getInstance() {
        if (instance == null) {
            instance = new MainPage();
        }
        return instance;
    }
    // Returns when the user logs out. Each page returns here when the user goes back.
    public void displayPage()
    {
    	while(true)
    	{
    		Screen.open("", UIElements);
    		Screen.println(menuBox());
    		int choice = ConsoleInput.getInstance().readInt("> ");
    		switch(choice)
    		{
    		case 0:
    			return;
    		case 1:
    			EventPage.getInstance().displayPage();
    			break;
    		case 2:
    			InvitesPage.getInstance().displayPage();
    			break;
    		case 3:
    			NotificationPage.getInstance().displayPage();
    			break;
    		case 4:
    			ReportPage.getInstance().displayPage();
    			break;
    		case 5:
    			FriendsPage.getInstance().displayPage();
    			break;
    		case 6:
    			ProfilePage.getInstance().displayPage();
    			break;
    		default:
    			Screen.error("Please choose one of the options listed.");
    		}
    	}
    }

    // The menu rows with live counts filled in for {invites} and {notifications}
    private List<String> menuBox()
    {
    	int userId = UserManagementFacade.getCurrUser().getId();
    	String invites = count(InviteManagerFacade.getInvites(userId).length, "pending");
    	String notifications = count(NotificationFacade.getNotifications(userId).size(), "new");
    	List<String> rows = new ArrayList<>();
    	for(TextElement element : UIElements.subList(1, UIElements.size()))
    	{
    		if(element.isAllowed())
    		{
    			String text = element.getContent().replace("{invites}", invites).replace("{notifications}", notifications);
    			rows.add(element.withContent(text).stylise());
    		}
    	}
    	return Widgets.box(null, null, rows, Role.FRAME);
    }

    private static String count(int value, String word)
    {
    	return value == 0 ? "" : Glyph.BULLET + " " + value + " " + word;
    }
}
