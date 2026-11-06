package TestSupport;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.rules.ExternalResource;

import ReportDashboardSubSystem.ConsoleInput;
import ReportDashboardSubSystem.Screen;
import ReportDashboardSubSystem.Terminal;
import UserMangerSubSystem.ConfigLoader;
import UserMangerSubSystem.CurrentApplicationUser;

/*
 * A throwaway world for one test: an empty data folder and a generated config
 * pointing at it, with no delays and a plain ASCII screen. The real Files folder
 * is never touched. Page text still comes from Files/UI, which is only read.
 */
public class TestEnvironment extends ExternalResource {

    private Path folder;
    private final Map<String, String> settings = new LinkedHashMap<>();

    @Override
    protected void before() throws IOException {
        folder = Files.createTempDirectory("event-organizer-test");
        settings.clear();
        put("app.name", "Event Management System");
        put("app.version", "1.0");
        put("app.mode", "dev");
        put("app.delay", "0");
        put("app.devStartupDelayMs", "0");
        put("app.registerDelayMs", "0");
        put("login.maxAttempts", "3");
        put("event.defaultType", "Public");
        put("event.maxCapacity", "500");
        put("event.allowCancellation", "true");
        put("ui.theme", "neon");
        put("ui.color", "off");
        put("ui.unicode", "off");
        put("ui.width", "200");
        for (String name : new String[] {"users", "events", "invites", "friends", "follow", "password", "notification", "rsvp"}) {
            put("Files." + name, path(name + ".txt"));
        }
        for (String page : new String[] {"mainpage", "eventpage", "invitepage", "notificationpage", "reportpage", "friendpage", "profilepage"}) {
            put("Files." + page, "Files/UI/" + page + ".txt");
        }
        writeConfig();
        System.setProperty(ConfigLoader.CONFIG_OVERRIDE, path("config.properties"));
        Terminal.reset();
        Screen.reset();
        CurrentApplicationUser.getInstance().logout();
        ConsoleInput.getInstance().useInput(new ByteArrayInputStream(new byte[0]));
    }

    @Override
    protected void after() {
        System.clearProperty(ConfigLoader.CONFIG_OVERRIDE);
        Terminal.reset();
        Screen.reset();
        CurrentApplicationUser.getInstance().logout();
        try (Stream<Path> files = Files.walk(folder)) {
            List<Path> all = new ArrayList<>();
            files.sorted(Comparator.reverseOrder()).forEach(all::add);
            for (Path file : all) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void put(String key, String value) {
        settings.put(key, value);
    }

    // Changes one setting for the rest of the test
    public void set(String key, String value) {
        put(key, value);
        try {
            writeConfig();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        Terminal.reset();
    }

    public void remove(String key) {
        settings.remove(key);
        try {
            writeConfig();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        Terminal.reset();
    }

    // Absolute path of a file inside the throwaway folder
    public String path(String name) {
        return folder.resolve(name).toString().replace('\\', '/');
    }

    public List<String> lines(String name) throws IOException {
        Path file = folder.resolve(name);
        return Files.exists(file) ? Files.readAllLines(file) : new ArrayList<>();
    }

    private void writeConfig() throws IOException {
        List<String> lines = new ArrayList<>();
        settings.forEach((key, value) -> lines.add(key + "=" + value));
        Files.write(folder.resolve("config.properties"), lines);
    }
}
