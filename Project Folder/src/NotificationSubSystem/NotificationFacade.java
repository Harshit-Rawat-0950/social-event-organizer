package NotificationSubSystem;

import java.util.List;

import UserMangerSubSystem.ConfigLoader;

/*
 * Goal is to implement all interaction of other subsystem through this class
 * only creating a unified interface for the entire system. However this class
 * in itself must not perform any function other than delegation to the
 * respective class of any and all interactions possible from other
 * parts of project.
 **/

public class NotificationFacade {

	private static NotificationRepo repo() {
		return new NotificationRepo(new ConfigLoader().getProperty("Files.notification"));
	}

	public static void sendNotification(int userId, String message) {
		new NotificationWriteWorker(repo()).save(userId, message);
	}

	public static List<Notification> getNotifications(int id) {
		return new NotificationReadWorker(repo()).fetch(id);
	}

	public static void clearNotifications(int id) {
		new NotificationWriteWorker(repo()).clear(id);
	}
}
