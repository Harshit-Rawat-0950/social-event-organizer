package UserMangerSubSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    // Run with -Dapp.config=<file> to use a different configuration, as the tests do
    public static final String CONFIG_OVERRIDE = "app.config";
    private static final String DEFAULT_CONFIG = "src/UserMangerSubSystem/configData.properties";

    private final Properties properties;

    public ConfigLoader() {
        properties = new Properties();
        String path = System.getProperty(CONFIG_OVERRIDE, DEFAULT_CONFIG);

        try (FileInputStream fis = new FileInputStream(path)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + path, e);
        }
    }

    // Returns value if key exists, otherwise returns null
    public String getProperty(String key) {
        String value = properties.getProperty(key);
        return value == null ? null : value.trim();
    }

    // Returns value if key exists, otherwise returns defaultValue
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue).trim();
    }

    //
    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);

        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Returns parsed boolean if key exists, otherwise returns defaultValue
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);

        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value.trim());
    }

	public String loadConfig() {
		return getProperty("app.name") + " v" + getProperty("app.version") + ", mode=" + getProperty("app.mode");
	}
}
