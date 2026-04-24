package ReportDashboardSubSystem;

import java.util.List;
import java.util.Scanner;

import UserMangerSubSystem.ConfigLoader;

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
    public void displayPage()
    {
    	TextElement.displayList(UIElements);
    	Scanner sc = new Scanner(System.in);
    	int choice = sc.nextInt();
    	sc.close();
    	switch(choice)
    	{
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
    	default:
    		MainPage.getInstance().displayPage();
    	}
    }
}
