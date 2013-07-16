/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.net.InetAddress;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
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
public class LatencyStoringServiceMonitorAdaptor implements ServiceMonitor {

    
    private static final Logger LOG = LoggerFactory.getLogger(LatencyStoringServiceMonitorAdaptor.class);
    
    /** Constant <code>DEFAULT_BASENAME="response-time"</code> */
    public static final String DEFAULT_BASENAME = "response-time";

    private ServiceMonitor m_serviceMonitor;
    private PollerConfig m_pollerConfig;
    private Package m_pkg;
    
    private LatencyThresholdingSet m_thresholdingSet;

    /**
     * <p>Constructor for LatencyStoringServiceMonitorAdaptor.</p>
     *
     * @param monitor a {@link org.opennms.netmgt.poller.ServiceMonitor} object.
     * @param config a {@link org.opennms.netmgt.config.PollerConfig} object.
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     */
    public LatencyStoringServiceMonitorAdaptor(ServiceMonitor monitor, PollerConfig config, Package pkg) {
        m_serviceMonitor = monitor;
        m_pollerConfig = config;
        m_pkg = pkg;
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(Map<String, Object> parameters) {
        m_serviceMonitor.initialize(parameters);
    }

    /**
     * <p>initialize</p>
     *
     * @param svc a {@link org.opennms.netmgt.poller.MonitoredService} object.
     */
    @Override
    public void initialize(MonitoredService svc) {
        m_serviceMonitor.initialize(svc);
    }

    /** {@inheritDoc} */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        PollStatus status = m_serviceMonitor.poll(svc, parameters);

        if (!status.getProperties().isEmpty()) {
            storeResponseTime(svc, new LinkedHashMap<String, Number>(status.getProperties()), parameters);
        }

        if ("true".equals(ParameterMap.getKeyedString(parameters, "invert-status", "false"))) {
            if (status.isAvailable()) {
                return PollStatus.unavailable("This is an inverted service and the underlying service has started responding");
            } else {
                return PollStatus.available();
            }
        }
        return status;
    }

    private void storeResponseTime(MonitoredService svc, LinkedHashMap<String, Number> entries, Map<String,Object> parameters) {
        String rrdPath     = ParameterMap.getKeyedString(parameters, "rrd-repository", null);
        String dsName      = ParameterMap.getKeyedString(parameters, "ds-name", DEFAULT_BASENAME);
        String rrdBaseName = ParameterMap.getKeyedString(parameters, "rrd-base-name", dsName);
        String thresholds  = ParameterMap.getKeyedString(parameters, "thresholding-enabled", "false");

        if (!entries.containsKey(dsName) && entries.containsKey(DEFAULT_BASENAME)) {
            entries.put(dsName, entries.get(DEFAULT_BASENAME));
            entries.remove(DEFAULT_BASENAME);
        }

        if (thresholds.toLowerCase().equals("true")) {
            applyThresholds(rrdPath, svc, dsName, entries);
        } else {
            LOG.debug("storeResponseTime: Thresholds processing is not enabled. Check thresholding-enabled parameter on service definition");
        }

        if (rrdPath == null) {
            LOG.debug("storeResponseTime: RRD repository not specified in parameters, latency data will not be stored.");
            return;
        }

        updateRRD(rrdPath, svc.getAddress(), rrdBaseName, entries);
    }

