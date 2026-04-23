package NotificationSubSystem;

import java.time.LocalDateTime;

public class Notification {
	public String text;
	LocalDateTime notifyDateTime;
	
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
}
