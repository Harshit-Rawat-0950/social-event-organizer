package UserMangerSubSystem;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import TestSupport.TestEnvironment;

public class ConfigLoaderTest {

	@Rule
	public TestEnvironment env = new TestEnvironment();

	@Test
	public void readsTheOverrideFile() {
		env.set("custom.key", "value");
		assertEquals("value", new ConfigLoader().getProperty("custom.key"));
	}

	@Test
	public void missingKeysGiveNullOrTheDefault() {
		assertNull(new ConfigLoader().getProperty("nope"));
		assertEquals("d", new ConfigLoader().getProperty("nope", "d"));
	}

	@Test
	public void valuesAreTrimmed() {
		env.set("spaced", "value   ");
		assertEquals("value", new ConfigLoader().getProperty("spaced"));
		assertEquals("value", new ConfigLoader().getProperty("spaced", "d"));
	}

	@Test
	public void integersFallBackWhenMissingEmptyOrInvalid() {
		env.set("good", " 42 ");
		env.set("bad", "abc");
		env.set("empty", "");
		ConfigLoader config = new ConfigLoader();
		assertEquals(42, config.getIntProperty("good", 1));
		assertEquals(1, config.getIntProperty("bad", 1));
		assertEquals(1, config.getIntProperty("empty", 1));
		assertEquals(1, config.getIntProperty("nope", 1));
	}

	@Test
	public void booleansFallBackWhenMissingOrEmpty() {
		env.set("yes", "true");
		env.set("no", "false");
		env.set("empty", "");
		ConfigLoader config = new ConfigLoader();
		assertTrue(config.getBooleanProperty("yes", false));
		assertFalse(config.getBooleanProperty("no", true));
		assertTrue(config.getBooleanProperty("empty", true));
		assertTrue(config.getBooleanProperty("nope", true));
	}

	@Test
	public void missingFileIsAClearError() {
		System.setProperty(ConfigLoader.CONFIG_OVERRIDE, env.path("does-not-exist.properties"));
		try {
			new ConfigLoader();
			fail("Missing config was accepted");
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().contains("does-not-exist"));
		}
	}

	@Test
	public void summaryNamesTheApp() {
		assertEquals("Event Management System v1.0, mode=dev", new ConfigLoader().loadConfig());
	}

	// The real settings file must define everything the application reads
	@Test
	public void realConfigDefinesEveryRequiredKey() {
		System.clearProperty(ConfigLoader.CONFIG_OVERRIDE);
		ConfigLoader real = new ConfigLoader();
		for (String key : new String[] {"app.name", "app.version", "app.mode", "app.delay", "app.devStartupDelayMs", "app.registerDelayMs",
				"login.maxAttempts", "Files.users", "Files.events", "Files.invites", "Files.friends", "Files.follow", "Files.password",
				"Files.notification", "Files.rsvp", "Files.mainpage", "Files.eventpage", "Files.invitepage", "Files.notificationpage",
				"Files.reportpage", "Files.friendpage", "Files.profilepage", "event.defaultType", "event.maxCapacity",
				"event.allowCancellation", "ui.theme", "ui.color", "ui.unicode", "ui.width"}) {
			assertNotNull("Missing " + key, real.getProperty(key));
		}
	}
}
