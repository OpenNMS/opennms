/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2008 Apr 30: Fix for bug #2445,  Also make all fields private, add the
 *              exception cause to rethrown exception messages, specify initial
 *              array size when we have an idea of what the size will end up
 *              being, and use generics to eliminate warnings. - dj@opennms.org 
 * 
 * Created August 22, 2006
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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

import org.apache.log4j.Level;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
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

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 */
public class LatencyStoringServiceMonitorAdaptor implements ServiceMonitor {

    public static final String DEFAULT_BASENAME = "response-time";

    private ServiceMonitor m_serviceMonitor;
    private PollerConfig m_pollerConfig;
    private Package m_pkg;
    
    private LatencyThresholdingSet m_thresholdingSet;

    public LatencyStoringServiceMonitorAdaptor(ServiceMonitor monitor, PollerConfig config, Package pkg) {
        m_serviceMonitor = monitor;
        m_pollerConfig = config;
        m_pkg = pkg;
    }

    public void initialize(Map<String, Object> parameters) {
        m_serviceMonitor.initialize(parameters);
    }

    public void initialize(MonitoredService svc) {
        m_serviceMonitor.initialize(svc);
    }

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
            log().debug("storeResponseTime: Thresholds processing is not enabled. Check thresholding-enabled parameter on service definition");
        }

        if (rrdPath == null) {
            log().debug("storeResponseTime: RRD repository not specified in parameters, latency data will not be stored.");
            return;
        }

        updateRRD(rrdPath, svc.getAddress(), rrdBaseName, entries);
    }

    private void applyThresholds(String rrdPath, MonitoredService service, String dsName, LinkedHashMap<String, Number> entries) {
	try {
        if (m_thresholdingSet == null) {
            RrdRepository repository = new RrdRepository();
            repository.setRrdBaseDir(new File(rrdPath));
            // Interval does not make sense for Latency Thresholding, because all values are gauge.
            m_thresholdingSet = new LatencyThresholdingSet(service.getNodeId(), service.getIpAddr(), service.getSvcName(), repository, 0);
        }
        LinkedHashMap<String, Double> attributes = new LinkedHashMap<String, Double>();
        for (String ds : entries.keySet()) {
            attributes.put(ds, entries.get(ds).doubleValue());
        }
        if (m_thresholdingSet.hasThresholds(attributes)) {
            List<Event> events = m_thresholdingSet.applyThresholds(dsName, attributes);
            if (events.size() > 0) {
                ThresholdingEventProxy proxy = new ThresholdingEventProxy();
                proxy.add(events);
                proxy.sendAllEvents();
            }
        }

	} catch(Exception e) {
	    log().error("Failed to threshold on " + service + " for " + dsName + " because of an exception", e);
	}
    }

    /**
     * Update an RRD database file with latency/response time data.
     * 
     * @param rrdJniInterface
     *            interface used to issue RRD commands.
     * @param repository
     *            path to the RRD file repository
     * @param addr
     *            interface address
     * @param dsName
     *            the datasource name to update 
     * @param value
     *            value to update the RRD file with
     */

    public void updateRRD(String repository, InetAddress addr, String rrdBaseName, String dsName, long value) {
        LinkedHashMap<String, Number> lhm = new LinkedHashMap<String, Number>();
        lhm.put(dsName, value);
        updateRRD(repository, addr, rrdBaseName, lhm);
    }

    /**
     * Update an RRD database file with multiple latency/response time data sources.
     * 
     * @param rrdJniInterface
     *            interface used to issue RRD commands.
     * @param repository
     *            path to the RRD file repository
     * @param addr
     *            interface address
     * @param entries
     *            the entries for the rrd, containing a Map of dsNames to values
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
            String path = repository + File.separator + addr.getHostAddress();

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
            RrdUtils.updateRRD(addr.getHostAddress(), path, rrdBaseName, value.toString());

        } catch (RrdException e) {
            if (log().isEnabledFor(Level.ERROR)) {
                String msg = e.getMessage();
                log().error(msg);
                throw new RuntimeException(msg, e);
            }
        }
    }

    /**
     * Create an RRD database file with a single dsName for storing latency/response time data.
     * 
     * @param rrdJniInterface
     *            interface used to issue RRD commands.
     * @param repository
     *            path to the RRD file repository
     * @param addr
     *            interface address
     * @param dsName
     *            data source/RRD file name
     * 
     * @return true if RRD file successfully created, false otherwise
     */
    public boolean createRRD(String repository, InetAddress addr, String rrdBaseName, String dsName) throws RrdException {
        List<RrdDataSource> dsList = Collections.singletonList(new RrdDataSource(dsName, "GAUGE", m_pollerConfig.getStep(m_pkg)*2, "U", "U"));
        return createRRD(repository, addr, rrdBaseName, dsList);

    }

    /**
     * Create an RRD database file with multiple dsNames for storing latency/response time data.
     * 
     * @param rrdJniInterface
     *            interface used to issue RRD commands.
     * @param repository
     *            path to the RRD file repository
     * @param addr
     *            interface address
     * @param dsName
     *            data source/RRD file name list
     * 
     * @return true if RRD file successfully created, false otherwise
     */
    public boolean createRRD(String repository, InetAddress addr, String rrdBaseName, List<RrdDataSource> dsList) throws RrdException {

        List<String> rraList = m_pollerConfig.getRRAList(m_pkg);

        // add interface address to RRD repository path
        String path = repository + File.separator + addr.getHostAddress();

        return RrdUtils.createRRD(addr.getHostAddress(), path, rrdBaseName, m_pollerConfig.getStep(m_pkg), dsList, rraList);

    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void release() {
        m_serviceMonitor.release();
    }

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
