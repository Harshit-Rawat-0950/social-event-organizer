package ReportDashboardSubSystem;

/*
 * Every decorative character, with an ASCII stand-in. The fancy ones are all
 * drawn by the Windows console fonts. They are written as escapes so the source
 * compiles under any editor encoding.
 */
public enum Glyph {
    DOUBLE_TOP_LEFT("\u2554", "+"),
    DOUBLE_TOP_RIGHT("\u2557", "+"),
    DOUBLE_BOTTOM_LEFT("\u255A", "+"),
    DOUBLE_BOTTOM_RIGHT("\u255D", "+"),
    DOUBLE_HORIZONTAL("\u2550", "="),
    DOUBLE_VERTICAL("\u2551", "|"),
    TOP_LEFT("\u250C", "+"),
    TOP_RIGHT("\u2510", "+"),
    BOTTOM_LEFT("\u2514", "+"),
    BOTTOM_RIGHT("\u2518", "+"),
    HORIZONTAL("\u2500", "-"),
    VERTICAL("\u2502", "|"),
    TREE_BRANCH("\u251C\u2500 ", "|- "),
    TREE_LAST("\u2514\u2500 ", "`- "),
    TREE_PIPE("\u2502  ", "|  "),
    FULL_BLOCK("\u2588", "#"),
    LIGHT_BLOCK("\u2591", "-"),
    PROMPT("\u25BA", ">"),
    BULLET("\u2022", "-"),
    OK("\u221A", "[ok]"),
    FAIL("\u203C", "[!]"),
    INFO("\u25BA", "[i]"),
    USER("\u263B", "*"),
    CRUMB("\u00BB", ">"),
    MUSIC("\u266B", "Band:"),
    NEWS("\u00A7", "News:"),
    OUTING("\u263C", "Outing:"),
    BUSINESS("\u2666", "Business:"),
    URGENT("\u25B2", "!");

    private final String fancy;
    private final String plain;

    Glyph(String fancy, String plain) {
        this.fancy = fancy;
        this.plain = plain;
    }

    public String fancy() {
        return fancy;
    }

    public String plain() {
        return plain;
    }

    @Override
    public String toString() {
        return Terminal.get().isUnicode() ? fancy : plain;
    }
}
