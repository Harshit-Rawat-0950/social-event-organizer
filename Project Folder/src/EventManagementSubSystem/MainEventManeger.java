package EventManagementSubSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import ReportDashboardSubSystem.ConsoleInput;
import ReportDashboardSubSystem.DashboardFacade;
import UserMangerSubSystem.User;
import UserMangerSubSystem.UserManagementFacade;

public class MainEventManeger {
	private final Path filePath;
	public MainEventManeger(String file_path) {
        this.filePath = Paths.get(file_path);
        createFileIfNotExists();
    }

	/* ---------- Console flows ---------- */

	public int createEvent(String defaultType)
	{
		char choice = ConsoleInput.getInstance().readChoice("Will this event have sub-events? (y/n): ");
		BasicEvent event = readEvent(choice == 'y', defaultType);
		event.visibility = readVisibility();
		return addEvent(event);
	}

	public int createBasicEvent()
	{
		return addEvent(readEvent(false, "Public"));
	}

	public int addSubEvent(int parentId, int userId, String defaultType)
	{
		requireComposite(requireOwned(readAll(), parentId, userId));
		char choice = ConsoleInput.getInstance().readChoice("Will this sub-event have its own sub-events? (y/n): ");
		return addSubEvent(parentId, userId, readEvent(choice == 'y', defaultType));
	}

	public void decorateEvent(int id, int userId)
	{
		ConsoleInput in = ConsoleInput.getInstance();
		requireOwned(readAll(), id, userId);
		char kind = in.readChoice("Decoration - (m)usic, (n)ews, (o)uting, (u)rgent, (b)usiness: ");
		String spec;
		switch (kind) {
			case 'm':
				spec = "MUSIC|" + in.readField("Band name: ");
				break;
			case 'n':
				spec = "NEWS|" + in.readField("Headline: ") + "|" + in.readField("News summary: ");
				break;
			case 'o':
				spec = "OUTING|" + in.readIntInRange("Hotel star rating (1-5): ", 1, 5) + "|" + in.readField("Hotel: ");
				break;
			case 'u':
				spec = "URGENT|" + in.readIntInRange("Urgency level (1-5): ", 1, 5);
				break;
			case 'b':
				spec = "BUSINESS|" + in.readField("Business type: ");
				break;
			default:
				throw new IllegalArgumentException("Unknown decoration.");
		}
		addDecoration(id, userId, spec);
	}

	public void editEvent(int id, int userId)
	{
		ConsoleInput in = ConsoleInput.getInstance();
		requireOwned(readAll(), id, userId);
		char field = in.readChoice("Edit (n)ame, d(e)scription, (d)ate, (t)ime, (l)ocation, t(y)pe or (v)isibility: ");
		switch (field) {
			case 'n':
				updateEventField(id, userId, "name", in.readField("New name: "));
				break;
			case 'e':
				updateEventField(id, userId, "description", in.readField("New description: "));
				break;
			case 'd':
				updateEventField(id, userId, "date", in.readFutureDate("New date (yyyy-MM-dd): "));
				break;
			case 't':
				updateEventField(id, userId, "time", in.readTime("New time (HH:mm): "));
				break;
			case 'l':
				updateEventField(id, userId, "location", in.readField("New location: "));
				break;
			case 'y':
				updateEventField(id, userId, "type", in.readField("New type: "));
				break;
			case 'v':
				if(findEvent(id).getParentId() != 0)
					throw new IllegalArgumentException("Sub-events follow the visibility of their main event.");
				updateEventField(id, userId, "visibility", readVisibility().name());
				break;
			default:
				throw new IllegalArgumentException("Unknown field.");
		}
	}

	private BasicEvent readEvent(boolean composite, String defaultType)
	{
		ConsoleInput in = ConsoleInput.getInstance();
		String name = in.readField("Enter name: ");
		String description = in.readField("Enter description: ");
		String date = in.readFutureDate("Enter date (yyyy-MM-dd): ");
		String time = in.readTime("Enter time (HH:mm): ");
		String location = in.readField("Enter location: ");
		String type = in.readFieldOrDefault("Enter type", defaultType);
		return composite ? new CompositeEvent(name, description, date, time, location, type)
				: new BasicEvent(name, description, date, time, location, type);
	}

