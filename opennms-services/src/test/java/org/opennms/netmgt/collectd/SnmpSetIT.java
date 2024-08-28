/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.rpc.mock.MockRpcClientFactory;
import org.opennms.netmgt.mock.OpenNMSITCase;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.snmp.proxy.common.LocationAwareSnmpClientRpcImpl;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValueFactory;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.snmp.RowStatus;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
import org.snmp4j.agent.mo.snmp.SnmpNotificationMIB;
import org.snmp4j.agent.mo.snmp.SnmpTargetMIB;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.transport.TransportMappings;

import com.google.common.collect.Lists;

public class SnmpSetIT extends OpenNMSITCase {
    private static class TestSnmpAgent extends BaseAgent {
        private static final String ADDRESS = "127.0.0.1/9161";

        private TestSnmpAgent(TemporaryFolder tempFolder) throws IOException {
            super(tempFolder.newFile("conf.agent"), tempFolder.newFile("bootCounter.agent"), new CommandProcessor(new OctetString(MPv3.createLocalEngineID())));
            final MOScalar myScalar1 = new MOScalar(new OID(".1.3.0"), MOAccessImpl.ACCESS_READ_WRITE, new OctetString("initial1"));
            final MOScalar myScalar2 = new MOScalar(new OID(".1.4.0"), MOAccessImpl.ACCESS_READ_WRITE, new OctetString("initial2"));
            try {
                server.register(myScalar1, null);
                server.register(myScalar2, null);
            } catch (DuplicateRegistrationException e) {
                //ignore
            }
        }

        @Override
        protected void initTransportMappings() throws IOException {
            transportMappings = new TransportMapping[1];
            final Address addr = GenericAddress.parse(ADDRESS);
            final TransportMapping<? extends Address> tm = TransportMappings.getInstance().createTransportMapping(addr);
            transportMappings[0] = tm;
        }

        @Override
        protected void registerManagedObjects() {
        }

        @Override
        protected void unregisterManagedObjects() {
        }

        @Override
        protected void addUsmUser(USM usm) {
        }

        @Override
        protected void addNotificationTargets(final SnmpTargetMIB snmpTargetMIB, final SnmpNotificationMIB snmpNotificationMIB) {
        }

        public void start() throws IOException {
            init();
            addShutdownHook();
            getServer().addContext(new OctetString("public"));
            finishInit();
            SecurityProtocols.getInstance().addDefaultProtocols();
            run();
            sendColdStartNotification();
        }

