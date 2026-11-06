package NotificationSubSystem;

import java.time.LocalDateTime;

// Handles every change to the stored notifications
class NotificationWriteWorker {
	private final NotificationRepo repo;

	NotificationWriteWorker(NotificationRepo repo) {
		this.repo = repo;
	}

	void save(int userId, String message) {
		repo.createNotification(userId, message, LocalDateTime.now());
	}

	void clear(int userId) {
		repo.deleteNotifications(userId);
	}
}
