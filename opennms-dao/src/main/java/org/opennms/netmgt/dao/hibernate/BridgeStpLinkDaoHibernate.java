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

import org.opennms.netmgt.dao.api.BridgeStpLinkDao;
import org.opennms.netmgt.model.BridgeStpLink;




public class BridgeStpLinkDaoHibernate extends AbstractDaoHibernate<BridgeStpLink, Integer> implements BridgeStpLinkDao {

    /**
     * <p>
     * Constructor for BridgeStpLinkDaoHibernate.
     * </p>
     */
    public BridgeStpLinkDaoHibernate() {
        super(BridgeStpLink.class);
    }

	@Override
	public List<BridgeStpLink> findByNodeId(Integer id) {
		return find("from BridgeStpLink rec where rec.node.id = ?", id);
	}

	@Override
	public BridgeStpLink getByNodeIdBridgePort(Integer id, Integer port) {
		return findUnique("from BridgeStpLink rec where rec.node.id = ?  and rec.stpPort = ?", id,port);
	}

	@Override
	public List<BridgeStpLink> findByDesignatedBridge(String designated) {
		return find("from BridgeStpLink rec where rec.designatedBridge = ?", designated);
	}


	@Override
	public List<BridgeStpLink> findByDesignatedRoot(String root) {
		return find("from BridgeStpLink rec where rec.designatedRoot = ?", root);
	}



	@Override
	public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
	    getHibernateTemplate().bulkUpdate("delete from BridgeStpLink rec where rec.node.id = ? and rec.bridgeStpLinkLastPollTime < ?",
		                                  new Object[] {nodeId,now});
	}

	@Override
        public void deleteByNodeId(Integer nodeId) {
	    getHibernateTemplate().bulkUpdate("delete from BridgeStpLink rec where rec.node.id = ? ",
	                                      new Object[] {nodeId});
        }





}
