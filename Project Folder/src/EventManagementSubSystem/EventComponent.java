package EventManagementSubSystem;

interface EventComponent {
	void getInvited();
    void display();
    void add(EventComponent event);
    void remove(EventComponent event);
    EventComponent getChild(int index);
}