        @Override
        protected void addViews(final VacmMIB vacmMIB) {
            // define read community access
            vacmMIB.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c, new OctetString("cpublic"), new OctetString("v1v2group1"), StorageType.nonVolatile);
            vacmMIB.addAccess(new OctetString("v1v2group1"), new OctetString("public"), SecurityModel.SECURITY_MODEL_SNMPv2c, SecurityLevel.NOAUTH_NOPRIV, MutableVACM.VACM_MATCH_EXACT, new OctetString("fullReadView1"), new OctetString("fullWriteView1"), new OctetString("fullNotifyView1"), StorageType.nonVolatile);
            vacmMIB.addViewTreeFamily(new OctetString("fullReadView1"), new OID("1.3"), new OctetString(), VacmMIB.vacmViewIncluded, StorageType.nonVolatile);
            vacmMIB.addViewTreeFamily(new OctetString("fullReadView1"), new OID("1.4"), new OctetString(), VacmMIB.vacmViewIncluded, StorageType.nonVolatile);
            // define write community access
            vacmMIB.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c, new OctetString("cprivate"), new OctetString("v1v2group2"), StorageType.nonVolatile);
            vacmMIB.addAccess(new OctetString("v1v2group2"), new OctetString("public"), SecurityModel.SECURITY_MODEL_SNMPv2c, SecurityLevel.NOAUTH_NOPRIV, MutableVACM.VACM_MATCH_EXACT, new OctetString("fullReadView2"), new OctetString("fullWriteView2"), new OctetString("fullNotifyView2"), StorageType.nonVolatile);
            vacmMIB.addViewTreeFamily(new OctetString("fullReadView2"), new OID("1.3"), new OctetString(), VacmMIB.vacmViewIncluded, StorageType.nonVolatile);
            vacmMIB.addViewTreeFamily(new OctetString("fullWriteView2"), new OID("1.3"), new OctetString(), VacmMIB.vacmViewIncluded, StorageType.nonVolatile);
            vacmMIB.addViewTreeFamily(new OctetString("fullReadView2"), new OID("1.4"), new OctetString(), VacmMIB.vacmViewIncluded, StorageType.nonVolatile);
            vacmMIB.addViewTreeFamily(new OctetString("fullWriteView2"), new OID("1.4"), new OctetString(), VacmMIB.vacmViewIncluded, StorageType.nonVolatile);
        }

        @Override
        protected void addCommunities(final SnmpCommunityMIB communityMIB) {
            // read community
            final Variable[] com2sec = new Variable[]{new OctetString("public"), new OctetString("cpublic"), getAgent().getContextEngineID(), new OctetString("public"), new OctetString(), new Integer32(StorageType.nonVolatile), new Integer32(RowStatus.active)};
            final MOTableRow row = communityMIB.getSnmpCommunityEntry().createRow(new OctetString("public2public").toSubIndex(true), com2sec);
            communityMIB.getSnmpCommunityEntry().addRow((SnmpCommunityMIB.SnmpCommunityEntryRow) row);
            // write community
            final Variable[] com2sec2 = new Variable[]{new OctetString("private"), new OctetString("cprivate"), getAgent().getContextEngineID(), new OctetString("public"), new OctetString(), new Integer32(StorageType.nonVolatile), new Integer32(RowStatus.active)};
            final MOTableRow row2 = communityMIB.getSnmpCommunityEntry().createRow(new OctetString("public2public").toSubIndex(true), com2sec2);
            communityMIB.getSnmpCommunityEntry().addRow((SnmpCommunityMIB.SnmpCommunityEntryRow) row2);
        }
    }

    private final LocationAwareSnmpClient m_locationAwareSnmpClient = new LocationAwareSnmpClientRpcImpl(new MockRpcClientFactory());

    private TestSnmpAgent testSnmpAgent;

    @Before
    @Override
    public void setUp() throws Exception {
        setStartEventd(false);
        super.setUp();

        SnmpUtils.unsetStrategyResolver();
        System.getProperties().remove("org.opennms.snmp.strategyClass");

        final TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        testSnmpAgent = new TestSnmpAgent(temporaryFolder);
        testSnmpAgent.start();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        testSnmpAgent.stop();
        testSnmpAgent = null;
    }

    @Test
    public void testSnmpSet() throws InterruptedException, ExecutionException {
        final SnmpAgentConfig snmpAgentConfig = new SnmpAgentConfig();
        snmpAgentConfig.setAddress(myLocalHost());
        snmpAgentConfig.setPort(9161);
        snmpAgentConfig.setReadCommunity("public");
        snmpAgentConfig.setWriteCommunity("private");
        snmpAgentConfig.setVersion(SnmpAgentConfig.VERSION2C);
        snmpAgentConfig.setRetries(2);

        // first get, text should be "initial1"
        final SnmpValue result1 = m_locationAwareSnmpClient.get(snmpAgentConfig, SnmpObjId.get(".1.3.0")).execute().get();
        assertEquals("initial1", result1.toString());

        // invoke set, return value should be "foobar"
        final SnmpValue modifiedText = new Snmp4JValueFactory().getOctetString("foobar".getBytes());
        final SnmpValue result2 = m_locationAwareSnmpClient.set(snmpAgentConfig, SnmpObjId.get(".1.3.0"), modifiedText).execute().get();
        assertEquals("foobar", result2.toString());

        // now get again, value should be "foobar" now
        final SnmpValue result3 = m_locationAwareSnmpClient.get(snmpAgentConfig, SnmpObjId.get(".1.3.0")).execute().get();
        assertEquals("foobar", result3.toString());
    }

    @Test
    public void testTwoSnmpSet() throws InterruptedException, ExecutionException {
        final SnmpAgentConfig snmpAgentConfig = new SnmpAgentConfig();
        snmpAgentConfig.setAddress(myLocalHost());
        snmpAgentConfig.setPort(9161);
        snmpAgentConfig.setReadCommunity("public");
        snmpAgentConfig.setWriteCommunity("private");
        snmpAgentConfig.setVersion(SnmpAgentConfig.VERSION2C);
        snmpAgentConfig.setRetries(2);

        // first get, text should be "initial1"
        final SnmpValue result1 = m_locationAwareSnmpClient.get(snmpAgentConfig, SnmpObjId.get(".1.3.0")).execute().get();
        assertEquals("initial1", result1.toString());
        final SnmpValue result2 = m_locationAwareSnmpClient.get(snmpAgentConfig, SnmpObjId.get(".1.4.0")).execute().get();
        assertEquals("initial2", result2.toString());

        // invoke set, passing both variable changes at once, return value should be "foobar1" or "foobar2"
        final SnmpValue modifiedText1 = new Snmp4JValueFactory().getOctetString("foobar1".getBytes());
        final SnmpValue modifiedText2 = new Snmp4JValueFactory().getOctetString("foobar2".getBytes());
        final SnmpValue result3 = m_locationAwareSnmpClient.set(snmpAgentConfig, Lists.newArrayList(SnmpObjId.get(".1.3.0"), SnmpObjId.get(".1.4.0")), Lists.newArrayList(modifiedText1, modifiedText2)).execute().get();
        assertTrue("foobar1".equals(result3.toString()) || "foobar2".equals(result3.toString()));

        // now get again, values should be "foobar1" and "foobar2" now
        final SnmpValue result4 = m_locationAwareSnmpClient.get(snmpAgentConfig, SnmpObjId.get(".1.3.0")).execute().get();
        assertEquals("foobar1", result4.toString());
        final SnmpValue result5 = m_locationAwareSnmpClient.get(snmpAgentConfig, SnmpObjId.get(".1.4.0")).execute().get();
        assertEquals("foobar2", result5.toString());
    }
}
