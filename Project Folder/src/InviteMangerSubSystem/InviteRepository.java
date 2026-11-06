package InviteMangerSubSystem;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class InviteRepository implements InviteRepo {
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
        	e.printStackTrace();
        }
    }

    // Appends a single Invite to the text file
    @Override
    public void saveInvite(Invite invite) {
        try {
            // Converts the invite to a string, adds a newline, and writes it as bytes
            Files.write(filePath, (invite.toString() + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }

    // Reads all lines from the text file (useful for checking expired invites later)
    @Override
    public List<String> getAllRawInvites() {
        List<String> rawInvites = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    rawInvites.add(line);
                }
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }
        return rawInvites;
    }

    // Returns false when the invite was not in the file
    @Override
    public boolean removeInvite(String rawInvite) {
        List<String> lines = getAllRawInvites();
        boolean removed = lines.remove(rawInvite);
        if (removed) {
            try {
                Files.write(filePath, lines);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return removed;
    }
}
