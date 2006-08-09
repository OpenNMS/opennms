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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.web.svclayer.support;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.svclayer.AggregateStatus;
import org.opennms.web.svclayer.AggregateStatusColor;
import org.opennms.web.svclayer.AggregateStatusDefinition;
import org.opennms.web.svclayer.AggregateStatusService;

public class DefaultAggregateStatusService implements AggregateStatusService {
    
    private NodeDao m_nodeDao;

    public Collection<AggregateStatus> createAggregateStatusUsingBuilding(
            String building, Collection<AggregateStatusDefinition> categoryGrouping) {
        
        /*
         * We'll return this collection populated with all the aggregated statuss for the
         * devices in the building (site) by for each group of categories.
         */
        Collection<AggregateStatus> stati = new ArrayList<AggregateStatus>();
        
        /*
         * Iterate over the status definitions and create aggregated statuss
         */
        for (AggregateStatusDefinition statusDef : categoryGrouping) {
            AggregateStatus status = new AggregateStatus();
            status.setLabel(statusDef.getAggrStatusLabel());
            
            Collection<OnmsNode> nodes = m_nodeDao.findAllByVarCharAssetColumnCategoryList("building", building, statusDef.getCategories());
            
            status.setDownEntityCount(computeDownCount(nodes));
            status.setTotalEntityCount(nodes.size());
            status.setColor(computeColor(nodes, status));
            stati.add(status);
        }
        
        return stati;
    }
    
    
    private Color computeColor(Collection<OnmsNode> nodes, AggregateStatus status) {
        
        Color color = AggregateStatusColor.ALL_NODES_UP;
        
        if (status.getDownEntityCount() >= 1) {
            color = AggregateStatusColor.NODES_ARE_DOWN;
            return color;
        }
        
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            OnmsNode node = (OnmsNode) it.next();
            Set<OnmsIpInterface> ifs = node.getIpInterfaces();
            for (Iterator ifIter = ifs.iterator(); ifIter.hasNext();) {
                OnmsIpInterface ipIf = (OnmsIpInterface) ifIter.next();
                Set<OnmsMonitoredService> svcs = ipIf.getMonitoredServices();
                for (Iterator svcIter = svcs.iterator(); svcIter.hasNext();) {
                    OnmsMonitoredService svc = (OnmsMonitoredService) svcIter.next();
                    if (svc.isDown()) {
                        color = AggregateStatusColor.ONE_SERVICE_DOWN;
                        return color;  //quick exit this mess
                    }
                }
            }
        }
        return color;
    }

    
    private Integer computeDownCount(Collection<OnmsNode> nodes) {
        int totalNodesDown = 0;
        
        for (OnmsNode node : nodes) {
            if (node.isDown()) {
                totalNodesDown += 1;
            }
        }
        return new Integer(totalNodesDown);
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    

}
