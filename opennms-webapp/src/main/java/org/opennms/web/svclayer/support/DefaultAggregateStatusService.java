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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.opennms.netmgt.dao.AggregateStatusViewDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.AggregateStatusDefinition;
import org.opennms.netmgt.model.AggregateStatusView;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.svclayer.AggregateStatus;
import org.opennms.web.svclayer.AggregateStatusService;

/**
 * This service layer class creates a collection that represents the current
 * status of devices per site (a column from the asset table such as building,
 * floor, etc.)  The status per site is broken down into rows of categories from
 * the categories table.
 * 
 * example:
 * 
 *              site: HQBLDB
 * 
 *  |Routers/Switches |   1 of  20 |
 *  |Servers          |   0 of 200 |
 *  |Hubs/APs         |   5 of  30 |
 *  
 *  
 * @author david hustace
 *
 */
public class DefaultAggregateStatusService implements AggregateStatusService {
    
    private NodeDao m_nodeDao;
    private AggregateStatusViewDao m_statusViewDao;

    public AggregateStatusView createAggregateStatusView(String statusViewName) {
        AggregateStatusView statusView = m_statusViewDao.findByName(statusViewName);
        return statusView;
    }
    
    public Collection<AggregateStatus> createAggreateStatuses(AggregateStatusView statusView) {
        return createAggregateStatus(statusView.getTableName(), statusView.getColumnName(), statusView.getColumnValue(), statusView.getStatusDefinitions());
    }

    //Overloaded method that overrides the column value in the defined view
    public Collection<AggregateStatus> createAggreateStatuses(AggregateStatusView statusView, String statusSite) {
        return createAggregateStatus(statusView.getTableName(), statusView.getColumnName(), statusSite, statusView.getStatusDefinitions());
    }

    private Collection<AggregateStatus> createAggregateStatus(String tableName, String columnName, String columnValue, Collection<AggregateStatusDefinition> statusDefinitions) {
        if (tableName != null && !tableName.equalsIgnoreCase("assets")) {
            throw new UnsupportedOperationException("This service currently only implmented for aggregation on asset columns.");
        }
        return createAggregateStatusUsingAssetColumn(columnName, columnValue, statusDefinitions);
    }

    public Collection<AggregateStatus> createAggregateStatusUsingAssetColumn(String assetColumn,
            String columnValue, Collection<AggregateStatusDefinition> categoryGrouping) {
        
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
            status.setLabel(statusDef.getName());
            
            Collection<OnmsNode> nodes = m_nodeDao.findAllByVarCharAssetColumnCategoryList(assetColumn, columnValue, statusDef.getCategories());
            
//            for (OnmsNode node : nodes) {
//                m_nodeDao.getHierarchy(node.getId());
//            }
            
            status.setDownEntityCount(computeDownCount(nodes));
            status.setTotalEntityCount(nodes.size());
            status.setStatus(computeStatus(nodes, status));
            stati.add(status);
        }
        
        return stati;
    }
    
    
    private String computeStatus(Collection<OnmsNode> nodes, AggregateStatus status) {
        
        String color = AggregateStatus.ALL_NODES_UP;
        
        if (status.getDownEntityCount() >= 1) {
            color = AggregateStatus.NODES_ARE_DOWN;
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
                        color = AggregateStatus.ONE_SERVICE_DOWN;
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
    
    public void setStatusViewDao(AggregateStatusViewDao dao) {
        m_statusViewDao = dao;
    }

}
