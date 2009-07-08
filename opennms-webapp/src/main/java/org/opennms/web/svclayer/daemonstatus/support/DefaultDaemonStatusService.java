/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.svclayer.daemonstatus.support;

import java.util.Collection;
import java.util.Map;

import org.opennms.netmgt.dao.DaemonStatusDao;
import org.opennms.netmgt.dao.ServiceInfo;
import org.opennms.netmgt.model.ServiceDaemon;
import org.opennms.web.svclayer.daemonstatus.DaemonStatusService;

/**
 * 
 * @author <a href="mailto:skareti@users.sourceforge.net">skareti</a>
 */
public class DefaultDaemonStatusService implements DaemonStatusService {
	
	private DaemonStatusDao daemonStatusDao; 

	public void setDaemonStatusDao(DaemonStatusDao daemonStatusDao) {
		this.daemonStatusDao = daemonStatusDao;
	}
	
	public Map<String, ServiceInfo> getCurrentDaemonStatus() {
		// TODO Auto-generated method stub
		Map<String, ServiceInfo> info = daemonStatusDao.getCurrentDaemonStatus();
        return info;
	}
	
	public Collection<ServiceInfo> getCurrentDaemonStatusColl() {
		// TODO Auto-generated method stub
		return daemonStatusDao.getCurrentDaemonStatus().values();
        
	}

	public Map<String, ServiceInfo> performOperationOnDaemons(String operation, String[] daemons) {
		// TODO Auto-generated method stub
		for(int i = 0; i < daemons.length; i++){
			if(operation.equalsIgnoreCase("start")) {
				startDaemon(daemons[i]);
			} else if(operation.equalsIgnoreCase("stop")) {
				stopDaemon(daemons[i]);				
			} else if(operation.equalsIgnoreCase("restart")) {
				restartDaemon(daemons[i]);
			} else if(operation.equalsIgnoreCase("refresh")) {
				// do nothing
			} else {
				// TBD raise an exception...or ignore...
			}
		}
		return getCurrentDaemonStatus();
	}

	public Map<String, ServiceInfo> restartDaemon(String service) {
		ServiceDaemon serviceDaemon = daemonStatusDao.getServiceHandle(service);
		serviceDaemon.stop();
		serviceDaemon.start();
		return getCurrentDaemonStatus();
	}

	public Map<String, ServiceInfo> startDaemon(String service) {
		ServiceDaemon serviceDaemon = daemonStatusDao.getServiceHandle(service);
		serviceDaemon.start();
		return getCurrentDaemonStatus();
	}

	public Map<String, ServiceInfo> stopDaemon(String service) {
		// TODO Auto-generated method stub
		return getCurrentDaemonStatus();
	}

}
