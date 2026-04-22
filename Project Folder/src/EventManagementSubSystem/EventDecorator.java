package EventManagementSubSystem;

abstract class EventDecorator implements EventComponent {
    protected EventComponent decoratedEvent;

    public EventDecorator(EventComponent decoratedEvent) {
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
        return decoratedEvent.getChild(index);
    }
}
