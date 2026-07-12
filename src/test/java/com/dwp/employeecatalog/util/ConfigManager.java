package com.dwp.employeecatalog.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads {@code config.properties} from the test classpath and exposes typed
 * accessors. Every property can be overridden with a JVM system property of the
 * same name, e.g. {@code -Dbase.uri=...}, which is handy for pointing the suite
 * at a local instance of the service.
 */
public final class ConfigManager {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = ConfigManager.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException("config.properties not found on the test classpath");
            }
            PROPERTIES.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config.properties", e);
        }
    }

    private ConfigManager() {
    }

    /** System property wins over the file value, so runs can be overridden on the CLI. */
    public static String get(String key) {
        String override = System.getProperty(key);
        if (override != null && !override.isBlank()) {
            return override;
        }
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing configuration key: " + key);
        }
        return value;
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key).trim());
    }

    public static long getLong(String key) {
        return Long.parseLong(get(key).trim());
    }

    public static String baseUri() {
        return get("base.uri");
    }

    public static String adminUsername() {
        return get("admin.username");
    }

    public static String adminPassword() {
        return get("admin.password");
    }
}
