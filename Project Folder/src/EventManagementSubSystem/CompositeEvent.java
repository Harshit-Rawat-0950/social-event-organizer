package EventManagementSubSystem;

import java.util.ArrayList;
import java.util.List;

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

}