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

import org.opennms.netmgt.dao.api.IsIsLinkDao;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.util.Assert;

/**
 * <p>IpInterfaceDaoHibernate class.</p>
 *
 * @author antonio
 */
public class IsIsLinkDaoHibernate extends AbstractDaoHibernate<IsIsLink, Integer>  implements IsIsLinkDao {

    /**
     * <p>Constructor for IpInterfaceDaoHibernate.</p>
     */
    public IsIsLinkDaoHibernate() {
        super(IsIsLink.class);
    }

    /** {@inheritDoc} */
    @Override
    public IsIsLink get(OnmsNode node, Integer isisCircIndex,
            Integer isisISAdjIndex) {
        return findUnique("from IsIsLink as isisLink where isisLink.node = ? and isisLink.isisCircIndex = ? and isisLink.isisISAdjIndex = ? ",
                          node, isisCircIndex, isisISAdjIndex);
    }

    /** {@inheritDoc} */
    @Override
    public IsIsLink get(Integer nodeId, Integer isisCircIndex, Integer isisISAdjIndex) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        Assert.notNull(isisCircIndex, "isisCircIndex cannot be null");
        Assert.notNull(isisISAdjIndex, "isisISAdjIndex cannot be null");
        return findUnique(
        		"from IsIsLink as isisLink where isisLink.node.id = ? and isisLink.isisCircIndex = ? and isisLink.isisISAdjIndex = ? ",
        		nodeId, isisCircIndex, isisISAdjIndex);
    }
    
    /** {@inheritDoc} */
    @Override
    public List<IsIsLink> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        return find("from IsIsLink isisLink where isisLink.node.id = ?", nodeId);
    }

    @Override
    public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
        getHibernateTemplate().bulkUpdate("delete from IsIsLink isisLink where isisLink.node.id = ? and isisLinkLastPollTime < ?",
                                  new Object[] {nodeId, now});
    }

    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from IsIsLink isisLink where isisLink.node.id = ? ",
                                  new Object[] {nodeId});
    }

}
