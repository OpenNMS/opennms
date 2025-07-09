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

import org.opennms.netmgt.enlinkd.persistence.api.CdpLinkDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.enlinkd.model.CdpLink;
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
        return findUnique("from CdpLink as cdpLink where cdpLink.node = ?1 and cdpLink.cdpCacheIfIndex = ?2 and cdpCacheDeviceIndex = ?3", node, cdpCacheifIndex, cdpCacheDeviceIndex);
    }

    /** {@inheritDoc} */
    @Override
    public CdpLink get(Integer nodeId, Integer cdpCacheifIndex, Integer cdpCacheDeviceIndex) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        Assert.notNull(cdpCacheifIndex, "cdpCacheifIndex cannot be null");
        Assert.notNull(cdpCacheDeviceIndex, "cdpCacheDeviceIndex cannot be null");
        return findUnique("from CdpLink as cdpLink where cdpLink.node.id = ?1 and cdpLink.cdpCacheIfIndex = ?2 and cdpCacheDeviceIndex = ?3", nodeId, cdpCacheifIndex, cdpCacheDeviceIndex);
    }
    
    /** {@inheritDoc} */
    @Override
    public List<CdpLink> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        return find("from CdpLink cdpLink where cdpLink.node.id = ?1", nodeId);
    }

    @Override
    public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
        getHibernateTemplate().bulkUpdate("delete from CdpLink cdpLink where cdpLink.node.id = ?1 and cdpLink.cdpLinkLastPollTime < ?2",
                nodeId,now);
    }

    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from CdpLink cdpLink where cdpLink.node.id = ?1 ",
                                          new Object[] {nodeId});
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete from CdpLink");
    }

}
