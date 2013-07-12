/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

/**
 * <p>ResourceDao interface.</p>
 */
public interface ResourceDao {
    
    /**
     * <p>getRrdDirectory</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getRrdDirectory();
    
    /**
     * <p>getRrdDirectory</p>
     *
     * @param verify a boolean.
     * @return a {@link java.io.File} object.
     */
    public File getRrdDirectory(boolean verify);

    /**
     * <p>getResourceTypes</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<OnmsResourceType> getResourceTypes();
    
    /**
     * <p>getResourceById</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public OnmsResource getResourceById(String id);

    /**
     * <p>getResourceListById</p>
     *
     * Fetch a specific list of resources by string ID.
     * @param id a {@link java.lang.String} object.
     * @return Resources or null if resources cannot be found.
     */
    public List<OnmsResource> getResourceListById(String id);
    
    /**
     * <p>findNodeResources</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findNodeResources();

    /**
     * <p>findDomainResources</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findDomainResources();
    
    /**
     * <p>findNodeSourceResources</p>
     * 
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findNodeSourceResources();
    
    /**
     * <p>findTopLevelResources</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findTopLevelResources();

    /**
     * <p>getResourceForNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public OnmsResource getResourceForNode(OnmsNode node);

    /**
     * <p>getResourceForIpInterface</p>
     *
     * @param ipInterface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public OnmsResource getResourceForIpInterface(OnmsIpInterface ipInterface);
    
    /**
     * <p>getResourceForIpInterface</p>
     *
     * @param ipInterface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     * @param locationMonitor a {@link org.opennms.netmgt.model.OnmsLocationMonitor} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public OnmsResource getResourceForIpInterface(OnmsIpInterface ipInterface, OnmsLocationMonitor locationMonitor);

}
