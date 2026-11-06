package ReportDashboardSubSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import EventManagementSubSystem.BasicEvent;
import EventManagementSubSystem.EventMangementFacade;
import FriendSubSystem.FriendFacade;
import InviteMangerSubSystem.InviteManagerFacade;
import InviteMangerSubSystem.RsvpStatus;
import NotificationSubSystem.NotificationFacade;
import ReportDashboardSubSystem.Theme.Role;
import UserMangerSubSystem.ConfigLoader;
import UserMangerSubSystem.User;
import UserMangerSubSystem.UserManagementFacade;

public class ReportPage implements Page {

    private static final Role[] RSVP_ROLES = {Role.SUCCESS, Role.WARNING, Role.INFO, Role.ERROR};
    private static ReportPage instance;
    static List<TextElement> UIElements;
	private ReportPage() {
		ConfigLoader configLoader = new ConfigLoader();
	    UIElements = TextElement.readFile(configLoader.getProperty("Files.reportpage"));
    }

    public static synchronized ReportPage getInstance() {
        if (instance == null) {
            instance = new ReportPage();
        }
        return instance;
    }

	@Override
	public void displayPage() {
		User user = UserManagementFacade.getCurrUser();
		int id = user.getId();
		Screen.open("Reports", UIElements);

		String[] labels = {"Events created", "Pending invites", "Notifications", "Friends", "Followers", "Following"};
		int[] values = {
			EventMangementFacade.getEventsCreatedBy(id).size(),
			InviteManagerFacade.getInvites(id).length,
			NotificationFacade.getNotifications(id).size(),
			FriendFacade.getConnections(id, "Friend").size(),
			FriendFacade.getConnections(id, "Follower").size(),
			FriendFacade.getConnections(id, "Following").size(),
		};
		int max = 1;
		for(int value : values)
			max = Math.max(max, value);
		List<String> stats = new ArrayList<>();
		for(int i = 0; i < labels.length; i++)
		{
			stats.add(Style.pad(Style.color(Role.TEXT, labels[i]), 17)
					+ Style.pad(Style.bold(Role.TITLE, String.valueOf(values[i])), 5)
					+ Widgets.meter(values[i], max, 24, Role.ACCENT));
		}
		Screen.println(Widgets.box(Style.bold(Role.ACCENT, "Report for " + user.getName()), null, stats, Role.FRAME));

		List<String> responses = new ArrayList<>();
		for(BasicEvent event : EventMangementFacade.getEventRecordsCreatedBy(id))
		{
			if(event.getParentId() != 0)
				continue;
			Map<RsvpStatus, Integer> counts = InviteManagerFacade.getRsvpCounts(event.getId());
			int[] parts = {counts.get(RsvpStatus.ACCEPTED), counts.get(RsvpStatus.MAYBE), counts.get(RsvpStatus.PENDING), counts.get(RsvpStatus.DECLINED)};
			responses.add(Style.color(Role.TITLE, "#" + event.getId() + " " + event.getName()));
			responses.add("  " + Widgets.stackedBar(parts, RSVP_ROLES, 16) + "  "
					+ Style.color(Role.TEXT, EventCardRenderer.rsvpText(parts[0], parts[1], parts[2], parts[3])));
		}
		if(responses.isEmpty())
			responses.add(Style.color(Role.MUTED, "(you have not created any events)"));
		Screen.println(Widgets.box(Style.bold(Role.ACCENT, "Responses to your events"), null, responses, Role.FRAME));
		ConsoleInput.getInstance().readLine("Press Enter to go back.");
	}

}
