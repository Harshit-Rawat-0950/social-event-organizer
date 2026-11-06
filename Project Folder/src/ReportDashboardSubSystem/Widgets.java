package ReportDashboardSubSystem;

import java.util.ArrayList;
import java.util.List;

import ReportDashboardSubSystem.Theme.Role;

// Builds decorated blocks of text. Everything returns lines and nothing prints.
public final class Widgets {

    // Written with ASCII stand-ins: X block, { } [ ] corners, = horizontal, ! vertical
    private static final String[] LOGO = {
        " XXXXXXX}XX}   XX}XXXXXXX}XXX}   XX}XXXXXXXX}XXXXXXX}",
        " XX{====]XX!   XX!XX{====]XXXX}  XX![==XX{==]XX{====]",
        " XXXXX}  XX!   XX!XXXXX}  XX{XX} XX!   XX!   XXXXXXX}",
        " XX{==]  [XX} XX{]XX{==]  XX![XX}XX!   XX!   [====XX!",
        " XXXXXXX} [XXXX{] XXXXXXX}XX! [XXXX!   XX!   XXXXXXX!",
        " [======]  [===]  [======][=]  [===]   [=]   [======]",
    };

    private Widgets() {
    }

    // Empty on terminals that cannot draw the block characters
    public static List<String> logo() {
        List<String> lines = new ArrayList<>();
        if (!Terminal.get().isUnicode()) {
            return lines;
        }
        int[] gradient = Terminal.get().getTheme().getLogoGradient();
        for (int i = 0; i < LOGO.length; i++) {
            lines.add(Style.colorCode(gradient[i % gradient.length], LOGO[i]
                    .replace("X", Glyph.FULL_BLOCK.fancy())
                    .replace("{", Glyph.DOUBLE_TOP_LEFT.fancy())
                    .replace("}", Glyph.DOUBLE_TOP_RIGHT.fancy())
                    .replace("[", Glyph.DOUBLE_BOTTOM_LEFT.fancy())
                    .replace("]", Glyph.DOUBLE_BOTTOM_RIGHT.fancy())
                    .replace("=", Glyph.DOUBLE_HORIZONTAL.fancy())
                    .replace("!", Glyph.DOUBLE_VERTICAL.fancy())));
        }
        return lines;
    }

    public static int width() {
        return Terminal.get().getWidth();
    }

    // Space for text inside a box
    public static int innerWidth() {
        return width() - 4;
    }

    public static List<String> titleBar(String title) {
        int inner = width() - 2;
        String frameBar = Glyph.DOUBLE_HORIZONTAL.toString().repeat(inner);
        String side = Style.color(Role.FRAME, Glyph.DOUBLE_VERTICAL.toString());
        String text = Style.truncate("  " + title.toUpperCase(), inner);
        int plainLength = text.length();
        List<String> lines = new ArrayList<>();
        lines.add(Style.color(Role.FRAME, Glyph.DOUBLE_TOP_LEFT + frameBar + Glyph.DOUBLE_TOP_RIGHT));
        lines.add(side + Style.bold(Role.TITLE, text) + " ".repeat(inner - plainLength) + side);
        lines.add(Style.color(Role.FRAME, Glyph.DOUBLE_BOTTOM_LEFT + frameBar + Glyph.DOUBLE_BOTTOM_RIGHT));
        return lines;
    }

    // title and right sit in the top border and may be null. Long lines are cut to fit.
    public static List<String> box(String title, String right, List<String> body, Role frame) {
        List<String> lines = new ArrayList<>();
        lines.add(topBorder(title, right, frame));
        String side = Style.color(frame, Glyph.VERTICAL.toString());
        for (String entry : body) {
            for (String line : entry.split("\n", -1)) {
                lines.add(side + " " + Style.pad(Style.truncate(line, innerWidth()), innerWidth()) + " " + side);
            }
        }
        lines.add(Style.color(frame, Glyph.BOTTOM_LEFT + Glyph.HORIZONTAL.toString().repeat(width() - 2) + Glyph.BOTTOM_RIGHT));
        return lines;
    }

    private static String topBorder(String title, String right, Role frame) {
        String dash = Glyph.HORIZONTAL.toString();
        String rightPart = right == null ? "" : " " + Style.truncate(right, width() / 2) + " ";
        // Corners plus at least one dash take 3 columns
        int room = width() - 3 - Style.visibleLength(rightPart);
        String titlePart = title == null ? "" : " " + Style.truncate(title, Math.max(1, room - 2)) + " ";
        int fill = room - Style.visibleLength(titlePart) + 1;
        return Style.color(frame, Glyph.TOP_LEFT.toString()) + titlePart
                + Style.color(frame, dash.repeat(fill)) + rightPart
                + Style.color(frame, Glyph.TOP_RIGHT.toString());
    }

    // Columns size themselves to their widest cell. Cells may be coloured.
    public static List<String> table(String[] headers, List<String[]> rows) {
        int[] widths = new int[headers.length];
        for (int c = 0; c < headers.length; c++) {
            widths[c] = headers[c].length();
            for (String[] row : rows) {
                widths[c] = Math.max(widths[c], Style.visibleLength(row[c]));
            }
        }
        List<String> lines = new ArrayList<>();
        String[] styledHeaders = new String[headers.length];
        String[] rules = new String[headers.length];
        for (int c = 0; c < headers.length; c++) {
            styledHeaders[c] = Style.bold(Role.MUTED, headers[c]);
            rules[c] = Style.color(Role.MUTED, Glyph.HORIZONTAL.toString().repeat(widths[c]));
        }
        lines.add(tableRow(styledHeaders, widths));
        lines.add(tableRow(rules, widths));
        for (String[] row : rows) {
            lines.add(tableRow(row, widths));
        }
        return lines;
    }

    private static String tableRow(String[] cells, int[] widths) {
        StringBuilder line = new StringBuilder(" ");
        for (int c = 0; c < cells.length; c++) {
            line.append(c == cells.length - 1 ? cells[c] : Style.pad(cells[c], widths[c]) + "   ");
        }
        return Style.truncate(line.toString(), width());
    }

    // value out of max, always showing at least one block for a non-zero value
    public static String meter(int value, int max, int barWidth, Role role) {
        int filled = max <= 0 ? 0 : Math.min(barWidth, (int) Math.round((double) value * barWidth / max));
        if (value > 0 && filled == 0 && max > 0) {
            filled = 1;
        }
        return Style.color(role, Glyph.FULL_BLOCK.toString().repeat(filled))
                + Style.color(Role.MUTED, Glyph.LIGHT_BLOCK.toString().repeat(barWidth - filled));
    }

    // One bar split between several values. Always exactly barWidth wide.
    public static String stackedBar(int[] values, Role[] roles, int barWidth) {
        int total = 0;
        for (int value : values) {
            total += value;
        }
        StringBuilder bar = new StringBuilder();
        int used = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == 0) {
                continue;
            }
            int blocks = Math.max(1, (int) Math.round((double) values[i] * barWidth / total));
            blocks = Math.min(blocks, barWidth - used);
            bar.append(Style.color(roles[i], Glyph.FULL_BLOCK.toString().repeat(blocks)));
            used += blocks;
        }
        return bar + Style.color(Role.MUTED, Glyph.LIGHT_BLOCK.toString().repeat(barWidth - used));
    }

    public static String badge(String text, Role role) {
        return Style.color(role, "[" + text + "]");
    }
}
