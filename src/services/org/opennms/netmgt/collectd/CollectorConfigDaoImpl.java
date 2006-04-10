package org.opennms.netmgt.collectd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.dao.CollectorConfigDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsPackage;

public class CollectorConfigDaoImpl implements CollectorConfigDao {
	
    /**
     * Map of all available ServiceCollector objects indexed by service name
     */
    static Map m_svcCollectors;

	public CollectorConfigDaoImpl() {
        m_svcCollectors = Collections.synchronizedMap(new TreeMap());

		loadConfigFactory();
		
		instantiateCollectors();
	}

	public OnmsPackage load(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public OnmsPackage get(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public OnmsPackage findPackageForService(OnmsMonitoredService svc) {
		// TODO Auto-generated method stub
		return null;
	}
	
	CollectdConfiguration getConfig() {
		return CollectdConfigFactory.getInstance().getConfiguration();
	}

	private void loadConfigFactory() {
	    // Load collectd configuration file
	    try {
	        CollectdConfigFactory.reload();
	    } catch (MarshalException ex) {
	        log().fatal("init: Failed to load collectd configuration", ex);
	        throw new UndeclaredThrowableException(ex);
	    } catch (ValidationException ex) {
	        log().fatal("init: Failed to load collectd configuration", ex);
	        throw new UndeclaredThrowableException(ex);
	    } catch (IOException ex) {
	        log().fatal("init: Failed to load collectd configuration", ex);
	        throw new UndeclaredThrowableException(ex);
	    }
	}

	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	private void instantiateCollectors() {
			CollectdConfiguration config = getConfig();
	    /*
	     * Load up an instance of each collector from the config
	     * so that the event processor will have them for
	     * new incomming events to create collectable service objects.
	     */
	    Enumeration eiter = config.enumerateCollector();
	    while (eiter.hasMoreElements()) {
	        Collector collector = (Collector) eiter.nextElement();
	        String svcName = collector.getService();
			try {
	            if (log().isDebugEnabled()) {
	                log().debug("init: Loading collector " 
	                          + svcName + ", classname "
	                          + collector.getClassName());
	            }
	            Class cc = Class.forName(collector.getClassName());
	            ServiceCollector sc = (ServiceCollector) cc.newInstance();
	
	            // Attempt to initialize the service collector
	            Map properties = null; // properties not currently used
	            sc.initialize(properties);
	
	            setServiceCollector(svcName, sc);
	        } catch (Throwable t) {
	            if (log().isEnabledFor(Priority.WARN)) {
	                log().warn("init: Failed to load collector "
	                         + collector.getClassName() + " for service "
	                         + svcName, t);
	            }
	        }
	    }
	}

	void setServiceCollector(String svcName, ServiceCollector sc) {
		m_svcCollectors.put(svcName, sc);
	}

	public ServiceCollector getServiceCollector(String svcName) {
        return (ServiceCollector) m_svcCollectors.get(svcName);
	}

	public Set getCollectorName() {
		return m_svcCollectors.keySet();
	}

	public int getSchedulerThreads() {
		return getConfig().getThreads();
	}

	public Collection getPackagesForService(OnmsMonitoredService svc) {
		Collection matchingPkgs = new LinkedList();

        CollectdConfigFactory cCfgFactory = CollectdConfigFactory.getInstance();
        CollectdConfiguration cConfig = getConfig();
        Enumeration epkgs = cConfig.enumeratePackage();
        
        
        /*
         * Compare interface/service pair against each collectd package
         * For each match, create new SnmpCollector object and
         * schedule it for collection
         */
        while (epkgs.hasMoreElements()) {
            Package pkg = (Package) epkgs.nextElement();

            /*
             * Make certain the the current service is in the package
             * and enabled!
             */
             if (!cCfgFactory.serviceInPackageAndEnabled(svc.getServiceType().getName(), pkg)) {
                if (log().isDebugEnabled()) {
                    log().debug("scheduleInterface: address/service: " + svc + " not scheduled, service is not "
                              + "enabled or does not exist in package: "
                              + pkg.getName());
                }
                continue;
            }

            // Is the interface in the package?
            if (!cCfgFactory.interfaceInPackage(svc.getIpAddress(), pkg)) {
                if (log().isDebugEnabled()) {
                    log().debug("scheduleInterface: address/service: " + svc + " not scheduled, interface "
                              + "does not belong to package: " + pkg.getName());
                }
                continue;
            }
            
            matchingPkgs.add(pkg);
        }
		return matchingPkgs;
	}

}
