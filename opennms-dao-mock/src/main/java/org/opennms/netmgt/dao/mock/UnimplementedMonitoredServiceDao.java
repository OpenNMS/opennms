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
import java.util.Set;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.ServiceSelector;

public class UnimplementedMonitoredServiceDao implements MonitoredServiceDao {
    @Override
    public List<OnmsMonitoredService> findMatching(OnmsCriteria criteria) {
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
    public void delete(OnmsMonitoredService entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void delete(Integer key) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsMonitoredService> findAll() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsMonitoredService> findMatching(Criteria criteria) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int countMatching(Criteria onmsCrit) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsMonitoredService get(Integer id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsMonitoredService load(Integer id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Integer save(OnmsMonitoredService entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void saveOrUpdate(OnmsMonitoredService entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void update(OnmsMonitoredService entity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsMonitoredService get(Integer nodeId, InetAddress ipAddress, Integer serviceId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsMonitoredService get(Integer nodeId, InetAddress ipAddr, Integer ifIndex, Integer serviceId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsMonitoredService get(Integer nodeId, InetAddress ipAddress, String svcName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsMonitoredService> findByType(String typeName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsMonitoredService> findMatchingServices(ServiceSelector serviceSelector) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsMonitoredService> findAllServices() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }


    @Override
    public Set<OnmsMonitoredService> findByApplication(OnmsApplication application) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsMonitoredService getPrimaryService(Integer nodeId, String svcName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }
}
