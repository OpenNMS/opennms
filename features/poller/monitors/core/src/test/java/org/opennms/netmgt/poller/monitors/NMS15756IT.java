/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.jmx.JmxConfig;
import org.opennms.netmgt.config.jmx.MBeanServer;
import org.opennms.netmgt.config.jmx.Parameter;
import org.opennms.netmgt.dao.jmx.JmxConfigDao;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.poller.mock.MonitorTestUtils;
import org.opennms.netmgt.utils.DnsUtils;

public class NMS15756IT {
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    @Test
    public void testMetadata() throws Exception {
        try (final MockedStatic<BeanUtils> beanUtils = mockStatic(BeanUtils.class)) {
            beanUtils.when(() -> BeanUtils.getBean("daoContext", "jmxConfigDao", JmxConfigDao.class)).thenReturn(new JmxConfigDao() {
                @Override
                public JmxConfig getConfig() {
                    final MBeanServer mBeanServer = new MBeanServer();
                    mBeanServer.setIpAddress("127.0.0.1");
                    mBeanServer.setPort(0);
                    final List<Parameter> parameters = new ArrayList<>();
                    final Parameter p1 = new Parameter();
                    p1.setKey("username");
                    p1.setValue("${scv:jmx:username}");
                    parameters.add(p1);
                    final Parameter p2 = new Parameter();
                    p2.setKey("password");
                    p2.setValue("${scv:jmx:password}");
                    parameters.add(p2);
                    mBeanServer.setParameters(parameters);
                    final Set<MBeanServer> mBeanServers = new HashSet<>();
                    mBeanServers.add(mBeanServer);
                    final JmxConfig jmxConfig = new JmxConfig();
                    jmxConfig.setMBeanServer(mBeanServers);

                    return jmxConfig;
                }
            });

            final JMXMonitor monitor = new JMXMonitor();
            final Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
            parameters.put("some", "string");
            final MockMonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", false), "JMX");
            final Map<String, Object> subbedParams = monitor.getRuntimeAttributes(svc, parameters);
            assertEquals(subbedParams.get("username").getClass().getSimpleName(), "ToBeInterpolated");
            assertEquals(subbedParams.get("password").getClass().getSimpleName(), "ToBeInterpolated");
        }
    }
}