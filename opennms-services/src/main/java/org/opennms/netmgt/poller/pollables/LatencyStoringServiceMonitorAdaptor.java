/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.SingleResourceCollectionSet;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.poller.LatencyCollectionAttribute;
import org.opennms.netmgt.poller.LatencyCollectionAttributeType;
import org.opennms.netmgt.poller.LatencyCollectionResource;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.threshd.LatencyThresholdingSet;
import org.opennms.netmgt.threshd.ThresholdingEventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>LatencyStoringServiceMonitorAdaptor class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 */
public class LatencyStoringServiceMonitorAdaptor implements ServiceMonitorAdaptor {

    private static final Logger LOG = LoggerFactory.getLogger(LatencyStoringServiceMonitorAdaptor.class);

    public static final int HEARTBEAT_STEP_MULTIPLIER = 2;

    private PollerConfig m_pollerConfig;
    private Package m_pkg;
    private final PersisterFactory m_persisterFactory;
    private final ResourceStorageDao m_resourceStorageDao;

    private LatencyThresholdingSet m_thresholdingSet;

    /**
     * <p>Constructor for LatencyStoringServiceMonitorAdaptor.</p>
     *
     * @param monitor a {@link org.opennms.netmgt.poller.ServiceMonitor} object.
     * @param config a {@link org.opennms.netmgt.config.PollerConfig} object.
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     */
    public LatencyStoringServiceMonitorAdaptor(PollerConfig config, Package pkg, PersisterFactory persisterFactory, ResourceStorageDao resourceStorageDao) {
        m_pollerConfig = config;
        m_pkg = pkg;
        m_persisterFactory = persisterFactory;
        m_resourceStorageDao = resourceStorageDao;
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
        String dsName      = ParameterMap.getKeyedString(parameters, "ds-name", PollStatus.PROPERTY_RESPONSE_TIME);
        String rrdBaseName = ParameterMap.getKeyedString(parameters, "rrd-base-name", dsName);
        String thresholds  = ParameterMap.getKeyedString(parameters, "thresholding-enabled", "false");

        if (!entries.containsKey(dsName) && entries.containsKey(PollStatus.PROPERTY_RESPONSE_TIME)) {
            entries.put(dsName, entries.get(PollStatus.PROPERTY_RESPONSE_TIME));
            entries.remove(PollStatus.PROPERTY_RESPONSE_TIME);
        }

        if (thresholds.equalsIgnoreCase("true")) {
            applyThresholds(rrdPath, svc, dsName, entries);
        } else {
            LOG.debug("storeResponseTime: Thresholds processing is not enabled. Check thresholding-enabled parameter on service definition");
        }

        if (rrdPath == null) {
            LOG.debug("storeResponseTime: RRD repository not specified in parameters, latency data will not be stored.");
            return;
        }

        LOG.debug("storeResponseTime: Persisting latency data for {}", svc);
        persistLatencySamples(svc, entries, new File(rrdPath), rrdBaseName);
    }

    private void applyThresholds(String rrdPath, MonitoredService service, String dsName, Map<String, Number> entries) {
        try {
            if (m_thresholdingSet == null) {
                RrdRepository repository = new RrdRepository();
                repository.setRrdBaseDir(new File(rrdPath));
                m_thresholdingSet = new LatencyThresholdingSet(service.getNodeId(), service.getIpAddr(), service.getSvcName(), service.getNodeLocation(), repository, m_resourceStorageDao);
            }
            LinkedHashMap<String, Double> attributes = new LinkedHashMap<String, Double>();
            for (String ds : entries.keySet()) {
                Number sampleValue = entries.get(ds);
                if (sampleValue == null) {
                    attributes.put(ds, Double.NaN);
                } else {
                    attributes.put(ds, sampleValue.doubleValue());
                }
            }
            if (m_thresholdingSet.isNodeInOutage()) {
                LOG.info("applyThresholds: the threshold processing will be skipped because the service {} is on a scheduled outage.", service);
            } else if (m_thresholdingSet.hasThresholds(attributes)) {
                List<Event> events = m_thresholdingSet.applyThresholds(dsName, attributes);
                if (events.size() > 0) {
                    ThresholdingEventProxy proxy = new ThresholdingEventProxy();
                    proxy.add(events);
                    proxy.sendAllEvents();
                }
            }
	} catch(Throwable e) {
	    LOG.error("Failed to threshold on {} for {} because of an exception", service, dsName, e);
	}
    }

    private void persistLatencySamples(MonitoredService service, Map<String, Number> entries, File rrdRepositoryRoot, String rrdBaseName) {
        RrdRepository repository = new RrdRepository();
        repository.setStep(m_pollerConfig.getStep(m_pkg));
        repository.setRraList(m_pollerConfig.getRRAList(m_pkg));
        repository.setHeartBeat(repository.getStep() * HEARTBEAT_STEP_MULTIPLIER);
        repository.setRrdBaseDir(rrdRepositoryRoot);

        // When making calls directly to RrdUtils#createRrd() and RrdUtils#updateRrd(),
        // the behavior was as follows:
        // 1) All samples get written to response/${ipAddr}/${rrdBaseName}.rrd
        //     This happens whether or not storeByGroup is enabled.
        // 2) If multiple entries are present, the DSs are created in the same order that they
        //    appear in the map

        LatencyCollectionResource latencyResource = new LatencyCollectionResource(service.getSvcName(), service.getIpAddr(), service.getNodeLocation());
        for (final Entry<String, Number> entry : entries.entrySet()) {
            final String ds = entry.getKey();
            final Number value = entry.getValue() != null ? entry.getValue() : Double.NaN;
            LatencyCollectionAttributeType latencyType = new LatencyCollectionAttributeType(rrdBaseName, ds);
            latencyResource.addAttribute(new LatencyCollectionAttribute(latencyResource, latencyType, ds, value.doubleValue()));
        }

        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        CollectionSetVisitor persister = m_persisterFactory.createPersister(params, repository, false, true, true);

        SingleResourceCollectionSet collectionSet = new SingleResourceCollectionSet(latencyResource, new Date());
        collectionSet.setStatus(CollectionStatus.SUCCEEDED);
        collectionSet.visit(persister);
    }

    /**
     * Should be called when thresholds configuration has been reloaded
     */
    public void refreshThresholds() {
        if (m_thresholdingSet != null)
            m_thresholdingSet.reinitialize();
    }

}
