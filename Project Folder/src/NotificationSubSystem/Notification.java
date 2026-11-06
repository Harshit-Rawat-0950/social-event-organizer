package NotificationSubSystem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {
	public String text;
	LocalDateTime notifyDateTime;
	int userId;

	public Notification(String t)
	{
		this.text = t;
		this.notifyDateTime = LocalDateTime.now();
	}
	public Notification(String t, LocalDateTime time)
	{
		this.text = t;
		this.notifyDateTime = time;
	}
	public Notification(int userId, String t, LocalDateTime time)
	{
		this(t, time);
		this.userId = userId;
	}
	public int getUserId() {
		return userId;
	}
	public String timeAgo() {
		return timeAgo(LocalDateTime.now());
	}
	String timeAgo(LocalDateTime now) {
		long minutes = Duration.between(notifyDateTime, now).toMinutes();
		if (minutes < 1) {
			return "just now";
		}
		if (minutes < 60) {
			return minutes + " min ago";
		}
		if (minutes < 60 * 24) {
			return (minutes / 60) + " hr ago";
		}
		return (minutes / (60 * 24)) + " days ago";
	}
	@Override
	public String toString() {
		return notifyDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " - " + text;
	}
}
