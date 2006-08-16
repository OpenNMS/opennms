package org.opennms.netmgt.collectd;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockLogAppender;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.snmp.mock.MockSnmpAgent;

import junit.framework.TestCase;

public class SnmpCollectorTest extends TestCase {
    private static final String s_rrdConfig = "org.opennms.rrd.strategyClass=org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy";
    
	private SnmpCollector m_snmpCollector;
	private MockSnmpAgent m_agent;
	
    @Override
	protected void setUp() throws Exception {
		super.setUp();
        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
		MockLogAppender.setupLogging();
		
        MockNetwork m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "Router");
        m_network.addInterface("127.0.0.1");
        m_network.addService("ICMP");

        MockDatabase m_db = new MockDatabase();
        m_db.populate(m_network);
        
        DataSourceFactory.setInstance(m_db);
    }

    @Override
	public void runTest() throws Throwable {
		super.runTest();
		MockLogAppender.assertNoWarningsOrGreater();
	}
	
	@Override
	protected void tearDown() throws Exception {
		if (m_agent != null) {
			m_agent.shutDownAndWait();
		}
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
        super.tearDown();
	}

	
	public void testInstantiate() throws Exception {
		new SnmpCollector();
	}
	
	public void testInitialize() throws Exception {
		initialize();
	}

	public void testCollect() throws Exception {
		String svcName = "SNMP";
		
		String m_snmpConfig = "<?xml version=\"1.0\"?>\n"
			+ "<snmp-config port=\"1691\" retry=\"3\" timeout=\"800\"\n"
		    + "               read-community=\"public\"\n"
		    + "               version=\"v1\">\n"
		    + "</snmp-config>\n";
		
        initializeAgent();
		
		Reader dataCollectionConfig = getReaderForFile("/org/opennms/netmgt/config/datacollection-config.xml");
		initialize(new StringReader(m_snmpConfig), dataCollectionConfig);
		dataCollectionConfig.close();

		OnmsNode node = new OnmsNode();
		node.setId(new Integer(1));
		node.setSysObjectId(".1.3.6.1.4.1.1588.2.1.1.1");
		OnmsIpInterface iface = new OnmsIpInterface("127.0.0.1", node);
		
        Collection outageCalendars = new LinkedList();
        
        Package pkg = new Package();
        Filter filter = new Filter();
        filter.setContent("IPADDR IPLIKE *.*.*.*");
        pkg.setFilter(filter);
        Service service = new Service();
        service.setName(svcName);
        pkg.addService(service);
        
        CollectdPackage wpkg = new CollectdPackage(pkg, "foo", false);
        CollectionSpecification spec = new CollectionSpecification(wpkg, svcName, outageCalendars, m_snmpCollector);
        
        CollectionAgent agent = new CollectionAgent(iface);

        assertEquals("collection status", ServiceCollector.COLLECTION_SUCCEEDED, spec.collect(agent));
	}
	
	public void testBrocadeCollect() throws Exception {
		String svcName = "SNMP";
		
		String m_snmpConfig = "<?xml version=\"1.0\"?>\n"
			+ "<snmp-config port=\"1691\" retry=\"3\" timeout=\"800\"\n"
		    + "               read-community=\"public\"\n"
		    + "               version=\"v1\">\n"
		    + "</snmp-config>\n";

        initializeAgent("target/test-classes/org/opennms/netmgt/snmp/brocadeTestData1.properties");

		Reader dataCollectionConfig = getReaderForFile("/org/opennms/netmgt/config/datacollection-brocade-config.xml");
		initialize(new StringReader(m_snmpConfig), dataCollectionConfig);
		dataCollectionConfig.close();

		OnmsNode node = new OnmsNode();
		node.setId(new Integer(1));
		node.setSysObjectId(".1.3.6.1.4.1.1588.2.1.1.1");
		OnmsIpInterface iface = new OnmsIpInterface("127.0.0.1", node);
		
        Collection outageCalendars = new LinkedList();
        
        Package pkg = new Package();
        Filter filter = new Filter();
        filter.setContent("IPADDR IPLIKE *.*.*.*");
        pkg.setFilter(filter);
        Service service = new Service();
        service.setName(svcName);
        pkg.addService(service);
        
        CollectdPackage wpkg = new CollectdPackage(pkg, "foo", false);
        CollectionSpecification spec = new CollectionSpecification(wpkg, svcName, outageCalendars, m_snmpCollector);
        
        CollectionAgent agent = new CollectionAgent(iface);

        assertEquals("collection status", ServiceCollector.COLLECTION_SUCCEEDED, spec.collect(agent));
	}

	
	public void initialize(Reader snmpConfig, Reader dataCollectionConfig) throws MarshalException, ValidationException, IOException {
		RrdConfig.loadProperties(new ByteArrayInputStream(s_rrdConfig.getBytes()));

		SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfig));
		DataCollectionConfigFactory.setInstance(new DataCollectionConfigFactory(dataCollectionConfig));
		
        Reader rdr = getReaderForFile("/org/opennms/netmgt/config/test-database-schema.xml");
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(rdr));
        rdr.close();
		
		m_snmpCollector = new SnmpCollector();
		m_snmpCollector.initialize(null); // no properties are passed
	}

	public void initialize() throws IOException, MarshalException, ValidationException {
		Reader snmpConfig = getReaderForFile("/org/opennms/netmgt/config/snmp-config.xml");
		Reader dataCollectionConfig = getReaderForFile("/org/opennms/netmgt/config/datacollection-config.xml");
		
		initialize(snmpConfig, dataCollectionConfig);
		
		snmpConfig.close();
		dataCollectionConfig.close();
	}
    
    private void initializeAgent(String testData) throws InterruptedException {
        m_agent = MockSnmpAgent.createAgentAndRun(
				new File(testData),
				"127.0.0.1/1691");
	}
    
    private void initializeAgent() throws InterruptedException {
    	initializeAgent("target/test-classes/org/opennms/netmgt/snmp/mock/loadSnmpDataTest.properties");
    }


	public Reader getReaderForFile(String fileName) {
		InputStream is = getClass().getResourceAsStream(fileName);
		assertNotNull("could not get file resource '" + fileName + "'", is);
		return new InputStreamReader(is);
	}

}
