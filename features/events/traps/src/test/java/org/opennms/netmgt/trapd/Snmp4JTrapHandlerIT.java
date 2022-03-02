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

package org.opennms.netmgt.trapd;


import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpV3TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.str;

/**
 * {@link TrapHandlerITCase} which uses the snmp strategy {@link org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy}.
 *
 * @author brozow
 */
public class Snmp4JTrapHandlerIT extends TrapHandlerITCase {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy");
    }

    @Override
    public void sendTrap(final String version, final String enterprise, final int generic, final int specific) throws Exception {
        if ("v3".equals(version)) {
            sendV3Trap(enterprise);
        } else {
            super.sendTrap(version, enterprise, generic, specific);
        }
    }

    @Override
    public void sendTrap(final String version, final String enterprise, final int generic, final int specific, final LinkedHashMap<String, SnmpValue> varbinds) throws Exception {
        if ("v3".equals(version)) {
            sendV3Trap(enterprise);
        } else {
            super.sendTrap(version, enterprise, generic, specific, varbinds);
        }
    }

    private void sendV3Trap(final String enterprise) throws Exception {
        final Snmp4JStrategy strategy = new Snmp4JStrategy();
        final SnmpObjId enterpriseId = SnmpObjId.get(enterprise == null ? ".0.0" : enterprise);
        final SnmpObjId trapOID = SnmpObjId.get(enterpriseId, new SnmpInstId(1));
        final SnmpV3TrapBuilder pduv3 = strategy.getV3TrapBuilder();
        pduv3.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), strategy.getValueFactory().getTimeTicks(0));
        pduv3.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), strategy.getValueFactory().getObjectId(trapOID));
        pduv3.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), strategy.getValueFactory().getObjectId(enterpriseId));
        pduv3.send(str(m_ip), m_trapdConfig.getSnmpTrapPort(), SnmpConfiguration.NOAUTH_NOPRIV, "noAuthUser", null, null, null, null);
    }

    /**
     * See NMS-13489 for details.
     */
    @Test
    public void testTrapdInstrumentationCounters() throws Exception {
        final long v1 = TrapSinkConsumer.trapdInstrumentation.getV1TrapsReceived();
        final long v2 = TrapSinkConsumer.trapdInstrumentation.getV2cTrapsReceived();
        final long v3 = TrapSinkConsumer.trapdInstrumentation.getV3TrapsReceived();
        final long all = TrapSinkConsumer.trapdInstrumentation.getTrapsReceived();

        m_cache.clear();
        anticipateAndSend(false, false, "uei.opennms.org/default/trap", "v1", null, 6, 1);

        m_cache.clear();
        anticipateAndSend(false, false, "uei.opennms.org/default/trap", "v2c", null, 6, 1);

        m_cache.clear();
        anticipateAndSend(false, false, "uei.opennms.org/default/trap", "v3", null, 6, 1);

        assertEquals(v1 + 1, TrapSinkConsumer.trapdInstrumentation.getV1TrapsReceived());
        assertEquals(v2 + 1, TrapSinkConsumer.trapdInstrumentation.getV2cTrapsReceived());
        assertEquals(v3 + 1, TrapSinkConsumer.trapdInstrumentation.getV3TrapsReceived());
        assertEquals(all + 3, TrapSinkConsumer.trapdInstrumentation.getTrapsReceived());
    }
}
