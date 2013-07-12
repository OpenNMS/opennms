/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.alarm;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.web.alarm.AcknowledgeType;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.event.WebEventRepository;
import org.opennms.web.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This servlet receives an HTTP POST with a list of alarms and purge or purge all action 
 * for the selected alarms, and then it redirects the client to a URL for display.
 * The target URL is configurable in the servlet config (web.xml file).
 *
 */

public class AlarmPurgeController extends AbstractController implements InitializingBean {
    
    /** Constant <code>PURGE_ACTION="1"</code> */
    public final static String PURGE_ACTION = "1";
    
    /** Constant <code>PURGEALL_ACTION="2"</code> */
    public final static String PURGEALL_ACTION = "2";
    
    /** Constant <code>SUCCESS_ACTION="Y"</code> */
    public final static String SUCCESS_ACTION = "Y";
    
    /** Constant <code>FAILURE_ACTION="N"</code> */
    public final static String FAILURE_ACTION = "N";
    
	/**
	 * OpenNMS alarm default acknowledge type
	 */
	private AcknowledgeType m_defaultAlarmAcknowledgeType = AcknowledgeType.UNACKNOWLEDGED;
	
    /**
	 * OpenNMS alarm repository
	 */
    private AlarmRepository m_webAlarmRepository;
    
    /**
     * OpenNMS event repository
     */
    private WebEventRepository m_webEventRepository;
    
    /** To hold default redirectView page */
    private String m_redirectView;
    
    /**
     * Logging
     */
    private Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + AlarmPurgeController.class.getName());

    /**
     * <p>setRedirectView</p>
     *
     * @param redirectView a {@link java.lang.String} object.
     */
    public void setRedirectView(String redirectView) {
        m_redirectView = redirectView;
    }
    
    /**
     * <p>setWebAlarmRepository</p>
     *
     * @param webAlarmRepository a {@link org.opennms.netmgt.dao.AlarmRepository} object.
     */
    public void setAlarmRepository(AlarmRepository webAlarmRepository) {
        m_webAlarmRepository = webAlarmRepository;
    }

    /**
     * <p>setWebEventRepository</p>
     *
     * @param webEventRepository a {@link org.opennms.web.event.WebEventRepository} object.
     */
    public void setWebEventRepository(WebEventRepository webEventRepository) {
        m_webEventRepository = webEventRepository;
    }
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_webAlarmRepository, "webAlarmRepository must be set");
        Assert.notNull(m_webEventRepository, "webEventRepository must be set");
        Assert.notNull(m_redirectView, "redirectView must be set");
    }

    /**
     * {@inheritDoc}
     *
     * Purge or purge all action of the selected alarms specified in the POST and then redirect 
     * the client to an appropriate URL for display.
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
    	logger.info("Enter into the AlarmPurgeController action");
    	
    	// Handle the alarm and actionCode parameter
    	String[] alarmIdStrings = request.getParameterValues("alarm");
        String action = request.getParameter("actionCode");
        
        List<Integer> alarmIds = new ArrayList<Integer>();
        if (alarmIdStrings != null && action.equals(PURGE_ACTION)) {
            for (int i = 0; i < alarmIdStrings.length; i++) {
            	try{
            		alarmIds.add(WebSecurityUtils.safeParseInt(alarmIdStrings[i]));
            	} catch (Exception e) {
    				logger.error("Could not parse alarm ID '{}' to integer.",alarmIdStrings[i]);
    			}
            }
        }
        
        // Handle the acknowledge type parameter
        String ackTypeString = request.getParameter("acktype");
        AcknowledgeType alarmAckType = m_defaultAlarmAcknowledgeType;
        
        if (ackTypeString != null) {
        	try{
		        alarmAckType = AcknowledgeType.getAcknowledgeType(ackTypeString);
	        } catch (Exception e) {
				logger.error("Could not retrieve acknowledge type for this '{}'.",ackTypeString);
			}
        }
        
        // Handle the filter parameter
        List<Filter> filterList = new ArrayList<Filter>();
        String[] filterStrings = request.getParameterValues("filter");
        
        if (action.equals(PURGEALL_ACTION)) {
        	if(filterStrings != null){
	            for (int i = 0; i < filterStrings.length; i++) {
	                Filter filter = AlarmUtil.getFilter(filterStrings[i], getServletContext());
	                if (filter != null) {
	                    filterList.add(filter);
	                }
	            }
        	}
        }
        
        //Get the alarms by alarm criteria
        Filter[] alarmFilters = filterList.toArray(new Filter[0]);
        
        if(action.equals(PURGEALL_ACTION)){
        	alarmIds.clear();
        	AlarmCriteria alarmQueryCriteria = new AlarmCriteria(alarmAckType,alarmFilters);
	        OnmsAlarm[] alarms = m_webAlarmRepository.getMatchingAlarms(AlarmUtil.getOnmsCriteria(alarmQueryCriteria));
	        for(OnmsAlarm alarm : alarms){
	        	alarmIds.add(alarm.getId());
	        }
        }
        
        // Handle the purge action
        if (action.equals(PURGE_ACTION) || action.equals(PURGEALL_ACTION)) {
        	try{
        		m_webAlarmRepository.purgeAlarms(alarmIds);
        		request.getSession().setAttribute("actionStatus", alarmIds.size()+","+SUCCESS_ACTION);
        		logger.info("The Purge action is successfully completed for the alarm Id's "+alarmIds+" ");
        	} catch(final Exception ex){
        		ex.printStackTrace();
        		request.getSession().setAttribute("actionStatus", alarmIds.size()+","+FAILURE_ACTION);
        	    logger.error("Unable to do purge action for this alarm Id's.", alarmIds);
        	}
        } else {
        	logger.error("Unknown alarm action: " + action);
        }
        
        // Handle the redirect parameters
        String redirectParms = request.getParameter("redirectParms");
        String viewName = m_redirectView;
        if(redirectParms!=null){
        	viewName = m_redirectView + "?" + redirectParms;
        }
        
        RedirectView view = new RedirectView(viewName, true);
        logger.info("Terminated from the AlarmPurgeController action");
        
        return new ModelAndView(view);
    }
}
