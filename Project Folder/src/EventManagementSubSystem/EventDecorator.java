package EventManagementSubSystem;

abstract class EventDecorator implements EventComponent {
    protected EventDecorator decoratedEvent;

    public EventDecorator(EventDecorator decoratedEvent) {
        this.decoratedEvent = decoratedEvent;
    }
    @Override
    public void display() {
        decoratedEvent.display();
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
    	return null;
    }
}
