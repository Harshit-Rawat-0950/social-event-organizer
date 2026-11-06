package ReportDashboardSubSystem;

import java.util.ArrayList;
import java.util.List;

import ReportDashboardSubSystem.Theme.Role;
import UserMangerSubSystem.ConfigLoader;
import UserMangerSubSystem.User;
import UserMangerSubSystem.UserManagementFacade;

public class ProfilePage implements Page {

    private static ProfilePage instance;
    static List<TextElement> UIElements;
	private ProfilePage() {
		ConfigLoader configLoader = new ConfigLoader();
	    UIElements = TextElement.readFile(configLoader.getProperty("Files.profilepage"));
    }

    public static synchronized ProfilePage getInstance() {
        if (instance == null) {
            instance = new ProfilePage();
        }
        return instance;
    }

	@Override
	public void displayPage() {
		ConsoleInput in = ConsoleInput.getInstance();
		while(true)
		{
			User user = UserManagementFacade.getCurrUser();
			Screen.open("Profile", UIElements);
			List<String> details = new ArrayList<>();
			details.add(row("Id", String.valueOf(user.getId())));
			details.add(row("Name", user.getName()));
			details.add(row("Email", user.getEmail()));
			details.add(row("Role", user.getRole()));
			details.add(row("Status", user.getStatus()));
			Screen.println(Widgets.box(null, null, details, Role.FRAME));
			Screen.menu(UIElements);
			try {
				switch (in.readChoice("> "))
				{
				case 'n':
					UserManagementFacade.updateProfile(in.readField("New name: "), user.getEmail());
					Screen.success("Name updated.");
					break;
				case 'e':
					UserManagementFacade.updateProfile(user.getName(), in.readField("New email: "));
					Screen.success("Email updated.");
					break;
				case 'p':
					UserManagementFacade.changePassword(in.readSecret("Current password: "), in.readPassword("New password: "));
					Screen.success("Password changed.");
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

	private static String row(String label, String value) {
		return Style.pad(Style.color(Role.MUTED, label + ":"), 9) + Style.color(Role.TEXT, value);
	}
}
