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

import org.opennms.netmgt.enlinkd.persistence.api.BridgeMacLinkDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.enlinkd.model.BridgeMacLink;

public class BridgeMacLinkDaoHibernate extends
        AbstractDaoHibernate<BridgeMacLink, Integer> implements
        BridgeMacLinkDao {

    /**
     * <p>
     * Constructor for BridgeMacLinkDaoHibernate.
     * </p>
     */
    public BridgeMacLinkDaoHibernate() {
        super(BridgeMacLink.class);
    }

    @Override
    public List<BridgeMacLink> findByNodeId(Integer id) {
        return find("from BridgeMacLink rec where rec.node.id = ?", id);
    }

    @Override
    public BridgeMacLink getByNodeIdBridgePortMac(Integer id, Integer port,
            String mac) {
        return findUnique("from BridgeMacLink rec where rec.node.id = ?  and rec.bridgePort = ? and rec.macAddress = ? ",
                          id, port, mac);
    }

    @Override
    public List<BridgeMacLink> findByNodeIdBridgePort(Integer id, Integer port) {
        return find("from BridgeMacLink rec where rec.node.id = ?  and rec.bridgePort = ? ",
                    id, port);
    }

    @Override
    public List<BridgeMacLink> findByMacAddress(String mac) {
        return find("from BridgeMacLink rec where rec.macAddress = ?", mac);
    }

    @Override
    public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
        getHibernateTemplate().bulkUpdate("delete from BridgeMacLink rec where rec.node.id = ? and rec.bridgeMacLinkLastPollTime < ?",
                nodeId, now);
    }

    @Override
    public void deleteByNodeId(Integer nodeId) {
        getHibernateTemplate().bulkUpdate("delete from BridgeMacLink rec where rec.node.id = ?",
                                          new Object[] { nodeId });
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete from BridgeMacLink");
    }

}
