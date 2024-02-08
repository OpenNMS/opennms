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

public class LoggingEvent {
    private final String m_name;
    private final Level m_level;
    private final String m_message;

    public LoggingEvent(final String name, final Level level, final String message) {
        m_name = name;
        m_level = level;
        m_message = message;
    }

    public String getLoggerName() {
        return m_name;
    }

    public String getMessage() {
        return m_message;
    }

    public Level getLevel() {
        return m_level;
    }

    @Override
    public String toString() {
        return m_name + "(" + m_level + "): " + m_message;
    }
}
