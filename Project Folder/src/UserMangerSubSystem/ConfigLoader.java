package UserMangerSubSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    private final Properties properties;

    public ConfigLoader() {
        properties = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
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


	public String loadConfig() {
		// TODO Auto-generated method stub
		
		return null;
	}
}