package EventManagementSubSystem;

public class UrgentEventDecorator extends EventDecorator{
	private int UrgencyLevel;

	public UrgentEventDecorator(EventComponent decoratedEvent,int UrgencyLevel) {
		super(decoratedEvent);
		this.setUrgencyLevel(UrgencyLevel);
		// TODO Auto-generated constructor stub
	}

	public int getUrgencyLevel() {
		return UrgencyLevel;
	}

	public void setUrgencyLevel(int urgencyLevel) {
		UrgencyLevel = urgencyLevel;
	}

}
