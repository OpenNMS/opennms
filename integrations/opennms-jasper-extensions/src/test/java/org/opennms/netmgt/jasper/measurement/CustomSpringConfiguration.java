/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.measurement;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jrobin.core.RrdException;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.support.InterfaceSnmpResourceType;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.impl.AbstractRrdBasedFetchStrategy;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DefaultSpringConfiguration.class)
public class CustomSpringConfiguration {

    @Bean(name="measurementFetchStrategy")
    public MeasurementFetchStrategy createFetchStrategy() {
        return new AbstractRrdBasedFetchStrategy() {

            @Override
            protected FetchResults fetchMeasurements(long start, long end, long step, int maxrows, Map<Source, String> rrdsBySource, Map<String, Object> constants) throws RrdException {
                final long[] timestamps = new long[] {start, end};
                final Map columnMap = new HashMap<>();
                if (!rrdsBySource.isEmpty()) {
                    for (Source eachKey : rrdsBySource.keySet()) {
                        columnMap.put(eachKey.getLabel(), new double[]{13, 17});
                    }
                }
                return new FetchResults(timestamps, columnMap, step, constants);
            }
        };
    }

    @Bean(name="resourceDao")
    public ResourceDao createResourceDao() {
        return new ResourceDao() {
            @Override
            public Collection<OnmsResourceType> getResourceTypes() {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<OnmsResource> findTopLevelResources() {
                throw new UnsupportedOperationException();
            }

            @Override
            public OnmsResource getResourceById(ResourceId id) {
                if (id.toString().startsWith("node[1]")) {
                    final OnmsResource onmsResource = new OnmsResource(id.toString(), id.toString(), new InterfaceSnmpResourceType(null), new HashSet<OnmsAttribute>(), new ResourcePath());
                    if (id.toString().contains("interfaceSnmp[127.0.0.1]")) {
                        final RrdGraphAttribute attribute = new RrdGraphAttribute();
                        attribute.setName("ifInErrors");
                        attribute.setResource(onmsResource);
                        onmsResource.getAttributes().add(attribute);
                    }
                    return onmsResource;
                }
                return null;
            }

            @Override
            public OnmsResource getResourceForNode(OnmsNode node) {
                throw new UnsupportedOperationException();
            }

            @Override
            public OnmsResource getResourceForIpInterface(OnmsIpInterface ipInterface, OnmsLocationMonitor locationMonitor) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean deleteResourceById(ResourceId resourceId) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
