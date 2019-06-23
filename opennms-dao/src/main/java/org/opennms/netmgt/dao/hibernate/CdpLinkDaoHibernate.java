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

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.dao.api.CdpLinkDao;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.util.Assert;

/**
 * <p>CdpLinkDaoHibernate class.</p>
 *
 * @author antonio
 */
public class CdpLinkDaoHibernate extends AbstractDaoHibernate<CdpLink, Integer>  implements CdpLinkDao {

    /**
     * <p>Constructor for IpInterfaceDaoHibernate.</p>
     */
    public CdpLinkDaoHibernate() {
        super(CdpLink.class);
    }

    /** {@inheritDoc} */
    @Override
    public CdpLink get(OnmsNode node, Integer cdpCacheifIndex, Integer cdpCacheDeviceIndex) {
        Assert.notNull(node, "node cannot be null");
        Assert.notNull(cdpCacheifIndex, "cdpCacheifIndex cannot be null");
        Assert.notNull(cdpCacheDeviceIndex, "cdpCacheDeviceIndex cannot be null");
        return findUnique("from CdpLink as cdpLink where cdpLink.node = ? and cdpLink.cdpCacheIfIndex = ? and cdpCacheDeviceIndex = ?", node, cdpCacheifIndex, cdpCacheDeviceIndex);
    }

    /** {@inheritDoc} */
    @Override
    public CdpLink get(Integer nodeId, Integer cdpCacheifIndex, Integer cdpCacheDeviceIndex) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        Assert.notNull(cdpCacheifIndex, "cdpCacheifIndex cannot be null");
        Assert.notNull(cdpCacheDeviceIndex, "cdpCacheDeviceIndex cannot be null");
        return findUnique("from CdpLink as cdpLink where cdpLink.node.id = ? and cdpLink.cdpCacheIfIndex = ? and cdpCacheDeviceIndex = ?", nodeId, cdpCacheifIndex, cdpCacheDeviceIndex);
    }
    
    /** {@inheritDoc} */
    @Override
    public List<CdpLink> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        return find("from CdpLink cdpLink where cdpLink.node.id = ?", nodeId);
    }

    @Override
    public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
        getHibernateTemplate().bulkUpdate("delete from CdpLink cdpLink where cdpLink.node.id = ? and cdpLink.cdpLinkLastPollTime < ?",
                                          new Object[] {nodeId,now});
    }

    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from CdpLink cdpLink where cdpLink.node.id = ? ",
                                          new Object[] {nodeId});
    }

}
