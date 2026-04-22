package InviteMangerSubSystem;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class InviteRepository {
    private final Path filePath;

    // Constructor requires the file path (e.g., "data/invites.txt")
    public InviteRepository(String filePath) {
        this.filePath = Paths.get(filePath);
        createFileIfNotExists();
    }

    // Ensures the directory and file exist before we try to write to them
    private void createFileIfNotExists() {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
        	
        }
    }

    // Appends a single Invite to the text file
    public void saveInvite(FollowerRequest request) {
        try {
            // Converts the invite to a string, adds a newline, and writes it as bytes
            Files.write(filePath, (request.toString() + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
        	
        }
    }

    // Reads all lines from the text file (useful for checking expired request later)
    public List<String> getAllRawRequest() {
        List<String> rawRequest = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    rawRequest.add(line);
                }
            }
        } catch (IOException e) {

        }
        return rawRequest;
    }
}