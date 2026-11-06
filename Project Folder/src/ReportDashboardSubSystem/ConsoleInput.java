package ReportDashboardSubSystem;

import java.io.Console;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/*
 * The one Scanner over System.in for the whole application. Closing a Scanner
 * closes System.in with it, so individual pages must never create their own.
 */
public class ConsoleInput {

    private static ConsoleInput instance;
    private Scanner scanner = new Scanner(System.in);
    private boolean readingSystemIn = true;

    private ConsoleInput() {
    }

    public static synchronized ConsoleInput getInstance() {
        if (instance == null) {
            instance = new ConsoleInput();
        }
        return instance;
    }

    // Reads from the given source from now on instead of the keyboard
    public void useInput(InputStream source) {
        this.scanner = new Scanner(source);
        this.readingSystemIn = false;
    }

    // True when the replaced input source still has unread lines
    public boolean hasMoreInput() {
        return !readingSystemIn && scanner.hasNextLine();
    }

    // Returns the line exactly as typed (passwords may contain spaces)
    public String readLine(String prompt) {
        Screen.flush();
        System.out.print(Screen.stylePrompt(prompt));
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine();
    }

    public int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt).trim());
            } catch (NumberFormatException e) {
                Screen.error("Please enter a whole number.");
            }
        }
    }

    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            Screen.error("Please enter a number from " + min + " to " + max + ".");
        }
    }

    // First character in lower case, or a space for an empty line
    public char readChoice(String prompt) {
        String line = readLine(prompt).trim();
        return line.isEmpty() ? ' ' : Character.toLowerCase(line.charAt(0));
    }

    // A value that gets stored in a CSV file: required and free of separators
    public String readField(String prompt) {
        while (true) {
            String value = readLine(prompt).trim();
            if (value.isEmpty()) {
                Screen.error("This field cannot be empty.");
            } else if (!isStorable(value)) {
                Screen.error("Please avoid these characters: , ; |");
            } else {
                return value;
            }
        }
    }

    public String readFieldOrDefault(String prompt, String defaultValue) {
        while (true) {
            String value = readLine(prompt + " [" + defaultValue + "]: ").trim();
            if (value.isEmpty()) {
                return defaultValue;
            }
            if (isStorable(value)) {
                return value;
            }
            Screen.error("Please avoid these characters: , ; |");
        }
    }

    // Hidden while typing when running in a real console
    public String readSecret(String prompt) {
        Console console = readingSystemIn ? System.console() : null;
        if (console == null) {
            return readLine(prompt);
        }
        Screen.flush();
        char[] typed = console.readPassword("%s", Screen.stylePrompt(prompt));
        if (typed == null) {
            throw new InputClosedException();
        }
        return new String(typed);
    }

    public String readPassword(String prompt) {
        while (true) {
            String value = readSecret(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            Screen.error("Password cannot be empty.");
        }
    }

    public String readDate(String prompt) {
        while (true) {
            String value = readLine(prompt).trim();
            try {
                return LocalDate.parse(value).toString();
            } catch (DateTimeParseException e) {
                Screen.error("Please use the format yyyy-MM-dd, for example 2026-12-31.");
            }
        }
    }

    // Today or later
    public String readFutureDate(String prompt) {
        while (true) {
            String value = readDate(prompt);
            if (!LocalDate.parse(value).isBefore(LocalDate.now())) {
                return value;
            }
            Screen.error("The date cannot be in the past.");
        }
    }

    public String readTime(String prompt) {
        while (true) {
            String value = readLine(prompt).trim();
            try {
                return LocalTime.parse(value).toString();
            } catch (DateTimeParseException e) {
                Screen.error("Please use the 24 hour format HH:mm, for example 19:30.");
            }
        }
    }

    private static boolean isStorable(String value) {
        return !value.contains(",") && !value.contains(";") && !value.contains("|");
    }
}
