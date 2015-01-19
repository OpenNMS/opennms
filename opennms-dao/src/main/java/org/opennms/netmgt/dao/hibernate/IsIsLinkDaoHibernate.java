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

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.IsIsLinkDao;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.orm.hibernate3.HibernateCallback;
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
		return findUnique(
				"from IsIsLink as isisLink where isisLink.node = ? and isisLink.isisCircIndex = ? and isisLink.isisISAdjIndex = ? ",
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
		for (IsIsLink link: find("from IsIsLink isisLink where isisLink.node.id = ? and isisLinkLastPollTime < ?",nodeId,now)) {
			delete(link);
		}
	}

    /**
     * Gets the ISIS links between nodes and returns a list of Object[],
     * with the following mapping:
     * [0] = distinct id for combining links can be ignored
     * [1] = link1 id
     * [2] = link1 nodeid
     * [3] = link1 isiscircifindex
     * [4] = link2 id
     * [5] = link2 nodeid
     * [6] = link2 isiscircifindex
     * @return A list of Object[] see notes for index mapping
     */
    public List<Object[]> getLinksForTopology() {
        return getHibernateTemplate().execute(new HibernateCallback<List<Object[]>>() {

            @Override
            public List<Object[]> doInHibernate(Session session) throws HibernateException, SQLException {
                List<Object[]> list = session.createSQLQuery("select distinct on (distinct_id) " +
                        "least(l1.id, l2.id) as distinct_id, " +
                        "l1.id as source_id, " +
                        "l1.nodeid as source_nodeid, " +
                        "l1.isiscircifindex as l1_isiscircifindex, " +
                        "l2.id as target_id,  " +
                        "l2.nodeid as target_nodeid, " +
                        "l2.isiscircifindex as l2_isiscircifindex " +

                        "from isislink l1 " +
                        "left join isiselement e1 on l1.nodeid = e1.nodeid " +
                        "left join isiselement e2 on l1.isisisadjneighsysid = e2.isissysid " +
                        "left join isislink l2 on e2.nodeid=l2.nodeid " +
                        "where l1.isisisadjindex = l2.isisisadjindex and l2.isisisadjneighsysid = e1.isissysid " +
                        "order by distinct_id;").list();
                return list;
            }

        });

    }
    
    
}
