package EventManagementSubSystem;

public class BusinessEventDecorator extends EventDecorator{
	private String type;

	public BusinessEventDecorator(EventComponent decoratedEvent,String type) {
		super(decoratedEvent);
		this.type=type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	protected String getDecoration() {
		return "Business: " + type;
	}

	@Override
	protected EventView.Tag getTag() {
		return new EventView.Tag(EventView.TagKind.BUSINESS, getDecoration(), type, 0);
	}
}
