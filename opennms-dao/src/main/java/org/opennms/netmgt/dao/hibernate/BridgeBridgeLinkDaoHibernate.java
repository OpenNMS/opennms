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

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.dao.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.model.BridgeBridgeLink;




public class BridgeBridgeLinkDaoHibernate extends AbstractDaoHibernate<BridgeBridgeLink, Integer> implements BridgeBridgeLinkDao {

    /**
     * <p>
     * Constructor for BridgeBridgeLinkDaoHibernate.
     * </p>
     */
    public BridgeBridgeLinkDaoHibernate() {
        super(BridgeBridgeLink.class);
    }


	@Override
	public List<BridgeBridgeLink> findByNodeId(Integer id) {
		return find("from BridgeBridgeLink rec where rec.node.id = ?", id);
	}

	@Override
	public List<BridgeBridgeLink> findByDesignatedNodeId(Integer id) {
		return find("from BridgeBridgeLink rec where rec.designatedNode.id = ?", id);
	}

	@Override
	public BridgeBridgeLink getByNodeIdBridgePort(Integer id, Integer port) {
		return findUnique("from BridgeBridgeLink rec where rec.node.id = ?  and rec.bridgePort = ?", id,port);
	}

	@Override
	public BridgeBridgeLink getByNodeIdBridgePortIfIndex(Integer id, Integer ifindex) {
		return findUnique("from BridgeBridgeLink rec where rec.node.id = ?  and rec.bridgePortIfIndex = ?", id,ifindex);
	}

	@Override
	public List<BridgeBridgeLink> getByDesignatedNodeIdBridgePort(Integer id, Integer port) {
		return find("from BridgeBridgeLink rec where rec.designatedNode.id = ?  and rec.designatedPort = ?", id,port);
	}

	@Override
	public List<BridgeBridgeLink> getByDesignatedNodeIdBridgePortIfIndex(Integer id, Integer ifindex) {
		return find("from BridgeBridgeLink rec where rec.designatedNode.id = ?  and rec.designatedPortIfIndex = ?", id,ifindex);
	}

	@Override
	public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
	    getHibernateTemplate().bulkUpdate("delete from BridgeBridgeLink rec where rec.node.id = ? and rec.bridgeBridgeLinkLastPollTime < ?",
	                                      new Object[] {nodeId,now});
	}

	@Override
	public void deleteByDesignatedNodeIdOlderThen(Integer nodeId, Date now) {
	    getHibernateTemplate().bulkUpdate("delete from BridgeBridgeLink rec where rec.designatedNode.id = ? and rec.bridgeBridgeLinkLastPollTime < ?",
	                                      new Object[] {nodeId,now}); 
	}

        @Override
        public void deleteByNodeId(Integer nodeId) {
            getHibernateTemplate().bulkUpdate("delete from BridgeBridgeLink rec where rec.node.id = ? ",
                                              new Object[] {nodeId});
        }

        @Override
        public void deleteByDesignatedNodeId(Integer nodeId) {
            getHibernateTemplate().bulkUpdate("delete from BridgeBridgeLink rec where rec.designatedNode.id = ? ",
                                              new Object[] {nodeId});
        }




}
