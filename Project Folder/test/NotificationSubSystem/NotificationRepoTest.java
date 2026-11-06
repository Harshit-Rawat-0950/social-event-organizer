package NotificationSubSystem;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NotificationRepoTest {
	private Path file;
	private NotificationRepo repo;

	@Before
	public void setUp() throws Exception {
		file = Files.createTempFile("notifications", ".txt");
		repo = new NotificationRepo(file.toString());
	}

	@After
	public void tearDown() throws Exception {
		Files.deleteIfExists(file);
	}

	@Test
	public void workersOnlyShowARecipientTheirOwnNotifications() {
		new NotificationWriteWorker(repo).save(1, "for one");
		new NotificationWriteWorker(repo).save(2, "for two");
		List<Notification> forOne = new NotificationReadWorker(repo).fetch(1);
		assertEquals(1, forOne.size());
		assertEquals("for one", forOne.get(0).text);
	}

	@Test
	public void textMayContainCommas() {
		repo.createNotification(1, "Event updated: a, b, c", null);
		assertEquals("Event updated: a, b, c", repo.getAllNotifications().get(0).text);
	}

	@Test
	public void clearingOnlyAffectsThatUser() {
		repo.createNotification(1, "one", null);
		repo.createNotification(2, "two", null);
		repo.deleteNotifications(1);
		List<Notification> left = repo.getAllNotifications();
		assertEquals(1, left.size());
		assertEquals(2, left.get(0).getUserId());
	}

	@Test
	public void writeWorkerClearsThroughTheRepository() {
		repo.createNotification(1, "one", null);
		new NotificationWriteWorker(repo).clear(1);
		assertTrue(repo.getAllNotifications().isEmpty());
	}

	@Test
	public void timesAreKeptToTheSecond() {
		LocalDateTime time = LocalDateTime.of(2026, 3, 4, 5, 6, 7);
		repo.createNotification(1, "x", time);
		assertEquals(time, repo.getAllNotifications().get(0).notifyDateTime);
	}

	@Test
	public void blankLinesAreIgnored() throws Exception {
		Files.write(file, "\n1,2026-01-01 10:00:00,hello\n\n".getBytes());
		assertEquals(1, repo.getAllNotifications().size());
	}

	@Test
	public void missingFileIsCreated() throws Exception {
		Files.delete(file);
		new NotificationRepo(file.toString());
		assertTrue(Files.exists(file));
	}
}
