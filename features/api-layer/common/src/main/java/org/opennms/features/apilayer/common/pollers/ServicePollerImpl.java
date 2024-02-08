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
package org.opennms.features.apilayer.common.pollers;

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
            final org.opennms.netmgt.poller.PollStatus mappedPollStatus;
            switch (pollStatus.getStatus()) {
                case Up:
                    mappedPollStatus = PollStatus.up();
                    break;
                case Down:
                    mappedPollStatus = PollStatus.down(pollStatus.getReason());
                    break;
                case UnResponsive:
                    mappedPollStatus = PollStatus.unresponsive(pollStatus.getReason());
                    break;
                default:
                    mappedPollStatus = PollStatus.unknown(pollStatus.getReason());
            }
            mappedPollStatus.setProperties(pollStatus.getProperties());
            return mappedPollStatus;
        } catch (InterruptedException | ExecutionException e) {
            return PollStatus.down(e.getMessage());
        }
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
