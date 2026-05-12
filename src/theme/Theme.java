package theme;
/**
 * Theme constants for consistent UI styling across the app.
 */
public class Theme {

    // === COLORS ===
    public static final java.awt.Color PRIMARY_BLUE = new java.awt.Color(66, 133, 244);
    public static final java.awt.Color SUCCESS_GREEN = new java.awt.Color(52, 168, 83);
    public static final java.awt.Color ACCENT_ORANGE = new java.awt.Color(255, 152, 0);

    public static final java.awt.Color BG_DARK = new java.awt.Color(32, 33, 36);
    public static final java.awt.Color BG_LIGHT = new java.awt.Color(245, 245, 245);
    public static final java.awt.Color BG_WHITE = new java.awt.Color(255, 255, 255);

    public static final java.awt.Color TEXT_PRIMARY = new java.awt.Color(33, 33, 33);
    public static final java.awt.Color TEXT_SECONDARY = new java.awt.Color(117, 117, 117);
    public static final java.awt.Color TEXT_LIGHT = new java.awt.Color(255, 255, 255);

    public static final java.awt.Color BUBBLE_SELF = new java.awt.Color(66, 133, 244);
    public static final java.awt.Color BUBBLE_OTHER = new java.awt.Color(232, 232, 232);

    // === DIMENSIONS ===
    public static final int WINDOW_WIDTH = 450;
    public static final int WINDOW_HEIGHT = 600;
    public static final int BORDER_RADIUS = 12;
    public static final int PADDING = 20;

    // === FONTS ===
    public static final java.awt.Font TITLE_FONT = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28);
    public static final java.awt.Font LABEL_FONT = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);
    public static final java.awt.Font INPUT_FONT = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15);
    public static final java.awt.Font MESSAGE_FONT = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);
    public static final java.awt.Font TIME_FONT = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11);

    // === AVATAR COLORS ===
    public static final java.awt.Color[] AVATAR_COLORS = {
        new java.awt.Color(66, 133, 244),
        new java.awt.Color(52, 168, 83),
        new java.awt.Color(255, 152, 0),
        new java.awt.Color(156, 39, 176),
        new java.awt.Color(244, 67, 54),
        new java.awt.Color(0, 188, 212)
    };

    /**
     * Get avatar color based on name hash
     */
    public static java.awt.Color getAvatarColor(String name) {
        int index = Math.abs(name.hashCode()) % AVATAR_COLORS.length;
        return AVATAR_COLORS[index];
    }

    /**
     * Get initials from name (first letter of first two words)
     */
    public static String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        }
        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    }
}