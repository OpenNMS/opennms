/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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