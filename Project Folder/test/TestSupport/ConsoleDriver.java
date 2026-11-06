package TestSupport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ReportDashboardSubSystem.ConsoleInput;
import ReportDashboardSubSystem.InputClosedException;
import ReportDashboardSubSystem.Screen;
import UserMangerSubSystem.AppStartUp;

/*
 * Types into the application and returns what it printed. A script that runs
 * short or has lines left over fails with the transcript, so a changed menu
 * flow cannot go unnoticed.
 */
public final class ConsoleDriver {

    private ConsoleDriver() {
    }

    // Runs the whole application from the login screen
    public static String app(String... keys) {
        return call(() -> AppStartUp.getInstance().startApplication(), keys);
    }

    // Logs in, types the keys from the main menu, and logs out
    public static String session(int userId, String... keys) {
        List<String> all = new ArrayList<>();
        all.add(String.valueOf(userId));
        all.add(Fixtures.password(userId));
        all.addAll(Arrays.asList(keys));
        all.add("0");
        return app(all.toArray(new String[0]));
    }

    // Runs one action with scripted input and returns its output, messages included
    public static String call(Runnable action, String... keys) {
        String script = keys.length == 0 ? "" : String.join("\n", keys) + "\n";
        ConsoleInput input = ConsoleInput.getInstance();
        input.useInput(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            action.run();
            Screen.flush();
        } catch (InputClosedException e) {
            throw new AssertionError("The script ran out of input. Output so far:\n" + text(captured), e);
        } finally {
            System.setOut(original);
        }
        if (input.hasMoreInput()) {
            throw new AssertionError("The script had unused input. Output:\n" + text(captured));
        }
        return text(captured);
    }

    // Like call, but the input is allowed to run out. Returns the output up to that point.
    public static String callUntilInputEnds(Runnable action, String... keys) {
        try {
            return call(action, keys);
        } catch (AssertionError e) {
            if (e.getCause() instanceof InputClosedException) {
                return e.getMessage();
            }
            throw e;
        }
    }

    private static String text(ByteArrayOutputStream captured) {
        return new String(captured.toByteArray(), StandardCharsets.UTF_8);
    }
}
