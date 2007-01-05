//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.threshd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Stub;
import org.jmock.core.stub.StubSequence;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class ThresholderTestCase extends MockObjectTestCase {

	private EventAnticipator m_anticipator;
	private EventProxy m_proxy;
	private Mock m_mockRrdStrategy;
	protected Map m_serviceParameters;
	protected IPv4NetworkInterface m_iface;
	protected Map m_parameters;
	private String m_fileName;
	private int m_step;
	protected ServiceThresholder m_thresholder;
	private MockEventIpcManager m_eventMgr;
	protected MockNetwork m_network;
    private String m_serviceName;
    private String m_ipAddress;

	protected void setUp() throws Exception {
		super.setUp();


	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public Stub onConsecutiveCalls(Stub[] stubs) {
	    return new StubSequence(stubs);
	}

	protected void setupEventManager() {
		m_anticipator = new EventAnticipator();
		m_eventMgr = new MockEventIpcManager();
		m_eventMgr.setEventAnticipator(m_anticipator);
		m_proxy = new EventProxy() {
	        public void send(Event e) {
	            m_eventMgr.sendNow(e);
	        }
	
	        public void send(Log log) {
	            m_eventMgr.sendNow(log);
	        }
	    };
	}

	protected void setupThresholdConfig(String dirName, String fileName, String ipAddress, String serviceName, String groupName) throws IOException, UnknownHostException, FileNotFoundException, MarshalException, ValidationException {
		File dir = new File(dirName);
		File f = createFile(dir, fileName);
		m_fileName = f.getAbsolutePath();
		m_step = 300000;
		m_iface = new IPv4NetworkInterface(InetAddress.getByName(ipAddress));
		m_serviceParameters = new HashMap();
		m_serviceParameters.put("svcName", serviceName);
		m_parameters = new HashMap();
		m_parameters.put("thresholding-group", groupName);
        m_ipAddress = ipAddress;
        m_serviceName = serviceName;
		
		Resource config = new ClassPathResource("/test-thresholds.xml");
		Reader r = new InputStreamReader(config.getInputStream());
		ThresholdingConfigFactory.setInstance(new ThresholdingConfigFactory(r));
		r.close();
		ThresholdingConfigFactory.getInstance().getGroup(groupName).setRrdRepository(dir.getParentFile().getAbsolutePath());
	}

    private File createFile(File dir, String fileName) throws IOException {
        dir.mkdirs();
		File f = new File(dir, fileName);
		PrintWriter out = new PrintWriter(new FileWriter(f));
		out.println("unused");
		out.close();
		f.deleteOnExit();
		dir.deleteOnExit();
        return f;
    }

	protected void createMockRrd() {
		// set this so we don't get exceptions in the log
	    RrdConfig.setProperties(new Properties());
		m_mockRrdStrategy = mock(RrdStrategy.class);
		RrdUtils.setStrategy((RrdStrategy)m_mockRrdStrategy.proxy());
		m_mockRrdStrategy.expects(atLeastOnce()).method("initialize");
        m_mockRrdStrategy.expects(atLeastOnce()).method("getDefaultFileExtension").will(returnValue(".mockRrd"));
	}

	protected void setupDatabase() {
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
		MockDatabase db = new MockDatabase(getName());
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
	        Event event = MockEventUtil.createServiceEvent("Test", uei, m_network.getService(1, m_ipAddress, m_serviceName), null);
	        Parms parms = new Parms();
	        Parm parm = new Parm();
	        parm.setParmName("ds");
	        Value val = new Value();
	        val.setContent(dsName);
	        parm.setValue(val);
	        parms.addParm(parm);
	        event.setParms(parms);
	        m_anticipator.anticipateEvent(event);
	    }
	    for(int i = 0; i < count; i++) {
	        m_thresholder.check(m_iface, m_proxy, m_parameters);
	    }
	    verifyAnticipated(1000);
	}

	protected void ensureNoEventAfterFetches(String dsName, int count) {
	    ensureEventAfterFetches(count, null, null);
	}

	protected void setupFetchSequence(double[] values) {
	    Stub[] stubs = new Stub[values.length];
	    for(int i = 0; i < values.length; i++) {
	        stubs[i] = returnValue(values[i]);
	    }
	    m_mockRrdStrategy
	    .expects(atLeastOnce())
	    .method("fetchLastValue")
	    .with( eq(m_fileName), eq(m_step))
	    .will(onConsecutiveCalls(stubs));
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
	    try { Thread.sleep(millis); } catch (InterruptedException e) {}
	}

}
