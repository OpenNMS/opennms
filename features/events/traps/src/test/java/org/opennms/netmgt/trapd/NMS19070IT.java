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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.TrapdConfigFactory;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-trapDaemon.xml",
        "classpath:/org/opennms/netmgt/trapd/applicationContext-trapDaemonTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class NMS19070IT {
    @Autowired
    private TrapdConfigFactory m_trapdConfig;

    @Autowired
    Trapd m_trapd;

    @Autowired
    MockEventIpcManager m_mockEventIpcManager;

    private final InetAddress LOCAL_ADDR = InetAddressUtils.getLocalHostAddress();
    private final String LOCALHOST = InetAddressUtils.toIpAddrString(LOCAL_ADDR);
    private final SnmpObjId TRAP_OID = SnmpObjId.get(".1.2.3.4.5.6.7.1");

    @Before
    public void setUp() {
        m_mockEventIpcManager.setSynchronous(true);
        m_trapd.setSecureCredentialsVault(new TrapdIT.MockSecureCredentialsVault());
        m_trapd.onStart();
    }

    @After
    public void tearDown() {
        m_trapd.onStop();
        m_mockEventIpcManager.getEventAnticipator().verifyAnticipated(3000, 0, 0, 0, 0);
    }

    private void testTrap(final Map<String, SnmpValue> varbinds, final boolean vbnumber, final boolean success) throws Exception {
        final SnmpTrapBuilder pdu = SnmpUtils.getV2TrapBuilder();
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), SnmpUtils.getValueFactory().getTimeTicks(0));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), SnmpUtils.getValueFactory().getObjectId(TRAP_OID));

        for(final Map.Entry<String, SnmpValue> entry : varbinds.entrySet()) {
            pdu.addVarBind(SnmpObjId.get(entry.getKey()), entry.getValue());
        }

        final EventBuilder defaultTrapBuilder;
        if (success) {
            defaultTrapBuilder = new EventBuilder("uei.opennms.org/vendor/nms19070/traps/" + (vbnumber ? "vbnumber" : "vboid"), "trapd");
        } else {
            defaultTrapBuilder = new EventBuilder("uei.opennms.org/default/trap", "trapd");
        }

        defaultTrapBuilder.setInterface(LOCAL_ADDR);
        defaultTrapBuilder.setSnmpVersion("v2c");
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(defaultTrapBuilder.getEvent());

        final EventBuilder newSuspectBuilder = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "trapd");
        newSuspectBuilder.setInterface(LOCAL_ADDR);
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(newSuspectBuilder.getEvent());

        pdu.send(LOCALHOST, m_trapdConfig.getSnmpTrapPort(), "public");

        // Wait until we received the expected events
        await().until(() -> m_mockEventIpcManager.getEventAnticipator().getAnticipatedEventsReceived(), hasSize(2));
    }

    @Test
    public void testFailVbNumber() throws Exception {
        final Map<String, SnmpValue> varbinds = new LinkedHashMap<>();
        varbinds.put(".1.2.3.4.5.6.7.1", SnmpUtils.getValueFactory().getGauge32(1));
        varbinds.put(".1.2.3.4.5.6.7.99", SnmpUtils.getValueFactory().getGauge32(99));
        varbinds.put(".1.2.3.4.5.6.7.3", SnmpUtils.getValueFactory().getGauge32(3));
        testTrap(varbinds, true, false);
    }

    @Test
    public void testSuccessVbNumber() throws Exception {
        final Map<String, SnmpValue> varbinds = new LinkedHashMap<>();
        varbinds.put(".1.2.3.4.5.6.7.1", SnmpUtils.getValueFactory().getGauge32(1));
        varbinds.put(".1.2.3.4.5.6.7.99", SnmpUtils.getValueFactory().getGauge32(2));
        varbinds.put(".1.2.3.4.5.6.7.3", SnmpUtils.getValueFactory().getGauge32(3));
        testTrap(varbinds, true, true);
    }

    @Test
    public void testFailVbOid() throws Exception {
        final Map<String, SnmpValue> varbinds = new LinkedHashMap<>();
        varbinds.put(".1.2.3.4.5.6.7.1", SnmpUtils.getValueFactory().getGauge32(1));
        varbinds.put(".1.2.3.4.5.6.7.3", SnmpUtils.getValueFactory().getGauge32(3));
        varbinds.put(".1.2.3.4.5.6.7.2", SnmpUtils.getValueFactory().getGauge32(99));
        testTrap(varbinds, false, false);
    }

    @Test
    public void testSuccessVbOid() throws Exception {
        final Map<String, SnmpValue> varbinds = new LinkedHashMap<>();
        varbinds.put(".1.2.3.4.5.6.7.1", SnmpUtils.getValueFactory().getGauge32(1));
        varbinds.put(".1.2.3.4.5.6.7.3", SnmpUtils.getValueFactory().getGauge32(3));
        varbinds.put(".1.2.3.4.5.6.7.2", SnmpUtils.getValueFactory().getGauge32(2));
        testTrap(varbinds, false, true);
    }
}
