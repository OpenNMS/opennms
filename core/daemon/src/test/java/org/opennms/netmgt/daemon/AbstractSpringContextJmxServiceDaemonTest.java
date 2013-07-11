/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.daemon;

import org.junit.Test;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class AbstractSpringContextJmxServiceDaemonTest {
    /**
     * This is a test for bug #2479 where we would see an NPE in manager.log
     * if we tried to call stop() on a daemon that hadn't been successfully
     * init()ed.  This happens when this daemon, or one before it fails to
     * initialize and the manager application still tries to shutdown all
     * daemons.
     */
    @Test
    public void testStopWithNoInit() throws Exception {
        MockDaemon<MockServiceDaemon> daemon = new MockDaemon<MockServiceDaemon>();
        daemon.stop();
    }

    public class MockDaemon<T extends SpringServiceDaemon> extends AbstractSpringContextJmxServiceDaemon<T> {
        @Override
        public String getSpringContext() {
            return "thisIsABogusSpringContext";
        }

        @Override
        public String getLoggingPrefix() {
            return "thisIsABogusLoggingPrefix";
        }
    }

    public class MockServiceDaemon extends AbstractServiceDaemon {
        public MockServiceDaemon() {
            super("mock-service-daemon");
        }

        @Override
        protected void onInit() {
        }
    }
}
