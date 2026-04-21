package EventManagementSubSystem;

public class OutingEventDecorator extends EventDecorator{
	private String hotel;
	private int starRating;
	public OutingEventDecorator(EventComponent decoratedEvent,int starRating, String hotel) {
		super(decoratedEvent);
		this.setStarRating(starRating);
		this.setHotel(hotel);
		// TODO Auto-generated constructor stub
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
}
