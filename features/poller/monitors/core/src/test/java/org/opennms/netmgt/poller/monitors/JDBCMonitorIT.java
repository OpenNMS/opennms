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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.JUnitHttpServerExecutionListener;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.dao.mock.MockMonitoringLocationDao;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.poller.mock.MonitorTestUtils;
import org.opennms.netmgt.utils.DnsUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
    "classpath:/META-INF/opennms/emptyContext.xml",
})
@JUnitConfigurationEnvironment
@DirtiesContext
public class JDBCMonitorIT {
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    @Test
    public void testParameterSubstitution() throws Exception {
        JDBCMonitor monitor = new JDBCMonitor();
        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        parameters.put("url", "{nodeLabel}");
        MockMonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", false), "JDBC");
        Map<String, Object> subbedParams = monitor.getRuntimeAttributes(svc, parameters);
        assertTrue(subbedParams.get("subbed-url").toString().equals("localhost"));
    }
}
