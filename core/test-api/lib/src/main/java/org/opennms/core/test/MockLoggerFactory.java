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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class MockLoggerFactory implements ILoggerFactory {
    final static MockLoggerFactory INSTANCE = new MockLoggerFactory();
    static MockLogAppender s_appender = MockLogAppender.getInstance();

    final Map<String,Logger> m_loggerMap;

    public MockLoggerFactory() {
        m_loggerMap = new HashMap<String,Logger>();
    }

    public static void setAppender(final MockLogAppender appender) {
        s_appender = appender;
    }

    /**
     * Return an appropriate {@link MockLogger} instance by name.
     */
    public Logger getLogger(final String name) {
        if (s_appender == null) {
            System.err.println("WARNING: getLogger(" + name + ") called, but MockLogAppender hasn't been set up yet!");
        }
        Logger slogger = null;
        // protect against concurrent access of the loggerMap
        synchronized (this) {
            slogger = (Logger) m_loggerMap.get(name);
            if (slogger == null) {
                slogger = new MockLogger(name);
                m_loggerMap.put(name, slogger);
            }
        }
        return slogger;
    }
}