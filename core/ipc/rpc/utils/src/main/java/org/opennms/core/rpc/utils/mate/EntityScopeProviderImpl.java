/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.utils.mate;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Strings;

public class EntityScopeProviderImpl implements EntityScopeProvider {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    @Autowired
    private TransactionTemplate transactions;

    @Override
    public Scope getScopeForNode(final Integer nodeId) {
        if (nodeId == null) {
            return EmptyScope.EMPTY;
        }

        final Scope metaDataScope = this.transactions.execute((tx) -> {
            final OnmsNode node = nodeDao.get(nodeId);
            if (node == null) {
                return EmptyScope.EMPTY;
            }

            return new SimpleScope(transform(node.getMetaData()));
        });

        return metaDataScope;
    }

    @Override
    public Scope getScopeForInterface(final Integer nodeId, final String ipAddress) {
        if (nodeId == null || Strings.isNullOrEmpty(ipAddress)) {
            return EmptyScope.EMPTY;
        }

        final Scope metaDataScope = this.transactions.execute((tx) -> {
            final OnmsIpInterface ipInterface = this.ipInterfaceDao.findByNodeIdAndIpAddress(nodeId, ipAddress);
            if (ipInterface == null) {
                return EmptyScope.EMPTY;
            }

            return new SimpleScope(transform(ipInterface.getMetaData()));
        });

        return metaDataScope;
    }



    @Override
    public Scope getScopeForService(final Integer nodeId, final InetAddress ipAddress, final String serviceName) {
        if (nodeId == null || ipAddress == null || Strings.isNullOrEmpty(serviceName)) {
            return EmptyScope.EMPTY;
        }

        final Scope metaDataScope = this.transactions.execute((tx) -> {
            final OnmsMonitoredService monitoredService = this.monitoredServiceDao.get(nodeId, ipAddress, serviceName);
            if (monitoredService == null) {
                return EmptyScope.EMPTY;
            }

            return new SimpleScope(transform(monitoredService.getMetaData()));
        });

        return metaDataScope;
    }

    private static Map<ContextKey, String> transform(Collection<OnmsMetaData> metaData) {
        return metaData.stream().collect(Collectors.toMap(
                        e -> new ContextKey(e.getContext(), e.getKey()),
                        e -> e.getValue()));
    }
}
