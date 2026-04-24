package ReportDashboardSubSystem;

public interface Page {
	public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
	public void displayPage();
}
