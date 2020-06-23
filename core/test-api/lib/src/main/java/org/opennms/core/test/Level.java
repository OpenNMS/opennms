/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
