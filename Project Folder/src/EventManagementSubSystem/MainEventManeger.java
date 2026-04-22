package EventManagementSubSystem;

import java.util.List;

public class MainEventManeger {
	
	public void createEvent(String Type, EventData e)
	{
		
	}
	class EventData{
		List<EventComponent> Events;
	    String name;
	    String description;
	    String date;
	    String time;
	    String location;
	    String type;
	    public String toString(EventComponent event)
	    {
	    	if(event instanceof BasicEvent)
	    	{
	    		String result="";
	    		
	    		return result;
	    	}
	    	return null;
	    }
	}
}
