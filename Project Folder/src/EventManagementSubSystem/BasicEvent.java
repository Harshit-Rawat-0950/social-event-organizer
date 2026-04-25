package EventManagementSubSystem;

import InviteMangerSubSystem.InviteManagerFacade;
import ReportDashboardSubSystem.DashboardFacade;

public class BasicEvent implements EventComponent {
    protected String name;
    protected String description;
    protected String date;
    protected String time;
    protected String location;
    protected String type;
    //Field declaration 

    public BasicEvent(String name, String description, String date, String time, String location, String type) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.type = type;
    }
    //Constructor 
    //Delegates to invite and notification system

	@Override
	public void display() {
    	DashboardFacade.displayEvent(this);
    }

    @Override
    public void add(EventComponent event) {
        throw new UnsupportedOperationException("BasicEvent cannot have children.");
    }

    @Override
    public void remove(EventComponent event) {
        throw new UnsupportedOperationException("BasicEvent cannot remove children.");
    }

    @Override
    public EventComponent getChild(int index) {
        throw new UnsupportedOperationException("BasicEvent has no children.");
    }
}
