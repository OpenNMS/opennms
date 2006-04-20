package org.opennms.netmgt.collectd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CollectdConfig;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.dao.CollectorConfigDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsPackage;

public class CollectorConfigDaoImpl implements CollectorConfigDao {
	
    /**
     * Map of all available ServiceCollector objects indexed by service name
     */
    private static Map m_svcCollectors;
    
	private ScheduledOutagesDao m_scheduledOutagesDao;

	public CollectorConfigDaoImpl() {
        m_svcCollectors = Collections.synchronizedMap(new TreeMap());

		loadConfigFactory();
		
	}
	
	public void setScheduledOutageConfigDao(ScheduledOutagesDao scheduledOutagesDao) {
		m_scheduledOutagesDao = scheduledOutagesDao;
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

	public Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	private CollectdConfig getConfig() {
		return CollectdConfigFactory.getInstance().getCollectdConfig();
	}

	private ServiceCollector getServiceCollector(String svcName) {
		return getConfig().getServiceCollector(svcName);
	}

	public Set getCollectorNames() {
		return getConfig().getCollectorNames();
	}

	public int getSchedulerThreads() {
		return getConfig().getThreads();
	}

	public Collection getSpecificationsForInterface(OnmsIpInterface iface, String svcName) {
		Collection matchingPkgs = new LinkedList();

        CollectdConfigFactory cCfgFactory = CollectdConfigFactory.getInstance();
        
        /*
         * Compare interface/service pair against each collectd package
         * For each match, create new SnmpCollector object and
         * schedule it for collection
         */
        CollectdConfig config = cCfgFactory.getCollectdConfig();
        for (Iterator it = config.getPackages().iterator(); it.hasNext();) {
			CollectdPackage wpkg = (CollectdPackage) it.next();
			Package pkg = wpkg.getPackage();
        
            /*
             * Make certain the the current service is in the package
             * and enabled!
             */
             if (!wpkg.serviceInPackageAndEnabled(svcName)) {
                if (log().isDebugEnabled()) {
                    log().debug("scheduleInterface: address/service: " + iface + '/' + svcName + " not scheduled, service is not "
                              + "enabled or does not exist in package: "
                              + pkg.getName());
                }
                continue;
            }

            // Is the interface in the package?
            if (!wpkg.interfaceInPackage(iface.getIpAddress())) {
                if (log().isDebugEnabled()) {
                    log().debug("scheduleInterface: address/service: " + iface + '/' + svcName + " not scheduled, interface "
                              + "does not belong to package: " + pkg.getName());
                }
                continue;
            }
            
            Collection outageCalendars = new LinkedList();
            Enumeration enumeration = pkg.enumerateOutageCalendar();
            while (enumeration.hasMoreElements()) {
				String outageName = (String) enumeration.nextElement();
				outageCalendars.add(m_scheduledOutagesDao.get(outageName));
			}
            
            
            matchingPkgs.add(new CollectionSpecification(pkg, svcName, outageCalendars, getServiceCollector(svcName)));
        }
		return matchingPkgs;
	}

}
