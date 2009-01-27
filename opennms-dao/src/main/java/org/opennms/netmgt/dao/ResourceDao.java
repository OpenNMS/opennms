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
// 2009 Jan 26: Add getResourceListById - part of ksc performance improvement. - ayres@opennms.org
// 2008 Oct 19: Cleanup getResourceById methods, changing to getResourceById
//              and loadResourceById, for returning null when the resource
//              isn't found, and throwing an exception, respectively. - dj@opennms.org
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
package org.opennms.netmgt.dao;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

public interface ResourceDao {
    
    public File getRrdDirectory();
    
    public File getRrdDirectory(boolean verify);

    public Collection<OnmsResourceType> getResourceTypes();
    
    public OnmsResource getResourceById(String id);

    public OnmsResource loadResourceById(String id);
    
    public List<OnmsResource> getResourceListById(String id);
    
    public List<OnmsResource> findNodeResources();

    public List<OnmsResource> findDomainResources();
    
    public List<OnmsResource> findTopLevelResources();

    public OnmsResource getResourceForNode(OnmsNode node);

    public OnmsResource getResourceForIpInterface(OnmsIpInterface ipInterface);
    
    public OnmsResource getResourceForIpInterface(OnmsIpInterface ipInterface, OnmsLocationMonitor locationMonitor);

}
