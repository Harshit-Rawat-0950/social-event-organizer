package EventManagementSubSystem;

// Who may see an event. Sub-events always follow their top level event.
public enum EventVisibility {
	PUBLIC("Public"),
	FOLLOWERS("Followers"),
	FRIENDS("Friends"),
	PRIVATE("Private");

	private final String label;

	EventVisibility(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	// Anything unrecognised is treated as public, as all older events were
	public static EventVisibility fromStored(String value) {
		for (EventVisibility visibility : values()) {
			if (visibility.name().equalsIgnoreCase(value.trim())) {
				return visibility;
			}
		}
		return PUBLIC;
	}
}
