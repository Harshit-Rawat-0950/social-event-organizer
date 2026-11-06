package ReportDashboardSubSystem;

import java.util.ArrayList;
import java.util.List;

import ReportDashboardSubSystem.Theme.Role;
import UserMangerSubSystem.User;
import UserMangerSubSystem.UserManagementFacade;

/*
 * The frame around every page. Status messages are held back and shown just
 * above the next prompt, so a screen redraw can never wipe them away.
 */
public final class Screen {

    private static final List<String> PENDING = new ArrayList<>();

    private Screen() {
    }

    // Clears the screen and draws the header and the first element of the page text
    public static void open(String crumb, List<TextElement> page) {
        Terminal.get().clear();
        header(crumb);
        if (!page.isEmpty()) {
            page.get(0).display();
        }
    }

    // Draws the remaining elements of the page text, which are its menu
    public static void menu(List<TextElement> page) {
        if (page.size() > 1) {
            TextElement.displayList(page.subList(1, page.size()));
        }
    }

    private static void header(String crumb) {
        User user = UserManagementFacade.getCurrUser();
        String left = user == null ? "" : Glyph.USER + " " + user.getName() + " (" + user.getId() + ") "
                + Glyph.BULLET + " " + user.getRole();
        String right = "Home" + (crumb.isEmpty() ? "" : " " + Glyph.CRUMB + " " + crumb);
        int gap = Math.max(1, Widgets.width() - left.length() - right.length() - 1);
        System.out.println(" " + Style.color(Role.ACCENT, left) + " ".repeat(gap) + Style.color(Role.MUTED, right));
    }

    public static void println(String line) {
        System.out.println(line);
    }

    public static void println(List<String> lines) {
        for (String line : lines) {
            System.out.println(line);
        }
    }

    public static void section(String title) {
        System.out.println(" " + Style.bold(Role.ACCENT, title));
    }

    public static void empty(String text) {
        System.out.println("   " + Style.color(Role.MUTED, text));
    }

    public static void success(String message) {
        PENDING.add(" " + Style.color(Role.SUCCESS, Glyph.OK + " " + message));
    }

    public static void error(String message) {
        PENDING.add(" " + Style.color(Role.ERROR, Glyph.FAIL + " " + message));
    }

    public static void info(String message) {
        PENDING.add(" " + Style.color(Role.INFO, Glyph.INFO + " " + message));
    }

    public static synchronized void flush() {
        for (String message : PENDING) {
            System.out.println(message);
        }
        PENDING.clear();
    }

    // Forgets undelivered messages
    public static synchronized void reset() {
        PENDING.clear();
    }

    static String stylePrompt(String prompt) {
        if (prompt.trim().equals(">")) {
            return " " + Style.bold(Role.ACCENT, Glyph.PROMPT.toString()) + " ";
        }
        return " " + Style.color(Role.ACCENT, prompt);
    }
}