	private EventVisibility readVisibility()
	{
		while(true)
		{
			switch (ConsoleInput.getInstance().readChoice("Who can see it? (p)ublic, (f)ollowers, fr(i)ends, pri(v)ate [public]: "))
			{
				case ' ':
				case 'p':
					return EventVisibility.PUBLIC;
				case 'f':
					return EventVisibility.FOLLOWERS;
				case 'i':
					return EventVisibility.FRIENDS;
				case 'v':
					return EventVisibility.PRIVATE;
				default:
					DashboardFacade.showError("Please choose p, f, i or v.");
			}
		}
	}

	/* ---------- Operations ---------- */

	public int createBasicEvent(String ... args)
	{
		if(args.length < 6)
			throw new IllegalArgumentException("An event needs name, description, date, time, location and type.");
		return this.addEvent(new BasicEvent(args[0],args[1],args[2],args[3],args[4],args[5]));
	}

	// Saves the event as a new record and returns its generated id
	public int addEvent(BasicEvent e)
	{
		validate(e);
		int nextId = 1;
		for(BasicEvent existing : readAll())
			nextId = Math.max(nextId, existing.id + 1);
		e.id = nextId;
		User currentUser = UserManagementFacade.getCurrUser();
		if(e.ownerId == 0 && currentUser != null)
			e.ownerId = currentUser.getId();
		try {
			Files.write(this.filePath,(BasicEvent.toString(e) + "\n").getBytes(),StandardOpenOption.APPEND);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return e.id;
	}

	public int addSubEvent(int parentId, int userId, BasicEvent child)
	{
		requireComposite(requireOwned(readAll(), parentId, userId));
		child.parentId = parentId;
		child.ownerId = userId;
		child.visibility = getRootEvent(parentId).visibility;
		return addEvent(child);
	}

	public void addDecoration(int id, int userId, String spec)
	{
		List<BasicEvent> events = readAll();
		BasicEvent event = requireOwned(events, id, userId);
		event.decorations = event.decorations.isEmpty() ? spec : event.decorations + ";" + spec;
		writeAll(events);
	}

	public void updateEventField(int id, int userId, String field, String value)
	{
		List<BasicEvent> events = readAll();
		BasicEvent event = requireOwned(events, id, userId);
		switch (field) {
			case "name": event.name = value; break;
			case "description": event.description = value; break;
			case "date": event.date = value; break;
			case "time": event.time = value; break;
			case "location": event.location = value; break;
			case "type": event.type = value; break;
			case "visibility": event.visibility = EventVisibility.fromStored(value); break;
			default: throw new IllegalArgumentException("Unknown field: " + field);
		}
		validate(event);
		writeAll(events);
	}

	// Cancels the event and all of its sub-events, returning every event cancelled
	public List<BasicEvent> cancelEvent(int id, int userId)
	{
		List<BasicEvent> events = readAll();
		BasicEvent event = requireOwned(events, id, userId);
		List<BasicEvent> cancelled = new ArrayList<>();
		cancelWithChildren(event, events, cancelled);
		writeAll(events);
		return cancelled;
	}

	private void cancelWithChildren(BasicEvent event, List<BasicEvent> events, List<BasicEvent> cancelled)
	{
		if(!event.isCancelled()) {
			event.status = BasicEvent.CANCELLED;
			cancelled.add(event);
		}
		for(BasicEvent other : events)
			if(other.parentId == event.id)
				cancelWithChildren(other, events, cancelled);
	}

	/* ---------- Queries ---------- */

	public BasicEvent findEvent(int id)
	{
		return find(readAll(), id);
	}

	// The top level event an event belongs to, which may be the event itself
	public BasicEvent getRootEvent(int id)
	{
		List<BasicEvent> events = readAll();
		BasicEvent event = find(events, id);
		while(event != null && event.parentId != 0)
			event = find(events, event.parentId);
		return event;
	}

	public List<EventComponent> getAllEvents()
	{
		return getAllEvents(event -> true);
	}

	public List<EventComponent> getAllEvents(Predicate<BasicEvent> filter)
	{
		List<BasicEvent> events = readAll();
		List<EventComponent> result = new ArrayList<>();
		for(BasicEvent e : events)
			if(e.parentId == 0 && filter.test(e))
				result.add(build(e, events));
		return result;
	}

	// Active top level events that have not passed yet, soonest first
	public List<EventComponent> getUpcomingEvents()
	{
		return getUpcomingEvents(event -> true);
	}

	public List<EventComponent> getUpcomingEvents(Predicate<BasicEvent> filter)
	{
		List<BasicEvent> events = readAll();
		List<BasicEvent> upcoming = new ArrayList<>();
		for(BasicEvent e : events)
			if(e.parentId == 0 && !e.isCancelled() && !LocalDate.parse(e.date).isBefore(LocalDate.now()) && filter.test(e))
				upcoming.add(e);
		upcoming.sort(Comparator.comparing(BasicEvent::getDate).thenComparing(BasicEvent::getTime));
		List<EventComponent> result = new ArrayList<>();
		for(BasicEvent e : upcoming)
			result.add(build(e, events));
		return result;
	}

	public List<EventComponent> getEventsByOwner(int ownerId)
	{
		List<BasicEvent> events = readAll();
		List<EventComponent> result = new ArrayList<>();
		for(BasicEvent e : events)
			if(e.ownerId == ownerId && e.parentId == 0)
				result.add(build(e, events));
		return result;
	}

	// Every event the user created, sub-events included
	public List<BasicEvent> getRecordsByOwner(int ownerId)
	{
		List<BasicEvent> result = new ArrayList<>();
		for(BasicEvent e : readAll())
			if(e.ownerId == ownerId)
				result.add(e);
		return result;
	}

	// Turns stored records into the Composite + Decorator object structure
	private EventComponent build(BasicEvent record, List<BasicEvent> events)
	{
		for(BasicEvent other : events)
			if(other.parentId == record.id)
				record.add(build(other, events));
		EventComponent component = record;
		if(!record.decorations.isEmpty())
			for(String spec : record.decorations.split(";"))
				component = EventDecorator.fromSpec(component, spec);
		return component;
	}

	/* ---------- Checks and file handling ---------- */

	private BasicEvent requireOwned(List<BasicEvent> events, int id, int userId)
	{
		BasicEvent event = find(events, id);
		if(event == null)
			throw new IllegalArgumentException("No event with id " + id + ".");
		if(event.ownerId != userId)
			throw new IllegalArgumentException("Only the creator of an event can change it.");
		if(event.isCancelled())
			throw new IllegalArgumentException("Event " + id + " has been cancelled.");
		return event;
	}

	private static void requireComposite(BasicEvent event)
	{
		if(!(event instanceof CompositeEvent))
			throw new IllegalArgumentException("Event " + event.id + " was not created to hold sub-events.");
	}

	private static BasicEvent find(List<BasicEvent> events, int id)
	{
		for(BasicEvent e : events)
			if(e.id == id)
				return e;
		return null;
	}

	private static void validate(BasicEvent e)
	{
		for(String value : new String[] {e.name, e.description, e.date, e.time, e.location, e.type})
			if(value == null || value.trim().isEmpty() || value.matches(".*[,;|].*"))
				throw new IllegalArgumentException("Event fields cannot be empty or contain , ; |");
		try {
			LocalDate.parse(e.date);
			LocalTime.parse(e.time);
		} catch (DateTimeParseException ex) {
			throw new IllegalArgumentException("Event date must be yyyy-MM-dd and time must be HH:mm.");
		}
	}

	private List<BasicEvent> readAll()
	{
		List<BasicEvent> events = new ArrayList<>();
		try {
			for(String line : Files.readAllLines(this.filePath))
				if(!line.trim().isEmpty())
					events.add(BasicEvent.fromString(line));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return events;
	}

	private void writeAll(List<BasicEvent> events)
	{
		List<String> lines = new ArrayList<>();
		for(BasicEvent e : events)
			lines.add(BasicEvent.toString(e));
		try {
			Files.write(this.filePath, lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createFileIfNotExists() {
		try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
