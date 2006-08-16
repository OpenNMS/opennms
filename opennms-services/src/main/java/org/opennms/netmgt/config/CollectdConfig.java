/**
 * 
 */
package org.opennms.netmgt.config;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Package;

public class CollectdConfig {
	private CollectdConfiguration m_config;
	private Collection<CollectdPackage> m_packages;
	private Map<String,ServiceCollector> m_collectors = new HashMap<String,ServiceCollector>(4);
	
	/**
	 * Convenience object for CollectdConfiguration.
	 * 
	 * @param config collectd configuration object
	 * @param localServer local server name from opennms-server.xml
	 * @param verifyServer verify server option from opennms-server.xml
	 */
	protected CollectdConfig(CollectdConfiguration config, String localServer, boolean verifyServer) {
		m_config = config;
		
		instantiateCollectors();
		
		createPackageObjects(localServer, verifyServer);
		
		initialize(localServer, verifyServer);
		
	}

	private void createPackageObjects(String localServer, boolean verifyServer) {
		m_packages = new LinkedList<CollectdPackage>();
		Enumeration pkgEnum = m_config.enumeratePackage();
		while (pkgEnum.hasMoreElements()) {
			Package pkg = (Package) pkgEnum.nextElement();
			m_packages.add(new CollectdPackage(pkg, localServer, verifyServer));
		}
	}
	
	public CollectdConfiguration getConfig() {
		return m_config;
	}

	public Collection<CollectdPackage> getPackages() {
		return m_packages;
	}

	public int getThreads() {
		return m_config.getThreads();
	}
	
	public void setServiceCollector(String svcName, ServiceCollector collector) {
		m_collectors.put(svcName, collector);
	}
	
	public ServiceCollector getServiceCollector(String svcName) {
		return m_collectors.get(svcName);
	}

	public Set<String> getCollectorNames() {
		return m_collectors.keySet();
	}

	private void instantiateCollectors() {
        log().debug("instantiateCollectors: Loading collectors");

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
	                log().debug("instantiateCollectors: Loading collector " 
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
	        	log().warn("instantiateCollectors: Failed to load collector "
	        			+ collector.getClassName() + " for service "
	        			+ svcName, t);
	        }
	    }
	}

	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	/**
	 * This method is used to establish package agaist iplist mapping, with
	 * which, the iplist is selected per package via the configured filter rules
	 * from the database.
	 * @param verifyServer2 
	 * @param localServer2 
	 * @param localServer TODO
	 * @param verifyServer TODO
	 */
	protected void createPackageIpListMap(String localServer, boolean verifyServer) {
	
		// Multiple threads maybe asking for the m_pkgIpMap field so create
		// with temp map then assign when finished.
		
		for (Iterator it = getPackages().iterator(); it.hasNext();) {
			CollectdPackage wpkg = (CollectdPackage) it.next();
			wpkg.createIpList(localServer, verifyServer);
		}
	}

	/**
	 * @param localServer TODO
	 * @param verifyServer TODO
	 */
	protected void initialize(String localServer, boolean verifyServer)  {
		createPackageIpListMap(localServer, verifyServer);
		
	}

	public CollectdPackage getPackage(String name) {
	    for (Iterator it = getPackages().iterator(); it.hasNext();) {
			CollectdPackage wpkg = (CollectdPackage) it.next();
			if (wpkg.getName().equals(name)) {
				return wpkg;
			}
		}
		return null;
	}

	/**
	 * Returns true if collection domain exists
	 * 
	 * @param name
	 *            The domain name to check
	 * @return True if the domain exists
	 */
	public boolean domainExists(String name) {
	    for (Iterator it = getPackages().iterator(); it.hasNext();) {
			CollectdPackage wpkg = (CollectdPackage) it.next();
			if ((wpkg.ifAliasDomain() != null)
					&& wpkg.ifAliasDomain().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the specified interface is included by at least one
	 * package which has the specified service and that service is enabled (set
	 * to "on").
	 * 
	 * @param ipAddr
	 *            IP address of the interface to lookup
	 * @param svcName
	 *            The service name to lookup
	 * @return true if Collectd config contains a package which includes the
	 *         specified interface and has the specified service enabled.
	 */
	public boolean isServiceCollectionEnabled(String ipAddr, String svcName) {
		boolean result = false;
	
	    for (Iterator it = getPackages().iterator(); it.hasNext();) {
			CollectdPackage wpkg = (CollectdPackage) it.next();
	
			// Does the package include the interface?
			//
			if (wpkg.interfaceInPackage(ipAddr)) {
				// Yes, now see if package includes
				// the service and service is enabled
				//
				if (wpkg.serviceInPackageAndEnabled(svcName)) {
					// Thats all we need to know...
					result = true;
				}
			}
		}
	
		return result;
	}
	
}