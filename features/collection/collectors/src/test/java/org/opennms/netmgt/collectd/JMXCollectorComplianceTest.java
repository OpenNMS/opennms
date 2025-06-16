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
package org.opennms.netmgt.collectd;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.config.JMXDataCollectionConfigDao;
import org.opennms.netmgt.config.collectd.jmx.JmxCollection;
import org.opennms.netmgt.config.jmx.JmxConfig;
import org.opennms.netmgt.config.jmx.MBeanServer;
import org.opennms.netmgt.dao.jmx.JmxConfigDao;
import org.opennms.netmgt.jmx.connection.JmxServerConnector;
import org.opennms.netmgt.snmp.InetAddrUtils;

import com.google.common.collect.ImmutableMap;

public class JMXCollectorComplianceTest extends CollectorComplianceTest {

    private static final String COLLECTION = "default";

    public JMXCollectorComplianceTest() {
        super(Jsr160Collector.class, true);
    }

    @Override
    public String getCollectionName() {
        return COLLECTION;
    }

    @Override
    public Map<String, Object> getRequiredParameters() {
        return new ImmutableMap.Builder<String, Object>()
            .put("collection", COLLECTION)
            .put("port", JmxServerConnector.DEFAULT_OPENNMS_JMX_PORT)
            .build();
    }

    @Override
    public Map<String, Object> getRequiredBeans() {
        MBeanServer mbeanServer = new MBeanServer();
        final String host = InetAddrUtils.str(InetAddrUtils.getLocalHostAddress());
        JmxConfig jmxConfig = mock(JmxConfig.class);
        when(jmxConfig.lookupMBeanServer(host, JmxServerConnector.DEFAULT_OPENNMS_JMX_PORT)).thenReturn(mbeanServer);
        JmxConfigDao jmxConfigDao = mock(JmxConfigDao.class);
        when(jmxConfigDao.getConfig()).thenReturn(jmxConfig);

        JmxCollection collection = new JmxCollection();
        JMXDataCollectionConfigDao jmxCollectionDao = mock(JMXDataCollectionConfigDao.class, RETURNS_DEEP_STUBS);
        when(jmxCollectionDao.getJmxCollection(COLLECTION)).thenReturn(collection);
        return new ImmutableMap.Builder<String, Object>()
                .put("jmxConfigDao", jmxConfigDao)
                .put("jmxDataCollectionConfigDao", jmxCollectionDao)
                .build();
    }
}
