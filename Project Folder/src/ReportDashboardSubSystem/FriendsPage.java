package ReportDashboardSubSystem;

import java.util.ArrayList;
import java.util.List;

import FriendSubSystem.FriendFacade;
import FriendSubSystem.FriendManagement;
import ReportDashboardSubSystem.Theme.Role;
import UserMangerSubSystem.ConfigLoader;
import UserMangerSubSystem.User;
import UserMangerSubSystem.UserManagementFacade;

public class FriendsPage implements Page {

    private static FriendsPage instance;
    static List<TextElement> UIElements;
	private FriendsPage() {
		ConfigLoader configLoader = new ConfigLoader();
	    UIElements = TextElement.readFile(configLoader.getProperty("Files.friendpage"));
    }

    public static synchronized FriendsPage getInstance() {
        if (instance == null) {
            instance = new FriendsPage();
        }
        return instance;
    }

	@Override
	public void displayPage() {
		ConsoleInput in = ConsoleInput.getInstance();
		int userId = UserManagementFacade.getCurrUser().getId();
		while(true)
		{
			Screen.open("Friends", UIElements);
			showConnections(FriendFacade.getUserConnections(userId));
			Screen.menu(UIElements);
			try {
				switch (in.readChoice("> "))
				{
				case 's':
					showSearch(UserManagementFacade.searchUsers(in.readField("Search by name or email: ")));
					break;
				case 'a':
					FriendFacade.sendFriendRequest(userId, in.readInt("User id: "));
					Screen.success("Friend request sent.");
					break;
				case 'd':
					FriendFacade.removeFriend(userId, in.readInt("User id: "));
					Screen.success("Friend removed.");
					break;
				case 'f':
					FriendFacade.followUser(userId, in.readInt("User id: "));
					Screen.success("Now following.");
					break;
				case 'u':
					FriendFacade.unfollowUser(userId, in.readInt("User id: "));
					Screen.success("Unfollowed.");
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

	private static void showConnections(List<FriendManagement> connections) {
		if(connections.isEmpty()) {
			Screen.empty("(no connections yet)");
			return;
		}
		List<String[]> rows = new ArrayList<>();
		for(FriendManagement connection : connections)
		{
			User other = UserManagementFacade.findUser(connection.getTargetUserId());
			rows.add(new String[] {
				relationBadge(connection.getStatus()),
				Style.color(Role.TEXT, other == null ? "Unknown" : other.getName()),
				Style.color(Role.MUTED, String.valueOf(connection.getTargetUserId())),
			});
		}
		Screen.println(Widgets.table(new String[] {"Relation", "Name", "Id"}, rows));
	}

	private static String relationBadge(String status) {
		Role role = status.equals("FRIEND") ? Role.SUCCESS : status.equals("FOLLOWER") ? Role.INFO : Role.ACCENT;
		return Widgets.badge(status, role);
	}

	// Results are held back as one message so they stay on screen after the redraw
	private static void showSearch(List<User> users) {
		if(users.isEmpty()) {
			Screen.info("No users found.");
			return;
		}
		Screen.info("Found " + users.size() + " user(s):");
		for(User user : users)
		{
			Screen.info(user.getName() + " (" + user.getId() + ") - " + user.getEmail());
		}
	}

}
