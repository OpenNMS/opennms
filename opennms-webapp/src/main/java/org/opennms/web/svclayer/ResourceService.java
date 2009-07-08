/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.web.svclayer;

import java.io.File;
import java.util.List;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@Transactional(readOnly = true)
public interface ResourceService {
    public File getRrdDirectory();
    public List<OnmsResource> findTopLevelResources();
    public List<OnmsResource> findNodeResources();
    public List<OnmsResource> findDomainResources();
    public List<OnmsResource> findNodeChildResources(int nodeId);
    public List<OnmsResource> findDomainChildResources(String domain);
    public List<OnmsResource> findChildResources(OnmsResource resource, String... resourceTypeMatches);
    public OnmsResource getResourceById(String id);
    public OnmsResource loadResourceById(String id);
    public PrefabGraph[] findPrefabGraphsForResource(OnmsResource resource);
    public PrefabGraph getPrefabGraph(String name);
    public PrefabGraph[] findPrefabGraphsForChildResources(OnmsResource resource, String... resourceTypeMatches);
    public void promoteGraphAttributesForResource(OnmsResource resource);
    public void promoteGraphAttributesForResource(String resourceId);
    public List<OnmsResource> getResourceListById(String resourceId);
}
