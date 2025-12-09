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

import java.net.InetAddress;

import org.junit.Assert;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.mock.MockSnmpPeerFactory;
import org.springframework.test.context.ContextConfiguration;

/**
 * The Test Class for SnmpTrapNorthbounder.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
//@RunWith(OpenNMSJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {
//        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
//})
public class SnmpTrapHelperTest extends AbstractTrapReceiverTest {

    /** The SNMP trap helper. */
    private SnmpTrapHelper trapHelper = new SnmpTrapHelper();

    /** The SNMP trap configuration. */
    private SnmpTrapConfig config;

    /** The host address. */
    private InetAddress hostAddress;

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.northbounder.snmptrap.AbstractTrapReceiverTest#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        config = new SnmpTrapConfig();
        hostAddress = InetAddressUtils.addr("10.0.0.1");
    }

    /**
     * Test forward traps and informs.
     *
     * @throws Exception the exception
     */
    @Test
    public void testForwarding() throws Exception {
        // Create a sample trap configuration
        config.setDestinationAddress(TRAP_DESTINATION);
        config.setDestinationPort(TRAP_PORT);
        config.setEnterpriseId(".1.3.6.1.4.1.5813");
        config.setSpecific(2);
        config.setHostAddress(hostAddress);
        config.addParameter(".1.3.6.1.2.1.2.2.1.1.3", "3", VarbindType.TYPE_SNMP_INT32.value());

        // Send a V1 Trap
        config.setVersion(SnmpVersion.V1);
        forwardTrap(config);

        // Send a V2c Trap
        config.setVersion(SnmpVersion.V2c);
        forwardTrap(config);

        // Send a V3 Trap
        config.setVersion(SnmpVersion.V3);
        forwardTrap(config);

        // Send a V2 Inform
        config.setVersion(SnmpVersion.V2_INFORM);
        forwardTrap(config);

        // Send a V3 Inform
        config.setVersion(SnmpVersion.V3_INFORM);
        forwardTrap(config);
    }

    /**
     * Forwards a trap.
     *
     * @param config the SNMP Trap configuration
     * @throws Exception the exception
     */
    private void forwardTrap(SnmpTrapConfig config) throws Exception {
        resetTrapsReceived();
        trapHelper.forwardTrap(config);
        Thread.sleep(5000); // Introduce a delay to make sure the trap was sent and received.
        Assert.assertEquals(1, getTrapsReceivedCount());
        TrapData data = getTrapsReceived().get(0);
        LOG.debug("Received: {}", data);
        Assert.assertEquals(".1.3.6.1.4.1.5813", data.getEnterpriseOid());
        Assert.assertEquals(6, data.getGeneric());
        Assert.assertEquals(2, data.getSpecific());
    }

}
