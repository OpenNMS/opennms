/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OspfLink;
import org.springframework.util.Assert;

/**
 * <p>IpInterfaceDaoHibernate class.</p>
 *
 * @author antonio
 */
public class OspfLinkDaoHibernate extends AbstractDaoHibernate<OspfLink, Integer>  implements OspfLinkDao {

    /**
     * <p>Constructor for OspfLinkDaoHibernate.</p>
     */
    public OspfLinkDaoHibernate() {
        super(OspfLink.class);
    }

    /** {@inheritDoc} */
    @Override
    public OspfLink get(OnmsNode node, InetAddress ospfRemRouterId,
            InetAddress ospfRemIpAddr, Integer ospfRemAddressLessIndex) {
        return findUnique("from OspfLink as ospfLink where ospfLink.node = ? and ospfLink.ospfRemRouterId = ? and ospfLink.ospfRemIpAddr = ? and ospfLink.ospfRemAddressLessIndex = ?",
                          node, ospfRemRouterId, ospfRemIpAddr,
                          ospfRemAddressLessIndex);
    }

    /** {@inheritDoc} */
    @Override
    public OspfLink get(Integer nodeId, InetAddress ospfRemRouterId,
            InetAddress ospfRemIpAddr, Integer ospfRemAddressLessIndex) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        Assert.notNull(ospfRemRouterId, "ospfRemRouterId cannot be null");
        Assert.notNull(ospfRemIpAddr, "ospfRemIpAddr cannot be null");
        Assert.notNull(ospfRemAddressLessIndex,
                       "ospfRemAddressLessIndex cannot be null");
        return findUnique("from OspfLink as ospfLink where ospfLink.node.id = ? and ospfLink.ospfRemRouterId = ? and ospfLink.ospfRemIpAddr = ? and ospfLink.ospfRemAddressLessIndex = ?",
                          nodeId, ospfRemRouterId, ospfRemIpAddr,
                          ospfRemAddressLessIndex);
    }
    
    /** {@inheritDoc} */
    @Override
    public List<OspfLink> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        return find("from OspfLink ospfLink where ospfLink.node.id = ?", nodeId);
    }

    @Override
    public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
        getHibernateTemplate().bulkUpdate("delete from OspfLink ospfLink where ospfLink.node.id = ? and ospfLinkLastPollTime < ?",
                                 new Object[] {nodeId, now});
    }    
    
    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from OspfLink ospfLink where ospfLink.node.id = ? ",
                                 new Object[] {nodeId});
    }
}
