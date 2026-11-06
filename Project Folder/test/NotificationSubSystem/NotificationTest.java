package NotificationSubSystem;

import static org.junit.Assert.*;

import java.time.LocalDateTime;

import org.junit.Test;

public class NotificationTest {
	private final LocalDateTime now = LocalDateTime.of(2026, 1, 10, 12, 0);

	private Notification at(LocalDateTime time) {
		return new Notification(1, "x", time);
	}

	@Test
	public void timeAgoChoosesASensibleUnit() {
		assertEquals("just now", at(now.minusSeconds(30)).timeAgo(now));
		assertEquals("1 min ago", at(now.minusMinutes(1)).timeAgo(now));
		assertEquals("59 min ago", at(now.minusMinutes(59)).timeAgo(now));
		assertEquals("1 hr ago", at(now.minusHours(1)).timeAgo(now));
		assertEquals("23 hr ago", at(now.minusHours(23)).timeAgo(now));
		assertEquals("1 days ago", at(now.minusDays(1)).timeAgo(now));
	}

	@Test
	public void constructorsFillTheirFields() {
		assertEquals("hello", new Notification("hello").text);
		assertNotNull(new Notification("hello").notifyDateTime);
		assertEquals(now, new Notification("hello", now).notifyDateTime);
		assertEquals(7, new Notification(7, "hello", now).getUserId());
		assertEquals("just now", new Notification("hello").timeAgo());
	}

	@Test
	public void textFormIncludesTheTime() {
		assertEquals("2026-01-10 12:00 - x", at(now).toString());
	}
}
