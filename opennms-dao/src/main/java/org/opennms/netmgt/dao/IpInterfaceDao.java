//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jan 26: Add getInterfacesForNodes. - dj@opennms.org
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
package org.opennms.netmgt.dao;

import java.util.Collection;
import java.util.Map;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;


/**
 * @author Ted Kazmark
 * @author David Hustace
 * @author Matt Brozowski
 *
 */
public interface IpInterfaceDao extends OnmsDao<OnmsIpInterface, Integer> {

    OnmsIpInterface get(OnmsNode node, String ipAddress);
    
    OnmsIpInterface findByNodeIdAndIpAddress(Integer nodeId, String ipAddress);

    OnmsIpInterface findByForeignKeyAndIpAddress(String foreignSource, String foreignId, String ipAddress);

    Collection<OnmsIpInterface> findByIpAddress(String ipAddress);

    Collection<OnmsIpInterface> findByServiceType(String svcName);

    Collection<OnmsIpInterface> findHierarchyByServiceType(String svcName);

    /**
     * Returns a map of all IP to node ID mappings in the database.
     */
    Map<String, Integer> getInterfacesForNodes();


}
