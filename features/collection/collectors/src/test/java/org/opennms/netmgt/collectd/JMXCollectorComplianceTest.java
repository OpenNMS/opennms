/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.config.JMXDataCollectionConfigDao;
import org.opennms.netmgt.config.collectd.jmx.JmxCollection;
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
        JmxConfigDao jmxConfigDao = mock(JmxConfigDao.class, RETURNS_DEEP_STUBS);
        final String host = InetAddrUtils.str(InetAddrUtils.getLocalHostAddress());
        when(jmxConfigDao.getConfig().lookupMBeanServer(host, JmxServerConnector.DEFAULT_OPENNMS_JMX_PORT)).thenReturn(mbeanServer);

        JmxCollection collection = new JmxCollection();
        JMXDataCollectionConfigDao jmxCollectionDao = mock(JMXDataCollectionConfigDao.class, RETURNS_DEEP_STUBS);
        when(jmxCollectionDao.getJmxCollection(COLLECTION)).thenReturn(collection);
        return new ImmutableMap.Builder<String, Object>()
                .put("jmxConfigDao", jmxConfigDao)
                .put("jmxDataCollectionConfigDao", jmxCollectionDao)
                .build();
    }
}
