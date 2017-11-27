/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.api.IsIsElementDao;
import org.opennms.netmgt.model.IsIsElement;

public class IsIsElementDaoHibernate extends AbstractDaoHibernate<IsIsElement, Integer> implements IsIsElementDao {

    /**
     * <p>
     * Constructor for IsIsElementDaoHibernate.
     * </p>
     */
    public IsIsElementDaoHibernate() {
        super(IsIsElement.class);
    }

    /**
     * <p>
     * findByNodeId
     * </p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.IsIsElement} object.
     */
    @Override
    public IsIsElement findByNodeId(Integer id) {
        return findUnique("from IsIsElement rec where rec.node.id = ?", id);
    }

    @Override
    public IsIsElement findByIsIsSysId(String isisSysId) {
        return findUnique("from IsIsElement rec where rec.isisSysID = ?",
                          isisSysId);
    }
  
    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from IsIsElement rec where rec.node.id = ? ",
                                    new Object[] {nodeId});
    }
	
}
