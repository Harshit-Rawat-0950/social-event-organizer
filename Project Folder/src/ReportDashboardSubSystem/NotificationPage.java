package ReportDashboardSubSystem;

import java.util.List;

import NotificationSubSystem.Notification;
import NotificationSubSystem.NotificationFacade;
import ReportDashboardSubSystem.Theme.Role;
import UserMangerSubSystem.ConfigLoader;
import UserMangerSubSystem.UserManagementFacade;

public class NotificationPage implements Page {

    private static NotificationPage instance;
    static List<TextElement> UIElements;
	private NotificationPage() {
		ConfigLoader configLoader = new ConfigLoader();
	    UIElements = TextElement.readFile(configLoader.getProperty("Files.notificationpage"));
    }

    public static synchronized NotificationPage getInstance() {
        if (instance == null) {
            instance = new NotificationPage();
        }
        return instance;
    }

	@Override
	public void displayPage() {
		int userId = UserManagementFacade.getCurrUser().getId();
		while(true)
		{
			Screen.open("Notifications", UIElements);
			List<Notification> notifications = NotificationFacade.getNotifications(userId);
			if(notifications.isEmpty())
				Screen.empty("(no notifications)");
			for(Notification notification : notifications)
			{
				showNotification(notification);
			}
			Screen.menu(UIElements);
			switch (ConsoleInput.getInstance().readChoice("> "))
			{
			case 'c':
				NotificationFacade.clearNotifications(userId);
				Screen.success("Notifications cleared.");
				break;
			case 'r':
				return;
			default:
				Screen.error("Please choose one of the options listed.");
			}
		}
	}

	private static void showNotification(Notification notification) {
		String bullet = "   " + Style.color(Role.ACCENT, Glyph.BULLET.toString()) + " ";
		String indent = " ".repeat(Style.visibleLength(bullet));
		List<String> lines = Style.wrap(notification.text, Widgets.width() - indent.length());
		for(int i = 0; i < lines.size(); i++)
		{
			Screen.println((i == 0 ? bullet : indent) + Style.color(Role.TEXT, lines.get(i)));
		}
		Screen.println(indent + Style.color(Role.MUTED, notification.timeAgo()));
	}

}
