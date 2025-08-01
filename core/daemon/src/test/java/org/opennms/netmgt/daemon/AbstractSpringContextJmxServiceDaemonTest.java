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
        MockDaemon<MockServiceDaemon> daemon = new MockDaemon<>();
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

        @Override
        public long getStartTimeMilliseconds() { return 0; }

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
