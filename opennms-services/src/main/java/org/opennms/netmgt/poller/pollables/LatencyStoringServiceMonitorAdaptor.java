//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.poller.pollables;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.ping.StatisticalArrayList;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.utils.ParameterMap;

public class LatencyStoringServiceMonitorAdaptor implements ServiceMonitor {
	
	public static final String DEFAULT_MULTI_BASENAME = "multi-response-time";
    public static final String DEFAULT_DSNAME = "response-time";

	
	ServiceMonitor m_serviceMonitor;
	PollerConfig m_pollerConfig;
	Package m_pkg;

	public LatencyStoringServiceMonitorAdaptor(ServiceMonitor monitor, PollerConfig config, Package pkg) {
		m_serviceMonitor = monitor;
		m_pollerConfig = config;
		m_pkg = pkg;
	}

	public void initialize(Map parameters) {
		m_serviceMonitor.initialize(parameters);
	}

	public void initialize(MonitoredService svc) {
		m_serviceMonitor.initialize(svc);
	}

	public PollStatus poll(MonitoredService svc, Map parameters) {
		PollStatus status = m_serviceMonitor.poll(svc, parameters);
		if (status.getResponseTimes() != null && status.getResponseTimes().size() > 0) {
			storeResponseTime(svc, status.getResponseTimes(), parameters);
		}
		if (status.getResponseTime() >= 0) {
			storeResponseTime(svc, status.getResponseTime(), parameters);
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
	
	private void storeResponseTime(MonitoredService svc, Collection<Long> responseTimes, Map parameters) {
		String rrdPath = ParameterMap.getKeyedString(parameters, "rrd-repository", null);
		String rrdBaseName = ParameterMap.getKeyedString(parameters, "rrd-base-name", DEFAULT_MULTI_BASENAME);
		
		if (rrdPath == null) {
			log().info("storeResponseTime(multi): RRD repository not specified in parameters, latency data will not be stored.");
		}
		
		Long timestamp = System.currentTimeMillis();

		StatisticalArrayList<Long> sal = new StatisticalArrayList<Long>(responseTimes);

		List<String> dsNames = new ArrayList<String>();
		List<String> values  = new ArrayList<String>();
		
		dsNames.add("loss");
		values.add((new Integer(sal.countNull())).toString());
		
		dsNames.add("median");
		values.add(sal.median().toString());
		
		Long responseTime = 0L;
		
		for (int i = 0; i < sal.size(); i++) {
			responseTime = sal.get(i);
			dsNames.add("ping" + (i + 1));
			if (responseTime == null) {
				values.add("");
			} else {
				values.add(responseTime.toString());
			}
		}
		updateRRD(rrdPath, svc.getAddress(), rrdBaseName, dsNames, values);
	}
	
	private void storeResponseTime(MonitoredService svc, long responseTime, Map parameters) {
        String rrdPath = ParameterMap.getKeyedString(parameters, "rrd-repository", null);
        String dsName = ParameterMap.getKeyedString(parameters, "ds-name", DEFAULT_DSNAME);
        String rrdBaseName = ParameterMap.getKeyedString(parameters, "rrd-base-name", dsName);

        if (rrdPath == null) {
            log().info("storeResponseTime(single): RRD repository not specified in parameters, latency data will not be stored.");
        }
        
        // RRD BEGIN
        // Store response time in RRD
        if (responseTime >= 0 && rrdPath != null) {
            try {
                updateRRD(rrdPath, svc.getAddress(), rrdBaseName, dsName, responseTime);
            } catch (RuntimeException rex) {
                log().debug("There was a problem writing the RRD:" + rex);
            }
        }
        // RRD END


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
     * @param dsName2 
     * @param value
     *            value to update the RRD file with
     */
	
	public void updateRRD(String repository, InetAddress addr, String rrdBaseName, String dsName, long value) {
		updateRRD(repository, addr, rrdBaseName, Collections.singletonList(dsName), Collections.singletonList(Long.toString(value)));
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
     * @param dsNames
     *            the dsName
     * @param values
     *            value to update the RRD file with
     */
	
    public void updateRRD(String repository, InetAddress addr, String rrdBaseName, List<String> dsNames, List<String> values) {
        Category log = ThreadCategory.getInstance(this.getClass());

        try {
            // Create RRD if it doesn't already exist
        	List<RrdDataSource> dsList = new ArrayList<RrdDataSource>();
        	for (int i = 0; i < dsNames.size(); i++) {
        		dsList.add(new RrdDataSource(dsNames.get(i), "GAUGE", m_pollerConfig.getStep(m_pkg)*2, "U", "U"));
        	}
            createRRD(repository, addr, rrdBaseName, dsList);

            // add interface address to RRD repository path
            String path = repository + File.separator + addr.getHostAddress();

            StringBuffer value = new StringBuffer();
            Iterator<String> i = values.iterator();
            while (i.hasNext()) {
            	value.append(i.next());
            	if (i.hasNext()) {
            		value.append(":");
            	}
            }
            RrdUtils.updateRRD(addr.getHostAddress(), path, rrdBaseName, value.toString());

        } catch (RrdException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                String msg = e.getMessage();
                log.error(msg);
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

        List rraList = m_pollerConfig.getRRAList(m_pkg);

        // add interface address to RRD repository path
        String path = repository + File.separator + addr.getHostAddress();

        return RrdUtils.createRRD(addr.getHostAddress(), path, rrdBaseName, m_pollerConfig.getStep(m_pkg), dsList, rraList);

    }

    private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	public void release() {
		m_serviceMonitor.release();
	}

	public void release(MonitoredService svc) {
		m_serviceMonitor.release(svc);
	}

}
