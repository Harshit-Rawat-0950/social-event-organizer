package ReportDashboardSubSystem;

import java.util.List;

import InviteMangerSubSystem.InviteManagerFacade;
import ReportDashboardSubSystem.Theme.Role;
import UserMangerSubSystem.ConfigLoader;
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
		while(true)
		{
			Screen.open("Invites", UIElements);
			String[] invites = InviteManagerFacade.getInvites(UserManagementFacade.getCurrUser().getId());
			if(invites.length == 0)
				Screen.empty("(no pending invites)");
			for(int i = 0; i < invites.length; i++)
			{
				String title = i == 0 ? Style.bold(Role.ACCENT, Glyph.PROMPT + " Answering this one") : Style.color(Role.MUTED, "Next");
				Screen.println(Widgets.box(title, null, Style.wrap(InviteManagerFacade.describeInvite(invites[i]), Widgets.innerWidth()), i == 0 ? Role.ACCENT : Role.MUTED));
			}
			Screen.menu(UIElements);
			char choice = ConsoleInput.getInstance().readChoice("> ");
			if(choice == 'r')
				return;
			if(choice != 'y' && choice != 'n' && choice != 'm') {
				Screen.error("Please choose one of the options listed.");
				continue;
			}
			if(invites.length == 0) {
				Screen.error("There are no invites to answer.");
				continue;
			}
			try {
				if(choice == 'y') {
					InviteManagerFacade.acceptInvite(invites[0]);
					Screen.success("Invite accepted.");
				} else if(choice == 'n') {
					InviteManagerFacade.rejectInvite(invites[0]);
					Screen.success("Invite declined.");
				} else {
					InviteManagerFacade.maybeInvite(invites[0]);
					Screen.success("Marked as maybe. You can still answer later.");
				}
			} catch (IllegalArgumentException e) {
				Screen.error(e.getMessage());
			}
		}
	}

}
