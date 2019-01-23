/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.pollers;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.opennms.integration.api.v1.pollers.PollerRequest;
import org.opennms.integration.api.v1.pollers.PollerResult;
import org.opennms.integration.api.v1.pollers.ServicePoller;
import org.opennms.integration.api.v1.pollers.ServicePollerFactory;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;

/**
 * Maps {@link ServicePollerFactory} to {@link ServiceMonitor}.
 * The factory mapping is intentional. integration-api will have factories for service pollers.
 * @param <T>
 */
public class ServicePollerImpl<T extends ServicePoller> implements ServiceMonitor {

    private ServicePollerFactory<T> servicePollerFactory;

    public ServicePollerImpl(ServicePollerFactory servicePollerFactory) {
        this.servicePollerFactory = servicePollerFactory;
    }

    @Override
    public org.opennms.netmgt.poller.PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        Map<String, String> attributes = getAttributes(parameters);
        PollerRequest monitoredService = new PollerRequestImpl(svc, attributes);
        ServicePoller servicePoller = servicePollerFactory.createPoller();
        CompletableFuture<PollerResult> future = servicePoller.poll(monitoredService);
        PollerResult pollStatus;
        try {
            pollStatus = future.get();
            switch (pollStatus.getStatus()) {
                case Up:
                    return PollStatus.up();
                case Down:
                    return PollStatus.down(pollStatus.getReason());
                case UnResponsive:
                    return PollStatus.unresponsive(pollStatus.getReason());
                case Unknown:
                    return PollStatus.unknown(pollStatus.getReason());
            }
        } catch (InterruptedException | ExecutionException e) {
            return PollStatus.down(e.getMessage());
        }
        return PollStatus.unknown(pollStatus.getReason());
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(MonitoredService svc, Map<String, Object> parameters) {
        Map<String, Object> runTimeAttributes = new HashMap<>();
        Map<String, String> attributes = servicePollerFactory.getRuntimeAttributes(new PollerRequestImpl(svc, getAttributes(parameters)));
        attributes.forEach(runTimeAttributes::put);
        return runTimeAttributes;
    }

    private Map<String, String> getAttributes(Map<String, Object> parameters) {
        Map<String, String> attributes = new HashMap<>();
        parameters.forEach((parameter, value) -> {
            if (value instanceof String) {
                attributes.put(parameter, (String) value);
            }
        });
        return attributes;
    }

    @Override
    public String getEffectiveLocation(String location) {
        return null;
    }

    protected class PollerRequestImpl implements PollerRequest {

        private MonitoredService monitoredService;

        private Map<String, String> attributes;

        private PollerRequestImpl(MonitoredService svc, Map<String, String> attributes) {
            this.monitoredService = svc;
            this.attributes = attributes;
        }

        @Override
        public String getServiceName() {
            return monitoredService.getSvcName();
        }

        @Override
        public InetAddress getAddress() {
            return monitoredService.getAddress();
        }

        @Override
        public int getNodeId() {
            return monitoredService.getNodeId();
        }

        @Override
        public Map<String, String> getPollerAttributes() {
            return this.attributes;
        }
    }
}
