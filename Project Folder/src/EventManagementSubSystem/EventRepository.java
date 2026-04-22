package EventManagementSubSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import UserMangerSubSystem.User;

public class EventRepository {
	private final Path EventFilePath;

    public EventRepository(String filePath) {
        this.EventFilePath = Paths.get(filePath);
        createFileIfNotExists();
    }

    public void createFileIfNotExists() {
        try {
            if (EventFilePath.getParent() != null) {
                Files.createDirectories(EventFilePath.getParent());
            }
            if (!Files.exists(EventFilePath)) {
                Files.createFile(EventFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addEvent(EventComponent e) {
        try {
            Files.write(EventFilePath,(new EventData().toString(e) + "\n").getBytes(),StandardOpenOption.APPEND
            );
        } catch (IOException ex) {
        }
    }

    public List<EventComponent> getEvents(int id) {
    	List<EventComponent> events = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(EventFilePath);
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                	//TODO 
                }
            }
        } catch (IOException e) {
        }
        return events;
    }

//    public User findUserById(int id) {
//        for (User user : getAllUsers()) {
//            if (user.getId() == id) {
//                return user;
//            }
//        }
//        return null;
//    }

//    public void updateUser(User updatedUser) {
//        List<User> users = getAllUsers();
//        try {
//            List<String> updatedLines = new ArrayList<>();
//            for (User user : users) {
//                if (user.getId() == updatedUser.getId()) {
//                    updatedLines.add(updatedUser.toString());
//                } else {
//                    updatedLines.add(user.toString());
//                }
//            }
//            Files.write(EventFilePath, updatedLines, StandardOpenOption.TRUNCATE_EXISTING);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    public void deleteUserById(int id) {
//        List<User> users = getAllUsers();
//        try {
//            List<String> updatedLines = new ArrayList<>();
//            for (User user : users) {
//                if (user.getId() != id) {
//                    updatedLines.add(user.toString());
//                }
//            }
//            Files.write(userFilePath, updatedLines, StandardOpenOption.TRUNCATE_EXISTING);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

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
