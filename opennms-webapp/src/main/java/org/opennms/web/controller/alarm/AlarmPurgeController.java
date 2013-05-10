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
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.dao.AlarmRepository;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.event.Event;
import org.opennms.web.event.EventUtil;
import org.opennms.web.event.WebEventRepository;
import org.opennms.web.event.filter.EventCriteria;
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
	private org.opennms.web.alarm.AcknowledgeType m_defaultAlarmAcknowledgeType = org.opennms.web.alarm.AcknowledgeType.UNACKNOWLEDGED;;
	
	/**
	 * OpenNMS event default acknowledge type
	 */
	private org.opennms.web.event.AcknowledgeType m_defaultEventAcknowledgeType = org.opennms.web.event.AcknowledgeType.UNACKNOWLEDGED;;

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
	 * <p>
	 * getFiltersForEvent
	 * </p>
	 * 
	 * @return list of {@link org.opennms.web.filter.Filter} object.
	 */
    public List<Filter> getFiltersForEvent(OnmsAlarm alarm, String nodeid, String exactuei, String ipaddress){
    	
    	String filtersString[] = new String[3];
    	int filterCount = 0;
    	
    	if(alarm.getNodeId()!= null){
    		filtersString[filterCount++] = nodeid.concat(String.valueOf(alarm.getNodeId()));
    	}
    	if(alarm.getIpAddr()!= null){
    		filtersString[filterCount++] = ipaddress.concat(InetAddressUtils.str(alarm.getIpAddr()));
    	}
    	if(alarm.getUei()!= null){
    		filtersString[filterCount++] = exactuei.concat(alarm.getUei());
    	}
    	
    	List<Filter> filterList = new ArrayList<Filter>();
    	for (String filterString : filtersString) {
    		try{
    			if(filterString != null){
    				filterList.add(EventUtil.getFilter(filterString, getServletContext()));
    			}
    		} catch(Exception e){
    			logger.error("Could not retrieve filter name for filterString='{}'", filterString);
    		}
        }
    	return filterList;
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
        
        List<OnmsAlarm> alarmList = new ArrayList<OnmsAlarm>();
        if (alarmIdStrings != null) {
        	
        	// Convert the alarm id strings to int's
            int[] alarmIds = new int[alarmIdStrings.length];
            for (int i = 0; i < alarmIds.length; i++) {
            	try{
            		alarmIds[i] = WebSecurityUtils.safeParseInt(alarmIdStrings[i]);
            	} catch (Exception e) {
    				logger.error("Could not parse alarm ID '{}' to integer.",alarmIdStrings[i]);
    			}
            }
            
            // Get alarm by it's id
    		for (int alarmId : alarmIds) {
    			try {
        			alarmList.add(m_webAlarmRepository.getAlarm(alarmId));
    			} catch (Exception e) {
    				logger.error("Could not retrieve alarm from webAlarmRepository for ID='{}'", alarmId);
    			}
    		}
        }
        
        // Handle the acknowledge type parameter
        String ackTypeString = request.getParameter("acktype");
        
        org.opennms.web.alarm.AcknowledgeType alarmAckType = m_defaultAlarmAcknowledgeType;
        org.opennms.web.event.AcknowledgeType eventAckType = m_defaultEventAcknowledgeType;
        
        if (ackTypeString != null) {
        	try{
		        alarmAckType = org.opennms.web.alarm.AcknowledgeType.getAcknowledgeType(ackTypeString);
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
        	
        	AlarmCriteria alarmQueryCriteria = new AlarmCriteria(alarmAckType,alarmFilters);
	        OnmsAlarm[] alarms = m_webAlarmRepository.getMatchingAlarms(AlarmUtil.getOnmsCriteria(alarmQueryCriteria));
	        
	        for(OnmsAlarm alarm : alarms){
	        	alarmList.add(alarm);
	        }
        }
        
        // Handle the default event key filters parameter
        String nodeidKey = request.getParameter("nodeid");
        String exactueiKey = request.getParameter("exactuei");
        String ipaddressKey = request.getParameter("ipaddress");
        
        HashMap<Integer, List<Integer>> eventIdsForAlarms = new HashMap<Integer, List<Integer>>();
        HashMap<Integer, List<Integer>> ackRefIdsForAlarms = new HashMap<Integer, List<Integer>>();
        List<Integer> alarmIds = new ArrayList<Integer>();
        
        for(OnmsAlarm alarm : alarmList){
        	
	        //Get default event filters
        	filterList.clear();
	        filterList = getFiltersForEvent(alarm,nodeidKey,exactueiKey,ipaddressKey);

	    	Filter[] filters = filterList.toArray(new Filter[0]);
	        List<Integer> eventIdsList = new ArrayList<Integer>();
	        List<Integer> ackRefIdsList = new ArrayList<Integer>();
	        
	        if(alarm != null){
	        	
	        	//Get the events by event criteria
	        	Event[] events = null;
	        	EventCriteria eventCriteria = new EventCriteria(eventAckType, filters);
	        	try{
	        		events = m_webEventRepository.getMatchingEvents(eventCriteria);
	        	} catch(Exception e){
	        		logger.error("Could not retrieve events for this EventCriteria ='{}'", eventCriteria);
	        	}
		        
		        //Get the event Id's as well as acknowledge refId's
	        	for(Event event : events){
	        		eventIdsList.add(event.getId());
	        		if(event.getAlarmId()!=null && event.getAlarmId()>0 && (!ackRefIdsList.contains(event.getAlarmId())))
	        			ackRefIdsList.add(event.getAlarmId());
	    		}
	        }
	        alarmIds.add(alarm.getId());
	        eventIdsForAlarms.put(alarm.getId(), eventIdsList);
	        ackRefIdsForAlarms.put(alarm.getId(), ackRefIdsList);
		}
        
        // Handle the purge action
        if (action.equals(PURGE_ACTION) || action.equals(PURGEALL_ACTION)) {
        	try{
        		m_webAlarmRepository.purgeAlarms(alarmIds,eventIdsForAlarms,ackRefIdsForAlarms);
        		request.getSession().setAttribute("actionStatus", alarmList.size()+","+SUCCESS_ACTION);
        	} catch(final Exception e){
        		request.getSession().setAttribute("actionStatus", alarmList.size()+","+FAILURE_ACTION);
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
