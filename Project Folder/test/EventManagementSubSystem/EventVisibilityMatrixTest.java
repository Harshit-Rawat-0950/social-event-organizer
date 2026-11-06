package EventManagementSubSystem;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import InviteMangerSubSystem.RsvpStatus;
import TestSupport.Fixtures;
import TestSupport.TestEnvironment;

/*
 * Through the real facades and files: all 16 relationship states between a creator
 * and a viewer, times 4 visibilities, times 5 invite states, making 320 cases.
 */
@RunWith(Parameterized.class)
public class EventVisibilityMatrixTest {
	private static final int OWNER = 1;
	private static final int VIEWER = 2;
	private static final String NOT_INVITED = "NOT_INVITED";

	private enum Friendship { NONE, REQUEST_FROM_OWNER, REQUEST_FROM_VIEWER, FRIENDS }

	@Parameters(name = "{0} ownerFollows={1} viewerFollows={2} {3} {4}")
	public static List<Object[]> cases() {
		boolean[] both = {false, true};
		String[] invites = {NOT_INVITED, "PENDING", "MAYBE", "ACCEPTED", "DECLINED"};
		List<Object[]> cases = new ArrayList<>();
		for (Friendship friendship : Friendship.values()) {
			for (boolean ownerFollows : both) {
				for (boolean viewerFollows : both) {
					for (EventVisibility visibility : EventVisibility.values()) {
						for (String invite : invites) {
							cases.add(new Object[] {friendship, ownerFollows, viewerFollows, visibility, invite});
						}
					}
				}
			}
		}
		return cases;
	}

	@Rule
	public TestEnvironment env = new TestEnvironment();

	private final Friendship friendship;
	private final boolean ownerFollows;
	private final boolean viewerFollows;
	private final EventVisibility visibility;
	private final String invite;

	public EventVisibilityMatrixTest(Friendship friendship, boolean ownerFollows, boolean viewerFollows, EventVisibility visibility, String invite) {
		this.friendship = friendship;
		this.ownerFollows = ownerFollows;
		this.viewerFollows = viewerFollows;
		this.visibility = visibility;
		this.invite = invite;
	}

	private boolean shouldSee() {
		if (invite.equals("PENDING") || invite.equals("MAYBE") || invite.equals("ACCEPTED")) {
			return true;
		}
		boolean friends = friendship == Friendship.FRIENDS;
		switch (visibility) {
			case PUBLIC:
				return true;
			case FOLLOWERS:
				return friends || viewerFollows;
			case FRIENDS:
				return friends;
			default:
				return false;
		}
	}

	@Test
	public void viewerSeesExactlyWhatTheRuleAllows() {
		Fixtures.user(OWNER, "Owner");
		Fixtures.user(VIEWER, "Viewer");
		switch (friendship) {
			case FRIENDS:
				Fixtures.friends(OWNER, VIEWER);
				break;
			case REQUEST_FROM_OWNER:
				Fixtures.pendingRequest(OWNER, VIEWER);
				break;
			case REQUEST_FROM_VIEWER:
				Fixtures.pendingRequest(VIEWER, OWNER);
				break;
			default:
		}
		if (ownerFollows) {
			Fixtures.follows(OWNER, VIEWER);
		}
		if (viewerFollows) {
			Fixtures.follows(VIEWER, OWNER);
		}
		int top = Fixtures.composite(OWNER, "Target", visibility);
		int child = Fixtures.subEvent(top, OWNER, "Child");
		if (!invite.equals(NOT_INVITED)) {
			Fixtures.invited(OWNER, VIEWER, top, RsvpStatus.valueOf(invite));
		}

		Set<String> expected = shouldSee() ? Collections.singleton("Target") : new TreeSet<>();
		assertEquals(expected, Fixtures.names(EventMangementFacade.getUpcomingEventViews(VIEWER)));
		assertEquals(expected, Fixtures.names(EventMangementFacade.getAllEventViews(VIEWER)));
		assertEquals("Sub-events follow the main event", shouldSee(),
				EventMangementFacade.canView(EventMangementFacade.getEvent(child), VIEWER));
		assertEquals("The creator always sees it", Collections.singleton("Target"),
				Fixtures.names(EventMangementFacade.getUpcomingEventViews(OWNER)));
	}
}
