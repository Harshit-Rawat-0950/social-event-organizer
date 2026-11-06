package ReportDashboardSubSystem;

import EventManagementSubSystem.BasicEvent;
import EventManagementSubSystem.CompositeEvent;
import EventManagementSubSystem.EventComponent;
import ReportDashboardSubSystem.Theme.Role;
import UserMangerSubSystem.ConfigLoader;

/*
 * Goal is to implement all interaction of other subsystem through this class
 * only creating a unified interface for the entire system. However this class
 * in itself must not perform any function other than delegation to the
 * respective class of any and all interactions possible from other
 * parts of project.
 **/

public class DashboardFacade {

	public static void displayEvent(CompositeEvent compositeEvent) {
		Screen.println(EventCardRenderer.render(compositeEvent.describe()));
	}

	public static void displayEvent(BasicEvent basicEvent) {
		Screen.println(EventCardRenderer.render(basicEvent.describe()));
	}

	public static void displayEvent(EventComponent decoratedEvent) {
		Screen.println(EventCardRenderer.render(decoratedEvent.describe()));
	}

	// The logo and name screen shown before login and registration
	public static void displayLoginPage() {
		ConfigLoader configLoader = new ConfigLoader();
		Terminal.get().clear();
		Screen.println(Widgets.logo());
		Screen.println(" " + Style.bold(Role.TITLE, configLoader.getProperty("app.name") + " v" + configLoader.getProperty("app.version")));
	}

	public static void displayHeading(String title) {
		Screen.println(Widgets.titleBar(title));
	}

	// A muted block of startup information, shown in development mode
	public static void displayInfoBox(String title, java.util.List<String> lines) {
		java.util.List<String> styled = new java.util.ArrayList<>();
		for(String line : lines)
			styled.add(Style.color(Role.MUTED, line));
		Screen.println(Widgets.box(Style.color(Role.MUTED, title), null, styled, Role.MUTED));
	}

	// Fills a loading bar over the given time. Without colour it only prints the result.
	public static void displayProgress(String label, int totalMs) {
		int steps = 20;
		String prefix = " " + Style.color(Role.MUTED, label) + " ";
		if(Terminal.get().isColor() && totalMs > 0) {
			for(int i = 0; i <= steps; i++) {
				System.out.print("\r" + prefix + Widgets.meter(i, steps, steps, Role.ACCENT));
				System.out.flush();
				try {
					Thread.sleep(totalMs / steps);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
			System.out.println();
		} else {
			System.out.println(prefix + Widgets.meter(steps, steps, steps, Role.ACCENT));
		}
	}

	public static void showSuccess(String message) {
		Screen.success(message);
	}

	public static void showError(String message) {
		Screen.error(message);
	}

	public static void showInfo(String message) {
		Screen.info(message);
	}

	// Prints any waiting messages. Used when the application is about to end.
	public static void flushMessages() {
		Screen.flush();
	}

	public static void loadMainPage() {
		MainPage.getInstance().displayPage();
	}

}
