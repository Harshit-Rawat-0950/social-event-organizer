package EventManagementSubSystem;

public class UrgentEventDecorator extends EventDecorator{
	private int UrgencyLevel;

	public UrgentEventDecorator(EventComponent decoratedEvent,int UrgencyLevel) {
		super(decoratedEvent);
		this.setUrgencyLevel(UrgencyLevel);
	}

	public int getUrgencyLevel() {
		return UrgencyLevel;
	}

	public void setUrgencyLevel(int urgencyLevel) {
		UrgencyLevel = urgencyLevel;
	}

	@Override
	protected String getDecoration() {
		return "Urgency: " + UrgencyLevel + "/5";
	}

	@Override
	protected EventView.Tag getTag() {
		return new EventView.Tag(EventView.TagKind.URGENT, getDecoration(), "", UrgencyLevel);
	}
}
