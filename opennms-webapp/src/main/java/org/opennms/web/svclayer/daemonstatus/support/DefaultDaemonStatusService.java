/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 26, 2006
 *
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
package org.opennms.web.svclayer.daemonstatus.support;

import java.util.Collection;
import java.util.Map;

import org.opennms.netmgt.dao.DaemonStatusDao;
import org.opennms.netmgt.dao.ServiceInfo;
import org.opennms.netmgt.model.ServiceDaemon;
import org.opennms.web.svclayer.daemonstatus.DaemonStatusService;

/**
 * <p>DefaultDaemonStatusService class.</p>
 *
 * @author <a href="mailto:skareti@users.sourceforge.net">skareti</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultDaemonStatusService implements DaemonStatusService {
	
	private DaemonStatusDao daemonStatusDao; 

	/**
	 * <p>Setter for the field <code>daemonStatusDao</code>.</p>
	 *
	 * @param daemonStatusDao a {@link org.opennms.netmgt.dao.DaemonStatusDao} object.
	 */
	public void setDaemonStatusDao(DaemonStatusDao daemonStatusDao) {
		this.daemonStatusDao = daemonStatusDao;
	}
	
	/**
	 * <p>getCurrentDaemonStatus</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, ServiceInfo> getCurrentDaemonStatus() {
		// TODO Auto-generated method stub
		Map<String, ServiceInfo> info = daemonStatusDao.getCurrentDaemonStatus();
        return info;
	}
	
	/**
	 * <p>getCurrentDaemonStatusColl</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<ServiceInfo> getCurrentDaemonStatusColl() {
		// TODO Auto-generated method stub
		return daemonStatusDao.getCurrentDaemonStatus().values();
        
	}

	/**
	 * <p>performOperationOnDaemons</p>
	 *
	 * @param operation a {@link java.lang.String} object.
	 * @param daemons an array of {@link java.lang.String} objects.
	 * @return a {@link java.util.Map} object.
	 */
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

	/** {@inheritDoc} */
	public Map<String, ServiceInfo> restartDaemon(String service) {
		ServiceDaemon serviceDaemon = daemonStatusDao.getServiceHandle(service);
		serviceDaemon.stop();
		serviceDaemon.start();
		return getCurrentDaemonStatus();
	}

	/** {@inheritDoc} */
	public Map<String, ServiceInfo> startDaemon(String service) {
		ServiceDaemon serviceDaemon = daemonStatusDao.getServiceHandle(service);
		serviceDaemon.start();
		return getCurrentDaemonStatus();
	}

	/** {@inheritDoc} */
	public Map<String, ServiceInfo> stopDaemon(String service) {
		// TODO Auto-generated method stub
		return getCurrentDaemonStatus();
	}

}
