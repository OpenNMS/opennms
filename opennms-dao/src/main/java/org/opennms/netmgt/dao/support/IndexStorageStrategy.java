//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.support;

import java.io.File;
import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.StorageStrategyService;
import org.opennms.netmgt.config.StorageStrategy;
import org.opennms.netmgt.config.datacollection.Parameter;

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

    public void setParameters(List<Parameter> parameterCollection) {
        // Empty method, this strategy takes no parameters
    }
    
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}
