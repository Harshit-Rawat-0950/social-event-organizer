package InviteMangerSubSystem;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RsvpRepositoryTest {
	private Path file;
	private RsvpRepository repo;

	@Before
	public void setUp() throws Exception {
		file = Files.createTempFile("rsvps", ".txt");
		repo = new RsvpRepository(file.toString());
	}

	@After
	public void tearDown() throws Exception {
		Files.deleteIfExists(file);
	}

	@Test
	public void latestResponseReplacesTheEarlierOneAndCountsAdd() {
		repo.setResponse(1, 10, RsvpStatus.PENDING);
		repo.setResponse(1, 10, RsvpStatus.MAYBE);
		repo.setResponse(1, 11, RsvpStatus.ACCEPTED);
		repo.setResponse(1, 12, RsvpStatus.DECLINED);
		repo.setResponse(2, 10, RsvpStatus.PENDING);
		assertEquals(RsvpStatus.MAYBE, repo.getResponses(1).get(10));
		assertEquals(Integer.valueOf(1), repo.getCounts(1).get(RsvpStatus.MAYBE));
		assertEquals(Integer.valueOf(1), repo.getCounts(1).get(RsvpStatus.ACCEPTED));
		assertEquals(Integer.valueOf(1), repo.getCounts(1).get(RsvpStatus.DECLINED));
		assertEquals(Integer.valueOf(0), repo.getCounts(1).get(RsvpStatus.PENDING));
		assertEquals(Integer.valueOf(1), repo.getCounts(2).get(RsvpStatus.PENDING));
	}

	@Test
	public void userIdPrefixesDoNotCollide() {
		repo.setResponse(1, 1, RsvpStatus.PENDING);
		repo.setResponse(1, 10, RsvpStatus.PENDING);
		repo.setResponse(11, 1, RsvpStatus.PENDING);
		repo.setResponse(1, 1, RsvpStatus.ACCEPTED);
		assertEquals(2, repo.getResponses(1).size());
		assertEquals(1, repo.getResponses(11).size());
		assertEquals(RsvpStatus.PENDING, repo.getResponses(11).get(1));
	}

	@Test
	public void eventWithNoResponsesCountsZeroForEveryStatus() {
		assertTrue(repo.getResponses(5).isEmpty());
		for (RsvpStatus status : RsvpStatus.values()) {
			assertEquals(Integer.valueOf(0), repo.getCounts(5).get(status));
		}
	}

	@Test
	public void responsesSurviveReopening() {
		repo.setResponse(1, 10, RsvpStatus.ACCEPTED);
		assertEquals(RsvpStatus.ACCEPTED, new RsvpRepository(file.toString()).getResponses(1).get(10));
	}

	@Test
	public void responsesKeepTheOrderTheyWereRecordedIn() {
		repo.setResponse(1, 30, RsvpStatus.PENDING);
		repo.setResponse(1, 10, RsvpStatus.PENDING);
		assertEquals(30, (int) new ArrayList<>(repo.getResponses(1).keySet()).get(0));
	}

	@Test
	public void missingFileIsCreated() throws Exception {
		Files.delete(file);
		new RsvpRepository(file.toString());
		assertTrue(Files.exists(file));
	}
}
