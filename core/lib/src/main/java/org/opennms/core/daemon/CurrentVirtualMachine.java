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
package org.opennms.core.daemon;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public abstract class CurrentVirtualMachine {
    private static final Logger LOG = LoggerFactory.getLogger(CurrentVirtualMachine.class);

    private static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";
    private static final String VIRTUAL_MACHINE_IDENTIFIER_PROPERTY = "org.opennms.core.daemon.virtualmachine.identifier";

    private static VirtualMachine s_self = null;

    protected CurrentVirtualMachine() {}

    /**
     * Retrieve the current virtual machine using the attach API.
     *
     * @return null if attach is unavailable or the machine could not be found, otherwise the {@link VirtualMachine}
     */
    public static VirtualMachine get() {
        if (s_self == null) {
            try {
                // Check to see if the com.sun.tools.attach classes are loadable in this JVM
                Class.forName("com.sun.tools.attach.VirtualMachine");
                Class.forName("com.sun.tools.attach.VirtualMachineDescriptor");
                Class.forName("com.sun.tools.attach.AttachNotSupportedException");
            } catch (ClassNotFoundException e) {
                LOG.info("The Attach API is not available in this JVM");
                return null;
            }

            final var id = UUID.randomUUID().toString();
            System.setProperty(VIRTUAL_MACHINE_IDENTIFIER_PROPERTY, id);

            for (final VirtualMachineDescriptor vmDescr : VirtualMachine.list()) {
                try {
                    final VirtualMachine vm = VirtualMachine.attach(vmDescr);
                    final String attachedIdentifier = vm.getSystemProperties().getProperty(VIRTUAL_MACHINE_IDENTIFIER_PROPERTY);
                    if (attachedIdentifier.equals(id)) {
                        s_self = vm;
                        break;
                    }
                    vm.detach();
                } catch (final Exception e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Unable to connect to {}", vmDescr.displayName(), e);
                    }
                }
            }
        }
        return s_self;
    }

    /**
     * Retrieve the JMX RMI URI to connect to the local VM.
     *
     * @return the URI string
     * @throws {@link IllegalStateException} if unable to attach internally to the VM
     */
    public static String getJmxUri() {
        String connectorAddress = null;
        final var vm = get();

        if (vm == null) {
            throw new IllegalStateException("Unable to determine current virtual machine!");
        }

        try {
            // Get the local JMX connector URI
            connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
        } catch (final IOException e) {
            throw new IllegalStateException("IOException when fetching JMX URI from JVM with ID: " + vm.id(), e);
        }

        // If there is no local JMX connector URI, we need to launch the
        // JMX agent via this VirtualMachine attachment.
        if (connectorAddress == null) {
            LOG.info("Starting local management agent in JVM with ID: {}", vm);

            try {
                vm.startLocalManagementAgent();
            } catch (final IOException e) {
                throw new IllegalStateException("IOException when starting local JMX management agent in JVM with ID: " + vm.id(), e);
            }

            // Agent is started, get the connector address
            try {
                connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
            } catch (final IOException e) {
                throw new IllegalStateException("IOException when fetching JMX URI from JVM with ID: " + vm.id(), e);
            }
        }

        try {
            vm.detach();
        } catch (final IOException e) {
            LOG.warn("failed to detach from VM {}", vm);
        }

        return connectorAddress;
    }
}
