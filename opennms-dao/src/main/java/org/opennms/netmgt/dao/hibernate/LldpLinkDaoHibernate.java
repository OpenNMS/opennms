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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.topology.LldpTopologyLink;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.Assert;

/**
 * <p>IpInterfaceDaoHibernate class.</p>
 *
 * @author antonio
 */
public class LldpLinkDaoHibernate extends AbstractDaoHibernate<LldpLink, Integer>  implements LldpLinkDao {

    /**
     * <p>Constructor for IpInterfaceDaoHibernate.</p>
     */
    public LldpLinkDaoHibernate() {
        super(LldpLink.class);
    }

    /** {@inheritDoc} */
    @Override
    public LldpLink get(OnmsNode node, Integer lldpLocalPortNum) {
        return findUnique("from LldpLink as lldpLink where lldpLink.node = ? and lldpLink.lldpLocalPortNum = ?", node, lldpLocalPortNum);
    }

    /** {@inheritDoc} */
    @Override
    public LldpLink get(Integer nodeId, Integer lldpLocalPortNum) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        Assert.notNull(lldpLocalPortNum, "lldpLocalPortNum cannot be null");
        return findUnique("from LldpLink as lldpLink where lldpLink.node.id = ? and lldpLink.lldpLocalPortNum = ?", nodeId, lldpLocalPortNum);
    }
    
    /** {@inheritDoc} */
    @Override
    public List<LldpLink> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        return find("from LldpLink lldpLink where lldpLink.node.id = ?", nodeId);
    }

	@Override
	public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
		for (LldpLink link: find("from LldpLink lldpLink where lldpLink.node.id = ? and lldpLink.lldpLinkLastPollTime < ?",nodeId,now)) {
			delete(link);
		}
	}

    @Override
    public List<LldpTopologyLink> findAllTopologyLinks() {
        return getHibernateTemplate().execute(new HibernateCallback<List<LldpTopologyLink>>() {
            @Override
            public List<LldpTopologyLink> doInHibernate(Session session) throws HibernateException, SQLException {
                List<Object[]> list = session.createSQLQuery("select DISTINCT on (distint_id) " +
                        "least(l1.id, l2.id) as distint_id, " +
                        "l1.id as source_id, " +
                        "l1.nodeid as source_node, " +
                        "l2.id as target_id, " +
                        "l2.nodeid as target_nodeid" +

                        "from lldplink l1 " +
                        "left join lldpelement e1 on l1.nodeid = e1.nodeid " +
                        "left join lldpelement e2 on l1.lldpremchassisid = e2.lldpchassisid " +
                        "left join lldplink l2 on e2.nodeid = l2.nodeid " +

                        "where l1.lldpremportid = l2.lldpportid " +
                        "and l1.lldpremportdescr = l2.lldpportdescr " +
                        "and l1.lldpremchassisid = e2.lldpchassisid " +
                        "and l1.lldpremsysname = e2.lldpsysname " +
                        "and l1.lldpremportidsubtype = l2.lldpportidsubtype").list();

                List<LldpTopologyLink> links = new ArrayList<LldpTopologyLink>();

                for(Object[] objects : list){
                    links.add(new LldpTopologyLink((Integer)objects[1], (Integer) objects[2], (Integer) objects[3], (Integer) objects[4]));
                }
                return links;
            }
        });

    }

    public List<LldpLink> findLinksForIds(List<Integer> linkIds) {

        StringBuilder sql = new StringBuilder();
        sql.append("FROM LldpLink lldplink ");
        if(linkIds.size() == 1){
            sql.append("where lldplink.id = " + linkIds.get(0) + " ");
        } else{
            sql.append("where lldplink.id in (");
            int counter = 0;
            for (Integer id : linkIds) {
                sql.append(id);
                if(counter < linkIds.size() - 1 ) {
                    sql.append(",");
                }
                counter++;
            }
            sql.append(")");
        }

        return find(sql.toString());
    }
    
    
}
