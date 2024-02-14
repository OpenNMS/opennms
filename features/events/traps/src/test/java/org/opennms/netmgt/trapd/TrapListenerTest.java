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
package org.opennms.netmgt.trapd;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.awaitility.Awaitility;
import java.util.concurrent.TimeUnit;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;

public class TrapListenerTest {

    private TrapdConfigBean initialConfig;
    private TrapListener receiver;

    @Before
    public void setUp() throws Exception {
        initialConfig = new TrapdConfigBean();
        initialConfig.setSnmpTrapPort(1162);
        initialConfig.setSnmpTrapAddress("localhost");

        // Overwrite start() and stop() to not listen for traps, as this is not required here
        receiver = new TrapListener(initialConfig) {
            @Override
            public void start() {
                // dont register snmp trap listener
            }

            @Override
            public void stop() {
                // dont unregister snmp trap listener
            }
        };
    }

    @Test
    public void verifyCheckForTrapdConfigurationChange() throws Exception {
        // Empty config should be different
        TrapdConfiguration config = new TrapdConfiguration();
        Assert.assertEquals(Boolean.TRUE, receiver.hasConfigurationChanged(new TrapdConfigBean(config)));

        // Equal config should not be different
        config.setSnmpTrapPort(initialConfig.getSnmpTrapPort());
        config.setSnmpTrapAddress(initialConfig.getSnmpTrapAddress());
        Assert.assertEquals(Boolean.FALSE, receiver.hasConfigurationChanged(new TrapdConfigBean(config)));

        // Add a user, should result in a difference
        config.getSnmpv3UserCollection().add(createUser("MD5", "0p3nNMSv3", "some-engine-id", "DES", "0p3nNMSv3", "some-security-name"));
        Assert.assertEquals(Boolean.TRUE, receiver.hasConfigurationChanged(new TrapdConfigBean(config)));
        receiver.setTrapdConfig(config); // update and verify that it now is equal
        Assert.assertEquals(Boolean.FALSE, receiver.hasConfigurationChanged(new TrapdConfigBean(config)));

        // Adding another user should also result in a difference
        config.getSnmpv3UserCollection().add(createUser("MD5", "0p3nNMSv3", "some-engine-id", "DES", "0p3nNMSv3", "some-security-name-2"));
        Assert.assertEquals(Boolean.TRUE, receiver.hasConfigurationChanged(new TrapdConfigBean(config)));
        receiver.setTrapdConfig(config); // update and verify that it now is equal
        Assert.assertEquals(Boolean.FALSE, receiver.hasConfigurationChanged(new TrapdConfigBean(config)));

        // Adding another copy of an existing user will now trigger an update
        config.getSnmpv3UserCollection().add(createUser("MD5", "0p3nNMSv3", "some-engine-id", "DES", "0p3nNMSv3", "some-security-name"));
        Assert.assertEquals(Boolean.TRUE, receiver.hasConfigurationChanged(new TrapdConfigBean(config)));

        // Editing a user should result in a difference
        config.getSnmpv3UserCollection().get(0).setPrivacyProtocol("AES");
        Assert.assertEquals(Boolean.TRUE, receiver.hasConfigurationChanged(new TrapdConfigBean(config)));
        receiver.setTrapdConfig(config); // update and verify that it now is equal
        Assert.assertEquals(Boolean.FALSE, receiver.hasConfigurationChanged(new TrapdConfigBean(config)));
    }

    @Test
    public void verifyCheckForSnmpV3ConfigurationChange() throws Exception {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(initialConfig.getSnmpTrapPort() + 1);

        Assert.assertEquals(Boolean.FALSE, receiver.hasSnmpV3UsersChanged(new TrapdConfigBean(config)));

        // Add a user, should result in a difference
        config.getSnmpv3UserCollection().add(createUser("MD5", "0p3nNMSv3", "some-engine-id", "DES", "0p3nNMSv3", "some-security-name"));
        Assert.assertEquals(Boolean.TRUE, receiver.hasSnmpV3UsersChanged(new TrapdConfigBean(config)));
        receiver.setTrapdConfig(config); // update and verify that it now is equal
        Assert.assertEquals(Boolean.FALSE, receiver.hasSnmpV3UsersChanged(new TrapdConfigBean(config)));

        // Changing the port should not result in a difference
        config.setSnmpTrapPort(config.getSnmpTrapPort() + 1);
        Assert.assertEquals(Boolean.FALSE, receiver.hasSnmpV3UsersChanged(new TrapdConfigBean(config)));
    }


    public static Snmpv3User createUser(String authProtocol,
                                         String autoPassPhrase,
                                         String engineId,
                                         String privatcyProtocol,
                                         String privacyPassPhrase,
                                         String securityName) {
        Snmpv3User user = new Snmpv3User();
        user.setAuthProtocol(authProtocol);
        user.setAuthPassphrase(autoPassPhrase);
        user.setEngineId(engineId);
        user.setPrivacyPassphrase(privacyPassPhrase);
        user.setPrivacyProtocol(privatcyProtocol);
        user.setSecurityName(securityName);
        return user;
    }

    @Test
    public void noSubscriberTest() throws Exception {
        TrapListener listener = new TrapListener(initialConfig);
        listener.setSubscriberTimeoutMs(2 * 1000);
        listener.start();
        Assert.assertEquals(Boolean.FALSE, listener.isRegisteredForTraps());
        
        Awaitility.await().atMost(10 * 1000, TimeUnit.MILLISECONDS).with()
            .pollInterval(1, TimeUnit.SECONDS)
            .until(() -> listener.isRegisteredForTraps());
    }
}
