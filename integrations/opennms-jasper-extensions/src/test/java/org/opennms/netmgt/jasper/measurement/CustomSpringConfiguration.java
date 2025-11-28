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
package org.opennms.netmgt.jasper.measurement;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.support.InterfaceSnmpResourceType;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.impl.AbstractRrdBasedFetchStrategy;
import org.opennms.netmgt.measurements.model.QueryMetadata;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.rrd.RrdException;
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
            protected FetchResults fetchMeasurements(long start, long end, long step, int maxrows, Map<Source, String> rrdsBySource, Map<String, Object> constants, QueryMetadata metadata) throws RrdException {
                final long[] timestamps = new long[] {start, end};
                final Map columnMap = new HashMap<>();
                if (!rrdsBySource.isEmpty()) {
                    for (Source eachKey : rrdsBySource.keySet()) {
                        columnMap.put(eachKey.getLabel(), new double[]{13, 17});
                    }
                }
                return new FetchResults(timestamps, columnMap, step, constants, metadata);
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
            public boolean deleteResourceById(ResourceId resourceId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ResourceId getResourceId(CollectionResource resource, long nodeId) {
                return null;
            }
        };
    }
}
