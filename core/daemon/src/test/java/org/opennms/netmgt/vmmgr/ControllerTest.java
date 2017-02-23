/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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
