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


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.netmgt.config.EventConfTestUtil;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpV3TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.str;

/**
 * {@link TrapHandlerITCase} which uses the snmp strategy {@link org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy}.
 *
 * @author brozow
 */
public class Snmp4JTrapHandlerIT extends TrapHandlerITCase {

    @Autowired
    private EventConfDao eventConfDao;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy");
    }

    @Before
    public void setUp() throws Exception {
        List<EventConfEvent> events = EventConfTestUtil.parseResourcesAsEventConfEvents(
                new FileSystemResource("src/test/resources/org/opennms/netmgt/trapd/eventconf.xml"));
        // Load into DB
        eventConfDao.loadEventsFromDB(events);
        super.setUp();
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
