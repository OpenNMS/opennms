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
package org.opennms.netmgt.collection.persistence.tcp;

import java.util.Set;

import org.opennms.netmgt.collection.api.AbstractPersister;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpBasePersister extends AbstractPersister {
    
    protected static final Logger LOG = LoggerFactory.getLogger(TcpBasePersister.class);

    private final TcpOutputStrategy m_tcpStrategy;

    private TcpPersistOperationBuilder m_builder;

    protected TcpBasePersister(ServiceParameters params, RrdRepository repository, TcpOutputStrategy tcpStrategy) {
        super(params, repository);
        m_tcpStrategy = tcpStrategy;
    }

    @Override
    protected void persistStringAttribute(ResourcePath path, String key, String value) {
        m_builder.setStringAttributeValue(key, value);
    }

    protected TcpPersistOperationBuilder createBuilder(CollectionResource resource, String name, Set<CollectionAttributeType> attributeTypes) {
        final TcpPersistOperationBuilder builder = new TcpPersistOperationBuilder(getTcpStrategy(), resource, name);
        if (resource.getTimeKeeper() != null) {
            builder.setTimeKeeper(resource.getTimeKeeper());
        }
        return builder;
    }

    protected void setBuilder(TcpPersistOperationBuilder builder) {
        m_builder = builder;
        super.setBuilder(builder);
    }

    public TcpOutputStrategy getTcpStrategy() {
        return m_tcpStrategy;
    }
}
