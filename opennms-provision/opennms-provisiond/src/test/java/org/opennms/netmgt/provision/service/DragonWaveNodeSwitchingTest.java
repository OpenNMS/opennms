package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.tasks.Task;
import org.opennms.mock.snmp.JUnitSnmpAgent;
import org.opennms.mock.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.mock.snmp.MockSnmpAgentAware;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( { OpenNMSConfigurationExecutionListener.class, TemporaryDatabaseExecutionListener.class, JUnitSnmpAgentExecutionListener.class, DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/applicationContext-dao.xml", "classpath:/META-INF/opennms/applicationContext-daemon.xml", "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml", "classpath:/META-INF/opennms/mockEventIpcManager.xml", "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml", "classpath*:/META-INF/opennms/component-dao.xml", "classpath*:/META-INF/opennms/detectors.xml", "classpath:/importerServiceTest.xml" })
@JUnitTemporaryDatabase()
public class DragonWaveNodeSwitchingTest implements MockSnmpAgentAware {

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private Provisioner m_provisioner;

    @Autowired
    private ResourceLoader m_resourceLoader;

    @Autowired
    private MockEventIpcManager m_eventSubscriber;

    private MockSnmpAgent m_snmpAgent;

    @BeforeClass
    public static void setUpSnmpConfig() {
        SnmpPeerFactory.setFile(new File("src/test/proxy-snmp-config.xml"));

        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");

        MockLogAppender.setupLogging(props);

    }

    @Before
    public void setUp() throws Exception {
        m_provisioner.start();
    }

    public void runScan(NodeScan scan) throws InterruptedException, ExecutionException {
        Task t = scan.createTask();
        t.schedule();
        t.waitFor();
    }

    @Test
    @JUnitSnmpAgent(host = "127.0.0.1", port = 9161, resource = "classpath:/dw/walks/node1-walk.properties")
    public void testInitialSetup() throws Exception {
        
        final CountDownLatch eventRecieved = anticipateEvents(EventConstants.PROVISION_SCAN_COMPLETE_UEI, EventConstants.PROVISION_SCAN_ABORTED_UEI );

        assertEquals(".1.3.6.1.4.1.7262.2.3", getSnmpValue("192.168.255.22", ".1.3.6.1.2.1.1.2.0").toDisplayString());

        importResource("classpath:/dw/import/dw_test_import.xml");

        OnmsNode onmsNode = m_nodeDao.findByForeignId("dw", "arthur");
        eventRecieved.await();

        String sysObjectId = onmsNode.getSysObjectId();
        assertEquals(".1.3.6.1.4.1.7262.2.3", sysObjectId);

        m_snmpAgent.updateValuesFromResource(m_resourceLoader.getResource("classpath:/dw/walks/node3-walk.properties"));

        // Make sure agent reports the proper OID
        assertEquals(".1.3.6.1.4.1.7262.1", getSnmpValue("192.168.255.22", ".1.3.6.1.2.1.1.2.0").toDisplayString());

        NodeScan scan2 = m_provisioner.createNodeScan(onmsNode.getId(), onmsNode.getForeignSource(), onmsNode.getForeignId());
        runScan(scan2);

        m_nodeDao.flush();

        OnmsNode node = m_nodeDao.findByForeignId("dw", "arthur");

        String sysObjectId2 = node.getSysObjectId();
        assertEquals(".1.3.6.1.4.1.7262.1", sysObjectId2);
    }

    private CountDownLatch anticipateEvents(String... ueis) {
        final CountDownLatch eventRecieved = new CountDownLatch(1);
        m_eventSubscriber.addEventListener(new EventListener() {

            public void onEvent(Event e) {
                eventRecieved.countDown();
            }

            public String getName() {
                return "Test Initial Setup";
            }
        }, Arrays.asList(ueis));
        return eventRecieved;
    }

    private SnmpValue getSnmpValue(String host, String oid) throws UnknownHostException {
        return SnmpUtils.get(SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(host)), SnmpObjId.get(oid));
    }

    @Test
    @JUnitSnmpAgent(host = "127.0.0.1", port = 9161, resource = "classpath:/dw/walks/node3-walk.properties")
    public void testASetup() throws Exception {

        importResource("classpath:/dw/import/dw_test_import.xml");

        OnmsNode onmsNode = m_nodeDao.get(1);
        String sysObjectId = onmsNode.getSysObjectId();

        assertEquals(".1.3.6.1.4.1.7262.1", sysObjectId);
    }

    private void importResource(String location) throws Exception {
        m_provisioner.importModelFromResource(m_resourceLoader.getResource(location));
    }

    public void setMockSnmpAgent(MockSnmpAgent agent) {
        m_snmpAgent = agent;

    }
}
