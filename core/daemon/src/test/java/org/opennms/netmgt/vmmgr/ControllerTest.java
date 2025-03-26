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

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;

import java.io.File;
import java.nio.file.Paths;

public class ControllerTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private JCEKSSecureCredentialsVault secureCredentialsVault;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        final File opennmsHome = tempFolder.getRoot();

        tempFolder.newFolder("etc");

        System.setProperty("opennms.home", opennmsHome.getAbsolutePath());

        final File keystoreFile = new File(Paths.get(opennmsHome.toString(), "etc", "scv.jce").toString());
        secureCredentialsVault = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), JCEKSSecureCredentialsVault.DEFAULT_KEYSTORE_KEY);
        secureCredentialsVault.setCredentials("secret", new Credentials("thehulk", "marvel123!"));
    }
    
    @After
    public void runTest() throws Throwable {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void testSystemProperties() throws Exception {
        System.setProperty("somethingThatNeedsToBeInterpolated", "my password is ${scv:secret:password}");
        System.setProperty("somethingElseThatNeedsToBeInterpolated", "my username is ${scv:secret:username}");
        System.setProperty("somethingThatShouldBeUntouched", "nothing will be changed here hopefully");

        Controller.setSecureCredentialsVault(secureCredentialsVault);
        Controller.interpolateSystemProperties();

        //controller.interpolateSystemProperties();
        Assert.assertEquals("my password is marvel123!", System.getProperty("somethingThatNeedsToBeInterpolated"));
        Assert.assertEquals("my username is thehulk", System.getProperty("somethingElseThatNeedsToBeInterpolated"));
        Assert.assertEquals("nothing will be changed here hopefully", System.getProperty("somethingThatShouldBeUntouched"));
    }

    @Test
    public void testSecureCredentialsVaultCreation() throws Exception {
        Controller.setSecureCredentialsVault(null);
        Controller.interpolateSystemProperties();

        Assert.assertNotNull(Controller.secureCredentialsVault);
    }

    @Test
    public void testStatus() throws Exception {
        final Controller controller = new Controller();
        // This can return different results depending on whether an OpenNMS JVM
        // is running while the unit test is running so don't do an assertion here.
        System.out.println("Status: " + controller.status());
    }

    @Test
    public void testNoInterpolationForNonMatchingProperties() throws Exception {
        // Define properties with placeholders not related to the secure credentials vault.
        final String nodePropertyKey = "org.opennms.timeseries.tin.metatags.tag.node";
        final String nodeOriginalValue = "${node:label}";
        final String locationPropertyKey = "org.opennms.timeseries.tin.metatags.tag.location";
        final String locationOriginalValue = "${node:location}";

        System.setProperty(nodePropertyKey, nodeOriginalValue);
        System.setProperty(locationPropertyKey, locationOriginalValue);

        Controller.setSecureCredentialsVault(secureCredentialsVault);
        Controller.interpolateSystemProperties();

        Assert.assertEquals("Property value should not be modified", nodeOriginalValue, System.getProperty(nodePropertyKey));
        Assert.assertEquals("Property value should not be modified", locationOriginalValue, System.getProperty(locationPropertyKey));
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
