/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote.support;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class Log4j2StringAppenderTest {
    @Test
    public void canCaptureLogMessage() {
        // Create a log42j specific logger
        Logger LOG = LogManager.getLogger(Log4j2StringAppenderTest.class);

        // Add our log appender to the root logger
        Log4j2StringAppender appender = Log4j2StringAppender.createAppender();
        appender.start();
        appender.addToLogger(LogManager.ROOT_LOGGER_NAME, Level.DEBUG);

        try {
            // Send a log message
            LOG.debug("w00t");
        } finally {
            // Remove the log appender from the root logger
            appender.removeFromLogger(LogManager.ROOT_LOGGER_NAME); 
        }

        // Verify that the log message was successfully captured
        assertTrue("Output did not contain test log message", appender.getOutput().contains("o.o.n.p.r.s.Log4j2StringAppenderTest: w00t"));
    }
}
