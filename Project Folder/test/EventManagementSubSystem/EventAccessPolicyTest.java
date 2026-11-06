package EventManagementSubSystem;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

// Every combination of the rule: 4 visibilities and 4 yes/no facts make 80 cases
@RunWith(Parameterized.class)
public class EventAccessPolicyTest {

	@Parameters(name = "{0} owner={1} invited={2} friend={3} follower={4}")
	public static List<Object[]> cases() {
		boolean[] both = {false, true};
		List<Object[]> cases = new ArrayList<>();
		for (EventVisibility visibility : EventVisibility.values()) {
			for (boolean owner : both) {
				for (boolean invited : both) {
					for (boolean friend : both) {
						for (boolean follower : both) {
							cases.add(new Object[] {visibility, owner, invited, friend, follower});
						}
					}
				}
			}
		}
		return cases;
	}

	private final EventVisibility visibility;
	private final boolean owner;
	private final boolean invited;
	private final boolean friend;
	private final boolean follower;

	public EventAccessPolicyTest(EventVisibility visibility, boolean owner, boolean invited, boolean friend, boolean follower) {
		this.visibility = visibility;
		this.owner = owner;
		this.invited = invited;
		this.friend = friend;
		this.follower = follower;
	}

	// The table agreed for the project, written out rather than computed from the code
	private boolean expected() {
		if (owner || invited) {
			return true;
		}
		String row;
		switch (visibility) {
			case PUBLIC:    row = "YYYY"; break;
			case FOLLOWERS: row = "NYYY"; break;
			case FRIENDS:   row = "NNYY"; break;
			default:        row = "NNNN";
		}
		// Column order: stranger, follower, friend, friend and follower
		int column = (friend ? 2 : 0) + (follower ? 1 : 0);
		return row.charAt(column) == 'Y';
	}

	@Test
	public void matchesTheAgreedTable() {
		assertEquals(expected(), EventAccessPolicy.canView(visibility, owner, invited, friend, follower));
	}
}
