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

/*
 * Stored as userId,timestamp,text. The text comes last so it may contain commas.
 */
public class NotificationRepo {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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
        	e.printStackTrace();
        }
    }

    public void createNotification(int userId, String noti, LocalDateTime time)
    {
    	if(time == null)
    	{
    		time = LocalDateTime.now();
    	}
    	try {
            Files.write(notificationFilePath,(userId + "," + time.format(FORMATTER) + "," + noti + "\n").getBytes(),StandardOpenOption.APPEND
            		);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteNotifications(int userId)
    {
        try {
            List<String> updatedLines = new ArrayList<>();
            for (String line : Files.readAllLines(notificationFilePath)) {
                if (!line.trim().isEmpty() && Integer.parseInt(line.split(",")[0]) != userId) {
                    updatedLines.add(line);
                }
            }
            Files.write(notificationFilePath, updatedLines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> getAllNotifications()
    {
    	List<Notification> lin = new ArrayList<>();
    	try {
            for (String line : Files.readAllLines(notificationFilePath)) {
                if (!line.trim().isEmpty()) {
                	String[] parts = line.split(",", 3);
                	lin.add(new Notification(Integer.parseInt(parts[0]), parts[2], LocalDateTime.parse(parts[1], FORMATTER)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lin;
    }
}
