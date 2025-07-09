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

import java.net.InetAddress;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.enlinkd.persistence.api.OspfLinkDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.model.OnmsNode;
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
        return findUnique("from OspfLink as ospfLink where ospfLink.node = ?1 and ospfLink.ospfRemRouterId = ?2 and ospfLink.ospfRemIpAddr = ?3 and ospfLink.ospfRemAddressLessIndex = ?4",
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
        return findUnique("from OspfLink as ospfLink where ospfLink.node.id = ?1 and ospfLink.ospfRemRouterId = ?2 and ospfLink.ospfRemIpAddr = ?3 and ospfLink.ospfRemAddressLessIndex = ?4",
                          nodeId, ospfRemRouterId, ospfRemIpAddr,
                          ospfRemAddressLessIndex);
    }
    
    /** {@inheritDoc} */
    @Override
    public List<OspfLink> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        return find("from OspfLink ospfLink where ospfLink.node.id = ?1", nodeId);
    }

    @Override
    public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
        getHibernateTemplate().bulkUpdate("delete from OspfLink ospfLink where ospfLink.node.id = ?1 and ospfLinkLastPollTime < ?2",
                nodeId, now);
    }    
    
    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from OspfLink ospfLink where ospfLink.node.id = ?1 ",
                                 new Object[] {nodeId});
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete from OspfLink");
    }
}
