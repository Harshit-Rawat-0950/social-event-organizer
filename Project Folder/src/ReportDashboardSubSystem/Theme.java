package ReportDashboardSubSystem;

import java.util.EnumMap;
import java.util.Map;

// Colour palettes, as xterm 256 colour numbers. Switch with ui.theme in the config.
public enum Theme {
    NEON(new int[] {201, 171, 141, 111, 81, 51},
            51, 201, 213, 255, 245, 48, 197, 220, 81, 207, 75, 120, 203, 214),
    AMBER(new int[] {226, 220, 214, 208, 202, 166},
            220, 172, 214, 223, 137, 148, 196, 226, 180, 215, 222, 186, 202, 178),
    OCEAN(new int[] {27, 33, 39, 38, 37, 44},
            255, 33, 44, 253, 67, 79, 210, 222, 117, 147, 75, 115, 209, 180);

    public enum Role {
        TITLE, FRAME, ACCENT, TEXT, MUTED, SUCCESS, ERROR, WARNING, INFO, MUSIC, NEWS, OUTING, URGENT, BUSINESS
    }

    private final int[] logoGradient;
    private final Map<Role, Integer> colours = new EnumMap<>(Role.class);

    Theme(int[] logoGradient, int... roleColours) {
        this.logoGradient = logoGradient;
        Role[] roles = Role.values();
        for (int i = 0; i < roles.length; i++) {
            colours.put(roles[i], roleColours[i]);
        }
    }

    public int colour(Role role) {
        return colours.get(role);
    }

    // One colour per line of the logo
    public int[] getLogoGradient() {
        return logoGradient.clone();
    }

    // An unknown name falls back to the default theme
    public static Theme fromName(String name) {
        for (Theme theme : values()) {
            if (theme.name().equalsIgnoreCase(name == null ? "" : name.trim())) {
                return theme;
            }
        }
        return NEON;
    }
}
