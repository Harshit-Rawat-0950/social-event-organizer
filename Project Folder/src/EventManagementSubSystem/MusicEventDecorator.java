package EventManagementSubSystem;

public class MusicEventDecorator extends EventDecorator{
	private String BandName;
	public MusicEventDecorator(EventComponent decoratedEvent,String BandName) {
		super(decoratedEvent);
		this.setBandName(BandName);
		// TODO Auto-generated constructor stub
	}
	public String getBandName() {
		return BandName;
	}
	public void setBandName(String bandName) {
		BandName = bandName;
	}
}
