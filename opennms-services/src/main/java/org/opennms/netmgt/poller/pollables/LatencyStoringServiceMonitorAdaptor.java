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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.LatencyCollectionAttribute;
import org.opennms.netmgt.collection.api.LatencyCollectionAttributeType;
import org.opennms.netmgt.collection.api.LatencyCollectionResource;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.SingleResourceCollectionSet;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.opennms.netmgt.collection.api.CollectionResource.INTERFACE_INFO_IN_TAGS;


/**
 * <p>LatencyStoringServiceMonitorAdaptor class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 */
public class LatencyStoringServiceMonitorAdaptor implements ServiceMonitorAdaptor {

    private static final Logger LOG = LoggerFactory.getLogger(LatencyStoringServiceMonitorAdaptor.class);

    public static final int HEARTBEAT_STEP_MULTIPLIER = 2;

    private static final ServiceParameters EMPTY_SERVICE_PARAMS = new ServiceParameters(Collections.emptyMap());

    private PollerConfig m_pollerConfig;
    private Package m_pkg;
    private final PersisterFactory m_persisterFactory;

    private final ThresholdingService m_thresholdingService;

    private ThresholdingSession m_thresholdingSession;

    public LatencyStoringServiceMonitorAdaptor(PollerConfig config, Package pkg, PersisterFactory persisterFactory, ThresholdingService thresholdingService) {
        m_pollerConfig = config;
        m_pkg = pkg;
        m_persisterFactory = persisterFactory;
        m_thresholdingService = thresholdingService;
    }

    @Override
    public PollStatus handlePollResult(MonitoredService svc, Map<String, Object> parameters, PollStatus status) {
        if (!status.getProperties().isEmpty()) {
            storeResponseTime(svc, new LinkedHashMap<String, Number>(status.getProperties()), parameters);
        }
        return status;
    }

    private void storeResponseTime(MonitoredService svc, Map<String, Number> entries, Map<String,Object> parameters) {
        String rrdPath     = ParameterMap.getKeyedString(parameters, "rrd-repository", null);
        String dsName      = ParameterMap.getKeyedString(parameters, "ds-name", svc.getSvcName().toLowerCase());
        String rrdBaseName = ParameterMap.getKeyedString(parameters, "rrd-base-name", dsName);
        String thresholds  = ParameterMap.getKeyedString(parameters, "thresholding-enabled", "false");
        boolean snmpInfoInTags = ParameterMap.getKeyedBoolean(parameters, INTERFACE_INFO_IN_TAGS, false);

        if (!entries.containsKey(dsName) && entries.containsKey(PollStatus.PROPERTY_RESPONSE_TIME)) {
            entries.put(dsName, entries.get(PollStatus.PROPERTY_RESPONSE_TIME));
            entries.remove(PollStatus.PROPERTY_RESPONSE_TIME);
        }

        if (rrdPath == null) {
            LOG.debug("storeResponseTime: RRD repository not specified in parameters, latency data will not be stored.");
            return;
        }

        CollectionSet collectionSet = getCollectionSet(svc, entries, rrdBaseName, snmpInfoInTags);
        RrdRepository repository = getRrdRepository(rrdPath);

        if (thresholds.equalsIgnoreCase("true")) {
            applyThresholds(collectionSet, svc, dsName);
        } else {
            LOG.debug("storeResponseTime: Thresholds processing is not enabled. Check thresholding-enabled parameter on service definition");
        }

        LOG.debug("storeResponseTime: Persisting latency data for {}", svc);
        persistCollectionSet(collectionSet, repository);
    }

    private RrdRepository getRrdRepository(String rrdPath) {
        File rrdRepositoryRoot = new File(rrdPath);
        RrdRepository repository = new RrdRepository();
        repository.setStep(m_pollerConfig.getStep(m_pkg));
        repository.setRraList(m_pollerConfig.getRRAList(m_pkg));
        repository.setHeartBeat(repository.getStep() * HEARTBEAT_STEP_MULTIPLIER);
        repository.setRrdBaseDir(rrdRepositoryRoot);
        return repository;
    }

    private void persistCollectionSet(CollectionSet collectionSet, RrdRepository repository) {
        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        CollectionSetVisitor persister = m_persisterFactory.createPersister(params, repository, false, true, true);
        collectionSet.visit(persister);
    }

    private void applyThresholds(CollectionSet collectionSet, MonitoredService service, String dsName) {
        try {
            if (m_thresholdingSession == null) {
                m_thresholdingSession = m_thresholdingService.createSession(service.getNodeId(), 
                                                                            service.getIpAddr(),
                                                                            service.getSvcName(),
                                                                            EMPTY_SERVICE_PARAMS);
            }
            m_thresholdingSession.accept(collectionSet);
        } catch (Throwable e) {
            LOG.error("Failed to threshold on {} for {} because of an exception", service, dsName, e);
        }
    }

    private CollectionSet getCollectionSet(MonitoredService service, Map<String, Number> entries, String rrdBaseName, boolean snmpInfoInTags) {
        // When making calls directly to RrdUtils#createRrd() and RrdUtils#updateRrd(),
        // the behavior was as follows:
        // 1) All samples get written to response/${ipAddr}/${rrdBaseName}.rrd
        //     This happens whether or not storeByGroup is enabled.
        // 2) If multiple entries are present, the DSs are created in the same order that they
        //    appear in the map
        // Add labels to be used in time series that depends on labels
        Map<String, String> tags = new HashMap<>();
        tags.put("node_label", service.getNodeLabel());
        tags.put("location", service.getNodeLocation());
        tags.put("node_id", Integer.toString(service.getNodeId()));
        LatencyCollectionResource latencyResource = new LatencyCollectionResource(service.getSvcName(), service.getIpAddr(), service.getNodeLocation(), tags);
        if (snmpInfoInTags) {
            latencyResource.addServiceParam(INTERFACE_INFO_IN_TAGS, "true");
        }
        for (final Entry<String, Number> entry : entries.entrySet()) {
            final String ds = entry.getKey();
            final Number value = entry.getValue() != null ? entry.getValue() : Double.NaN;
            LatencyCollectionAttributeType latencyType = new LatencyCollectionAttributeType(rrdBaseName, ds);
            latencyResource.addAttribute(new LatencyCollectionAttribute(latencyResource, latencyType, ds, value.doubleValue()));
        }

        SingleResourceCollectionSet collectionSet = new SingleResourceCollectionSet(latencyResource, new Date());
        collectionSet.setStatus(CollectionStatus.SUCCEEDED);

        return collectionSet;
    }

}
