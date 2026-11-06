package EventManagementSubSystem;

import InviteMangerSubSystem.InviteManagerFacade;
import ReportDashboardSubSystem.DashboardFacade;

public class BasicEvent implements EventComponent {
    static final String ACTIVE = "ACTIVE";
    static final String CANCELLED = "CANCELLED";

    protected String name;
    protected String description;
    protected String date;
    protected String time;
    protected String location;
    protected String type;
    protected int id;
    protected int ownerId;
    protected int parentId;
    protected String status = ACTIVE;
    protected String decorations = "";
    protected EventVisibility visibility = EventVisibility.PUBLIC;
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
    public void getInvited() {
        InviteManagerFacade.getInvitedToEvent(this);
    }

    @Override
    public void display() {
        DashboardFacade.displayEvent(this);
    }

    @Override
    public String getDetails() {
        String details = "#" + id + " " + name + " (" + type + ") - " + date + " " + time
                + " @ " + location + " - " + description;
        return isCancelled() ? details + " [CANCELLED]" : details;
    }

    @Override
    public EventView describe() {
        return new EventView(this);
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

    public int getId() { return id; }
    public int getOwnerId() { return ownerId; }
    public int getParentId() { return parentId; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public EventVisibility getVisibility() { return visibility; }
    public boolean isCancelled() { return CANCELLED.equals(status); }

    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    public void setVisibility(EventVisibility visibility) { this.visibility = visibility; }

    protected String getKind() {
        return "BASIC";
    }

    // id,owner,parent,status,kind,name,description,date,time,location,type,decorations,visibility
    public static String toString(BasicEvent e) {
        return e.id + "," + e.ownerId + "," + e.parentId + "," + e.status + "," + e.getKind() + ","
                + e.name + "," + e.description + "," + e.date + "," + e.time + "," + e.location + ","
                + e.type + "," + e.decorations + "," + e.visibility;
    }

    // Rows saved before visibility existed have 12 columns and load as public
    public static BasicEvent fromString(String line) {
        String[] p = line.split(",", -1);
        BasicEvent e = "COMPOSITE".equals(p[4])
                ? new CompositeEvent(p[5], p[6], p[7], p[8], p[9], p[10])
                : new BasicEvent(p[5], p[6], p[7], p[8], p[9], p[10]);
        e.id = Integer.parseInt(p[0]);
        e.ownerId = Integer.parseInt(p[1]);
        e.parentId = Integer.parseInt(p[2]);
        e.status = p[3];
        e.decorations = p[11];
        e.visibility = p.length > 12 ? EventVisibility.fromStored(p[12]) : EventVisibility.PUBLIC;
        return e;
    }
}
