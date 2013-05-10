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

package org.opennms.web.controller.event;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.ObjectNotFoundException;
import org.jfree.util.Log;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.dao.AlarmRepository;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.web.event.Event;
import org.opennms.web.event.EventUtil;
import org.opennms.web.event.WebEventRepository;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This servlet receives an HTTP POST with a list of events and acknowledgments for 
 * the selected alarms to purge , and then it redirects the client to a URL for display.
 * The target URL is configurable in the servlet config (web.xml file).
 *
 */

public class EventPurgeController extends AbstractController implements InitializingBean {
    
    /** Constant <code>PURGE_ACTION="1"</code> */
    public final static String PURGE_ACTION = "1";
    
    /** Constant <code>PURGEALL_ACTION="2"</code> */
    public final static String PURGEALL_ACTION = "2";
    
    /** Constant <code>SUCCESS_ACTION="Y"</code> */
    public final static String SUCCESS_ACTION = "Y";
    
    /** Constant <code>FAILURE_ACTION="N"</code> */
    public final static String FAILURE_ACTION = "N";
    
	/** To hold default redirectView page */
    private String m_redirectView;
    
    
    /**
     * OpenNMS event repository
     */
    private WebEventRepository m_webEventRepository;
    
    /**
     * OpenNMS alarm repository
     */
    private AlarmRepository m_alarmRepository;
    
    /**
     * Logging
     */
    private Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + EventPurgeController.class.getName());

    /**
     * <p>setRedirectView</p>
     *
     * @param redirectView a {@link java.lang.String} object.
     */
    public void setRedirectView(String redirectView) {
        m_redirectView = redirectView;
    }
    
  
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_redirectView, "redirectView must be set");
        Assert.notNull(m_webEventRepository, "webEventRepository must be set");
        Assert.notNull(m_alarmRepository, "alarmRepository must be set");
    }

  

    /**
     * {@inheritDoc}
     *
     * Purge of the selected alarms specified in the POST and then redirect the client
     * to an appropriate URL for display.
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
    	logger.info("Enter into the EventPurgeController action");
    	
    	// handle the event and actionCode parameter
    	String[] eventIdStrings = request.getParameterValues("event");
        String action = request.getParameter("actionCode");
        
        List<Event> eventList = new ArrayList<Event>();
        if (eventIdStrings != null) {
        	
        	// convert the event id strings to int's
            int[] eventIds = new int[eventIdStrings.length];
            for (int i = 0; i < eventIds.length; i++) {
            	try{
            		eventIds[i] = WebSecurityUtils.safeParseInt(eventIdStrings[i]);
            	} catch (Exception e) {
    				logger.error("Could not parse event ID '{}' to integer.",eventIdStrings[i]);
    			}
            }
            
            // Get event by it's id
    		for (int eventId : eventIds) {
    			try {
        			eventList.add(m_webEventRepository.getEvent(eventId));
    			} catch (Exception e) {
    				logger.error("Could not retrieve event from webeventRepository for ID='{}'", eventId);
    			}
    		}
        }
        
        
        // handle the filter parameter
        List<Filter> filterList = new ArrayList<Filter>();
        String[] filterStrings = request.getParameterValues("filter");
        if (action.equals(PURGEALL_ACTION)) {
        	if(filterStrings != null){
	            for (int i = 0; i < filterStrings.length; i++) {
	                Filter filter = EventUtil.getFilter(filterStrings[i], getServletContext());
	                if (filter != null) {
	                    filterList.add(filter);
	                }
	            }
        	}
        }
        
        //Get the events by event criteria
        Filter[] eventFilters = filterList.toArray(new Filter[0]);
        if(action.equals(PURGEALL_ACTION)){
        	
        	EventCriteria eventQueryCriteria = new EventCriteria(eventFilters);
	        Event[] events = m_webEventRepository.getMatchingEvents(eventQueryCriteria);
	        
	        for(Event event : events){
	        	eventList.add(event);
	        }
        }
        
        List<Integer> eventIds = new ArrayList<Integer>();
        
        for(Event event : eventList){
        	try {
        		int alarmId = event.getAlarmId();
        		OnmsAlarm alarm =  m_alarmRepository.getAlarm(alarmId);
        		if(alarm ==null || alarm.getId() != alarmId)
        			eventIds.add(event.getId());
        		else {
        			Log.debug("Active alarm is present for event with id " + event.getId());
        		}
        	}
	        catch (HibernateObjectRetrievalFailureException  e) {
	        	Log.error("HibernateObjectRetrievalFailureException : No active alarm is present for event with id " + event.getId());
	        	eventIds.add(event.getId());
	        	continue;
			}catch (ObjectNotFoundException oe) {
				Log.error("ObjectNotFoundException : No active alarm is present for event with id " + event.getId());
				eventIds.add(event.getId());
	        	continue;
			}
		}
        
        // handle the purge action
        if (action.equals(PURGE_ACTION) || action.equals(PURGEALL_ACTION) ) {
        	try{
        		m_webEventRepository.purgeEvents(eventIds);
        		request.getSession().setAttribute("actionStatus", eventList.size()+","+SUCCESS_ACTION);
        	} catch(final Exception e){
        		request.getSession().setAttribute("actionStatus", eventList.size()+","+FAILURE_ACTION);
        	    logger.error("Unable to do this action for this event Id's.", eventIds);
        	}
        } 
        
        // handle the redirect parameters
        String redirectParms = request.getParameter("redirectParms");
        String viewName = m_redirectView;
        if(redirectParms!=null){
        	viewName = m_redirectView + "?" + redirectParms;
        }
        RedirectView view = new RedirectView(viewName, true);
        logger.info("Terminated from the EventPurgeController action");
        return new ModelAndView(view);
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
     * <p>setAlarmRepository</p>
     *
     * @param webAlarmRepository a {@link org.opennms.web.event.WebAlarmRepository} object.
     */
    public void setAlarmRepository(AlarmRepository alarmRepository) {
        m_alarmRepository = alarmRepository;
    }
    
    
   
}
