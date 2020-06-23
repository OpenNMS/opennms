/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

public abstract class UnimplementedIpInterfaceDao implements IpInterfaceDao {
    @Override
    public List<OnmsIpInterface> findMatching(OnmsCriteria criteria) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int countMatching(OnmsCriteria onmsCrit) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void lock() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void initialize(Object obj) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int countAll() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void delete(OnmsIpInterface entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void delete(Integer key) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsIpInterface> findAll() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsIpInterface> findMatching(Criteria criteria) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int countMatching(Criteria onmsCrit) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsIpInterface get(Integer id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsIpInterface load(Integer id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Integer save(OnmsIpInterface entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void saveOrUpdate(OnmsIpInterface entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void update(OnmsIpInterface entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsIpInterface get(OnmsNode node, String ipAddress) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsIpInterface findByNodeIdAndIpAddress(Integer nodeId, String ipAddress) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsIpInterface findByForeignKeyAndIpAddress(String foreignSource, String foreignId, String ipAddress) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsIpInterface> findByIpAddress(String ipAddress) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsIpInterface> findByNodeId(Integer nodeId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsIpInterface> findByServiceType(String svcName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsIpInterface> findHierarchyByServiceType(String svcName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Map<InetAddress, Integer> getInterfacesForNodes() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsIpInterface findPrimaryInterfaceByNodeId(Integer nodeId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }
}
