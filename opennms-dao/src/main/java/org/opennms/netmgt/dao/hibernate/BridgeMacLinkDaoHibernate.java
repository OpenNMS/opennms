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

import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.model.BridgeMacLink;

public class BridgeMacLinkDaoHibernate extends
        AbstractDaoHibernate<BridgeMacLink, Integer> implements
        BridgeMacLinkDao {

    /**
     * <p>
     * Constructor for BridgeMacLinkDaoHibernate.
     * </p>
     */
    public BridgeMacLinkDaoHibernate() {
        super(BridgeMacLink.class);
    }

    @Override
    public List<BridgeMacLink> findByNodeId(Integer id) {
        return find("from BridgeMacLink rec where rec.node.id = ?", id);
    }

    @Override
    public BridgeMacLink getByNodeIdBridgePortMac(Integer id, Integer port,
            String mac) {
        return findUnique("from BridgeMacLink rec where rec.node.id = ?  and rec.bridgePort = ? and rec.macAddress = ? ",
                          id, port, mac);
    }

    @Override
    public List<BridgeMacLink> findByNodeIdBridgePort(Integer id, Integer port) {
        return find("from BridgeMacLink rec where rec.node.id = ?  and rec.bridgePort = ? ",
                    id, port);
    }

    @Override
    public List<BridgeMacLink> findByMacAddress(String mac) {
        return find("from BridgeMacLink rec where rec.macAddress = ?", mac);
    }

    @Override
    public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
        getHibernateTemplate().bulkUpdate("delete from BridgeMacLink rec where rec.node.id = ? and rec.bridgeMacLinkLastPollTime < ?",
                                          new Object[] { nodeId, now });
    }

    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from BridgeMacLink rec where rec.node.id = ?",
                                          new Object[] { nodeId });
    }

}
