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
// Modifications:
//
// 2007 Apr 05: Remove getRelativePathForAttribute and move attribute loading to
//              ResourceTypeUtils.getAttributesAtRelativePath. - dj@opennms.org
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.LazySet;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.LocationMonitorIpInterface;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * <p>DistributedStatusResourceType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DistributedStatusResourceType implements OnmsResourceType {
    /** Constant <code>DISTRIBUTED_DIRECTORY="distributed"</code> */
    public static final String DISTRIBUTED_DIRECTORY = "distributed";
    
    private ResourceDao m_resourceDao;
    private LocationMonitorDao m_locationMonitorDao;
    
    /**
     * <p>Constructor for DistributedStatusResourceType.</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.ResourceDao} object.
     * @param locationMonitorDao a {@link org.opennms.netmgt.dao.LocationMonitorDao} object.
     */
    public DistributedStatusResourceType(ResourceDao resourceDao, LocationMonitorDao locationMonitorDao) {
        m_resourceDao = resourceDao;
        m_locationMonitorDao = locationMonitorDao;
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return "Distributed Status";
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return "distributedStatus";
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<OnmsResource> getResourcesForDomain(String domain) {
        return Collections.EMPTY_LIST;
    }

    /** {@inheritDoc} */
    public List<OnmsResource> getResourcesForNode(int nodeId) {
        LinkedList<OnmsResource> resources =
            new LinkedList<OnmsResource>();
        
        Collection<LocationMonitorIpInterface> statuses = m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(nodeId);

        for (LocationMonitorIpInterface status : statuses) {
            String definitionName = status.getLocationMonitor().getDefinitionName();
            int id = status.getLocationMonitor().getId();
            String ipAddr = status.getIpInterface().getIpAddress();
            
            File iface = getInterfaceDirectory(id, ipAddr);
            
            if (iface.isDirectory()) {
                resources.add(createResource(definitionName, id, ipAddr));
            }
        }
        
        return OnmsResource.sortIntoResourceList(resources);
    }
    
    /**
     * <p>getResourcesForLocationMonitor</p>
     *
     * @param locationMonitorId a int.
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> getResourcesForLocationMonitor(int locationMonitorId) {
        ArrayList<OnmsResource> resources =
            new ArrayList<OnmsResource>();

        /*
         * Verify that the node directory exists so we can throw a good
         * error message if not.
         */
        File locationMonitorDirectory;
        try {
            locationMonitorDirectory =
                getLocationMonitorDirectory(locationMonitorId, true);
        } catch (DataAccessException e) {
            throw new ObjectRetrievalFailureException("The '" + getName() + "' resource type does not exist on this location Monitor.  Nested exception is: " + e.getClass().getName() + ": " + e.getMessage(), e);
        }
        
        File[] intfDirs =
            locationMonitorDirectory.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER);

        // XXX is this test even needed?
        if (intfDirs == null) {
            return resources; 
        }

        // XXX this isn't right at all
        for (File intfDir : intfDirs) {
            String d = intfDir.getName();
            String defName = getDefinitionNameFromLocationMonitorDirectory(d);
            int id = getLocationMonitorIdFromLocationMonitorDirectory(d);
            resources.add(createResource(defName, id, intfDir.getName()));
        }

        return resources;
    }

    private OnmsResource createResource(String definitionName,
            int locationMonitorId, String intf) {
        String monitor = definitionName + "-" + locationMonitorId;
        
        String label = intf + " from " + monitor;
        String resource = locationMonitorId + File.separator + intf;

        Set<OnmsAttribute> set =
            new LazySet<OnmsAttribute>(new AttributeLoader(definitionName, locationMonitorId,
                                            intf));
        return new OnmsResource(resource, label, this, set);
    }

    /** {@inheritDoc} */
    public boolean isResourceTypeOnDomain(String domain) {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isResourceTypeOnNode(int nodeId) {
        return getResourcesForNode(nodeId).size() > 0;
    }
    
    /*
    private int getLocationMonitorIdFromResource(String resource) {
        int index = resource.indexOf(File.separator);
        if (index == -1) {
            throw new IllegalArgumentException("Resource name \"" + resource
                                               + "\" isn't a valid resource "
                                               + "for resource type " +
                                               getName());
        }
        String dir = resource.substring(0, index);
        return getLocationMonitorIdFromLocationMonitorDirectory(dir); 
    }
    
    private String getIpAddressFromResource(String resource) {
        int index = resource.indexOf(File.separator);
        if (index == -1) {
            throw new IllegalArgumentException("Resource name \"" + resource
                                               + "\" isn't a valid resource "
                                               + "for resource type " +
                                               getName());
        }
        return resource.substring(index + 1);
    }
    */

    private String getDefinitionNameFromLocationMonitorDirectory(String dir) {
        int index = dir.indexOf("-");
        if (index == -1) {
            throw new IllegalArgumentException("Location monitor directory \""
                                               + dir + "\" isn't a valid "
                                               + "location monitor directory");
        }
        return dir.substring(0, index);
    }

    private int getLocationMonitorIdFromLocationMonitorDirectory(String dir) {
        int index = dir.indexOf("-");
        if (index == -1) {
            throw new IllegalArgumentException("Location monitor directory \""
                                               + dir + "\" isn't a valid "
                                               + "location monitor directory");
        }
        return Integer.parseInt(dir.substring(index + 1));
    }
    
    /**
     * <p>getInterfaceDirectory</p>
     *
     * @param id a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    public File getInterfaceDirectory(int id, String ipAddr) {
        return new File(m_resourceDao.getRrdDirectory(), getRelativeInterfacePath(id, ipAddr));
    }
    
    /**
     * <p>getRelativeInterfacePath</p>
     *
     * @param id a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getRelativeInterfacePath(int id, String ipAddr) {
        return DefaultResourceDao.RESPONSE_DIRECTORY
            + File.separator + DISTRIBUTED_DIRECTORY
            + File.separator + Integer.toString(id)
            + File.separator + ipAddr;
    }


    private File getLocationMonitorDirectory(int locationMonitorId, boolean verify) throws ObjectRetrievalFailureException {
        return getLocationMonitorDirectory(Integer.toString(locationMonitorId), verify);
    }
    
    private File getLocationMonitorDirectory(String locationMonitorId, boolean verify) throws ObjectRetrievalFailureException {
        File locationMonitorDirectory = new File(m_resourceDao.getRrdDirectory(verify), locationMonitorId);

        if (verify && !locationMonitorDirectory.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No node directory exists for node " + locationMonitorId + ": " + locationMonitorDirectory);
        }
        
        return locationMonitorDirectory;
    }
    
    private Category log() {
        return ThreadCategory.getInstance();
    }
    
    public class AttributeLoader implements LazySet.Loader<OnmsAttribute> {
        private String m_definitionName;
        private int m_locationMonitorId;
        private String m_intf;

        public AttributeLoader(String definitionName, int locationMonitorId, String intf) {
            m_definitionName = definitionName;
            m_locationMonitorId = locationMonitorId;
            m_intf = intf;
        }

        public Set<OnmsAttribute> load() {
            if (log().isDebugEnabled()) {
                log().debug("lazy-loading attributes for distributed status resource " + (m_definitionName + "-" + m_locationMonitorId + "/" + m_intf));
            }
            
            return ResourceTypeUtils.getAttributesAtRelativePath(m_resourceDao.getRrdDirectory(), getRelativeInterfacePath(m_locationMonitorId, m_intf));
        }
    }

    /** {@inheritDoc} */
    public String getLinkForResource(OnmsResource resource) {
        return null;
    }
}
