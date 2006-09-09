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
// Tab Size = 8

package org.opennms.netmgt.dao.hibernate;

import java.util.Collection;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsNode;

/**
 * @author Ted Kazmark
 * @author David Hustace
 *
 */
public class NodeDaoHibernate extends AbstractDaoHibernate<OnmsNode, Integer> implements NodeDao {

    public NodeDaoHibernate() {
		super(OnmsNode.class);
	}

    public Collection<OnmsNode> findNodes(final OnmsDistPoller distPoller) {
    	return find("from OnmsNode where distPoller = ?", distPoller);
    }

    public OnmsNode getHierarchy(Integer id) {
        return findUnique("select distinct n from OnmsNode as n " +
                          "left join fetch n.assetRecord "	 +	
                          "left join fetch n.ipInterfaces as iface " +
                          "left join fetch iface.monitoredServices as monSvc " +
                          "left join fetch monSvc.serviceType " +
                          "left join fetch monSvc.currentOutages " +
                          "where n.id = ?", id);
    	
    }

	public OnmsNode findByAssetNumber(String assetNumber) {
		return (OnmsNode)findUnique("from OnmsNode as n where n.assetRecord.assetNumber = ?", assetNumber);
	}

	public Collection<OnmsNode> findByLabel(String label) {
		return find("from OnmsNode as n where n.label = ?", label);
	}

	public Collection<OnmsNode> findAllByVarCharAssetColumn(String columnName, String columnValue) {
        return find("from OnmsNode as n where n.assetRecord."+columnName+" = ?", columnValue);
    }

	public Collection<OnmsNode> findAllByVarCharAssetColumnCategoryList(String columnName, String columnValue, Collection<OnmsCategory> categories) {
        return find("select distinct n from OnmsNode as n " +
        		"join n.categories c " +
                "left join fetch n.assetRecord "	 +	
                "left join fetch n.ipInterfaces as iface " +
                "left join fetch iface.monitoredServices as monSvc " +
                "left join fetch monSvc.serviceType " +
                "left join fetch monSvc.currentOutages " +
                "where n.assetRecord."+columnName+" = ? " +
                "and c in ?", columnValue, categories);
    }

    public Collection<OnmsNode> findAllByCategoryList(Collection<OnmsCategory> categories) {
        return find("select distinct n from OnmsNode as n " +
                "join n.categories c " +
                "left join fetch n.assetRecord "     +  
                "left join fetch n.ipInterfaces as iface " +
                "left join fetch iface.monitoredServices as monSvc " +
                "left join fetch monSvc.serviceType " +
                "left join fetch monSvc.currentOutages " +
                "where c in ?", categories);
    }

    public Collection<OnmsNode> findAllByCategoryLists(Collection<OnmsCategory> rowCatNames, Collection<OnmsCategory> colCatNames) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
