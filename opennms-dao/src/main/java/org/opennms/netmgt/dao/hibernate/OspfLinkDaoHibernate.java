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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.topology.OspfTopologyLink;
import org.springframework.orm.hibernate3.HibernateCallback;
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
		return findUnique(
				"from OspfLink as ospfLink where ospfLink.node = ? and ospfLink.ospfRemRouterId = ? and ospfLink.ospfRemIpAddr = ? and ospfLink.ospfRemAddressLessIndex = ?",
				node, ospfRemRouterId, ospfRemIpAddr, ospfRemAddressLessIndex);
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
		return findUnique(
				"from OspfLink as ospfLink where ospfLink.node.id = ? and ospfLink.ospfRemRouterId = ? and ospfLink.ospfRemIpAddr = ? and ospfLink.ospfRemAddressLessIndex = ?",
				nodeId, ospfRemRouterId, ospfRemIpAddr, ospfRemAddressLessIndex);
	}
    
    /** {@inheritDoc} */
    @Override
    public List<OspfLink> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        return find("from OspfLink ospfLink where ospfLink.node.id = ?", nodeId);
    }

	@Override
	public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
		for (OspfLink link: find("from OspfLink ospfLink where ospfLink.node.id = ? and ospfLinkLastPollTime < ?",nodeId,now)) {
			delete(link);
		}
	}

    @Override
    public List<OspfTopologyLink> findAllTopologyLinks() {

        return getHibernateTemplate().execute(new HibernateCallback<List<OspfTopologyLink>>() {
            @Override
            public List<OspfTopologyLink> doInHibernate(Session session) throws HibernateException, SQLException {
                List<Object[]> list = session.createSQLQuery("select DISTINCT on (distinct_id) " +
                        "least(l1.id, l2.id) as distinct_id, " +
                        "l1.id as source_id, " +
                        "l1.nodeid as source_nodeid, " +
                        "l2.id as target_id, " +
                        "l2.nodeid as target_nodeid" +
                        "from ospflink l1 " +
                        "left join ospfelement e1 on l1.nodeid = e1.nodeid " +
                        "left join ospfelement l2 on l1.ospfipaddr = l2.ospfremipaddr " +
                        "where l1.ospfipaddr = l2.ospfremipaddr").list();

                List<OspfTopologyLink> links = new ArrayList<OspfTopologyLink>();
                for (Object[] objects : list) {
                    links.add(new OspfTopologyLink((Integer)objects[1], (Integer) objects[2], (Integer) objects[3], (Integer) objects[4]));
                }
                return links;

            }
        });
    }


}
