/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.dao.api.StpInterfaceDao;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsStpInterface;

public class StpInterfaceDaoHibernate extends AbstractDaoHibernate<OnmsStpInterface, Integer>  implements StpInterfaceDao {
    
    public StpInterfaceDaoHibernate() {
        super(OnmsStpInterface.class);
    }

	@Override
	public void markDeletedIfNodeDeleted() {
		final OnmsCriteria criteria = new OnmsCriteria(OnmsStpInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.type", "D"));
        
        for (final OnmsStpInterface stpIface : findMatching(criteria)) {
        	stpIface.setStatus(StatusType.DELETED);
        	saveOrUpdate(stpIface);
        }
	}

	
    @Override
    public void deactivateForNodeIdIfOlderThan(final int nodeid, final Date scanTime) {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsStpInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.id", nodeid));
        criteria.add(Restrictions.lt("lastPollTime", scanTime));
        criteria.add(Restrictions.eq("status", StatusType.ACTIVE));
        
        for (final OnmsStpInterface item : findMatching(criteria)) {
            item.setStatus(StatusType.INACTIVE);
            saveOrUpdate(item);
        }
    }

    @Override
    public void deleteForNodeIdIfOlderThan(final int nodeid, final Date scanTime) {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsStpInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.id", nodeid));
        criteria.add(Restrictions.lt("lastPollTime", scanTime));
        criteria.add(Restrictions.not(Restrictions.eq("status", StatusType.ACTIVE)));
        
        for (final OnmsStpInterface item : findMatching(criteria)) {
            delete(item);
        }
    }

    @Override
    public void setStatusForNode(final Integer nodeid, final StatusType action) {
        // UPDATE stpinterface set status = ? WHERE nodeid = ?

        final OnmsCriteria criteria = new OnmsCriteria(OnmsStpInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.id", nodeid));

        for (final OnmsStpInterface item : findMatching(criteria)) {
            item.setStatus(action);
            saveOrUpdate(item);
        }
    }

    @Override
    public void setStatusForNodeAndIfIndex(final Integer nodeid, final Integer ifIndex, final StatusType action) {
        // UPDATE stpinterface set status = ? WHERE nodeid = ? AND ifindex = ?

        final OnmsCriteria criteria = new OnmsCriteria(OnmsStpInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.id", nodeid));
        criteria.add(Restrictions.eq("ifIndex", ifIndex));

        for (final OnmsStpInterface item : findMatching(criteria)) {
            item.setStatus(action);
            saveOrUpdate(item);
        }
    }

    @Override
    public OnmsStpInterface findByNodeAndVlan(final Integer nodeId, final Integer bridgePort, final Integer vlan) {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsStpInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.id", nodeId));
        criteria.add(Restrictions.eq("bridgePort", bridgePort));
        criteria.add(Restrictions.eq("vlan", vlan));

        final List<OnmsStpInterface> stpInterfaces = findMatching(criteria);
        if (stpInterfaces != null && stpInterfaces.size() > 0) {
            return stpInterfaces.get(0);
        }
        return null;
    }

}
