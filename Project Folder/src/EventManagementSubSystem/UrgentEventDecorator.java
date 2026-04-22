package EventManagementSubSystem;

public class UrgentEventDecorator extends EventDecorator{
	private int UrgencyLevel;

	public UrgentEventDecorator(EventDecorator decoratedEvent,int UrgencyLevel) {
		super(decoratedEvent);
		this.setUrgencyLevel(UrgencyLevel);
	}

	public int getUrgencyLevel() {
		return UrgencyLevel;
	}

	public void setUrgencyLevel(int urgencyLevel) {
		UrgencyLevel = urgencyLevel;
	}

}
