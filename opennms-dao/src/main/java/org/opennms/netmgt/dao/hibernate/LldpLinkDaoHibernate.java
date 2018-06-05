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

import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
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
        getHibernateTemplate().bulkUpdate("delete from LldpLink lldpLink where lldpLink.node.id = ? and lldpLink.lldpLinkLastPollTime < ?",
                                          new Object[] {nodeId,now});
    }

   @Override
   public void deleteByNodeId(Integer nodeId) {
       getHibernateTemplate().bulkUpdate("delete from LldpLink lldpLink where lldpLink.node.id = ? ",
                                         new Object[] {nodeId});
    }

    public List<LldpLink> findLinksForIds(List<Integer> linkIds) {

        final StringBuilder sql = new StringBuilder();
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
