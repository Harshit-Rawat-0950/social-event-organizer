package ReportDashboardSubSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ReportDashboardSubSystem.Theme.Role;

// Colouring, and text measuring that ignores the invisible colour codes
public final class Style {

    private static final Pattern ANSI = Pattern.compile("\033\\[[0-9;]*m");
    private static final String RESET = "\033[0m";

    private Style() {
    }

    public static String color(Role role, String text) {
        return wrapCodes(foreground(role), text);
    }

    public static String bold(Role role, String text) {
        return wrapCodes("1;" + foreground(role), text);
    }

    public static String strike(Role role, String text) {
        return wrapCodes("9;" + foreground(role), text);
    }

    public static String colorCode(int xtermColour, String text) {
        return wrapCodes("38;5;" + xtermColour, text);
    }

    private static String foreground(Role role) {
        return "38;5;" + Terminal.get().getTheme().colour(role);
    }

    private static String wrapCodes(String codes, String text) {
        if (!Terminal.get().isColor() || text.isEmpty()) {
            return text;
        }
        return "\033[" + codes + "m" + text + RESET;
    }

    public static String strip(String text) {
        return ANSI.matcher(text).replaceAll("");
    }

    public static int visibleLength(String text) {
        return strip(text).length();
    }

    public static String pad(String text, int width) {
        int missing = width - visibleLength(text);
        return missing > 0 ? text + " ".repeat(missing) : text;
    }

    // Shortens to the visible width with a ".." marker, keeping colour codes intact
    public static String truncate(String text, int width) {
        if (visibleLength(text) <= width) {
            return text;
        }
        int keep = Math.max(0, width - 2);
        StringBuilder result = new StringBuilder();
        int visible = 0;
        int i = 0;
        boolean coloured = false;
        while (i < text.length() && visible < keep) {
            if (text.charAt(i) == '\033') {
                int end = text.indexOf('m', i);
                result.append(text, i, end + 1);
                coloured = true;
                i = end + 1;
            } else {
                result.append(text.charAt(i));
                visible++;
                i++;
            }
        }
        result.append("..".substring(0, Math.min(2, width)));
        if (coloured) {
            result.append(RESET);
        }
        return result.toString();
    }

    // Word wraps plain text. Words longer than a line are split.
    public static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            String rest = word;
            while (rest.length() > width) {
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                lines.add(rest.substring(0, width));
                rest = rest.substring(width);
            }
            if (line.length() > 0 && line.length() + 1 + rest.length() > width) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) {
                line.append(' ');
            }
            line.append(rest);
        }
        if (line.length() > 0 || lines.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }
}
