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
package org.opennms.container.daemon;

import static org.junit.Assert.assertTrue;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.junit.Test;

public class KarafDaemonMBeanTest {
    @Test
    public void registersAsAStandardMBean() throws Exception {
        final MBeanServer server = MBeanServerFactory.newMBeanServer();
        final ObjectName name = new ObjectName("OpenNMS:Name=Karaf");

        server.registerMBean(new org.opennms.container.daemon.jmx.KarafDaemon(), name);

        assertTrue(server.isRegistered(name));
    }
}
