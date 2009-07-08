/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.springframework.orm.ObjectRetrievalFailureException;

public class NodeSnmpResourceType implements OnmsResourceType {

    private ResourceDao m_resourceDao;

    public NodeSnmpResourceType(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    public String getName() {
        return "nodeSnmp";
    }
    
    public String getLabel() {
        return "SNMP Node Data";
    }
    
    public boolean isResourceTypeOnNode(int nodeId) {
        return getResourceDirectory(nodeId, false).isDirectory();
    }
    
    public File getResourceDirectory(int nodeId, boolean verify) {
        File snmp = new File(m_resourceDao.getRrdDirectory(verify), DefaultResourceDao.SNMP_DIRECTORY);
        
        File node = new File(snmp, Integer.toString(nodeId));
        if (verify && !node.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No node directory exists for node " + nodeId + ": " + node);
        }
        
        return node;
    }
    
    public List<OnmsResource> getResourcesForNode(int nodeId) {
        ArrayList<OnmsResource> resources = new ArrayList<OnmsResource>();

        Set<OnmsAttribute> attributes = ResourceTypeUtils.getAttributesAtRelativePath(m_resourceDao.getRrdDirectory(), getRelativePathForResource(nodeId));
        
        OnmsResource resource = new OnmsResource("", "Node-level Performance Data", this, attributes);
        resources.add(resource);
        return resources;
    }
    
    private String getRelativePathForResource(int nodeId) {
        return DefaultResourceDao.SNMP_DIRECTORY + File.separator + Integer.toString(nodeId);
    }

    /**
     * This resource type is never available for domains.
     * Only the interface resource type is available for domains.
     */
    public boolean isResourceTypeOnDomain(String domain) {
        return false;
    }

    @SuppressWarnings("unchecked")
    public List<OnmsResource> getResourcesForDomain(String domain) {
        return Collections.EMPTY_LIST;
    }

    public String getLinkForResource(OnmsResource resource) {
        return null;
    }
}
