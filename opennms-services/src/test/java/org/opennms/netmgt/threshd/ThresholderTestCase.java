/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.threshd;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.eventd.mock.EventAnticipator;
import org.opennms.netmgt.eventd.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.poller.InetNetworkInterface;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class ThresholderTestCase extends TestCase {

    private EasyMockUtils m_easyMockUtils = new EasyMockUtils();
    
	private EventAnticipator m_anticipator;
    private RrdStrategy<?,?> m_rrdStrategy;
	protected Map<Object, Object> m_serviceParameters;
	protected ThresholdNetworkInterfaceImpl m_iface;
	protected Map<Object, Object> m_parameters;
	private String m_fileName;
	private int m_step;
	protected ServiceThresholder m_thresholder;
	private MockEventIpcManager m_eventMgr;
	protected MockNetwork m_network;
    private String m_serviceName;
    private String m_ipAddress;

    @Override
	protected void setUp() throws Exception {
		super.setUp();
	}

    @Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    protected void setupEventManager() {
		m_anticipator = new EventAnticipator();
		m_eventMgr = new MockEventIpcManager();
		m_eventMgr.setEventAnticipator(m_anticipator);
	}

	protected void setupThresholdConfig(String dirName, String fileName, int nodeId, String ipAddress, String serviceName, String groupName) throws IOException, UnknownHostException, FileNotFoundException, MarshalException, ValidationException {
		File dir = new File(dirName);
		File f = createFile(dir, fileName);
		m_fileName = f.getAbsolutePath();
		m_step = 300000;
		m_iface = new ThresholdNetworkInterfaceImpl(nodeId, InetAddressUtils.addr(ipAddress));
		m_serviceParameters = new HashMap<Object, Object>();
		m_serviceParameters.put("svcName", serviceName);
		m_parameters = new HashMap<Object, Object>();
		m_parameters.put("thresholding-group", groupName);
        m_ipAddress = ipAddress;
        m_serviceName = serviceName;
		
		Resource config = new ClassPathResource("/test-thresholds.xml");
		ThresholdingConfigFactory.setInstance(new ThresholdingConfigFactory(config.getInputStream()));
		ThresholdingConfigFactory.getInstance().getGroup(groupName).setRrdRepository(dir.getParentFile().getAbsolutePath());
	}

    private File createFile(File dir, String fileName) throws IOException {
        dir.mkdirs();
		File f = new File(dir, fileName);
		PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
		out.println("unused");
		out.close();
		f.deleteOnExit();
		dir.deleteOnExit();
        return f;
    }

	protected void createMockRrd() throws Exception {
        m_rrdStrategy = m_easyMockUtils.createMock(RrdStrategy.class);
        expectRrdStrategyCalls();
        RrdUtils.setStrategy(m_rrdStrategy);
    }
    
	protected void expectRrdStrategyCalls() throws Exception {
        expect(m_rrdStrategy.getDefaultFileExtension()).andReturn(".mockRrd").anyTimes();
	}

	protected void setupDatabase() throws Exception {
		m_network = new MockNetwork();
		m_network.setCriticalService("ICMP");
		m_network.addNode(1, "Router");
		m_network.addInterface("192.168.1.1");
		m_network.addService("ICMP");
		m_network.addService("SNMP");
		m_network.addInterface("192.168.1.2");
		m_network.addService("ICMP");
		m_network.addService("SMTP");
		m_network.addNode(2, "Server");
		m_network.addInterface("192.168.1.3");
		m_network.addService("ICMP");
		m_network.addService("HTTP");
		m_network.addNode(3, "Firewall");
		m_network.addInterface("192.168.1.4");
		m_network.addService("SMTP");
		m_network.addService("HTTP");
		m_network.addInterface("192.168.1.5");
		m_network.addService("SMTP");
		m_network.addService("HTTP");
		MockDatabase db = new MockDatabase();
		db.populate(m_network);
		DataSourceFactory.setInstance(db);
	}

	protected void ensureExceededAfterFetches(String dsName, int count) {
	    ensureEventAfterFetches(count, dsName, "uei.opennms.org/threshold/highThresholdExceeded");
	}

	protected void ensureRearmedAfterFetches(String dsName, int count) {
	    ensureEventAfterFetches(count, dsName, "uei.opennms.org/threshold/highThresholdRearmed");
	}

	private void ensureEventAfterFetches(int count, String dsName, String uei) {
	    if (uei != null) {
	        EventBuilder event = MockEventUtil.createServiceEventBuilder("Test", uei, m_network.getService(1, m_ipAddress, m_serviceName), null);
	        event.addParam("ds", dsName);
	        m_anticipator.anticipateEvent(event.getEvent());
	    }
	    for(int i = 0; i < count; i++) {
	        m_thresholder.check(m_iface, m_eventMgr, m_parameters);
	    }
	    verifyAnticipated(1000);
	}

	protected void ensureNoEventAfterFetches(String dsName, int count) {
	    ensureEventAfterFetches(count, null, null);
	}

	protected void setupFetchSequence(String ds, double... values) throws NumberFormatException, RrdException {
        // FIXME ds must be used like eq(m_ds)
		for (double value : values) {
            expect(m_rrdStrategy.fetchLastValue(eq(m_fileName), eq(ds), eq(m_step))).andReturn(value);
        }
	}

	private void verifyAnticipated(long millis) {
	    // make sure the down events are received
	    MockEventUtil.printEvents("Events we're still waiting for: ", m_anticipator.waitForAnticipated(millis));
	    MockEventUtil.printEvents("Unanticipated: ", m_anticipator.unanticipatedEvents());
	    assertTrue("Expected events not forthcoming", m_anticipator.waitForAnticipated(0).isEmpty());
	    sleep(200);
	    assertEquals("Received unexpected events", 0, m_anticipator.unanticipatedEvents().size());
	    m_anticipator.reset();
	}

	public void sleep(long millis) {
	    try {
            Thread.sleep(millis);
	    } catch (InterruptedException e) {
	        // do nothing
        }
	}
    
    public void replayMocks() {
        m_easyMockUtils.replayAll();
    }
    
    public void verifyMocks() {
        m_easyMockUtils.verifyAll();
    }

    public static class ThresholdNetworkInterfaceImpl extends InetNetworkInterface implements  ThresholdNetworkInterface {
        /**
         * Generated serial version ID.
         */
        private static final long serialVersionUID = 8363288174688092210L;
        
        private int m_nodeId;
        
        public ThresholdNetworkInterfaceImpl(int nodeId, InetAddress inetAddress) {
            super(inetAddress);
            m_nodeId = nodeId;
        }
        
        public int getNodeId() {
            return m_nodeId;
        }

    }
    
    //Avoid unnecessary warnings from Junit
    public void testDoNothing() {}
}

