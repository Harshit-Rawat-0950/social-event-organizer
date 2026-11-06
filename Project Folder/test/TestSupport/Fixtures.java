package TestSupport;

import java.util.Set;
import java.util.TreeSet;
import java.util.List;

import EventManagementSubSystem.BasicEvent;
import EventManagementSubSystem.CompositeEvent;
import EventManagementSubSystem.EventView;
import EventManagementSubSystem.EventVisibility;
import EventManagementSubSystem.MainEventManeger;
import FriendSubSystem.FriendRepo;
import InviteMangerSubSystem.Invite;
import InviteMangerSubSystem.InviteManagerFacade;
import InviteMangerSubSystem.InviteRepository;
import InviteMangerSubSystem.RsvpStatus;
import NotificationSubSystem.NotificationFacade;
import NotificationSubSystem.Notification;
import UserMangerSubSystem.ConfigLoader;
import UserMangerSubSystem.CurrentApplicationUser;
import UserMangerSubSystem.TestAccounts;
import UserMangerSubSystem.User;
import UserMangerSubSystem.UserManagementFacade;
import UserMangerSubSystem.UserRepository;

// Sets up test data directly in the stores, without creating notifications
public final class Fixtures {

    public static final String FUTURE = "2099-01-01";

    private Fixtures() {
    }

    private static String file(String key) {
        return new ConfigLoader().getProperty("Files." + key);
    }

    public static MainEventManeger events() {
        return new MainEventManeger(file("events"));
    }

    private static FriendRepo friendRepo() {
        return new FriendRepo(file("friends"), file("follow"));
    }

    public static String password(int id) {
        return "pw" + id;
    }

    public static User user(int id, String name) {
        User user = new User(id, name, name.toLowerCase() + "@x", "Tester", "ACTIVE");
        new UserRepository(file("users")).addUser(user);
        TestAccounts.setPassword(id, password(id));
        return user;
    }

    public static void login(int id) {
        CurrentApplicationUser.getInstance().login(UserManagementFacade.findUser(id));
    }

    public static void friends(int a, int b) {
        friendRepo().createRelationship(UserManagementFacade.findUser(a), UserManagementFacade.findUser(b), 0);
    }

    public static void follows(int follower, int target) {
        friendRepo().createRelationship(UserManagementFacade.findUser(follower), UserManagementFacade.findUser(target));
    }

    public static void pendingRequest(int from, int to) {
        new InviteRepository(file("invites")).saveInvite(new Invite(from, to));
    }

    public static int event(int owner, String name, EventVisibility visibility) {
        return eventOn(owner, name, visibility, FUTURE);
    }

    public static int eventOn(int owner, String name, EventVisibility visibility, String date) {
        BasicEvent event = new BasicEvent(name, "About " + name, date, "10:00", "Hall", "Party");
        event.setOwnerId(owner);
        event.setVisibility(visibility);
        return events().addEvent(event);
    }

    public static int composite(int owner, String name, EventVisibility visibility) {
        CompositeEvent event = new CompositeEvent(name, "About " + name, FUTURE, "10:00", "Hall", "Party");
        event.setOwnerId(owner);
        event.setVisibility(visibility);
        return events().addEvent(event);
    }

    public static int subEvent(int parent, int owner, String name) {
        return events().addSubEvent(parent, owner, new BasicEvent(name, "About " + name, FUTURE, "11:00", "Room", "Talk"));
    }

    public static int subComposite(int parent, int owner, String name) {
        return events().addSubEvent(parent, owner, new CompositeEvent(name, "About " + name, FUTURE, "11:00", "Room", "Talk"));
    }

    // Invites through the real facade, then answers. PENDING leaves it unanswered.
    public static void invited(int owner, int receiver, int eventId, RsvpStatus answer) {
        InviteManagerFacade.sendEventInvite(owner, receiver, eventId);
        String raw = findEventInvite(receiver, eventId);
        switch (answer) {
            case ACCEPTED:
                InviteManagerFacade.acceptInvite(raw);
                break;
            case DECLINED:
                InviteManagerFacade.rejectInvite(raw);
                break;
            case MAYBE:
                InviteManagerFacade.maybeInvite(raw);
                break;
            default:
        }
    }

    public static String findEventInvite(int receiver, int eventId) {
        for (String raw : InviteManagerFacade.getInvites(receiver)) {
            if (Invite.fromString(raw).getEventId() == eventId) {
                return raw;
            }
        }
        throw new AssertionError("No invite to event " + eventId + " for user " + receiver);
    }

    public static Set<String> names(List<EventView> views) {
        Set<String> names = new TreeSet<>();
        for (EventView view : views) {
            names.add(view.name);
        }
        return names;
    }

    public static Set<String> texts(int userId) {
        Set<String> texts = new TreeSet<>();
        for (Notification notification : NotificationFacade.getNotifications(userId)) {
            texts.add(notification.text);
        }
        return texts;
    }
}
