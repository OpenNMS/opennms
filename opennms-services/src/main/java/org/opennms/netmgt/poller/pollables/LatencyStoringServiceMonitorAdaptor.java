package org.opennms.netmgt.poller.pollables;

import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.utils.ParameterMap;

public class LatencyStoringServiceMonitorAdaptor implements ServiceMonitor {
	
    public static final String DEFAULT_DSNAME = "response-time";

	
	ServiceMonitor m_serviceMonitor;
	PollerConfig m_pollerConfig;
	Package m_pkg;

	public LatencyStoringServiceMonitorAdaptor(ServiceMonitor monitor, PollerConfig config, Package pkg) {
		m_serviceMonitor = monitor;
		m_pollerConfig = config;
		m_pkg = pkg;
	}

	public void initialize(PollerConfig config, Map parameters) {
		m_serviceMonitor.initialize(config, parameters);
	}

	public void initialize(MonitoredService svc) {
		m_serviceMonitor.initialize(svc);
	}

	public PollStatus poll(MonitoredService svc, Map parameters, Package pkg) {
		PollStatus status = m_serviceMonitor.poll(svc, parameters, pkg);
		if (status.getResponseTime() >= 0) {
			storeResponseTime(svc, status.getResponseTime(), parameters);
		}
		return status;
	}
	

	private void storeResponseTime(MonitoredService svc, long responseTime, Map parameters) {
        String rrdPath = ParameterMap.getKeyedString(parameters, "rrd-repository", null);
        String dsName = ParameterMap.getKeyedString(parameters, "ds-name", DEFAULT_DSNAME);

        if (rrdPath == null) {
            log().info("poll: RRD repository not specified in parameters, latency data will not be stored.");
        }
        
        // RRD BEGIN
        // Store response time in RRD
        if (responseTime >= 0 && rrdPath != null) {
            try {
                updateRRD(rrdPath, svc.getAddress(), dsName, responseTime);
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
     * @param value
     *            value to update the RRD file with
     * 
     * @return true if RRD file successfully created, false otherwise
     */
    public void updateRRD(String repository, InetAddress addr, String dsName, long value) {
        Category log = ThreadCategory.getInstance(this.getClass());

        try {
            // Create RRD if it doesn't already exist
            createRRD(repository, addr, dsName);

            // add interface address to RRD repository path
            String path = repository + File.separator + addr.getHostAddress();

            RrdUtils.updateRRD(addr.getHostAddress(), path, dsName, Long.toString(value));

        } catch (RrdException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                String msg = e.getMessage();
                log.error(msg);
                throw new RuntimeException(msg, e);
            }
        }
    }

    /**
     * Create an RRD database file for storing latency/response time data.
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
    public boolean createRRD(String repository, InetAddress addr, String dsName) throws RrdException {

        List rraList = m_pollerConfig.getRRAList(m_pkg);

        // add interface address to RRD repository path
        String path = repository + File.separator + addr.getHostAddress();

        return RrdUtils.createRRD(addr.getHostAddress(), path, dsName, m_pollerConfig.getStep(m_pkg), "GAUGE", 600, "U", "U", rraList);

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
