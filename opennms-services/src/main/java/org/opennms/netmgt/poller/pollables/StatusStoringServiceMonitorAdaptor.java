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
package org.opennms.netmgt.poller.pollables;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.dto.CollectionAgentDTO;
import org.opennms.netmgt.collection.dto.CollectionSetDTO;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.LatencyTypeResource;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.opennms.netmgt.rrd.RrdRepository;

import static org.opennms.netmgt.collection.api.CollectionResource.INTERFACE_INFO_IN_TAGS;


public class StatusStoringServiceMonitorAdaptor implements ServiceMonitorAdaptor {
    public static final int HEARTBEAT_STEP_MULTIPLIER = 2;

    private final PollerConfig pollerConfig;
    private final Package pkg;
    private final PersisterFactory persisterFactory;

    public StatusStoringServiceMonitorAdaptor(final PollerConfig config,
                                              final Package pkg,
                                              final PersisterFactory persisterFactory) {
        this.pollerConfig = Objects.requireNonNull(config);
        this.pkg = Objects.requireNonNull(pkg);
        this.persisterFactory = Objects.requireNonNull(persisterFactory);
    }

    @Override
    public PollStatus handlePollResult(final MonitoredService svc,
                                       final Map<String, Object> parameters,
                                       final PollStatus status) {
        if (ParameterMap.getKeyedBoolean(parameters, "rrd-status", true)) {
            storeStatus(svc, status, parameters);
        }

        return status;
    }

    private void storeStatus(final MonitoredService svc,
                             final PollStatus status,
                             final Map<String,Object> parameters) {
        final RrdRepository rrdRepository = new RrdRepository();
        rrdRepository.setStep(this.pollerConfig.getStep(this.pkg));
        rrdRepository.setRraList(this.pollerConfig.getRRAList(this.pkg));
        rrdRepository.setHeartBeat(rrdRepository.getStep() * HEARTBEAT_STEP_MULTIPLIER);
        rrdRepository.setRrdBaseDir(new File(System.getProperty("rrd.base.dir"), ResourceTypeUtils.STATUS_DIRECTORY));

        final String dsName      = ParameterMap.getKeyedString(parameters, "ds-name", svc.getSvcName().toLowerCase());
        final String rrdBaseName = ParameterMap.getKeyedString(parameters, "rrd-base-name", dsName);
        Boolean snmpInfoInTags = ParameterMap.getKeyedBoolean(parameters, INTERFACE_INFO_IN_TAGS, false);

        // Build collection agent
        final CollectionAgentDTO agent = new CollectionAgentDTO();
        agent.setAddress(svc.getAddress());
        agent.setNodeId(svc.getNodeId());
        agent.setNodeLabel(svc.getNodeLabel());
        agent.setLocationName(svc.getNodeLocation());
        agent.setStorageResourcePath(ResourcePath.get());
        agent.setStoreByForeignSource(false);

        // Create collection set from response times as gauges and persist
        final CollectionSetBuilder collectionSetBuilder = new CollectionSetBuilder(agent);
        final LatencyTypeResource resource = new LatencyTypeResource(svc.getSvcName(), svc.getIpAddr(), svc.getNodeLocation());
        resource.addTag("node_id", Integer.toString(svc.getNodeId()));
        resource.addTag("node_label", svc.getNodeLabel());
        resource.addTag("location", svc.getNodeLocation());
        if (snmpInfoInTags) {
            resource.addServiceParam(INTERFACE_INFO_IN_TAGS, "true");
        }
        collectionSetBuilder.withGauge(resource, rrdBaseName, dsName, buildPollStatusValue(status));

        final CollectionSetDTO collectionSetDTO = collectionSetBuilder.build();

        collectionSetDTO.visit(this.persisterFactory.createPersister(new ServiceParameters(Collections.emptyMap()),
                                                                     rrdRepository,
                                                                     false,
                                                                     true,
                                                                     true));
    }

    /**
     * Calculates a float value for the given poll status.
     *
     * @param status the status to calculate a value for
     * @return the calculated value
     */
    private static double buildPollStatusValue(final PollStatus status) {
        switch (status.getStatusCode()) {
            case PollStatus.SERVICE_AVAILABLE: return 1.0;
            case PollStatus.SERVICE_UNAVAILABLE: return 0.0;
            case PollStatus.SERVICE_UNRESPONSIVE: return -1.0;
            case PollStatus.SERVICE_UNKNOWN: return Double.NaN;
            default: throw new IllegalStateException("Unhandled status code");
        }
    }
}
