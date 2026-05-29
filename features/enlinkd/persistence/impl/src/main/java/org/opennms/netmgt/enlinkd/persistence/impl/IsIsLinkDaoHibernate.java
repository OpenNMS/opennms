/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd.persistence.impl;

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.enlinkd.persistence.api.IsIsLinkDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
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
        return findUnique("from IsIsLink as isisLink where isisLink.node = ?1 and isisLink.isisCircIndex = ?2 and isisLink.isisISAdjIndex = ?3 ",
                          node, isisCircIndex, isisISAdjIndex);
    }

    /** {@inheritDoc} */
    @Override
    public IsIsLink get(Integer nodeId, Integer isisCircIndex, Integer isisISAdjIndex) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        Assert.notNull(isisCircIndex, "isisCircIndex cannot be null");
        Assert.notNull(isisISAdjIndex, "isisISAdjIndex cannot be null");
        return findUnique(
        		"from IsIsLink as isisLink where isisLink.node.id = ?1 and isisLink.isisCircIndex = ?2 and isisLink.isisISAdjIndex = ?3 ",
        		nodeId, isisCircIndex, isisISAdjIndex);
    }
    
    /** {@inheritDoc} */
    @Override
    public List<IsIsLink> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        return find("from IsIsLink isisLink where isisLink.node.id = ?1", nodeId);
    }

    @Override
    public List<IsIsLink> findBySysIdAndAdjAndCircIndex(int nodeId) {
        return find("from IsIsLink r where exists (from IsIsElement e, IsIsLink l where " +
                    "r.node.id = e.node.id AND r.isisISAdjIndex = l.isisISAdjIndex AND r.isisCircIndex = l.isisCircIndex AND " +
                    "e.isisSysID = l.isisISAdjNeighSysID AND l.node.id = ?1)", nodeId);
    }

    @Override
    public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
        getHibernateTemplate().bulkUpdate("delete from IsIsLink isisLink where isisLink.node.id = ?1 and isisLinkLastPollTime < ?2",
                nodeId, now);
    }

    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from IsIsLink isisLink where isisLink.node.id = ?1 ",
                                  new Object[] {nodeId});
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete from IsIsLink");
    }

}
