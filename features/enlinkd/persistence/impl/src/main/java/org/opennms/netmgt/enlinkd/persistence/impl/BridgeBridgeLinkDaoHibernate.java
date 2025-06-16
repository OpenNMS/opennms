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

import org.opennms.netmgt.enlinkd.persistence.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.enlinkd.model.BridgeBridgeLink;




public class BridgeBridgeLinkDaoHibernate extends AbstractDaoHibernate<BridgeBridgeLink, Integer> implements BridgeBridgeLinkDao {

    /**
     * <p>
     * Constructor for BridgeBridgeLinkDaoHibernate.
     * </p>
     */
    public BridgeBridgeLinkDaoHibernate() {
        super(BridgeBridgeLink.class);
    }


	@Override
	public List<BridgeBridgeLink> findByNodeId(Integer id) {
		return find("from BridgeBridgeLink rec where rec.node.id = ?", id);
	}

	@Override
	public List<BridgeBridgeLink> findByDesignatedNodeId(Integer id) {
		return find("from BridgeBridgeLink rec where rec.designatedNode.id = ?", id);
	}

	@Override
	public BridgeBridgeLink getByNodeIdBridgePort(Integer id, Integer port) {
		return findUnique("from BridgeBridgeLink rec where rec.node.id = ?  and rec.bridgePort = ?", id,port);
	}

	@Override
	public BridgeBridgeLink getByNodeIdBridgePortIfIndex(Integer id, Integer ifindex) {
		return findUnique("from BridgeBridgeLink rec where rec.node.id = ?  and rec.bridgePortIfIndex = ?", id,ifindex);
	}

	@Override
	public List<BridgeBridgeLink> getByDesignatedNodeIdBridgePort(Integer id, Integer port) {
		return find("from BridgeBridgeLink rec where rec.designatedNode.id = ?  and rec.designatedPort = ?", id,port);
	}

	@Override
	public List<BridgeBridgeLink> getByDesignatedNodeIdBridgePortIfIndex(Integer id, Integer ifindex) {
		return find("from BridgeBridgeLink rec where rec.designatedNode.id = ?  and rec.designatedPortIfIndex = ?", id,ifindex);
	}

	@Override
	public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
	    getHibernateTemplate().bulkUpdate("delete from BridgeBridgeLink rec where rec.node.id = ? and rec.bridgeBridgeLinkLastPollTime < ?",
				nodeId,now);
	}

	@Override
	public void deleteByDesignatedNodeIdOlderThen(Integer nodeId, Date now) {
	    getHibernateTemplate().bulkUpdate("delete from BridgeBridgeLink rec where rec.designatedNode.id = ? and rec.bridgeBridgeLinkLastPollTime < ?",
				nodeId,now);
	}

	@Override
	public void deleteByNodeId(Integer nodeId) {
		getHibernateTemplate().bulkUpdate("delete from BridgeBridgeLink rec where rec.node.id = ? ",
									  new Object[] {nodeId});
	}

	@Override
	public void deleteByDesignatedNodeId(Integer nodeId) {
		getHibernateTemplate().bulkUpdate("delete from BridgeBridgeLink rec where rec.designatedNode.id = ? ",
										  new Object[] {nodeId});
	}

	@Override
	public void deleteAll() {
		getHibernateTemplate().bulkUpdate("delete from BridgeBridgeLink");
	}


}
