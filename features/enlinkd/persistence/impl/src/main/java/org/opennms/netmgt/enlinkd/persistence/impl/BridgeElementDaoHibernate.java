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

import org.opennms.netmgt.enlinkd.persistence.api.BridgeElementDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.enlinkd.model.BridgeElement;


public class BridgeElementDaoHibernate extends AbstractDaoHibernate<BridgeElement, Integer> implements BridgeElementDao {

    /**
     * <p>
     * Constructor for BridgeElementDaoHibernate.
     * </p>
     */
    public BridgeElementDaoHibernate() {
        super(BridgeElement.class);
    }

    /**
     * <p>
     * findByNodeId
     * </p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.enlinkd.model.LldpElement} object.
     */
    @Override
    public List<BridgeElement> findByNodeId(Integer id) {
        return find("from BridgeElement rec where rec.node.id = ?1", id);
    }
    
    @Override
    public BridgeElement getByNodeIdVlan(Integer id, Integer vlanId) {
        if (vlanId == null)
            return findUnique("from BridgeElement rec where rec.node.id = ?1 and rec.vlan is null",
                              id);
        return findUnique("from BridgeElement rec where rec.node.id = ?1 and rec.vlan = ?2",
                          id, vlanId);
    }

    @Override
    public List<BridgeElement> findByBridgeId(String id) {
        return find("from BridgeElement rec where rec.baseBridgeAddress = ?1",
                    id);
    }

    @Override
    public BridgeElement getByBridgeIdVlan(String id, Integer vlanId) {
        return findUnique("from BridgeElement rec where rec.baseBridgeAddress = ?1 and vlan = ?2",
                          id, vlanId);
    }

    @Override
    public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
        getHibernateTemplate().bulkUpdate("delete from BridgeElement rec where rec.node.id = ?1 and rec.bridgeNodeLastPollTime < ?2",
                nodeId, now);
    }

    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from BridgeElement rec where rec.node.id = ?1 ",
                                    new Object[] {nodeId});
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete from BridgeElement");
    }


}
