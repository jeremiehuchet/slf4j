package org.slf4j.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelConfigurationTest {

    @Test
    public void defaultLevelIsWarn() {
        Level.WARN.isSetForLogger(LoggerFactory.getLogger("com.company.first"));
        Level.WARN.isSetForLogger(LoggerFactory.getLogger("net.othercompany"));
        Level.WARN.isSetForLogger(LoggerFactory.getLogger(""));
    }

    @Test
    public void traceLevel() {
        Level.TRACE.isSetForLogger(LoggerFactory.getLogger("com.example.trace"));
        Level.TRACE.isSetForLogger(LoggerFactory.getLogger("com.example.trace."));
        Level.TRACE.isSetForLogger(LoggerFactory.getLogger("com.example.trace.sub.packages"));
    }

    @Test
    public void debugLevel() {
        Level.DEBUG.isSetForLogger(LoggerFactory.getLogger("com.example.debug"));
        Level.DEBUG.isSetForLogger(LoggerFactory.getLogger("com.example.debug."));
        Level.DEBUG.isSetForLogger(LoggerFactory.getLogger("com.example.debug.sub.packages"));
    }

    @Test
    public void infoLevel() {
        Level.INFO.isSetForLogger(LoggerFactory.getLogger("com.example.info"));
        Level.INFO.isSetForLogger(LoggerFactory.getLogger("com.example.info."));
        Level.INFO.isSetForLogger(LoggerFactory.getLogger("com.example.info.sub.packages"));
    }

    @Test
    public void warnLevel() {
        Level.WARN.isSetForLogger(LoggerFactory.getLogger("com.example.warn"));
        Level.WARN.isSetForLogger(LoggerFactory.getLogger("com.example.warn."));
        Level.WARN.isSetForLogger(LoggerFactory.getLogger("com.example.warn.sub.packages"));
    }

    @Test
    public void errorLevel() {
        Level.ERROR.isSetForLogger(LoggerFactory.getLogger("com.example.error"));
        Level.ERROR.isSetForLogger(LoggerFactory.getLogger("com.example.error."));
        Level.ERROR.isSetForLogger(LoggerFactory.getLogger("com.example.error.sub.packages"));
    }

    private enum Level {
        TRACE {
            @Override
            void isSetForLogger(final Logger log) {
                assertEnabled(log, log.isTraceEnabled(), "trace");
                assertEnabled(log, log.isDebugEnabled(), "debug");
                assertEnabled(log, log.isInfoEnabled(), "info");
                assertEnabled(log, log.isWarnEnabled(), "warn");
                assertEnabled(log, log.isErrorEnabled(), "error");
            }
        },
        DEBUG {
            @Override
            void isSetForLogger(final Logger log) {
                assertDisabled(log, log.isTraceEnabled(), "trace");
                assertEnabled(log, log.isDebugEnabled(), "debug");
                assertEnabled(log, log.isInfoEnabled(), "info");
                assertEnabled(log, log.isWarnEnabled(), "warn");
                assertEnabled(log, log.isErrorEnabled(), "error");
            }
        },
        INFO {
            @Override
            void isSetForLogger(final Logger log) {
                assertDisabled(log, log.isTraceEnabled(), "trace");
                assertDisabled(log, log.isDebugEnabled(), "debug");
                assertEnabled(log, log.isInfoEnabled(), "info");
                assertEnabled(log, log.isWarnEnabled(), "warn");
                assertEnabled(log, log.isErrorEnabled(), "error");
            }
        },
        WARN {
            @Override
            void isSetForLogger(final Logger log) {
                assertDisabled(log, log.isTraceEnabled(), "trace");
                assertDisabled(log, log.isDebugEnabled(), "debug");
                assertDisabled(log, log.isInfoEnabled(), "info");
                assertEnabled(log, log.isWarnEnabled(), "warn");
                assertEnabled(log, log.isErrorEnabled(), "error");
            }
        },
        ERROR {
            @Override
            void isSetForLogger(final Logger log) {
                assertDisabled(log, log.isTraceEnabled(), "trace");
                assertDisabled(log, log.isDebugEnabled(), "debug");
                assertDisabled(log, log.isInfoEnabled(), "info");
                assertDisabled(log, log.isWarnEnabled(), "warn");
                assertEnabled(log, log.isErrorEnabled(), "error");
            }
        };

        private static void assertEnabled(final Logger log, final boolean enabled, final String level) {
            assertTrue(String.format("%s level should be enabled for logger %s", level, log.getName()), enabled);
        }

        private static void assertDisabled(final Logger log, final boolean enabled, final String level) {
            assertFalse(String.format("%s level should be disabled for logger %s", level, log.getName()), enabled);
        }

        abstract void isSetForLogger(Logger log);
    }
}
