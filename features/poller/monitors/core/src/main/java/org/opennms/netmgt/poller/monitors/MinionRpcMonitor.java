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
package org.opennms.netmgt.poller.monitors;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.rpc.api.RpcExceptionHandler;
import org.opennms.core.rpc.api.RpcExceptionUtils;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.echo.EchoRequest;
import org.opennms.core.rpc.echo.EchoResponse;
import org.opennms.core.rpc.echo.EchoRpcModule;
import org.opennms.core.mate.api.MetadataConstants;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.FallbackScope;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class MinionRpcMonitor extends AbstractServiceMonitor implements RpcExceptionHandler<PollStatus> {
    private final Supplier<NodeDao> nodeDao = Suppliers.memoize(() -> BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class));
    private final Supplier<EntityScopeProvider> entityScopeProvider = Suppliers.memoize(() -> BeanUtils.getBean("daoContext", "entityScopeProvider", EntityScopeProvider.class));
    private final Supplier<RpcClientFactory> rpcClientFactory = Suppliers.memoize(() ->    BeanUtils.getBeanFactory("daoContext").getFactory().getBean(RpcClientFactory.class));

    private final static int DEFAULT_MESSAGE_SIZE = 1024;
    private final static String MESSAGE_SIZE = "message-size";

    @Override
    public PollStatus poll(final MonitoredService svc, final Map<String, Object> parameters) {

        // Create the client
        final RpcClient<EchoRequest, EchoResponse> client = rpcClientFactory.get().getClient(EchoRpcModule.INSTANCE);

        final Map<String, Object> interpolatedAttributes = Interpolator.interpolateObjects(parameters, new FallbackScope(
                entityScopeProvider.get().getScopeForNode(svc.getNodeId()),
                entityScopeProvider.get().getScopeForInterface(svc.getNodeId(), svc.getIpAddr()),
                entityScopeProvider.get().getScopeForService(svc.getNodeId(), svc.getAddress(), svc.getSvcName())
        ));

        Long ttlInMs = ParameterMap.getLongValue(MetadataConstants.TTL, interpolatedAttributes.get(MetadataConstants.TTL), null);

        int messageSize = ParameterMap.getIntValue( MESSAGE_SIZE, interpolatedAttributes.get(MESSAGE_SIZE), DEFAULT_MESSAGE_SIZE);
        if (messageSize < 0) {
            messageSize = 0;
        }

        // Build the request
        final OnmsNode node = nodeDao.get().get(svc.getNodeId());
        final EchoRequest request = new EchoRequest();
        request.setId(System.currentTimeMillis());
        request.setMessage(Strings.repeat("*", messageSize));
        request.setLocation(node.getLocation().getLocationName());
        request.setSystemId(node.getForeignId());
        request.setTimeToLiveMs(ttlInMs);
        request.addTracingInfo(RpcRequest.TAG_NODE_ID, String.valueOf(node.getId()));
        request.addTracingInfo(RpcRequest.TAG_NODE_LABEL, node.getLabel());
        request.addTracingInfo(RpcRequest.TAG_CLASS_NAME, MinionRpcMonitor.class.getCanonicalName());
        request.addTracingInfo(RpcRequest.TAG_IP_ADDRESS, InetAddressUtils.toIpAddrString(svc.getAddress()));

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
