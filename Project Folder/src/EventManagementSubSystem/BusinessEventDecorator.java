package EventManagementSubSystem;

public class BusinessEventDecorator extends EventDecorator{
	private String type;

	public BusinessEventDecorator(EventComponent decoratedEvent,String type) {
		super(decoratedEvent);
		this.type=type;
		// TODO Auto-generated constructor stub
	}

	public void getInvited() {
		// TODO Auto-generated method stub
		
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
