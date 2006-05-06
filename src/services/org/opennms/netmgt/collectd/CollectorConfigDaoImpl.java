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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

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
	
	private ScheduledOutagesDao m_scheduledOutagesDao;

	public CollectorConfigDaoImpl() {

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
        
            /*
             * Make certain the the current service is in the package
             * and enabled!
             */
             if (!wpkg.serviceInPackageAndEnabled(svcName)) {
                if (log().isDebugEnabled()) {
                    log().debug("scheduleInterface: address/service: " + iface + '/' + svcName + " not scheduled, service is not "
                              + "enabled or does not exist in package: "
                              + wpkg.getName());
                }
                continue;
            }

            // Is the interface in the package?
            if (!wpkg.interfaceInPackage(iface.getIpAddress())) {
                if (log().isDebugEnabled()) {
                    log().debug("scheduleInterface: address/service: " + iface + '/' + svcName + " not scheduled, interface "
                              + "does not belong to package: " + wpkg.getName());
                }
                continue;
            }
            
            Collection outageCalendars = new LinkedList();
            
            matchingPkgs.add(new CollectionSpecification(wpkg, svcName, outageCalendars, getServiceCollector(svcName)));
        }
		return matchingPkgs;
	}

}
