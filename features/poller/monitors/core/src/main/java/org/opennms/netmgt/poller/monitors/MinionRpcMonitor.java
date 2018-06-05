/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.rpc.api.RpcExceptionHandler;
import org.opennms.core.rpc.api.RpcExceptionUtils;
import org.opennms.core.rpc.echo.EchoRequest;
import org.opennms.core.rpc.echo.EchoResponse;
import org.opennms.core.rpc.echo.EchoRpcModule;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

@Distributable(DistributionContext.DAEMON)
public class MinionRpcMonitor extends AbstractServiceMonitor implements RpcExceptionHandler<PollStatus> {
    private final Supplier<NodeDao> nodeDao = Suppliers.memoize(() -> BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class));
    private final Supplier<RpcClientFactory> rpcClientFactory = Suppliers.memoize(() -> BeanUtils.getBean("daoContext", "camelRpcClientFactory", RpcClientFactory.class));

    private final static int DEFAULT_TTL_IN_MS = -1;
    private final static int DEFAULT_MESSAGE_SIZE = 1024;

    @Override
    public PollStatus poll(final MonitoredService svc, final Map<String, Object> parameters) {
        Long ttlInMs = ParameterMap.getKeyedLong(parameters, "ttl", DEFAULT_TTL_IN_MS);
        if (ttlInMs < 1) {
            // Use the global default
            ttlInMs = null;
        }

        int messageSize = ParameterMap.getKeyedInteger(parameters, "message-size", DEFAULT_MESSAGE_SIZE);
        if (messageSize < 0) {
            messageSize = 0;
        }

        // Create the client
        final RpcClient<EchoRequest, EchoResponse> client = rpcClientFactory.get().getClient(EchoRpcModule.INSTANCE);

        // Build the request
        final OnmsNode node = nodeDao.get().get(svc.getNodeId());
        final EchoRequest request = new EchoRequest();
        request.setId(System.currentTimeMillis());
        request.setMessage(Strings.repeat("*", messageSize));
        request.setLocation(node.getLocation().getLocationName());
        request.setSystemId(node.getForeignId());
        request.setTimeToLiveMs(ttlInMs);

        try {
            final EchoResponse response = client.execute(request).get();
            final Long responseTime = System.currentTimeMillis() - response.getId();
            return PollStatus.available(responseTime.doubleValue());
        } catch (InterruptedException|ExecutionException t) {
            return RpcExceptionUtils.handleException(t, this);
        }
    }

    @Override
    public String getEffectiveLocation(String location) {
        // Always run in the OpenNMS JVM
        return MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID;
    }

    @Override
    public PollStatus onInterrupted(Throwable t) {
        return PollStatus.unknown("Interrupted.");
    }

    @Override
    public PollStatus onTimedOut(Throwable t) {
        return PollStatus.unresponsive("Request timed out.");
    }

    @Override
    public PollStatus onRejected(Throwable t) {
        return PollStatus.unknown("Rejected.");
    }

    @Override
    public PollStatus onUnknown(Throwable t) {
        return PollStatus.unresponsive("Failed with unknown exception: " + t.getMessage());
    }
}
