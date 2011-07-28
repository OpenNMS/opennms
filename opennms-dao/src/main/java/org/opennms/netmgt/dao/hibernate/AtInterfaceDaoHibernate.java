/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.dao.AtInterfaceDao;
import org.opennms.netmgt.model.OnmsAtInterface;
import org.opennms.netmgt.model.OnmsCriteria;

public class AtInterfaceDaoHibernate extends AbstractDaoHibernate<OnmsAtInterface, Integer>  implements AtInterfaceDao {
    
    public AtInterfaceDaoHibernate() {
        super(OnmsAtInterface.class);
    }

	@Override
	public void markDeletedIfNodeDeleted() {
	    final OnmsCriteria criteria = new OnmsCriteria(OnmsAtInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.type", "D"));
        
        for (final OnmsAtInterface iface : findMatching(criteria)) {
        	iface.setStatus('D');
        	saveOrUpdate(iface);
        }
	}

    @Override
    public void deactivateForNodeIdIfOlderThan(final int nodeid, final Timestamp scanTime) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsAtInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.id", nodeid));
        criteria.add(Restrictions.lt("lastPollTime", scanTime));
        criteria.add(Restrictions.eq("status", "A"));
        
        for (final OnmsAtInterface iface : findMatching(criteria)) {
            iface.setStatus('N');
            saveOrUpdate(iface);
        }
    }

    @Override
    public Collection<OnmsAtInterface> findByMacAddress(final String macAddress) {
        // SELECT atinterface.nodeid, atinterface.ipaddr, ipinterface.ifindex from atinterface left JOIN ipinterface ON atinterface.nodeid = ipinterface.nodeid AND atinterface.ipaddr = ipinterface.ipaddr WHERE atphysaddr = ? AND atinterface.status <> 'D'

        final OnmsCriteria criteria = new OnmsCriteria(OnmsAtInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("macAddress", macAddress));
        criteria.add(Restrictions.ne("status", "D"));

        return findMatching(criteria);
    }

    @Override
    public void setStatusForNode(Integer nodeid, Character action) {
        // UPDATE atinterface set status = ?  WHERE sourcenodeid = ? OR nodeid = ?

        final OnmsCriteria criteria = new OnmsCriteria(OnmsAtInterface.class);
        criteria.add(Restrictions.or(Restrictions.eq("node.id", nodeid), Restrictions.eq("sourceNodeId", nodeid)));
        
        for (final OnmsAtInterface iface : findMatching(criteria)) {
            iface.setStatus(action);
            saveOrUpdate(iface);
        }
    }

    @Override
    public void setStatusForNodeAndIp(final Integer nodeid, final String ipAddr, final Character action) {
        // ps = dbConn.prepareStatement("UPDATE atinterface set status = ?  WHERE nodeid = ? AND ipaddr = ?");
        
        final OnmsCriteria criteria = new OnmsCriteria(OnmsAtInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.id", nodeid));
        criteria.add(Restrictions.eq("ipAddress", ipAddr));
        
        for (final OnmsAtInterface iface : findMatching(criteria)) {
            iface.setStatus(action);
            saveOrUpdate(iface);
        }
    }

    @Override
    public void setStatusForNodeAndIfIndex(final Integer nodeid, final Integer ifIndex, final Character action) {
        // UPDATE atinterface set status = ?  WHERE sourcenodeid = ? AND ifindex = ?

        final OnmsCriteria criteria = new OnmsCriteria(OnmsAtInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.id", nodeid));
        criteria.add(Restrictions.eq("ifIndex", ifIndex));
        
        for (final OnmsAtInterface iface : findMatching(criteria)) {
            iface.setStatus(action);
            saveOrUpdate(iface);
        }
    }

    @Override
    public OnmsAtInterface findByNodeAndAddress(final Integer nodeId, final String ipAddress, final String macAddress) {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsAtInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.id", nodeId));
        criteria.add(Restrictions.eq("ipAddress", ipAddress));
        criteria.add(Restrictions.eq("macAddress", macAddress));
        
        final List<OnmsAtInterface> ifaces = findMatching(criteria);
        if (ifaces.size() == 0) {
            return null;
        } else {
            return ifaces.get(0);
        }
    }

}
