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

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * @author Ted Kazmark
 * @author David Hustace
 *
 */
public class NodeDaoHibernate extends AbstractDaoHibernate implements NodeDao {

    public void save(OnmsNode node) {
        getHibernateTemplate().save(node);
    }

    public OnmsNode load(Integer id) {
        return (OnmsNode)getHibernateTemplate().load(OnmsNode.class, id);
    }
    
    public OnmsNode load(int id) {
        return load(new Integer(id));
    }

    public OnmsNode get(Integer id) {
        return (OnmsNode)getHibernateTemplate().get(OnmsNode.class, id);
    }
    
    public OnmsNode get(int id) {
        return get(new Integer(id));
    }

    public Collection findAll() {
        return getHibernateTemplate().loadAll(OnmsNode.class);
    }
    
    public Set findNodes(final OnmsDistPoller distPoller) {
        return (Set)getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return new HashSet(session.createQuery("from OnmsNode where distPoller = ?")
                                        .setEntity(0, distPoller)
                                        .list());
            }
            
        });
    }

    public void evict(OnmsNode node) {
        getHibernateTemplate().evict(node);
    }
    
    public void merge(OnmsNode node) {
        getHibernateTemplate().merge(node);
    }

    public int countAll() {
        return ((Integer)findUnique("select count(*) from OnmsNode")).intValue();
    }

    public void saveOrUpdate(OnmsNode node) {
        getHibernateTemplate().saveOrUpdate(node);
    }

    public void delete(OnmsNode node) {
        getHibernateTemplate().delete(node);
    }

    public void update(OnmsNode node) {
        getHibernateTemplate().update(node);
    }

    public OnmsNode getHierarchy(Integer id) {
        return (OnmsNode)findUnique("from OnmsNode as n " +
                                    "join fetch n.assetRecord " +
                                    "join fetch n.ipInterfaces " +
                                    "join fetch n.ipInterfaces.monitoredServices " +
                                    "join fetch n.ipInterfaces.monitoredServices.serviceType " +
                                    "where n.id = ?", id);
    }

	public OnmsNode findByAssetNumber(String assetNumber) {
		return (OnmsNode)findUnique("from OnmsNode as n where n.assetRecord.assetNumber = ?", assetNumber);
	}

	public Collection findByLabel(String label) {
		return find("from OnmsNode as n where n.label = ?", label);
	}

    public Collection<OnmsNode> findAllByVarCharAssetColumn(String columnName, String columnValue) {
        return find("from OnmsNode as n where n.assetRecord."+columnName+" = ?", columnValue);
    }

    public Collection<OnmsNode> findAllByVarCharAssetColumnCategoryList(String ColumnName, String ColumnValue, Collection<String> categoryNames) {
        // TODO Auto-generated method stub
        return null;
    }
	

    
}
