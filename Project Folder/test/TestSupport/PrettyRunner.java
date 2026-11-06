package TestSupport;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

// What test.bat runs: one coloured line per test class and a readable failure list
public final class PrettyRunner {

    private static final PrintStream OUT = System.out;
    private static boolean color;

    private PrettyRunner() {
    }

    public static void main(String[] args) throws Exception {
        color = System.console() != null;
        if (color) {
            enableEscapeCodes();
        }
        Request request = request(args);
        JUnitCore core = new JUnitCore();
        Listener listener = new Listener();
        core.addListener(listener);
        OUT.println(paint("1;38;5;51", "Running tests"));
        Result result = core.run(request);
        listener.finishClass();

        for (Failure failure : result.getFailures()) {
            OUT.println();
            OUT.println(paint("1;38;5;197", "FAILED: " + failure.getTestHeader()));
            OUT.println(shorten(failure.getTrace()));
        }
        OUT.println();
        String summary = (result.getRunCount() - result.getFailureCount()) + " passed, " + result.getFailureCount()
                + " failed, " + result.getIgnoreCount() + " ignored, in " + (result.getRunTime() / 1000.0) + "s";
        OUT.println(paint(result.wasSuccessful() ? "1;38;5;48" : "1;38;5;197", summary));
        System.exit(result.wasSuccessful() ? 0 : 1);
    }

    // No argument runs everything. A simple class name such as EventFacadeTest runs that class.
    private static Request request(String[] args) {
        if (args.length == 0) {
            return Request.aClass(AllTests.class);
        }
        for (Class<?> test : AllTests.class.getAnnotation(org.junit.runners.Suite.SuiteClasses.class).value()) {
            if (test.getSimpleName().equals(args[0])) {
                return Request.aClass(test);
            }
        }
        OUT.println("No test class called " + args[0]);
        System.exit(2);
        return null;
    }

    private static String shorten(String trace) {
        List<String> kept = new ArrayList<>();
        boolean first = true;
        for (String line : trace.split("\\R")) {
            if (first) {
                kept.add(line);
                first = false;
            } else if (!line.contains("org.junit.") && !line.contains("java.base/") && !line.contains("jdk.internal")) {
                kept.add(line);
            }
        }
        return String.join(System.lineSeparator(), kept.subList(0, Math.min(kept.size(), 40)));
    }

    private static String paint(String code, String text) {
        return color ? "\033[" + code + "m" + text + "\033[0m" : text;
    }

    private static void enableEscapeCodes() {
        try {
            new ProcessBuilder("cmd", "/c", "").inheritIO().start().waitFor();
        } catch (IOException | InterruptedException e) {
            color = false;
        }
    }

    private static final class Listener extends RunListener {
        private String currentClass;
        private int passed;
        private int failed;
        private boolean currentFailed;

        @Override
        public void testStarted(Description description) {
            String name = description.getTestClass().getSimpleName();
            if (!name.equals(currentClass)) {
                finishClass();
                currentClass = name;
            }
            currentFailed = false;
        }

        @Override
        public void testFailure(Failure failure) {
            currentFailed = true;
        }

        @Override
        public void testFinished(Description description) {
            if (currentFailed) {
                failed++;
            } else {
                passed++;
            }
        }

        void finishClass() {
            if (currentClass != null) {
                String mark = failed == 0 ? paint("38;5;48", "PASS") : paint("38;5;197", "FAIL");
                OUT.println("  " + mark + "  " + String.format("%-30s", currentClass) + passed + " passed"
                        + (failed == 0 ? "" : ", " + failed + " failed"));
            }
            currentClass = null;
            passed = 0;
            failed = 0;
        }
    }
}
