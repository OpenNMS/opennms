package org.opennms.core.test;

import org.slf4j.spi.LocationAwareLogger;

public enum Level {
    TRACE(LocationAwareLogger.TRACE_INT),
    DEBUG(LocationAwareLogger.DEBUG_INT),
    INFO(LocationAwareLogger.INFO_INT),
    WARN(LocationAwareLogger.WARN_INT),
    ERROR(LocationAwareLogger.ERROR_INT),
    FATAL(LocationAwareLogger.ERROR_INT + 10);
    
    private int m_code;
    private Level(final int code) {
        m_code = code;
    }
    
    public int getCode() {
        return m_code;
    }
    
    public static Level fromCode(final int code) {
        for (final Level l : Level.values()) {
            if (l.getCode() == code) {
                return l;
            }
        }
        return null;
    }
    
    public boolean gt(final Level level) {
        return getCode() > level.getCode();
    }
    
    public boolean ge(final Level level) {
        return getCode() >= level.getCode();
    }
    
    public boolean lt (final Level level) {
        return getCode() < level.getCode();
    }
    
    public boolean le (final Level level) {
        return getCode() <= level.getCode();
    }
    
    public boolean eq (final Level level) {
        return getCode() == level.getCode();
    }
}
