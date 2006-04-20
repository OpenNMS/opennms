/**
 * 
 */
package org.opennms.netmgt.config;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Package;

public class CollectdConfig {
	CollectdConfiguration m_config;
	Collection m_packages;
	Map m_collectors = new HashMap(4);
	
	CollectdConfig(CollectdConfiguration config) {
		m_config = config;
		
		instantiateCollectors();
		
		m_packages = new LinkedList();
		Enumeration pkgEnum = m_config.enumeratePackage();
		while (pkgEnum.hasMoreElements()) {
			Package pkg = (Package) pkgEnum.nextElement();
			m_packages.add(new CollectdPackage(pkg));
		}
		
		
		
	}
	
	public CollectdConfiguration getConfig() {
		return m_config;
	}

	public Collection getPackages() {
		return m_packages;
	}

	public int getThreads() {
		return m_config.getThreads();
	}
	
	public void setServiceCollector(String svcName, ServiceCollector collector) {
		m_collectors.put(svcName, collector);
	}
	
	public ServiceCollector getServiceCollector(String svcName) {
		return (ServiceCollector)m_collectors.get(svcName);
	}

	public Set getCollectorNames() {
		return m_collectors.keySet();
	}

	private void instantiateCollectors() {
        log().debug("init: Loading collectors");

		/*
	     * Load up an instance of each collector from the config
	     * so that the event processor will have them for
	     * new incomming events to create collectable service objects.
	     */
	    Enumeration eiter = getConfig().enumerateCollector();
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

	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}
	
}