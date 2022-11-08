/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.pollables;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LocationUtils;
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
        if (ParameterMap.getKeyedBoolean(parameters, "rrd-status", false)) {
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

        final String dsName      = ParameterMap.getKeyedString(parameters, "ds-name", PollStatus.PROPERTY_RESPONSE_TIME);
        final String rrdBaseName = ParameterMap.getKeyedString(parameters, "rrd-base-name", dsName);

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
