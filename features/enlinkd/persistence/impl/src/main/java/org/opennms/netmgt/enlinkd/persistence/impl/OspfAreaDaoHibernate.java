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

import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.enlinkd.model.OspfArea;
import org.opennms.netmgt.enlinkd.persistence.api.OspfAreaDao;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.util.Assert;

/**
 * <p>IpInterfaceDaoHibernate class.</p>
 *
 * @author antonio
 */
public class OspfAreaDaoHibernate extends AbstractDaoHibernate<OspfArea, Integer>  implements OspfAreaDao {

    /**
     * <p>Constructor for OspfAreaDaoHibernate.</p>
     */
    public OspfAreaDaoHibernate() {
        super(OspfArea.class);
    }

    /** {@inheritDoc} */
    @Override
    public OspfArea get(Integer nodeId, InetAddress ospfAreaId ){
        return findUnique("from OspfArea as ospfArea where ospfArea.node.id = ?1 and ospfArea.ospfAreaId = ?2 ",
                          nodeId, ospfAreaId);
    }


    /** {@inheritDoc} */
    @Override
    public List<OspfArea> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        return find("from OspfArea ospfArea where ospfArea.node.id = ?1", nodeId);
    }

    @Override
    public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
        getHibernateTemplate().bulkUpdate("delete from OspfArea ospfArea where ospfArea.node.id = ?1 and ospfArea.ospfAreaLastPollTime < ?2",
                nodeId, now);
    }    
    
    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from OspfArea ospfArea where ospfArea.node.id = ?1 ",
                                 new Object[] {nodeId});
    }
}
