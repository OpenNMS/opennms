/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.poller.monitors.SeleniumMonitor;
import org.opennms.netmgt.poller.monitors.SeleniumMonitor.BaseUrlUtils;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:/META-INF/opennms/emptyContext.xml")
@JUnitHttpServer(port=10342)
public class SeleniumMonitorTest {

	@Before
	public void setup() throws Exception{
	    MockLogAppender.setupLogging(true, "DEBUG");
		System.setProperty("opennms.home", "src/test/resources");
	}
	
	//Requires Firefox to be installed to run
	@Test
	@JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path = "src/test/resources/testWar"))
	public void testPollStatusNotNull() throws UnknownHostException{
	    MonitoredService monSvc = new MockMonitoredService(1, "papajohns", InetAddressUtils.addr("213.187.33.164"), "PapaJohnsSite");
	    
	    Map<String, Object> params = new HashMap<String, Object>();
	    params.put("selenium-test", "SeleniumGroovyTest.groovy");
	    params.put("base-url", "localhost");
	    params.put("port", "10342");
	    
		SeleniumMonitor ajaxPSM = new SeleniumMonitor();
		PollStatus pollStatus = ajaxPSM.poll(monSvc, params);
		
		assertNotNull("PollStatus must not be null", pollStatus);
		
		System.err.println("PollStatus message: " + pollStatus.getReason());
		assertEquals(PollStatus.available(), pollStatus);
		
	}
	
	@Test
	public void testBaseUrlUtils() 
	{
	    
	    String baseUrl = "http://${ipAddr}:8080";
	    String monSvcIpAddr = "192.168.1.1";
	    String finalUrl = "";
	    
	    finalUrl = BaseUrlUtils.replaceIpAddr(baseUrl, monSvcIpAddr);
	    
	    assertEquals("http://192.168.1.1:8080", finalUrl);
	}
	
	
	
	@After
	public void tearDown(){
		
	}
}
