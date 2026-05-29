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

import org.opennms.netmgt.enlinkd.persistence.api.BridgeStpLinkDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.enlinkd.model.BridgeStpLink;




public class BridgeStpLinkDaoHibernate extends AbstractDaoHibernate<BridgeStpLink, Integer> implements BridgeStpLinkDao {

    /**
     * <p>
     * Constructor for BridgeStpLinkDaoHibernate.
     * </p>
     */
    public BridgeStpLinkDaoHibernate() {
        super(BridgeStpLink.class);
    }

	@Override
	public List<BridgeStpLink> findByNodeId(Integer id) {
		return find("from BridgeStpLink rec where rec.node.id = ?1", id);
	}

	@Override
	public BridgeStpLink getByNodeIdBridgePort(Integer id, Integer port) {
		return findUnique("from BridgeStpLink rec where rec.node.id = ?1  and rec.stpPort = ?2", id,port);
	}

	@Override
	public List<BridgeStpLink> findByDesignatedBridge(String designated) {
		return find("from BridgeStpLink rec where rec.designatedBridge = ?1", designated);
	}


	@Override
	public List<BridgeStpLink> findByDesignatedRoot(String root) {
		return find("from BridgeStpLink rec where rec.designatedRoot = ?1", root);
	}



	@Override
	public void deleteByNodeIdOlderThen(Integer nodeId, Date now) {
	    getHibernateTemplate().bulkUpdate("delete from BridgeStpLink rec where rec.node.id = ?1 and rec.bridgeStpLinkLastPollTime < ?2",
				nodeId,now);
	}

	@Override
	public void deleteByNodeId(Integer nodeId) {
	    getHibernateTemplate().bulkUpdate("delete from BridgeStpLink rec where rec.node.id = ?1 ",
	                                      new Object[] {nodeId});
        }

	@Override
	public void deleteAll() {
		getHibernateTemplate().bulkUpdate("delete from BridgeStpLink");
	}


}
