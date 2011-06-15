package org.opennms.netmgt.linkd;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
		"classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-daemon.xml",
		"classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
		"classpath:/META-INF/opennms/mockEventIpcManager.xml",
		"classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
		"classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
		"classpath:/META-INF/opennms/applicationContext-linkd.xml",
		"classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class LinkdTest {

	@SuppressWarnings("unused")
    @Autowired
	private Linkd m_linkd;

	@Autowired
	private NodeDao m_nodeDao;

	@Before
	public void setUp() throws Exception {
		// Use the mock.logLevel system property to control the log level
		MockLogAppender.setupLogging(true);

		NetworkBuilder nb = new NetworkBuilder();
		nb.addNode("test.example.com").setForeignSource("rancid").setForeignId("1").setSysObjectId(".1.3");
		nb.addInterface("192.168.0.1");
		m_nodeDao.save(nb.getCurrentNode());
		m_nodeDao.flush();

		// Make sure that the localhost SNMP connection config factory has overridden
		// the normal config factory
		///assertTrue(m_adapter.getSnmpPeerFactory() instanceof ProxySnmpAgentConfigFactory);
	}

	@Test
	@Ignore
	//@JUnitSnmpAgent(resource = "snmpTestData.properties")
	public void testSomething() throws Exception {
	    // TODO: Add some functionality tests
	}
}
