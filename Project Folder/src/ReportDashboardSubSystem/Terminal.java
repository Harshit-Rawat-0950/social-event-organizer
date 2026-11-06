package ReportDashboardSubSystem;

import java.io.IOException;

import UserMangerSubSystem.ConfigLoader;

/*
 * Works out once what the screen can show. ui.color and ui.unicode take auto,
 * on or off. Auto means fancy output only in a real console, so pipes and the
 * Eclipse console get clean ASCII with no escape codes.
 */
public final class Terminal {

    private static final int MIN_WIDTH = 40;
    private static final int MAX_WIDTH = 300;
    private static Terminal current;

    private final boolean color;
    private final boolean unicode;
    private final int width;
    private final Theme theme;

    private Terminal(boolean color, boolean unicode, int width, Theme theme) {
        this.color = color;
        this.unicode = unicode;
        this.width = width;
        this.theme = theme;
    }

    public static synchronized Terminal get() {
        if (current == null) {
            current = detect();
        }
        return current;
    }

    // Makes the next get() read the configuration again
    public static synchronized void reset() {
        current = null;
    }

    private static Terminal detect() {
        ConfigLoader config = new ConfigLoader();
        boolean console = System.console() != null;
        boolean color = isOn(config.getProperty("ui.color", "auto"), console && System.getenv("NO_COLOR") == null);
        boolean unicode = isOn(config.getProperty("ui.unicode", "auto"),
                console && canShowGlyphs(System.out.charset()));
        int width = clampWidth(config.getIntProperty("ui.width", 78));
        if (color && console) {
            enableEscapeCodes();
        }
        return new Terminal(color, unicode, width, Theme.fromName(config.getProperty("ui.theme", "neon")));
    }

    // Some consoles use a character set that lacks the symbols. Those get ASCII.
    static boolean canShowGlyphs(java.nio.charset.Charset charset) {
        java.nio.charset.CharsetEncoder encoder = charset.newEncoder();
        for (Glyph glyph : Glyph.values()) {
            if (!encoder.canEncode(glyph.fancy())) {
                return false;
            }
        }
        return true;
    }

    static boolean isOn(String setting, boolean autoValue) {
        String value = setting.trim();
        if (value.equalsIgnoreCase("on")) {
            return true;
        }
        if (value.equalsIgnoreCase("off")) {
            return false;
        }
        return autoValue;
    }

    static int clampWidth(int width) {
        return Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, width));
    }

    // The older Windows console only honours escape codes after cmd has run in it
    private static void enableEscapeCodes() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("windows")) {
            return;
        }
        try {
            new ProcessBuilder("cmd", "/c", "").inheritIO().start().waitFor();
        } catch (IOException e) {
            // Colours may show as raw codes. ui.color=off turns them off.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isColor() {
        return color;
    }

    public boolean isUnicode() {
        return unicode;
    }

    public int getWidth() {
        return width;
    }

    public Theme getTheme() {
        return theme;
    }

    public void clear() {
        if (color) {
            System.out.print("\033[2J\033[3J\033[H");
            System.out.flush();
        }
    }
}
