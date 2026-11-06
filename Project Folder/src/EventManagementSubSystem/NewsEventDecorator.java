package EventManagementSubSystem;

public class NewsEventDecorator extends EventDecorator{
	private String headline;
	private String description;
	public NewsEventDecorator(EventComponent decoratedEvent,String headline, String description) {
		super(decoratedEvent);
		this.setHeadline(headline);
		this.setDescription(description);
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

	@Override
	protected String getDecoration() {
		return "News: " + headline + " - " + description;
	}

	@Override
	protected EventView.Tag getTag() {
		return new EventView.Tag(EventView.TagKind.NEWS, getDecoration(), headline + " - " + description, 0);
	}
}
