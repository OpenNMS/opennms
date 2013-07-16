/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.sms.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.InetNetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath*:/META-INF/spring/bundle-context.xml",
        "classpath*:/META-INF/opennms/bundle-context-opennms.xml",
        "classpath:/testContext.xml"
})
public class SMSPingMonitorTest implements InitializingBean {
	@Autowired
	ApplicationContext m_context;
	
	@Resource(name="smsService")
	SmsService m_smsService;

	MonitoredService m_service;
	
	@Override
	public void afterPropertiesSet() throws Exception {
	    BeanUtils.assertAutowiring(this);
	}

	@Before
	public void setUp() {
		
		m_service = new MonitoredService() {
                        @Override
			public InetAddress getAddress() {
				return InetAddressUtils.getLocalHostAddress();
			}

                        @Override
			public String getIpAddr() {
				return "127.0.0.1";
			}

                        @Override
			public NetworkInterface<InetAddress> getNetInterface() {
				return new InetNetworkInterface(getAddress());
			}

                        @Override
			public int getNodeId() {
				return 1;
			}

                        @Override
			public String getNodeLabel() {
				return "localhost";
			}

                        @Override
			public String getSvcName() {
				return "SMS";
			}

                        @Override
			public String getSvcUrl() {
			    return null;
			}
		};
	}

	@Test
	@DirtiesContext
	public void testPing() {
		assertNotNull(m_smsService);
		
		assertEquals("ACM0", m_smsService.getGateways().iterator().next().getGatewayId());
		
		SMSPingMonitor p = new SMSPingMonitor();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("retry", "0");
		parameters.put("timeout", "30000");
		PollStatus s = p.poll(m_service, parameters);
		System.err.println("reason = " + s.getReason());
		System.err.println("status name = " + s.getStatusName());
		assertEquals("ping should pass", PollStatus.SERVICE_AVAILABLE, s.getStatusCode());
	}
}