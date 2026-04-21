package EventManagementSubSystem;

public class NewsEventDecorator extends EventDecorator{
	private String headline;
	private String description;
	public NewsEventDecorator(EventComponent decoratedEvent,String headline, String description) {
		super(decoratedEvent);
		
		// TODO Auto-generated constructor stub
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHeadline() {
		return headline;
	}

	public void setHeadline(String headline) {
		this.headline = headline;
	}

}
