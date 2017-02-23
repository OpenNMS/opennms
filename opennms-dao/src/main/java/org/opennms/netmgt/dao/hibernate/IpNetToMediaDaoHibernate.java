/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.dao.api.IpNetToMediaDao;
import org.opennms.netmgt.model.IpNetToMedia;


public class IpNetToMediaDaoHibernate extends AbstractDaoHibernate<IpNetToMedia, Integer> implements IpNetToMediaDao {


	public IpNetToMediaDaoHibernate() {
		super(IpNetToMedia.class);
	}

	@Override
	public List<IpNetToMedia> findBySourceNodeId(Integer id) {
		return find("from IpNetToMedia rec where rec.sourceNode.id = ?",id);
	}

	@Override
	public IpNetToMedia getByNetAndPhysAddress(InetAddress netAddress,
			String physAddress) {
		return findUnique("from IpNetToMedia rec where rec.netAddress = ? and rec.physAddress = ?", netAddress, physAddress);
	}

	@Override
	public void deleteBySourceNodeIdOlderThen(Integer nodeId, Date now) {
		for (IpNetToMedia elem: find("from IpNetToMedia rec where rec.sourceNode.id = ? and rec.lastPollTime < ?",nodeId,now)) {
			delete(elem);
		}
	}

        @Override
        public void deleteBySourceNodeId(Integer nodeId) {
                for (IpNetToMedia elem: find("from IpNetToMedia rec where rec.sourceNode.id = ? ",nodeId)) {
                        delete(elem);
                }
        }
        
	@Override
	public List<IpNetToMedia> findByPhysAddress(String physAddress) {
		return find("from IpNetToMedia rec where rec.physAddress = ?",  physAddress);
	}

	@Override
	public List<IpNetToMedia> findByNetAddress(InetAddress netAddress) {
		return find("from IpNetToMedia rec where rec.netAddress = ? ", netAddress);
	}



}
