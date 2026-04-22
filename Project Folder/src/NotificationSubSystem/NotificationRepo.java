package NotificationSubSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class NotificationRepo {
	private final Path notificationFilePath;
	
	public NotificationRepo(String filepath) {
		this.notificationFilePath = Paths.get(filepath);
		createFileIfNotExists();
	}

    public void createFileIfNotExists() {
        try {
            if (notificationFilePath.getParent() != null) {
                Files.createDirectories(notificationFilePath.getParent());
            }
            if (!Files.exists(notificationFilePath)) {
                Files.createFile(notificationFilePath);
            }
        } catch (IOException e) {
        	
        }
    }
    
    public void createNotification(String noti, LocalDateTime time)
    {
    	if(time == null)
    	{
    		time = LocalDateTime.now();
    	}
    	try {
    		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Files.write(notificationFilePath,(noti + "," + time.format(formatter) + "\n").getBytes(),StandardOpenOption.APPEND
            		);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void deleteNotification(String noti)
    {
        try {
            List<String> lines = Files.readAllLines(notificationFilePath);
            List<String> updatedLines = new ArrayList<>();
            for (String line : lines) {
            	
            	    String[] parts = line.split(",");
                    if (parts[0] != noti) {
                        updatedLines.add(parts[0] + "," + parts[1]);
                    }
                }
            Files.write(notificationFilePath, updatedLines, StandardOpenOption.TRUNCATE_EXISTING);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
       
    }
    
    public List<Notification> getAllNotifications()
    {
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    	List<Notification> lin = new ArrayList<>();
    	try {
            List<String> lines = Files.readAllLines(notificationFilePath);
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                	String[] parts = line.split(",");
                   lin.add(new Notification(parts[0],LocalDateTime.parse(parts[1], formatter)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lin;
    }
}
