package org.opennms.netmgt.collectd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.JMXDataCollectionConfigFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.CollectorConfigDao;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockLogAppender;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.rrd.RrdConfig;

import junit.framework.TestCase;

public class CollectorConfigDaoImplTest extends TestCase {
    private static final String s_rrdConfig = "org.opennms.rrd.strategyClass=org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy";

    @Override
	protected void setUp() throws Exception {
		super.setUp();
        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
		MockLogAppender.setupLogging();
		
        MockDatabase m_db = new MockDatabase();
//        m_db.populate(m_network);
        
        DataSourceFactory.setInstance(m_db);

	}

    @Override
	public void runTest() throws Throwable {
		super.runTest();
		MockLogAppender.assertNoWarningsOrGreater();
	}
	
	@Override
	protected void tearDown() throws Exception {
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
        super.tearDown();
	}
	
	public Reader getReaderForFile(String fileName) {
		InputStream is = getClass().getResourceAsStream(fileName);
		assertNotNull("could not get file resource '" + fileName + "'", is);
		return new InputStreamReader(is);
	}
	
	public void testInstantiate() throws MarshalException, ValidationException, IOException {
		initialize();
	}
	
	private CollectorConfigDao initialize() throws IOException, MarshalException, ValidationException {
		Reader rdr;
		
		RrdConfig.loadProperties(new ByteArrayInputStream(s_rrdConfig.getBytes()));

		rdr = getReaderForFile("/org/opennms/netmgt/config/test-database-schema.xml");
		DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(rdr));
		rdr.close();
		
		rdr = getReaderForFile("/org/opennms/netmgt/config/jmx-datacollection-testdata.xml");
		JMXDataCollectionConfigFactory.setInstance(new JMXDataCollectionConfigFactory(rdr));
		rdr.close();

		rdr = getReaderForFile("/org/opennms/netmgt/config/snmp-config.xml");
		SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
		rdr.close();

		rdr = getReaderForFile("/org/opennms/netmgt/config/datacollection-config.xml");
		DataCollectionConfigFactory.setInstance(new DataCollectionConfigFactory(rdr));
		rdr.close();

		rdr = getReaderForFile("/org/opennms/netmgt/config/collectd-testdata.xml");
		CollectdConfigFactory.setInstance(new CollectdConfigFactory(rdr, "localhost", false));
		rdr.close();

		return new CollectorConfigDaoImpl();
	}
}
