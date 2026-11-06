package ReportDashboardSubSystem;

// Thrown when there is nothing left to read, for example when piped input runs out
public class InputClosedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InputClosedException() {
        super("Input closed");
    }
}
