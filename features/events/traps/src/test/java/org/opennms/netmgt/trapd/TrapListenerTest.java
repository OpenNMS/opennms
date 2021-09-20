/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

        // Adding another copy of an existing user will NOT trigger an update, the first entry found in the list will be used
        config.getSnmpv3UserCollection().add(createUser("MD5", "0p3nNMSv3", "some-engine-id", "DES", "0p3nNMSv3", "some-security-name"));
        Assert.assertEquals(Boolean.FALSE, receiver.hasConfigurationChanged(new TrapdConfigBean(config)));

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

    private static Snmpv3User createUser(String authProtocol,
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
}
