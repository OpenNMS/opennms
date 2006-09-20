package org.opennms.netmgt.threshd;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Properties;

import org.jmock.Mock;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class SnmpThresholderTest extends ThresholderTestCase {

	private IPv4NetworkInterface m_iface;
	private HashMap m_serviceParameters;
	private HashMap m_parameters;
	private Mock m_mockRrdStrategy;
	private MockNetwork m_network;

	public void testCreate() {
	}
	
	protected void setUp() throws Exception {
		super.setUp();
        
        MockLogAppender.setupLogging();
//	TODO: Finish pulling up into ThresholderTestCase like LatencyThresholderTest and finish writing the test case!!
        setupDatabase();

		// set this so we don't get exceptions in the log
        RrdConfig.setProperties(new Properties());
        m_mockRrdStrategy = mock(RrdStrategy.class);
        RrdUtils.setStrategy((RrdStrategy)m_mockRrdStrategy.proxy());
        m_mockRrdStrategy.expects(atLeastOnce()).method("initialize");

		Resource config = new ClassPathResource("/test-thresholds.xml");
		Reader r = new InputStreamReader(config.getInputStream());
		ThresholdingConfigFactory.setInstance(new ThresholdingConfigFactory(r));
		r.close();

        m_iface = new IPv4NetworkInterface(InetAddress.getByName("192.168.1.1"));
        m_serviceParameters = new HashMap();
        m_serviceParameters.put("svcName", "ICMP");
        m_parameters = new HashMap();
        m_parameters.put("thresholding-group", "icmp-latency");
        
        ThresholdingConfigFactory.getInstance().getGroup("icmp-latency").setRrdRepository("/tmp");

		SnmpThresholder m_thresholder = new SnmpThresholder();  
        m_thresholder.initialize(m_serviceParameters);
        m_thresholder.initialize(m_iface, m_parameters);

	}

	protected void tearDown() throws Exception {
		super.tearDown();
        MockLogAppender.assertNoWarningsOrGreater();
		
	}

}
