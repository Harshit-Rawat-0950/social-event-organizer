package EventManagementSubSystem;

import java.util.ArrayList;
import java.util.List;

import ReportDashboardSubSystem.DashboardFacade;

public class CompositeEvent extends BasicEvent implements EventComponent {
    private List<EventComponent> children = new ArrayList<>();

    public CompositeEvent(List<EventComponent> children) {
    	super("","","","","","");
        this.children = children;
    }
    public CompositeEvent(String name, String description, String date, String time, String location, String type) {
    	super(name,description,date,time,location,type);
    	this.children = new ArrayList<>();
}
    /*
     *  Add the given child event/sub-event into the children list.
     */
    @Override
    public void add(EventComponent event) {
    	this.children.add(event);
    }


    /* Remove the given child event/sub-event from the children list.
     *
     */
    @Override
    public void remove(EventComponent event) {
    	if(this.children.contains(event))
    		this.children.remove(event);
    }

    /* Return the child event at the given index from the children list.
     */
    @Override
    public EventComponent getChild(int index) {
    	return this.children.get(index);
    }
    protected List<EventComponent> getChildren()
    {
    	return this.children;
    }

    @Override
    public void display() {
    	DashboardFacade.displayEvent(this);
    }

    // Own details followed by every sub-event, indented one level per nesting
    @Override
    public String getDetails() {
    	StringBuilder details = new StringBuilder(super.getDetails());
    	for(EventComponent child : this.children) {
    		details.append("\n    ").append(child.getDetails().replace("\n", "\n    "));
    	}
    	return details.toString();
    }

    @Override
    public EventView describe() {
    	EventView view = super.describe();
    	for(EventComponent child : this.children) {
    		view.children.add(child.describe());
    	}
    	return view;
    }

    @Override
    protected String getKind() {
    	return "COMPOSITE";
    }
}
