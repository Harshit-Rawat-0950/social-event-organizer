package EventManagementSubSystem;

import ReportDashboardSubSystem.DashboardFacade;

abstract class EventDecorator implements EventComponent {
    protected EventComponent decoratedEvent;

    public EventDecorator(EventComponent decoratedEvent) {
        this.decoratedEvent = decoratedEvent;
    }

    // What this decoration adds to the text of the event, for example "Band: Queen"
    protected abstract String getDecoration();

    // The same decoration in a structured form, for drawing it
    protected abstract EventView.Tag getTag();

    // Inserted after the first line of the event so sub-events stay underneath it
    @Override
    public String getDetails() {
        String inner = decoratedEvent.getDetails();
        int lineEnd = inner.indexOf('\n');
        String tag = " [" + getDecoration() + "]";
        return lineEnd < 0 ? inner + tag : inner.substring(0, lineEnd) + tag + inner.substring(lineEnd);
    }

    @Override
    public EventView describe() {
        EventView view = decoratedEvent.describe();
        view.tags.add(getTag());
        return view;
    }

    @Override
    public void display() {
        DashboardFacade.displayEvent(this);
    }

    @Override
    public void getInvited() {
        decoratedEvent.getInvited();
    }

    @Override
    public void add(EventComponent event) {
        decoratedEvent.add(event);
    }

    @Override
    public void remove(EventComponent event) {
        decoratedEvent.remove(event);
    }

    @Override
    public EventComponent getChild(int index) {
        return decoratedEvent.getChild(index);
    }

    // Rebuilds a decorator from its stored form, for example "OUTING|4|Taj"
    static EventComponent fromSpec(EventComponent event, String spec) {
        String[] p = spec.split("\\|", -1);
        switch (p[0]) {
            case "MUSIC":
                return new MusicEventDecorator(event, p[1]);
            case "NEWS":
                return new NewsEventDecorator(event, p[1], p[2]);
            case "OUTING":
                return new OutingEventDecorator(event, Integer.parseInt(p[1]), p[2]);
            case "URGENT":
                return new UrgentEventDecorator(event, Integer.parseInt(p[1]));
            case "BUSINESS":
                return new BusinessEventDecorator(event, p[1]);
            default:
                throw new IllegalArgumentException("Unknown decoration: " + p[0]);
        }
    }
}
