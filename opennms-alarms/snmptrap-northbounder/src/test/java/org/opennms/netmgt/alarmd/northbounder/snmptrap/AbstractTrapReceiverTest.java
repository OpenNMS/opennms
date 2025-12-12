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
package org.opennms.netmgt.alarmd.northbounder.snmptrap;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.mate.api.SecureCredentialsVaultScope;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.mock.MockSnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.opennms.netmgt.snmp.snmp4j.TrapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Test Class for SnmpTrapNorthbounder.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public abstract class AbstractTrapReceiverTest implements TrapNotificationListener {

    /** The Constant LOG. */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractTrapReceiverTest.class);

    /** The Constant TRAP_PORT. */
    protected static final int TRAP_PORT = 1162;

    /** The trap receiver address. */
    protected static final InetAddress TRAP_DESTINATION = InetAddressUtils.getLocalHostAddress();

    /** The received trap notification. */
    private List<TrapData> trapNotifications = new ArrayList<>();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Sets up the test (initialize a trap listener).
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        SnmpPeerFactory.setInstance(new MockSnmpPeerFactory());

        final File keystoreFile = new File(tempFolder.getRoot(), "scv.jce");
        final SecureCredentialsVault secureCredentialsVault = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "notRealPassword");
        secureCredentialsVault.setCredentials("remote", new Credentials("john", "doe"));
        SnmpPeerFactory.setSecureCredentialsVaultScope(new SecureCredentialsVaultScope(secureCredentialsVault));

        MockLogAppender.setupLogging();
        resetTrapsReceived();
        System.setProperty("opennms.home", "src/test/resources");
        SnmpPeerFactory.init();
        Assert.assertEquals("Snmp4JStrategy", SnmpUtils.getStrategy().getClass().getSimpleName());

        SnmpAgentConfig config = SnmpPeerFactory.getInstance().getAgentConfig(TRAP_DESTINATION);
        SnmpV3User user = new SnmpV3User(config.getSecurityName(), config.getAuthProtocol(), config.getAuthPassPhrase(), config.getPrivProtocol(), config.getPrivPassPhrase());
        SnmpUtils.registerForTraps(this, TRAP_DESTINATION, TRAP_PORT, Collections.singletonList(user));
        LOG.info("Registered Trap Listener for {} on port {}", TRAP_DESTINATION, TRAP_PORT);
    }

    /**
     * Tears down the test (shutdown the trap listener)
     */
    @After
    public void tearDown() {
        try {
            SnmpUtils.unregisterForTraps(this);
            LOG.info("Unregistered Trap Listener for {} on port {}", TRAP_DESTINATION, TRAP_PORT);
        } catch (IOException e) {
            Assert.fail();
            LOG.error("Can't unregister Trap Listener for {} on port {}", TRAP_DESTINATION, TRAP_PORT, e);
        } finally {
            MockLogAppender.assertNoWarningsOrGreater();
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.snmp.TrapNotificationListener#trapReceived(org.opennms.netmgt.snmp.TrapNotification)
     */
    @Override
    public void trapReceived(TrapInformation trapNotification) {
        TrapData data = TrapUtils.getTrapData(trapNotification);
        if (data == null) {
            Assert.fail();
        } else {
            trapNotifications.add(data);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.snmp.TrapNotificationListener#trapError(int, java.lang.String)
     */
    @Override
    public void trapError(int error, String msg) {
        Assert.fail(msg);
    }

    /**
     * Gets the traps received.
     *
     * @return the traps received
     */
    protected List<TrapData> getTrapsReceived() {
        return trapNotifications;
    }

    /**
     * Gets the traps received count.
     *
     * @return the traps received count
     */
    protected int getTrapsReceivedCount() {
        return trapNotifications.size();
    }

    /**
     * Reset traps received.
     */
    protected void resetTrapsReceived() {
        trapNotifications.clear();
    }

}
