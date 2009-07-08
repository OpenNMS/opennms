/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.dao.support;

import java.io.File;

import org.opennms.netmgt.config.StorageStrategyService;
import org.opennms.netmgt.config.StorageStrategy;

public class IndexStorageStrategy implements StorageStrategy {
    private String m_resourceTypeName;
    protected StorageStrategyService m_storageStrategyService;

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

    public void setResourceTypeName(String name) {
        m_resourceTypeName = name;
    }

    public String getResourceTypeName() {
        return m_resourceTypeName;
    }

    public String getResourceNameFromIndex(String resourceParent, String resourceIndex) {
        return resourceIndex;
    }

    public void setStorageStrategyService(StorageStrategyService agent) {
        m_storageStrategyService = agent;
    }
}
