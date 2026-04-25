package UserMangerSubSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
 
    private final Properties properties;

    public ConfigLoader() {
        properties = new Properties();

        try (FileInputStream fis = new FileInputStream("src/UserMangerSubSystem/configData.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configData.properties", e);
        }
    }

    // Returns value if key exists, otherwise returns null
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    // Returns value if key exists, otherwise returns defaultValue
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
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
		// TODO Auto-generated method stub
		
		return null;
	}
}