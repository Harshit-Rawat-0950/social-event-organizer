package UserMangerSubSystem;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
public class UserRepository {
    private final Path userFilePath;

    public UserRepository(String filePath) {
        this.userFilePath = Paths.get(filePath);
        createFileIfNotExists();
    }

    public void createFileIfNotExists() {
        try {
            if (userFilePath.getParent() != null) {
                Files.createDirectories(userFilePath.getParent());
            }
            if (!Files.exists(userFilePath)) {
                Files.createFile(userFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addUser(User user) {
        try {
            Files.write(userFilePath,(user.toString() + "\n").getBytes(),StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(userFilePath);
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    users.add(User.fromString(line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    public User findUserById(int id) {
        for (User user : getAllUsers()) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    public void updateUser(User updatedUser) {
        List<User> users = getAllUsers();
        try {
            List<String> updatedLines = new ArrayList<>();
            for (User user : users) {
                if (user.getId() == updatedUser.getId()) {
                    updatedLines.add(updatedUser.toString());
                } else {
                    updatedLines.add(user.toString());
                }
            }
            Files.write(userFilePath, updatedLines, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteUserById(int id) {
        List<User> users = getAllUsers();
        try {
            List<String> updatedLines = new ArrayList<>();
            for (User user : users) {
                if (user.getId() != id) {
                    updatedLines.add(user.toString());
                }
            }
            Files.write(userFilePath, updatedLines, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int countUsers() {
        return getAllUsers().size();
    }

	public String getUserData() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}