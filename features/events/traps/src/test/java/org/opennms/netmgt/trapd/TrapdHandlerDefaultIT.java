/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import org.apache.activemq.broker.BrokerService;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.camel.util.KeyValueHolder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.TrapProcessor;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.EasyMockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.VariableBinding;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/emptyContext.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class TrapdHandlerDefaultIT extends CamelBlueprintTestSupport {

	private boolean mockInitialized = false;

	private static final Logger LOG = LoggerFactory.getLogger(TrapdHandlerDefaultIT.class);

	private static BrokerService m_broker = null;

	private EasyMockUtils m_mocks = new EasyMockUtils();

	private EventConfDao m_eventConfDao = m_mocks
			.createMock(EventConfDao.class);

	/**
	 * Use Aries Blueprint synchronous mode to avoid a blueprint deadlock bug.
	 * 
	 * @see https://issues.apache.org/jira/browse/ARIES-1051
	 * @see https://access.redhat.com/site/solutions/640943
	 */
	@Override
	public void doPreSetup() throws Exception {
		System.setProperty("org.apache.aries.blueprint.synchronous",
				Boolean.TRUE.toString());
		System.setProperty("de.kalpatec.pojosr.framework.events.sync",
				Boolean.TRUE.toString());

		if (!mockInitialized) {
			MockitoAnnotations.initMocks(this);
			mockInitialized = true;
		}
	}

	@Override
	public boolean isUseAdviceWith() {
		return true;
	}

	@Override
	public boolean isUseDebugger() {
		// must enable debugger
		return true;
	}

	@Override
	public String isMockEndpoints() {
		return "*";
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void addServicesOnStartup(
			Map<String, KeyValueHolder<Object, Dictionary>> services) {
		// Create a mock SyslogdConfig
		TrapdConfigBean config = new TrapdConfigBean();
		config.setSnmpTrapPort(10514);
		config.setSnmpTrapAddress("127.0.0.1");
		config.setNewSuspectOnTrap(false);

		services.put(
				TrapdConfig.class.getName(),
				new KeyValueHolder<Object, Dictionary>(config, new Properties()));
	}

	// The location of our Blueprint XML files to be used for testing
	@Override
	protected String getBlueprintDescriptor() {
		return "file:blueprint-trapd-handler-default.xml,file:src/test/resources/blueprint-empty-camel-context.xml";
	}

	@BeforeClass
	public static void startActiveMQ() throws Exception {
		m_broker = new BrokerService();
		m_broker.addConnector("tcp://127.0.0.1:61616");
		m_broker.start();
	}

	@AfterClass
	public static void stopActiveMQ() throws Exception {
		if (m_broker != null) {
			m_broker.stop();
		}
	}

	@Test
	public void testTrapd() throws Exception {
		// Expect one SyslogConnection message to be broadcast on the messaging
		// channel
		MockEndpoint broadcastTrapd = getMockEndpoint(
				"mock:activemq:broadcastTrap", false);
		broadcastTrapd.setExpectedMessageCount(1);

		MockEndpoint trapHandler = getMockEndpoint("mock:seda:trapHandler",
				false);
		trapHandler.setExpectedMessageCount(1);

		// Create a mock SyslogdConfig
		TrapdConfigBean config = new TrapdConfigBean();
		config.setSnmpTrapPort(10514);
		config.setSnmpTrapAddress("127.0.0.1");
		config.setNewSuspectOnTrap(false);

		TrapQueueProcessor trapQProcessor = new TrapQueueProcessor();
		TrapProcessor trapProcess = new TrapProcessorImpl();
		trapProcess.setAgentAddress(InetAddressUtils.ONE_TWENTY_SEVEN);
		trapProcess.setCommunity("comm");
		trapProcess.setTimeStamp(System.currentTimeMillis());
		trapProcess.setTrapAddress(InetAddressUtils.ONE_TWENTY_SEVEN);

		// create instance of snmp4JV2cTrap
		PDU snmp4JV2cTrapPdu = new PDU();
		
		OID oid = new OID(".1.3.6.1.2.1.1.3.0");
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
				new IpAddress("127.0.0.1")));

		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("Major")));
		snmp4JV2cTrapPdu.setType(PDU.NOTIFICATION);

		TrapNotification snmp4JV2cTrap = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
				InetAddressUtils.ONE_TWENTY_SEVEN, new String("public"),
				snmp4JV2cTrapPdu, trapProcess);

		trapQProcessor.setTrapNotification(snmp4JV2cTrap);

		trapQProcessor.setEventConfDao(m_eventConfDao);

		// Send a TrapQProcessor
		template.sendBody("activemq:broadcastTrap", trapQProcessor.call());

		assertMockEndpointsSatisfied();

		// Check that the input for the seda:trapHandler endpoint matches
		// the TrapQProcessor that we simulated via ActiveMQ
		TrapQueueProcessor result = trapHandler.getReceivedExchanges().get(0)
				.getIn().getBody(TrapQueueProcessor.class);
		System.out.println("Result ++++:" + result);
	}

	private static class TrapProcessorImpl implements TrapProcessor {

		private String community;

		private long timeStamp;

		private String version;

		private InetAddress agentAddress;

		private String varBind;

		private InetAddress trapAddress;

		private TrapIdentity trapIdentity;

		private SnmpObjId name;

		private SnmpValue value;

		public String getCommunity() {
			return community;
		}

		@Override
		public void setCommunity(String community) {
			this.community = community;
		}

		public long getTimeStamp() {
			return timeStamp;
		}

		@Override
		public void setTimeStamp(long timeStamp) {
			this.timeStamp = timeStamp;
		}

		public String getVersion() {
			return version;
		}

		@Override
		public void setVersion(String version) {
			this.version = version;
		}

		public InetAddress getAgentAddress() {
			return agentAddress;
		}

		@Override
		public void setAgentAddress(InetAddress agentAddress) {
			this.agentAddress = agentAddress;
		}

		public String getVarBind() {
			return varBind;
		}

		public InetAddress getTrapAddress() {
			return trapAddress;
		}

		@Override
		public void setTrapAddress(InetAddress trapAddress) {
			this.trapAddress = trapAddress;
		}

		public TrapIdentity getTrapIdentity() {
			return trapIdentity;
		}

		@Override
		public void setTrapIdentity(TrapIdentity trapIdentity) {
			this.trapIdentity = trapIdentity;
		}

		@Override
		public void processVarBind(SnmpObjId name, SnmpValue value) {
			this.name = name;
			this.value = value;

		}
	}
}
