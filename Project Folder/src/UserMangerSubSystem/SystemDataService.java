package UserMangerSubSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SystemDataService {

	// Reports how many records each data file currently holds
	public String loadInitialData() {
		ConfigLoader configLoader = new ConfigLoader();
		StringBuilder summary = new StringBuilder();
		for(String name : new String[] {"events", "invites", "friends", "follow", "notification", "rsvp"}) {
			summary.append(name).append("=").append(countRecords(configLoader.getProperty("Files." + name))).append(" ");
		}
		return summary.toString().trim();
	}

	private long countRecords(String filePath) {
		Path path = Paths.get(filePath);
		if(!Files.exists(path)) {
			return 0;
		}
		try {
			return Files.readAllLines(path).stream().filter(line -> !line.trim().isEmpty()).count();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
}
