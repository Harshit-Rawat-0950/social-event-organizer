package ReportDashboardSubSystem;

import java.util.List;
import java.util.Scanner;

import InviteMangerSubSystem.InviteManagerFacade;
import UserMangerSubSystem.ConfigLoader;
import UserMangerSubSystem.User;
import UserMangerSubSystem.UserManagementFacade;

public class InvitesPage implements Page {

    private static InvitesPage instance;
    static List<TextElement> UIElements;
	private InvitesPage() { 
		ConfigLoader configLoader = new ConfigLoader();
	    UIElements = TextElement.readFile(configLoader.getProperty("Files.invitepage"));
    }

    public static synchronized InvitesPage getInstance() {
        if (instance == null) {
            instance = new InvitesPage();
        }
        return instance;
    }

	@Override
	public void displayPage() {
		TextElement.displayList(UIElements);
		User user = UserManagementFacade.getCurrUser();
		String[] invites = InviteManagerFacade.getInvites(user.getId());
    	for(String invite:invites)
    	{
    		new TextElement(invite, 0, true, false).display();// Style to choose
    	}
		Scanner sc = new Scanner(System.in);
		char choice = sc.next().charAt(0);
		sc.close();
		// 4 Press y to accept topmost and n to reject press r to go back to mainpage 
    	switch(choice)
    	{
    	case 'y':
    		InviteManagerFacade.acceptInvite(invites[0]);
    		break;
    	case 'n':
    		InviteManagerFacade.rejectInvite(invites[0]);
    		break;
    	case 'r':
    		MainPage.getInstance().displayPage();
    		break;
    	default: 
    		InvitesPage.getInstance().displayPage();
    	}
	}	

}
