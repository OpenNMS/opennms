/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.netmgt.dao.hibernate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
/**
 * @author david
 *
 */
public class IpInterfaceDaoHibernate extends AbstractDaoHibernate<OnmsIpInterface, Integer>  implements IpInterfaceDao {
    
    String m_findByServiceTypeQuery = null;

    public IpInterfaceDaoHibernate() {
        super(OnmsIpInterface.class);
        
        
        m_findByServiceTypeQuery = System.getProperty("org.opennms.dao.ipinterface.findByServiceType", 
                                                      "select distinct ipIf from OnmsIpInterface as ipIf join ipIf.monitoredServices as monSvc where monSvc.serviceType.name = ?");
        
    }

    public OnmsIpInterface get(OnmsNode node, String ipAddress) {
        return findUnique("from OnmsIpInterface as ipIf where ipIf.node = ? and ipIf.ipAddress = ?", node, ipAddress);
    }

    public Collection<OnmsIpInterface> findByIpAddress(String ipAddress) {
        return find("from OnmsIpInterface ipIf where ipIf.ipAddress = ?", ipAddress);
    }
    
    public OnmsIpInterface findByNodeIdAndIpAddress(Integer nodeId, String ipAddress) {
        return findUnique("select iface from OnmsIpInterface as iface where iface.node.id = ? and iface.ipAddress = ?", 
                          nodeId, 
                          ipAddress);
        
    }

    public OnmsIpInterface findByForeignKeyAndIpAddress(String foreignSource, String foreignId, String ipAddress) {
        return findUnique("select iface from OnmsIpInterface as iface join iface.node as node where node.foreignSource = ? and node.foreignId = ? and iface.ipAddress = ?", 
                          foreignSource, 
                          foreignId, 
                          ipAddress);
        
    }

    public Collection<OnmsIpInterface> findByServiceType(String svcName) {
        
        return find(m_findByServiceTypeQuery, svcName);
    }

    public Collection<OnmsIpInterface> findHierarchyByServiceType(String svcName) {
        return find("select distinct ipIf " +
                    "from OnmsIpInterface as ipIf " +
                    "left join fetch ipIf.node as node " +
                    "left join fetch node.assetRecord " +
                    "left join fetch ipIf.node.snmpInterfaces as snmpIf " +
                    "left join fetch snmpIf.ipInterfaces " +
                    "join ipIf.monitoredServices as monSvc " +
                    "where monSvc.serviceType.name = ?", svcName);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Integer> getInterfacesForNodes() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        
        List l = getHibernateTemplate().find("select distinct ipIf.ipAddress, ipIf.node.id from OnmsIpInterface as ipIf");

        for (Object o : l) {
            String ip = (String) ((Object[]) o)[0];
            Integer nodeId = (Integer) ((Object[]) o)[1];
            map.put(ip, nodeId);
        }
        
        return map;
    }

}
