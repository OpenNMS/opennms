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
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.LazySet;
import org.opennms.netmgt.config.StorageStrategy;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.springframework.orm.ObjectRetrievalFailureException;

public class GenericIndexResourceType implements OnmsResourceType {
    private String m_name;
    private String m_label;
    private ResourceDao m_resourceDao;
    private StorageStrategy m_storageStrategy;

    public GenericIndexResourceType(ResourceDao resourceDao, String name, String label, StorageStrategy storageStrategy) {
        m_resourceDao = resourceDao;
        m_name = name;
        m_label = label;
        m_storageStrategy = storageStrategy;
    }
    
    public String getName() {
        return m_name;
    }
    
    public String getLabel() {
        return m_label;
    }
    
    public StorageStrategy getStorageStrategy() {
        return m_storageStrategy;
    }
    
    public boolean isResourceTypeOnNode(int nodeId) {
      return getResourceTypeDirectory(nodeId, false).isDirectory();
    }
    
    private File getResourceTypeDirectory(int nodeId, boolean verify) {
        File snmp = new File(m_resourceDao.getRrdDirectory(verify), DefaultResourceDao.SNMP_DIRECTORY);
        
        File node = new File(snmp, Integer.toString(nodeId));
        if (verify && !node.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No node directory exists for node " + nodeId + ": " + node);
        }

        File generic = new File(node, getName());
        if (verify && !generic.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No node directory exists for generic index " + getName() + ": " + generic);
        }

        return generic;
    }
    
    private File getResourceDirectory(int nodeId, String index) {
        return getResourceDirectory(nodeId, index, false);
    }
    
    private File getResourceDirectory(int nodeId, String index, boolean verify) {
        return new File(getResourceTypeDirectory(nodeId, verify), index);

    }
    
    
    public List<OnmsResource> getResourcesForNode(int nodeId) {
        ArrayList<OnmsResource> resources =
            new ArrayList<OnmsResource>();

        List<String> indexes = getQueryableIndexesForNode(nodeId);
        for (String index : indexes) {
            resources.add(getResourceByNodeAndIndex(nodeId, index));
        }
        return OnmsResource.sortIntoResourceList(resources);
    }
    
    public List<String> getQueryableIndexesForNode(int nodeId) {
        File nodeDir = getResourceTypeDirectory(nodeId, true);
        
        List<String> indexes = new LinkedList<String>();
        
        File[] indexDirs =
            nodeDir.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER);

        if (indexDirs == null) {
            return indexes;
        }
        
        for (File indexDir : indexDirs) {
            indexes.add(indexDir.getName());
        }
        
        return indexes;
    }

    
    public OnmsResource getResourceByNodeAndIndex(int nodeId,
            String index) {
        String label = index;

        Set<OnmsAttribute> set =
            new LazySet<OnmsAttribute>(new AttributeLoader(nodeId, index));
        return new OnmsResource(index, label, this, set);
    }


    public class AttributeLoader implements LazySet.Loader<OnmsAttribute> {
    
        private int m_nodeId;
        private String m_index;

        public AttributeLoader(int nodeId, String index) {
            m_nodeId = nodeId;
            m_index = index;
        }

        public Set<OnmsAttribute> load() {
            File resourceDirectory = getResourceDirectory(m_nodeId, m_index); 
            List<String> dataSources =
                ResourceTypeUtils.getDataSourcesInDirectory(resourceDirectory);

            Set<OnmsAttribute> attributes =
                new HashSet<OnmsAttribute>(dataSources.size());
        
            for (String dataSource : dataSources) {
                attributes.add(new RrdGraphAttribute(dataSource));
            }

            return attributes;
        }

    }

    public String getRelativePathForAttribute(String resourceParent, String resource, String attribute) {
        return m_storageStrategy.getRelativePathForAttribute(DefaultResourceDao.SNMP_DIRECTORY + File.separator + resourceParent, resource, attribute);
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
