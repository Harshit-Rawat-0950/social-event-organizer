package EventManagementSubSystem;

public class MusicEventDecorator extends EventDecorator{
	private String BandName;
	public MusicEventDecorator(EventComponent decoratedEvent,String BandName) {
		super(decoratedEvent);
		this.setBandName(BandName);
	}
	public String getBandName() {
		return BandName;
	}
	public void setBandName(String bandName) {
		BandName = bandName;
	}
	@Override
	protected String getDecoration() {
		return "Band: " + BandName;
	}

	@Override
	protected EventView.Tag getTag() {
		return new EventView.Tag(EventView.TagKind.MUSIC, getDecoration(), BandName, 0);
	}
}
