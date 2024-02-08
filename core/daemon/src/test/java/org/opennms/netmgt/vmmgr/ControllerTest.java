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
package org.opennms.netmgt.vmmgr;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class ControllerTest {

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }
    
    @After
    public void runTest() throws Throwable {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void testStatus() throws Exception {
        final Controller controller = new Controller();
        // This can return different results depending on whether an OpenNMS JVM
        // is running while the unit test is running so don't do an assertion here.
        System.out.println("Status: " + controller.status());
    }

    @Test
    public void testAttach() throws Exception {
    
        for (VirtualMachineDescriptor vm : VirtualMachine.list()) {
            System.out.println(vm.displayName() + " [" + vm.id() + "]");
        }

        for (VirtualMachineDescriptor vmDescr : VirtualMachine.list()) {
            if (vmDescr.displayName().contains("opennms_bootstrap")) {
                // Attach to the OpenNMS application
                VirtualMachine vm = VirtualMachine.attach(vmDescr);

                // Get the local JMX connector URI
                String connectorAddress = vm.getAgentProperties().getProperty(Controller.CONNECTOR_ADDRESS);

                // If there is no local JMX connector URI, we need to launch the
                // JMX agent via this VirtualMachine attachment.
                if (connectorAddress == null) {
                    System.out.println("Starting local management agent in JVM with ID: " + vm.id());

                    //String agent = vm.getSystemProperties().getProperty("java.home") + File.separator + "lib" + File.separator + "management-agent.jar";
                    //vm.loadAgent(agent);
                    vm.startLocalManagementAgent();

                    // agent is started, get the connector address
                    connectorAddress = vm.getAgentProperties().getProperty(Controller.CONNECTOR_ADDRESS);
                }

                System.out.println(connectorAddress);
            }
        }
    }
}
