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
    //Fields declaration 

    public BasicEvent(String name, String description, String date, String time, String location, String type) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.type = type;
    }
    
	@Override
	//Delegates display functionality to DashBoardSystem
	public void display() {
    	DashboardFacade.displayEvent(this);
    }
	//Can't add component as its basic
    @Override
    public void add(EventComponent event) {
        throw new UnsupportedOperationException("BasicEvent cannot have children.");
    }
    // Same as add
    @Override
    public void remove(EventComponent event) {
        throw new UnsupportedOperationException("BasicEvent cannot remove children.");
    }
    // Same as remove 
    @Override
    public EventComponent getChild(int index) {
        throw new UnsupportedOperationException("BasicEvent has no children.");
    }
}
