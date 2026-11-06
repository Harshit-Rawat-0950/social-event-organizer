package EventManagementSubSystem;

public class OutingEventDecorator extends EventDecorator{
	private String hotel;
	private int starRating;
	public OutingEventDecorator(EventComponent decoratedEvent,int starRating, String hotel) {
		super(decoratedEvent);
		this.setStarRating(starRating);
		this.setHotel(hotel);
	}
	public int getStarRating() {
		return starRating;
	}
	public void setStarRating(int starRating) {
		this.starRating = starRating;
	}
	public String getHotel() {
		return hotel;
	}
	public void setHotel(String hotel) {
		this.hotel = hotel;
	}
	@Override
	protected String getDecoration() {
		return "Outing: " + hotel + " - " + starRating + " star";
	}

	@Override
	protected EventView.Tag getTag() {
		return new EventView.Tag(EventView.TagKind.OUTING, getDecoration(), hotel, starRating);
	}
}
