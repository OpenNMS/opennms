/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.admin.discovery;

import javax.servlet.http.HttpServletRequest;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;



class GeneralSettingsLoader {
	/** Constant <code>log</code> */
	protected static ThreadCategory log = ThreadCategory.getInstance("WEB");
	
	/**
	 * <p>load</p>
	 *
	 * @param request a {@link javax.servlet.http.HttpServletRequest} object.
	 * @param config a {@link org.opennms.netmgt.config.discovery.DiscoveryConfiguration} object.
	 * @return a {@link org.opennms.netmgt.config.discovery.DiscoveryConfiguration} object.
	 */
	public static DiscoveryConfiguration load(HttpServletRequest request, DiscoveryConfiguration config){
		String initSTStr = request.getParameter("initialsleeptime");
		String restartSTStr = request.getParameter("restartsleeptime");
		String threadsStr = request.getParameter("threads");
		String retriesStr = request.getParameter("retries");
		String timeoutStr = request.getParameter("timeout");
		
		log.debug("initialsleeptime: "+initSTStr);
		log.debug("restartsleeptime: "+restartSTStr);
		log.debug("threads: "+threadsStr);
		log.debug("retries: "+retriesStr);
		log.debug("timeout: "+timeoutStr);
		
		
		long initSt = WebSecurityUtils.safeParseLong(initSTStr);
		long restartSt = WebSecurityUtils.safeParseLong(restartSTStr);
		
		config.setInitialSleepTime(initSt);
		config.setRestartSleepTime(restartSt);
		//set the general settings loaded into current configuration
		if(threadsStr!=null){
			config.setThreads(WebSecurityUtils.safeParseInt(threadsStr));
		}
		
		
		if(retriesStr!=null && (!retriesStr.trim().equals("") && !retriesStr.trim().equals("3"))){
				config.setRetries(WebSecurityUtils.safeParseInt(retriesStr));
		}else{
			config.deleteRetries();
		}
		
		if(timeoutStr!=null && (!timeoutStr.trim().equals("") && !timeoutStr.trim().equals("800"))){
			config.setTimeout(Long.valueOf(timeoutStr).longValue());
		}else{
			config.deleteTimeout();
		}
	
		
		log.debug("General settings uploaded.");
		
		return config;
	}

}
