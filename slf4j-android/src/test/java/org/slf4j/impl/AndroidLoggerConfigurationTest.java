package org.slf4j.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import android.util.Log;

public class AndroidLoggerConfigurationTest {

    private final AndroidLoggerConfiguration conf = AndroidLoggerConfiguration.getSingleton();
    
    @Test
    public void canObtainDefaultValue() {
        assertEquals(Log.WARN, conf.getPriority("any.category"));
    }
    
    @Test
    public void canObtainConfiguredValues() {
        assertEquals(Log.INFO, conf.getPriority("org.slf4j"));
        assertEquals(Log.DEBUG, conf.getPriority("org.slf4j.impl"));
    }
    
    @Test
    public void canObtainImplicitelyConfiguredValues() {
        assertEquals(Log.INFO, conf.getPriority("org.slf4j.other"));
        assertEquals(Log.DEBUG, conf.getPriority("org.slf4j.impl.other"));
    }
}
