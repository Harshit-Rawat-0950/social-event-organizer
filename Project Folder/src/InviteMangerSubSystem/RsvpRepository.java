package InviteMangerSubSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * One response per user per event, stored as eventId,userId,STATUS
 */
public class RsvpRepository {
	private final Path filePath;

	public RsvpRepository(String filePath) {
		this.filePath = Paths.get(filePath);
		try {
			if (this.filePath.getParent() != null) {
				Files.createDirectories(this.filePath.getParent());
			}
			if (!Files.exists(this.filePath)) {
				Files.createFile(this.filePath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setResponse(int eventId, int userId, RsvpStatus status) {
		String prefix = eventId + "," + userId + ",";
		List<String> updated = new ArrayList<>();
		for (String line : readLines()) {
			if (!line.startsWith(prefix)) {
				updated.add(line);
			}
		}
		updated.add(prefix + status);
		try {
			Files.write(filePath, updated);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// userId -> response, in the order they were recorded
	public Map<Integer, RsvpStatus> getResponses(int eventId) {
		Map<Integer, RsvpStatus> responses = new LinkedHashMap<>();
		for (String line : readLines()) {
			String[] parts = line.split(",");
			if (Integer.parseInt(parts[0]) == eventId) {
				responses.put(Integer.parseInt(parts[1]), RsvpStatus.valueOf(parts[2]));
			}
		}
		return responses;
	}

	public Map<RsvpStatus, Integer> getCounts(int eventId) {
		Map<RsvpStatus, Integer> counts = new EnumMap<>(RsvpStatus.class);
		for (RsvpStatus status : RsvpStatus.values()) {
			counts.put(status, 0);
		}
		for (RsvpStatus status : getResponses(eventId).values()) {
			counts.merge(status, 1, Integer::sum);
		}
		return counts;
	}

	private List<String> readLines() {
		List<String> lines = new ArrayList<>();
		try {
			for (String line : Files.readAllLines(filePath)) {
				if (!line.trim().isEmpty()) {
					lines.add(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
}
