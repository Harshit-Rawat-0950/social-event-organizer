package NotificationSubSystem;

import java.util.ArrayList;
import java.util.List;

// Handles every lookup of stored notifications
class NotificationReadWorker {
	private final NotificationRepo repo;

	NotificationReadWorker(NotificationRepo repo) {
		this.repo = repo;
	}

	List<Notification> fetch(int userId) {
		List<Notification> result = new ArrayList<>();
		for (Notification notification : repo.getAllNotifications()) {
			if (notification.getUserId() == userId) {
				result.add(notification);
			}
		}
		return result;
	}
}
