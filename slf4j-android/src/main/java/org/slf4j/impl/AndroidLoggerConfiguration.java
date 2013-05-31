package org.slf4j.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.helpers.Util;

import android.util.Log;

/**
 * Load a simple configuration file.
 * <p>
 * Sample content:
 * 
 * <pre>
 * root = INFO
 * com.example.package = DEBUG
 * com.example.package.sub = WARN
 * </pre>
 * 
 * @author Jeremie Huchet
 */
class AndroidLoggerConfiguration {

    /** The location of the configuration file: {@value} . */
    private static final String CONFIG_FILE = "assets/slf4j.properties";

    /** The key holding the default priority: {@value} . */
    private static final String CONFIG_KEY_DEFAULT_PRIORITY = "root";

    /** The single instance of configuration. */
    private static final AndroidLoggerConfiguration SINGLETON = new AndroidLoggerConfiguration();

    /** Holds the priority for each category listed in the configuration file. */
    private final Map<String, Integer> configuration = new HashMap<String, Integer>();

    /** Hodls the default priority value. */
    private final int defaultPriority;

    static AndroidLoggerConfiguration getSingleton() {
        return SINGLETON;
    }

    /**
     * Read and loads the configuration file {@value #CONFIG_FILE}.
     */
    private AndroidLoggerConfiguration() {
        // open and load file
        final Properties conf = new Properties();
        try {
            final InputStream in = AndroidLoggerConfiguration.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (null != in) {
                conf.load(in);
            } else {
                Util.report(String.format("Can't find logger configuration: please set a %s file", CONFIG_FILE));
            }
        } catch (final IOException e) {
            Util.report(String.format("Can't find logger configuration: please set a %s file", CONFIG_FILE), e);
        }
        // retrieve default root level
        if (!conf.containsKey(CONFIG_KEY_DEFAULT_PRIORITY)) {
            Util.report("Default priority undefined. Use 'root.level' property.");
            defaultPriority = Integer.MAX_VALUE;
        } else {
            defaultPriority = getPriorityFromLabel(conf.getProperty(CONFIG_KEY_DEFAULT_PRIORITY));
        }
        // read logger level configuration
        for (final Entry<Object, Object> e : conf.entrySet()) {
            final String key = (String) e.getKey();
            final String value = (String) e.getValue();
            // ignore "root.level" key
            if (!CONFIG_KEY_DEFAULT_PRIORITY.equals(key)) {
                final Integer level = getPriorityFromLabel(value);
                if (null != level) {
                    configuration.put(key, level);
                } else {
                    Util.report(String.format("Wrong level value for category %s", key));
                }
            }
        }
    }

    /**
     * Map a priority label to an integer value.
     * <ul>
     * <li>trace = <code>Log.VERBOSE</code></li>
     * <li>debug = <code>Log.DEBUG</code></li>
     * <li>info = <code>Log.INFO</code></li>
     * <li>warn = <code>Log.WARN</code></li>
     * <li>error = <code>Log.ERROR</code></li>
     * </ul>
     * 
     * @param priorityLabel
     *            the priority label
     * @return the priority as an integer value, or null if the label wasn't
     *         recognized
     */
    private Integer getPriorityFromLabel(final String priorityLabel) {
        if ("trace".equalsIgnoreCase(priorityLabel)) {
            return Log.VERBOSE;
        } else if ("debug".equalsIgnoreCase(priorityLabel)) {
            return Log.DEBUG;
        } else if ("info".equalsIgnoreCase(priorityLabel)) {
            return Log.INFO;
        } else if ("warn".equalsIgnoreCase(priorityLabel)) {
            return Log.WARN;
        } else if ("error".equalsIgnoreCase(priorityLabel)) {
            return Log.ERROR;
        } else {
            return null;
        }
    }

    /**
     * Obtains the priority for a given logger name.
     * <p>
     * If no priority was configured, then the {@link #defaultPriority} is
     * returned.
     * 
     * @param name
     *            the logger name
     * @return the configured priority for the given logger
     */
    int getPriority(final String name) {
        Integer priority = null;
        for (int i = name.length(); i > 0 && priority == null; i--) {
            final String key = name.substring(0, i);
            priority = configuration.get(key);
        }
        if (null == priority) {
            priority = defaultPriority;
        }
        return priority;
    }
}
