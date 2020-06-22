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

package org.opennms.netmgt.telemetry.protocols.openconfig.parser;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.rpc.utils.mate.ContextKey;
import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
import org.opennms.core.rpc.utils.mate.FallbackScope;
import org.opennms.core.rpc.utils.mate.Interpolator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.Config;
import org.opennms.netmgt.telemetry.listeners.StreamParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.netty.buffer.ByteBuf;

public class OpenConfigParser implements StreamParser {

    private static final Logger LOG = LoggerFactory.getLogger(OpenConfigParser.class);
    private final String name;
    private final AsyncDispatcher<TelemetryMessage> dispatcher;
    private final EntityScopeProvider entityScopeProvider;
    private final Map<String, String> parameters;
    private final IpInterfaceDao ipInterfaceDao;



    public OpenConfigParser(String name, Map<String, String> parameters,
                            AsyncDispatcher<TelemetryMessage> dispatcher,
                            EntityScopeProvider entityScopeProvider,
                            IpInterfaceDao ipInterfaceDao) {
        this.name = name;
        this.parameters = parameters;
        this.dispatcher = dispatcher;
        this.entityScopeProvider = entityScopeProvider;
        this.ipInterfaceDao = ipInterfaceDao;
    }

    @Override
    public Config getConfig(Integer nodeId, String ipAddress) {

       Map<String, String> params = Interpolator.interpolateStrings(parameters, new FallbackScope(
                entityScopeProvider.getScopeForNode(nodeId),
                entityScopeProvider.getScopeForInterface(nodeId, ipAddress)
        ));
       return new Config(nodeId, ipAddress, params);
    }

    @Override
    public List<Config> getMatchingConfig() {
        List<Config> configList = new ArrayList<>();
        String openConfigContext = parameters.get("enabled");
        if (!Strings.isNullOrEmpty(openConfigContext)) {
            try {
                ContextKey contextKey = new ContextKey(openConfigContext);
                List<OnmsIpInterface> interfaceList = ipInterfaceDao.findInterfacesWithMetadata(contextKey.getContext(), contextKey.getKey(), "true");
                interfaceList.forEach(onmsIpInterface -> {
                    Config config = getConfig(onmsIpInterface.getNodeId(), InetAddressUtils.toIpAddrString(onmsIpInterface.getIpAddress()));
                    configList.add(config);
                });
            } catch (Exception e) {
                LOG.error("Exception while getting openconfig metadata");
            }
        }
        return configList;
    }


    @Override
    public CompletableFuture<?> parse(ByteBuf buffer, InetSocketAddress remoteAddress) throws Exception {
        final TelemetryMessage msg = new TelemetryMessage(remoteAddress, buffer.nioBuffer());
        return dispatcher.send(msg);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start(ScheduledExecutorService executorService) {

    }

    @Override
    public void stop() {

    }
}
