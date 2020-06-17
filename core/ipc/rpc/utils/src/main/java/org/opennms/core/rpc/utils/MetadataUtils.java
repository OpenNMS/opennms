/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.utils;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.rpc.utils.mate.EmptyScope;
import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
import org.opennms.core.rpc.utils.mate.Scope;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;

public class MetadataUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataUtils.class);

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private EntityScopeProvider entityScopeProvider;

    public Map<InetAddress, Integer> getIpInterfacesWithContext(String context) {

        Map<InetAddress, Integer> matchingIpInterfaceMap = new HashMap<>();
        sessionUtils.withReadOnlyTransaction(() -> {
            try {
                Map<InetAddress, Integer> ipInterfaces = ipInterfaceDao.getInterfacesForNodes();
                ipInterfaces.forEach(((inetAddress, nodeId) -> {
                    Scope scope = entityScopeProvider.getScopeForInterface(nodeId, InetAddressUtils.toIpAddrString(inetAddress));
                    if (scope.keys().stream().anyMatch(contextKey -> contextKey.getContext().equals(context))) {
                        matchingIpInterfaceMap.put(inetAddress, nodeId);
                    }
                }));

            } catch (Exception e) {
                LOG.error("Encountered exception while getting interfaces with context {}", context, e);
            }
            return null;
        });
        return matchingIpInterfaceMap;
    }

    public Map<String, String> getConfigFromMetadata(Integer nodeId, String ipAddress, String context) {
        if (nodeId <= 0 || Strings.isNullOrEmpty(ipAddress)) {
            return new HashMap<>();
        }

        return sessionUtils.withReadOnlyTransaction(() -> {
            Scope scope = entityScopeProvider.getScopeForInterface(nodeId, ipAddress);
            if (scope.equals(EmptyScope.EMPTY)) {
                LOG.info("Required telemetry stream config is not present");
                return new HashMap<>();
            }

            Map<String, String> streamConfig = new HashMap<>();
            scope.keys().stream()
                    .filter(contextKey -> contextKey.getContext().contains(context)).forEach(contextKey -> {
                scope.get(contextKey).ifPresent(value -> streamConfig.put(contextKey.getKey(), value));
            });
            return streamConfig;
        });
    }
}
