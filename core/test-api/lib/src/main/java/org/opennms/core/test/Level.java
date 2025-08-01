/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
