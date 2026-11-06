package InviteMangerSubSystem;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InviteSubSystemTest {
	private Path invites;

	@Before
	public void setUp() throws Exception {
		invites = Files.createTempFile("invites", ".txt");
	}

	@After
	public void tearDown() throws Exception {
		Files.deleteIfExists(invites);
	}

	@Test
	public void inviteRoundTripsThroughItsStoredForm() {
		Invite event = Invite.fromString(new Invite(1, 2, 9).toString());
		assertEquals(1, event.getSenderId());
		assertEquals(2, event.getReceiverId());
		assertEquals(9, event.getEventId());
		assertTrue(event.isEventInvite());
		assertFalse(Invite.fromString(new Invite(1, 2).toString()).isEventInvite());
	}

	@Test
	public void inviteInTheOriginalThreeFieldFormatIsAFriendRequest() {
		assertFalse(Invite.fromString("1,2,2099-01-01T10:00:00").isEventInvite());
	}

	@Test
	public void newInvitesExpireInThreeMonths() {
		assertFalse(new Invite(1, 2).isExpired());
		assertFalse(new Invite(1, 2, LocalDateTime.now().plusMonths(3).minusMinutes(1), 0).isExpired());
		assertTrue(new Invite(1, 2, LocalDateTime.now().minusSeconds(1), 0).isExpired());
	}

	@Test
	public void missingFileIsCreatedWithItsFolders() throws Exception {
		Path nested = invites.resolveSibling("nested-" + System.nanoTime()).resolve("invites.txt");
		new InviteRepository(nested.toString());
		assertTrue(Files.exists(nested));
		Files.delete(nested);
		Files.delete(nested.getParent());
	}

	@Test
	public void blankLinesAreIgnored() throws Exception {
		Files.write(invites, "\n1,2,2099-01-01T10:00:00,0\n\n".getBytes());
		assertEquals(1, new InviteRepository(invites.toString()).getAllRawInvites().size());
	}

	@Test
	public void proxyHidesExpiredInvitesThatTheRealRepositoryStillHolds() {
		InviteRepo proxy = new InviteRepositoryProxy(invites.toString());
		proxy.saveInvite(new Invite(1, 2, LocalDateTime.now().minusDays(1), 0));
		proxy.saveInvite(new Invite(3, 2));
		assertEquals(1, proxy.getAllRawInvites().size());
		assertEquals(2, new InviteRepository(invites.toString()).getAllRawInvites().size());
	}

	@Test
	public void proxyDoesNotTouchTheFileUntilFirstUsed() throws Exception {
		Path untouched = invites.resolveSibling("lazy-" + System.nanoTime() + ".txt");
		InviteRepo proxy = new InviteRepositoryProxy(untouched.toString());
		assertFalse(Files.exists(untouched));
		assertTrue(proxy.getAllRawInvites().isEmpty());
		assertTrue(Files.exists(untouched));
		Files.delete(untouched);
	}

	@Test
	public void proxyRemovesThroughToTheRealRepository() {
		InviteRepo proxy = new InviteRepositoryProxy(invites.toString());
		Invite invite = new Invite(1, 2);
		proxy.saveInvite(invite);
		assertTrue(proxy.removeInvite(invite.toString()));
		assertTrue(new InviteRepository(invites.toString()).getAllRawInvites().isEmpty());
	}

	@Test
	public void removeInviteOnlyRemovesTheMatchingLine() {
		InviteRepo repo = new InviteRepository(invites.toString());
		Invite first = new Invite(1, 2);
		repo.saveInvite(first);
		repo.saveInvite(new Invite(3, 2));
		assertTrue(repo.removeInvite(first.toString()));
		assertFalse(repo.removeInvite(first.toString()));
		assertEquals(1, repo.getAllRawInvites().size());
	}
}
