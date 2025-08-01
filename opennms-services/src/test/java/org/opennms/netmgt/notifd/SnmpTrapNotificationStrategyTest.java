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
package org.opennms.netmgt.notifd;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
public class SnmpTrapNotificationStrategyTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLogAppender.setupLogging(true);
    }

    /*
     * Test method for 'org.opennms.netmgt.notifd.SnmpTrapNotificationStrategy.send(List)'
     */
    public void testSendWithEmptyArgumentList() {
        List<Argument> arguments = new ArrayList<>();
        NotificationStrategy strategy = new SnmpTrapNotificationStrategy();
        strategy.send(arguments);

    }

    /*
     * Test method for 'org.opennms.netmgt.notifd.SnmpTrapNotificationStrategy.send(List)'
     */
    public void testSendWithNamedHost() {
        List<Argument> arguments = new ArrayList<>();
        Argument arg = new Argument("trapHost", null, "localhost", false);
        arguments.add(arg);
        NotificationStrategy strategy = new SnmpTrapNotificationStrategy();
        strategy.send(arguments);

    }
    /*
     * Test method for 'org.opennms.netmgt.notifd.SnmpTrapNotificationStrategy.sendV1Trap()'
     */
    public void testSendV1Trap() {

    }

    /*
     * Test method for 'org.opennms.netmgt.notifd.SnmpTrapNotificationStrategy.sendV2Trap()'
     */
    public void testSendV2Trap() {

    }

}
