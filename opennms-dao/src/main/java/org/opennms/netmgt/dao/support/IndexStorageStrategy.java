/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.support;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.config.StorageStrategy;
import org.opennms.netmgt.config.StorageStrategyService;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.datacollection.Parameter;

public class IndexStorageStrategy implements StorageStrategy {
    
    private static final Logger LOG = LoggerFactory.getLogger(IndexStorageStrategy.class);
    
    private String m_resourceTypeName;
    protected StorageStrategyService m_storageStrategyService;

    /** {@inheritDoc} */
    @Override
    public String getRelativePathForAttribute(String resourceParent, String resource,
            String attribute) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(resourceParent);
        buffer.append(File.separator);
        buffer.append(m_resourceTypeName);
        buffer.append(File.separator);
        buffer.append(resource);
        if (attribute != null) {
            buffer.append(File.separator);
            buffer.append(attribute);
            buffer.append(RrdFileConstants.getRrdSuffix());
        }
        return buffer.toString();
    }

    /** {@inheritDoc} */
    @Override
    public void setResourceTypeName(String name) {
        m_resourceTypeName = name;
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceTypeName() {
        return m_resourceTypeName;
    }

    /** {@inheritDoc} */
    @Override
    public String getResourceNameFromIndex(CollectionResource resource) {
        return resource.getInstance();
    }

    /** {@inheritDoc} */
    @Override
    public void setStorageStrategyService(StorageStrategyService agent) {
        m_storageStrategyService = agent;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(List<Parameter> parameterCollection) throws IllegalArgumentException {
        // Empty method, this strategy takes no parameters
    }
}
