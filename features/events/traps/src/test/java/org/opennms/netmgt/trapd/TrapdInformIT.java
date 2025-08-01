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

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.TrapdConfigFactory;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JAgentConfig;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValue;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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
        // Overrides the port that Trapd binds to and sets newSuspectOnTrap to 'true'
        "classpath:/org/opennms/netmgt/trapd/applicationContext-trapDaemonTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class TrapdInformIT {

    private static final Logger LOG = LoggerFactory.getLogger(TrapdInformIT.class);

    @Autowired
    private TrapdConfigFactory m_trapdConfig;

    @Autowired
    Trapd m_trapd;

    @Autowired
    MockEventIpcManager m_mockEventIpcManager;

    private final Snmp4JStrategy strategy = new Snmp4JStrategy();

    private final InetAddress localAddr = InetAddressUtils.getLocalHostAddress();
    private final String localhost = InetAddressUtils.toIpAddrString(localAddr);

    @BeforeClass
    public static void setUpLogging() {
        MockLogAppender.setupLogging();
    }

    @Before
    public void setUp() {
        m_mockEventIpcManager.setSynchronous(true);
        m_trapd.setSecureCredentialsVault(new TrapdIT.MockSecureCredentialsVault());
        m_trapd.onStart();
    }

    @After
    public void tearDown() {
        m_trapd.onStop();
    }

    @Test
    public void discoverEngineIdAndVerifyInformResponse() throws Exception {
        // Retrieve a v3 user from the configuration
        m_trapdConfig.getConfig().setUseAddressFromVarbind(true);
        InetAddress remoteAddr = InetAddress.getByName("10.255.1.1");
        SecurityLevel securityLevel = SecurityLevel.noAuthNoPriv;
        final Snmpv3User v3User = m_trapdConfig.getConfig().getSnmpv3UserCollection().stream()
                .filter(u -> Objects.equals(securityLevel.getSnmpValue(), u.getSecurityLevel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existing SNMPv3 user configured with security level: " + securityLevel));

        ScopedPDU scopedPDU = createPDU();
        Snmp4JAgentConfig agentConfig = createAgentConfig(m_trapd.interpolateUser(v3User), scopedPDU);
        Snmp session = createSession(agentConfig);
        if(session == null) {
            Assert.fail("Session couldn't be created");
        }

        EventBuilder defaultTrapBuilder = new EventBuilder("uei.opennms.org/default/trap", "trapd");
        defaultTrapBuilder.setInterface(remoteAddr);
        defaultTrapBuilder.setSnmpVersion("v2c");
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(defaultTrapBuilder.getEvent());

        EventBuilder newSuspectBuilder = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "trapd");
        newSuspectBuilder.setInterface(remoteAddr);
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(newSuspectBuilder.getEvent());

        // Verify authoritative engine Id matches with local Engine Id.
        byte[] engineId = session.discoverAuthoritativeEngineID(agentConfig.getTarget().getAddress(), 3000);
        Assert.assertNotNull(engineId);
        Assert.assertEquals(Snmp4JStrategy.createLocalEngineId(), new OctetString(engineId));

        sendInformVerifyResponse(session, scopedPDU, agentConfig);
        // Wait until we received the expected events
        await().until(() -> m_mockEventIpcManager.getEventAnticipator().getAnticipatedEventsReceived(), hasSize(2));
        m_trapd.onStop();
        m_trapd.onStart();
        scopedPDU = createPDU();
        // Verify authoritative engine Id matches with local Engine Id.
        engineId = session.discoverAuthoritativeEngineID(agentConfig.getTarget().getAddress(), 3000);
        Assert.assertNotNull(engineId);
        Assert.assertEquals(Snmp4JStrategy.createLocalEngineId(), new OctetString(engineId));

        sendInformVerifyResponse(session, scopedPDU, agentConfig);
        await().until(() -> m_mockEventIpcManager.getEventAnticipator().getAnticipatedEventsReceived(), hasSize(2));
        closeQuietly(session);

    }

    private void sendInformVerifyResponse(Snmp session, ScopedPDU scopedPDU, Snmp4JAgentConfig agentConfig) throws IOException {
        AtomicInteger responseEventCount = new AtomicInteger(0);
        Assert.assertEquals(responseEventCount.get(), 0);
        session.send(scopedPDU, agentConfig.getTarget(), null, new ResponseListener() {
            @Override
            public void onResponse(final ResponseEvent responseEvent) {
                ScopedPDU pdu = (ScopedPDU) responseEvent.getResponse();
                if(pdu != null && pdu.getType() == PDU.RESPONSE) {
                    responseEventCount.incrementAndGet();
                }
            }
        });

        await().atMost(10, SECONDS).until(responseEventCount::get, greaterThanOrEqualTo(1));
    }

    private Snmp createSession(Snmp4JAgentConfig agentConfig) throws Exception {
        Snmp session = agentConfig.createSnmpSession();
        try {
            session.listen();
            return session;
        } catch (final Exception e) {
            closeQuietly(session);
            Assert.fail();
        }
        return null;
    }

    private Snmp4JAgentConfig createAgentConfig(Snmpv3User v3User, ScopedPDU scopedPDU) throws Exception {
        SnmpAgentConfig config = buildAgentConfig(localhost,
                m_trapdConfig.getSnmpTrapPort(),
                5000,
                3,
                v3User.getSecurityLevel(),
                v3User.getSecurityName(),
                v3User.getAuthPassphrase(),
                v3User.getAuthProtocol(),
                v3User.getPrivacyPassphrase(),
                v3User.getPrivacyProtocol(), scopedPDU);
        return new Snmp4JAgentConfig(config);
    }



    private ScopedPDU createPDU() throws UnknownHostException {
        ScopedPDU scopedPDU = new ScopedPDU();
        scopedPDU.setType(PDU.INFORM);
        OctetString contextName = new OctetString();
        scopedPDU.setContextName(contextName);
        OctetString contextEngineID = new OctetString();
        scopedPDU.setContextEngineID(contextEngineID);
        SnmpObjId enterpriseId = SnmpObjId.get(".1.3.6.1.4.1.5813");
        SnmpObjId trapOID = SnmpObjId.get(enterpriseId, new SnmpInstId(1));
        addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), strategy.getValueFactory().getTimeTicks(0), scopedPDU);
        addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), strategy.getValueFactory().getObjectId(trapOID), scopedPDU);
        addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), strategy.getValueFactory().getObjectId(enterpriseId), scopedPDU);
        addVarBind(TrapUtils.SNMP_TRAP_ADDRESS_OID, SnmpUtils.getValueFactory().getIpAddress(InetAddress.getByName("10.255.1.1")), scopedPDU);

        return scopedPDU;
    }

    private static void closeQuietly(Snmp session) {
        if (session == null) {
            return;
        }
        try {
            session.close();
        } catch (IOException e) {
            LOG.error("error closing SNMP connection", e);
        }
    }

    private SnmpAgentConfig buildAgentConfig(String address, int port, int timeout, int retries, int securityLevel,
                                               String securityName, String authPassPhrase, String authProtocol,
                                               String privPassPhrase, String privProtocol, PDU pdu) throws UnknownHostException, Exception {

        SnmpAgentConfig config = new SnmpAgentConfig();
        config.setAddress(InetAddress.getByName(address));
        config.setPort(port);
        config.setVersion(SnmpAgentConfig.VERSION3);
        config.setSecurityLevel(securityLevel);
        config.setSecurityName(securityName);
        config.setAuthPassPhrase(authPassPhrase);
        config.setAuthProtocol(authProtocol);
        config.setPrivPassPhrase(privPassPhrase);
        config.setPrivProtocol(privProtocol);
        config.setTimeout(timeout);
        config.setRetries(retries);
        return config;

    }

    public void addVarBind(SnmpObjId name, SnmpValue value, PDU pdu) {
        OID oid = new OID(name.getIds());
        Variable val = ((Snmp4JValue) value).getVariable();
        pdu.add(new VariableBinding(oid, val));
    }


}
