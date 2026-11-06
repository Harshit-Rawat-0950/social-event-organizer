package EventManagementSubSystem;

import java.util.ArrayList;
import java.util.List;

// Everything needed to display an event, gathered through the Composite and Decorator layers
public class EventView {

	public enum TagKind { MUSIC, NEWS, OUTING, URGENT, BUSINESS }

	public static final class Tag {
		public final TagKind kind;
		public final String label;
		public final String value;
		public final int level;

		public Tag(TagKind kind, String label, String value, int level) {
			this.kind = kind;
			this.label = label;
			this.value = value;
			this.level = level;
		}
	}

	public final int id;
	public final int ownerId;
	public final String name;
	public final String description;
	public final String date;
	public final String time;
	public final String location;
	public final String type;
	public final EventVisibility visibility;
	public final boolean cancelled;
	public final List<Tag> tags = new ArrayList<>();
	public final List<EventView> children = new ArrayList<>();
	public int going;
	public int maybe;
	public int waiting;
	public int declined;

	EventView(BasicEvent e) {
		this.id = e.id;
		this.ownerId = e.ownerId;
		this.name = e.name;
		this.description = e.description;
		this.date = e.date;
		this.time = e.time;
		this.location = e.location;
		this.type = e.type;
		this.visibility = e.visibility;
		this.cancelled = e.isCancelled();
	}

	public boolean hasRsvp() {
		return going + maybe + waiting + declined > 0;
	}
}
