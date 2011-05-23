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

import org.opennms.netmgt.model.inventory.OnmsInventoryAsset;
import org.opennms.netmgt.model.inventory.OnmsInventoryAssetProperty;
import org.opennms.netmgt.dao.InventoryAssetPropertyDao;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.Session;
import org.hibernate.HibernateException;

import java.util.Collection;
import java.sql.SQLException;

public class InventoryAssetPropertyDaoHibernate extends AbstractDaoHibernate<OnmsInventoryAssetProperty, Integer>
        implements InventoryAssetPropertyDao {

    public InventoryAssetPropertyDaoHibernate() {
        super(OnmsInventoryAssetProperty.class);
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Collection<OnmsInventoryAssetProperty> findAll(final Integer offset, final Integer limit) {
        return (Collection<OnmsInventoryAssetProperty>)getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createCriteria(OnmsInventoryAssetProperty.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
            }
        });
    }

    public OnmsInventoryAssetProperty findByPropertyId(int id) {
        return findUnique("from OnmsInventoryAssetProperty as prop where prop.id = ?", id);
    }

    public Collection<OnmsInventoryAssetProperty> findByAsset(OnmsInventoryAsset asset) {
        return find("from OnmsInventoryAssetProperty as prop where prop.inventoryAsset = ?", asset);
    }
}
