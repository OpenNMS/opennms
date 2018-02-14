/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
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