    private void applyThresholds(String rrdPath, MonitoredService service, String dsName, LinkedHashMap<String, Number> entries) {
        try {
            if (m_thresholdingSet == null) {
                RrdRepository repository = new RrdRepository();
                repository.setRrdBaseDir(new File(rrdPath));
                m_thresholdingSet = new LatencyThresholdingSet(service.getNodeId(), service.getIpAddr(), service.getSvcName(), repository);
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

    /**
     * Update an RRD database file with latency/response time data.
     *
     * @param repository
     *            path to the RRD file repository
     * @param addr
     *            interface address
     * @param dsName
     *            the datasource name to update
     * @param value
     *            value to update the RRD file with
     * @param rrdBaseName a {@link java.lang.String} object.
     */
    public void updateRRD(String repository, InetAddress addr, String rrdBaseName, String dsName, long value) {
        LinkedHashMap<String, Number> lhm = new LinkedHashMap<String, Number>();
        lhm.put(dsName, value);
        updateRRD(repository, addr, rrdBaseName, lhm);
    }

    /**
     * Update an RRD database file with multiple latency/response time data sources.
     *
     * @param repository
     *            path to the RRD file repository
     * @param addr
     *            interface address
     * @param entries
     *            the entries for the rrd, containing a Map of dsNames to values
     * @param rrdBaseName a {@link java.lang.String} object.
     */
    public void updateRRD(String repository, InetAddress addr, String rrdBaseName, LinkedHashMap<String, Number> entries) {
        try {
            // Create RRD if it doesn't already exist
            List<RrdDataSource> dsList = new ArrayList<RrdDataSource>(entries.size());
            for (String dsName : entries.keySet()) {
                dsList.add(new RrdDataSource(dsName, "GAUGE", m_pollerConfig.getStep(m_pkg)*2, "U", "U"));
            }
            createRRD(repository, addr, rrdBaseName, dsList);

            // add interface address to RRD repository path
            final String hostAddress = InetAddressUtils.str(addr);
			String path = repository + File.separator + hostAddress;

            StringBuffer value = new StringBuffer();
            Iterator<String> i = entries.keySet().iterator();
            while (i.hasNext()) {
                Number num = entries.get(i.next());
                if (num == null || Double.isNaN(num.doubleValue())) {
                    value.append("U");
                } else {
                    NumberFormat nf = NumberFormat.getInstance(Locale.US);
                    nf.setGroupingUsed(false);
                    nf.setMinimumFractionDigits(0);
                    nf.setMaximumFractionDigits(Integer.MAX_VALUE);
                    nf.setMinimumIntegerDigits(0);
                    nf.setMaximumIntegerDigits(Integer.MAX_VALUE);
                    value.append(nf.format(num.doubleValue()));
                }
                if (i.hasNext()) {
                    value.append(":");
                }
            }
            RrdUtils.updateRRD(hostAddress, path, rrdBaseName, value.toString());

        } catch (RrdException e) {
            String msg = e.getMessage();
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Create an RRD database file with a single dsName for storing latency/response time data.
     *
     * @param repository
     *            path to the RRD file repository
     * @param addr
     *            interface address
     * @param dsName
     *            data source/RRD file name
     * @return true if RRD file successfully created, false otherwise
     * @param rrdBaseName a {@link java.lang.String} object.
     * @throws org.opennms.netmgt.rrd.RrdException if any.
     */
    public boolean createRRD(String repository, InetAddress addr, String rrdBaseName, String dsName) throws RrdException {
        List<RrdDataSource> dsList = Collections.singletonList(new RrdDataSource(dsName, "GAUGE", m_pollerConfig.getStep(m_pkg)*2, "U", "U"));
        return createRRD(repository, addr, rrdBaseName, dsList);

    }

    /**
     * Create an RRD database file with multiple dsNames for storing latency/response time data.
     *
     * @param repository
     *            path to the RRD file repository
     * @param addr
     *            interface address
     * @return true if RRD file successfully created, false otherwise
     * @param rrdBaseName a {@link java.lang.String} object.
     * @param dsList a {@link java.util.List} object.
     * @throws org.opennms.netmgt.rrd.RrdException if any.
     */
    public boolean createRRD(String repository, InetAddress addr, String rrdBaseName, List<RrdDataSource> dsList) throws RrdException {

        List<String> rraList = m_pollerConfig.getRRAList(m_pkg);

        // add interface address to RRD repository path
        final String hostAddress = InetAddressUtils.str(addr);
		String path = repository + File.separator + hostAddress;

        return RrdUtils.createRRD(hostAddress, path, rrdBaseName, m_pollerConfig.getStep(m_pkg), dsList, rraList);

    }

    /**
     * <p>release</p>
     */
    @Override
    public void release() {
        m_serviceMonitor.release();
    }

    /** {@inheritDoc} */
    @Override
    public void release(MonitoredService svc) {
        m_serviceMonitor.release(svc);
    }

    /**
     * Should be called when thresholds configuration has been reloaded
     */
    public void refreshThresholds() {
        if (m_thresholdingSet != null)
            m_thresholdingSet.reinitialize();
    }

}
