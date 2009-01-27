/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2009 Jan 26: added getResourceListById - part of ksc performance improvement. - ayres@opennms.org
 * 2008 Oct 22: Use new nmes for getResourceById/loadResourceById. - dj@opennms.org
 * 2007 Aug 02: Add findTopLevelResources(). - dj@opennms.org
 * 2007 Aug 18: (merged to trunk by ayersw)
 * 2007 May 12: Add getRrdDirectory(). - dj@opennms.org
 *  
 * Created: January 2, 2007
 *
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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
