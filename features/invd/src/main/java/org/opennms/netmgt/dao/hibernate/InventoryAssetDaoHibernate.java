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
package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.InventoryAssetDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.inventory.OnmsInventoryAsset;
import org.opennms.netmgt.model.inventory.OnmsInventoryCategory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.Session;
import org.hibernate.HibernateException;

import java.util.Collection;
import java.util.Date;
import java.sql.SQLException;

public class InventoryAssetDaoHibernate extends AbstractDaoHibernate<OnmsInventoryAsset, Integer>
        implements InventoryAssetDao {

    public InventoryAssetDaoHibernate() {
        super(OnmsInventoryAsset.class);
    }

    public OnmsInventoryAsset findByAssetId(int id) {
        return findUnique("from OnmsInventoryAsset as asset where asset.id = ?", id);
    }

    private String getEffectiveDatedCriteria(Boolean effStatus) {
    	String effstStr = "";
    	
    	if(effStatus) {
    		effstStr = "and asset.effStatus = true ";
    	}
    	
    	return	"from OnmsInventoryAsset asset " +
    			"where asset.effectiveDate = (select max(asset2.effectiveDate) "+
    			"					  from OnmsInventoryAsset asset2 " +
    			"                     where asset2.id = asset.id " +
    			"                     and asset2.effectiveDate <= ? ) " +
    			effstStr;
    }
    
    public Collection<OnmsInventoryAsset> findAllEffectiveDate(Date effdt, Boolean effStatus) {
    	if(effdt == null) {
    		effdt = new Date();
    	}
    	return find(getEffectiveDatedCriteria(effStatus), effdt);
    }
    
    public Collection<OnmsInventoryAsset> findByName(String name) {
        return findByNameEffectiveDate(name, null, true);
    }
    
    public Collection<OnmsInventoryAsset> findByNameEffectiveDate(String name, Date effdt, Boolean effStatus) {
    	if(effdt == null) {
    		effdt = new Date();
    	}
        return find(getEffectiveDatedCriteria(effStatus) + " and asset.assetName = ?", effdt, name);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Collection<OnmsInventoryAsset> findAll(final Integer offset, final Integer limit) {
        return (Collection<OnmsInventoryAsset>)getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createCriteria(OnmsInventoryAsset.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
            }
        });
    }
    
    public Collection<OnmsInventoryAsset> findByNameAndNode(String name, OnmsNode owner) {
    	return findByNameAndNodeEffectiveDate(name, owner, null, true);
    
    }
    
    public Collection<OnmsInventoryAsset> findByNameAndNodeEffectiveDate(String name, OnmsNode owner, Date effdt, Boolean effStatus) {
    	if(effdt == null) {
    		effdt = new Date();
    	}
    	
		return find(
				getEffectiveDatedCriteria(effStatus) + " and asset.ownerNode = ? and asset.assetName = ?", 
				effdt, owner, name);
	}
    
    public OnmsInventoryAsset findByNameNodeAndCategory(String name, OnmsNode owner, OnmsInventoryCategory cat) {
    	return findByNameNodeAndCategoryEffectiveDate(name, owner, cat, null, true);
    }

	public OnmsInventoryAsset findByNameNodeAndCategoryEffectiveDate(String name, OnmsNode owner, OnmsInventoryCategory cat, Date effdt, Boolean effStatus) {
		if(effdt == null) {
    		effdt = new Date();
    	}
		
		return findUnique(
				getEffectiveDatedCriteria(effStatus) + " and asset.category = ? and asset.ownerNode = ? and asset.assetName = ?", 
				effdt, cat, owner, name);
	}
	public Collection<OnmsInventoryAsset> findByCategoryAndNode(OnmsInventoryCategory category, OnmsNode owner) {
		return findByCategoryAndNodeEffectiveDate(category, owner, null, true);
		
	}
	public Collection<OnmsInventoryAsset> findByCategoryAndNodeEffectiveDate(OnmsInventoryCategory category, OnmsNode owner, Date effdt, Boolean effStatus) {
		if(effdt == null) {
    		effdt = new Date();
    	}
		
		return find(
				getEffectiveDatedCriteria(effStatus) + " and asset.ownerNode = ? and asset.category = ?", 
				effdt, owner, category);
	}
	
	
}
