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
	/** Constant <code>log</code> */
	protected static Category log = ThreadCategory.getInstance("WEB");
	
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
