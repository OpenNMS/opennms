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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LazySet;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.LocationMonitorIpInterface;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

public class DistributedStatusResourceType implements OnmsResourceType {
    
    private static final Logger LOG = LoggerFactory.getLogger(DistributedStatusResourceType.class);
    
    /** Constant <code>DISTRIBUTED_DIRECTORY="distributed"</code> */
    public static final String DISTRIBUTED_DIRECTORY = "distributed";
    
    private ResourceDao m_resourceDao;
    private LocationMonitorDao m_locationMonitorDao;
    
    /**
     * <p>Constructor for DistributedStatusResourceType.</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     * @param locationMonitorDao a {@link org.opennms.netmgt.dao.api.LocationMonitorDao} object.
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
    @Override
    public String getLabel() {
        return "Distributed Status";
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "distributedStatus";
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForDomain(String domain) {
        List<OnmsResource> empty = Collections.emptyList();
        return empty;
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForNode(int nodeId) {
        LinkedList<OnmsResource> resources =
            new LinkedList<OnmsResource>();
        
        Collection<LocationMonitorIpInterface> statuses = m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(nodeId);

        for (LocationMonitorIpInterface status : statuses) {
            String definitionName = status.getLocationMonitor().getDefinitionName();
            int id = status.getLocationMonitor().getId();
            final OnmsIpInterface ipInterface = status.getIpInterface();
			String ipAddr = InetAddressUtils.str(ipInterface.getIpAddress());
            
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
    @Override
    public boolean isResourceTypeOnNodeSource(String nodeSource, int nodeId) {
        // is this right?
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForNodeSource(String nodeSource, int nodeId) {
        // is this right?
        List<OnmsResource> empty = Collections.emptyList();
        return empty;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isResourceTypeOnDomain(String domain) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
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
    
    public class AttributeLoader implements LazySet.Loader<OnmsAttribute> {
        private String m_definitionName;
        private int m_locationMonitorId;
        private String m_intf;

        public AttributeLoader(String definitionName, int locationMonitorId, String intf) {
            m_definitionName = definitionName;
            m_locationMonitorId = locationMonitorId;
            m_intf = intf;
        }

        @Override
        public Set<OnmsAttribute> load() {
            LOG.debug("lazy-loading attributes for distributed status resource {}-{}/{}", m_definitionName, m_locationMonitorId, m_intf);
            
            return ResourceTypeUtils.getAttributesAtRelativePath(m_resourceDao.getRrdDirectory(), getRelativeInterfacePath(m_locationMonitorId, m_intf));
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getLinkForResource(OnmsResource resource) {
        return null;
    }
}
