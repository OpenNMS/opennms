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
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.persistence.api.IpNetToMediaDao;


public class IpNetToMediaDaoHibernate extends AbstractDaoHibernate<IpNetToMedia, Integer> implements IpNetToMediaDao {


	public IpNetToMediaDaoHibernate() {
		super(IpNetToMedia.class);
	}

	@Override
	public List<IpNetToMedia> findBySourceNodeId(Integer id) {
		return find("from IpNetToMedia rec where rec.sourceNode.id = ?1",id);
	}

	@Override
	public IpNetToMedia getByNetAndPhysAddress(InetAddress netAddress,
			String physAddress) {
		return findUnique("from IpNetToMedia rec where rec.netAddress = ?1 and rec.physAddress = ?2", netAddress, physAddress);
	}

	@Override
	public void deleteBySourceNodeIdOlderThen(Integer nodeId, Date now) {
		for (IpNetToMedia elem: find("from IpNetToMedia rec where rec.sourceNode.id = ?1 and rec.lastPollTime < ?2",nodeId,now)) {
			delete(elem);
		}
	}

        @Override
        public void deleteBySourceNodeId(Integer nodeId) {
                for (IpNetToMedia elem: find("from IpNetToMedia rec where rec.sourceNode.id = ?1 ",nodeId)) {
                        delete(elem);
                }
        }
        
	@Override
	public List<IpNetToMedia> findByPhysAddress(String physAddress) {
		return find("from IpNetToMedia rec where rec.physAddress = ?1",  physAddress);
	}

	@Override
	public List<IpNetToMedia> findByNetAddress(InetAddress netAddress) {
		return find("from IpNetToMedia rec where rec.netAddress = ?1 ", netAddress);
	}

	@Override
	public List<IpNetToMedia> findByMacLinksOfNode(Integer nodeId) {
		return find("from IpNetToMedia m where m.physAddress in (select l.macAddress from BridgeMacLink l where l.node.id = ?1)",  nodeId);
	}

	@Override
	public void deleteAll() {
		getHibernateTemplate().bulkUpdate("delete from IpNetToMedia");
	}
}
