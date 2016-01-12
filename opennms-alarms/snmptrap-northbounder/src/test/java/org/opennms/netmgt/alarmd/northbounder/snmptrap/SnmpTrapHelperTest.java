/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.alarmd.northbounder.snmptrap;

import java.net.InetAddress;

import org.junit.Assert;
import org.junit.Test;

import org.opennms.core.utils.InetAddressUtils;

/**
 * The Test Class for SnmpTrapNorthbounder.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
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
