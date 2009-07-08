/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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

/*
 * Created on 1-giu-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.admin.discovery;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.web.WebSecurityUtils;



class GeneralSettingsLoader {
	protected static Category log = ThreadCategory.getInstance("WEB");
	
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
