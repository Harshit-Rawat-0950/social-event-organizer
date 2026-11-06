package ReportDashboardSubSystem;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import EventManagementSubSystem.EventView;
import ReportDashboardSubSystem.Theme.Role;

// Draws an event, its decorations and its sub-events as one card
public final class EventCardRenderer {

    private static final Role[] RSVP_ROLES = {Role.SUCCESS, Role.WARNING, Role.INFO, Role.ERROR};

    private EventCardRenderer() {
    }

    public static List<String> render(EventView event) {
        String title = "#" + event.id + " " + event.name;
        String styledTitle = event.cancelled ? Style.strike(Role.MUTED, title) : Style.bold(Role.TITLE, title);
        String status = event.cancelled ? Style.color(Role.ERROR, "CANCELLED") : countdown(event.date);
        String right = Style.color(Role.ACCENT, event.visibility.label())
                + (status.isEmpty() ? "" : Style.color(Role.MUTED, " " + Glyph.BULLET + " ") + status);

        Role textRole = event.cancelled ? Role.MUTED : Role.TEXT;
        int inner = Widgets.innerWidth();
        List<String> lines = new ArrayList<>();
        for (String line : Style.wrap(event.date + " " + event.time + " @ " + event.location + " " + Glyph.BULLET + " " + event.type, inner)) {
            lines.add(Style.color(textRole, line));
        }
        for (String line : Style.wrap(event.description, inner)) {
            lines.add(Style.color(Role.MUTED, line));
        }
        lines.addAll(tagLines(event, "", inner));
        addChildren(event, "", lines);
        if (event.hasRsvp()) {
            lines.addAll(rsvpLines(event, inner));
        }
        return Widgets.box(styledTitle, right, lines, event.cancelled ? Role.MUTED : Role.FRAME);
    }

    private static void addChildren(EventView parent, String prefix, List<String> lines) {
        int inner = Widgets.innerWidth();
        for (int i = 0; i < parent.children.size(); i++) {
            EventView child = parent.children.get(i);
            boolean last = i == parent.children.size() - 1;
            String branch = prefix + (last ? Glyph.TREE_LAST : Glyph.TREE_BRANCH);
            String below = prefix + (last ? "   " : Glyph.TREE_PIPE.toString());
            String head = "#" + child.id + " " + child.name + (child.cancelled ? " [CANCELLED]" : "");
            String when = child.date + " " + child.time + " @ " + child.location;
            Role role = child.cancelled ? Role.MUTED : Role.TITLE;
            String frame = Style.color(Role.MUTED, branch);
            if (branch.length() + head.length() + 2 + when.length() <= inner) {
                lines.add(frame + Style.color(role, head) + "  " + Style.color(Role.MUTED, when));
            } else {
                lines.add(frame + Style.color(role, head));
                for (String line : Style.wrap(when, inner - below.length())) {
                    lines.add(Style.color(Role.MUTED, below) + Style.color(Role.MUTED, line));
                }
            }
            lines.addAll(tagLines(child, below, inner));
            addChildren(child, below, lines);
        }
    }

    // Tags flow along a line and carry on to the next when they run out of room
    private static List<String> tagLines(EventView event, String prefix, int inner) {
        List<String> lines = new ArrayList<>();
        String styledPrefix = prefix.isEmpty() ? "" : Style.color(Role.MUTED, prefix);
        String current = "";
        int room = inner - prefix.length();
        for (EventView.Tag tag : event.tags) {
            Role role = roleOf(tag);
            for (String piece : Style.wrap(tagText(tag), room)) {
                int needed = Style.visibleLength(current) + (current.isEmpty() ? 0 : 3) + piece.length();
                if (!current.isEmpty() && needed > room) {
                    lines.add(styledPrefix + current);
                    current = "";
                }
                current += (current.isEmpty() ? "" : "   ") + Style.color(role, piece);
            }
        }
        if (!current.isEmpty()) {
            lines.add(styledPrefix + current);
        }
        return lines;
    }

    static String tagText(EventView.Tag tag) {
        if (!Terminal.get().isUnicode()) {
            return "[" + tag.label + "]";
        }
        switch (tag.kind) {
            case MUSIC:
                return Glyph.MUSIC + " " + tag.value;
            case NEWS:
                return Glyph.NEWS + " " + tag.value;
            case OUTING:
                return Glyph.OUTING + " " + tag.value + " " + "*".repeat(tag.level);
            case BUSINESS:
                return Glyph.BUSINESS + " " + tag.value;
            case URGENT:
                return Glyph.URGENT.toString().repeat(tag.level) + Glyph.LIGHT_BLOCK.toString().repeat(5 - tag.level) + " Urgent";
            default:
                return tag.label;
        }
    }

    private static Role roleOf(EventView.Tag tag) {
        switch (tag.kind) {
            case MUSIC:
                return Role.MUSIC;
            case NEWS:
                return Role.NEWS;
            case OUTING:
                return Role.OUTING;
            case BUSINESS:
                return Role.BUSINESS;
            default:
                return Role.URGENT;
        }
    }

    private static List<String> rsvpLines(EventView event, int inner) {
        String text = rsvpText(event.going, event.maybe, event.waiting, event.declined);
        int[] values = {event.going, event.maybe, event.waiting, event.declined};
        List<String> lines = new ArrayList<>();
        int barWidth = Math.min(16, inner / 3);
        if (barWidth + 2 + text.length() <= inner) {
            lines.add(Widgets.stackedBar(values, RSVP_ROLES, barWidth) + "  " + Style.color(Role.TEXT, text));
        } else {
            lines.add(Widgets.stackedBar(values, RSVP_ROLES, barWidth));
            for (String line : Style.wrap(text, inner)) {
                lines.add(Style.color(Role.TEXT, line));
            }
        }
        return lines;
    }

    public static String rsvpText(int going, int maybe, int waiting, int declined) {
        String separator = " " + Glyph.BULLET + " ";
        return going + " going" + separator + maybe + " maybe" + separator + waiting + " waiting" + separator + declined + " declined";
    }

    static String countdown(String date) {
        long days;
        try {
            days = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(date));
        } catch (DateTimeParseException e) {
            return "";
        }
        if (days < 0) {
            long ago = -days;
            return Style.color(Role.MUTED, ago == 1 ? "yesterday" : ago + " days ago");
        }
        if (days == 0) {
            return Style.color(Role.URGENT, "today");
        }
        if (days == 1) {
            return Style.color(Role.WARNING, "tomorrow");
        }
        return Style.color(days <= 7 ? Role.WARNING : Role.SUCCESS, "in " + days + " days");
    }
}
