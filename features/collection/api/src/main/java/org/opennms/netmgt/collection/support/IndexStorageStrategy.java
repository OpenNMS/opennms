/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.support;

import java.io.File;
import java.util.List;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.StorageStrategy;
import org.opennms.netmgt.collection.api.StorageStrategyService;
import org.opennms.netmgt.config.datacollection.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexStorageStrategy implements StorageStrategy {
    
    private static final Logger LOG = LoggerFactory.getLogger(IndexStorageStrategy.class);
    
    private String m_resourceTypeName;
    protected StorageStrategyService m_storageStrategyService;

    /** {@inheritDoc} */
    @Override
    public final String getRelativePathForAttribute(String resourceParent, String instance) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(resourceParent);
        buffer.append(File.separator);
        buffer.append(m_resourceTypeName);
        buffer.append(File.separator);
        buffer.append(instance);
        return buffer.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final void setResourceTypeName(String name) {
        m_resourceTypeName = name;
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public final String getResourceTypeName() {
        return m_resourceTypeName;
    }

    /** {@inheritDoc} */
    @Override
    public String getResourceNameFromIndex(CollectionResource resource) {
        // Use the instance value as the name of the resource
        return resource.getInstance();
    }

    /** {@inheritDoc} */
    @Override
    public final void setStorageStrategyService(StorageStrategyService agent) {
        m_storageStrategyService = agent;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(List<Parameter> parameterCollection) throws IllegalArgumentException {
        // Empty method, this strategy takes no parameters
    }
}
