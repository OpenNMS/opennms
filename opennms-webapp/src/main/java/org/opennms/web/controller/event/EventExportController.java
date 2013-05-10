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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.api.reporting.ReportFormat;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
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

/**
 * This servlet receives an HTTP POST with a list of events and export or export all action
 * for the selected events. and then it displays the event reports on current URL page
 *
 */

public class EventExportController extends AbstractController implements InitializingBean {
    
    /** Constant <code>EXPORT_ACTION="1"</code> */
    public final static String EXPORT_ACTION = "1";
    
    /** Constant <code>EXPORTALL_ACTION="2"</code> */
    public final static String EXPORTALL_ACTION = "2";
    
    /** Constant <code>ACTION_STATUS="N"</code> */
    public static String ACTION_STATUS = "N";
    
	/**
     * OpenNMS event repository
     */
    private WebEventRepository m_webEventRepository;
    
    /**
     * OpenNMS report wrapper service
     */
    private ReportWrapperService m_reportWrapperService;
    
    /**
     * Logging
     */
    private Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + EventExportController.class.getName());

    /**
     * <p>setWebEventRepository</p>
     *
     * @param webEventRepository a {@link org.opennms.web.event.WebEventRepository} object.
     */
    public void setWebEventRepository(WebEventRepository webEventRepository) {
        m_webEventRepository = webEventRepository;
    }
    
    /**
     * <p>setReportWrapperService</p>
     *
     * @param reportWrapperService a {@link org.opennms.reporting.core.svclayer.ReportWrapperService} object.
     */
    public void setReportWrapperService(ReportWrapperService reportWrapperService) {
        m_reportWrapperService = reportWrapperService;
    }
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
       Assert.notNull(m_webEventRepository, "webEventRepository must be set");
       Assert.notNull(m_reportWrapperService, "reportWrapperService must be set");
    }

    /**
     * {@inheritDoc}
     *
     * Export or export all action of the selected events specified in the POST and 
     * then display the client to an appropriate URL.
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
    	logger.info("Enter into the EventExportController action");
    	
    	// Handle the event and actionCode parameter
    	String[] eventIdStrings = request.getParameterValues("event");
        String action = request.getParameter("actionCode");
        
        List<Event> eventList = new ArrayList<Event>();
        if (eventIdStrings != null) {
        	
        	// Convert the event id strings to int's
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
    				logger.error("Could not retrieve event from webEventRepository for ID='{}'", eventId);
    			}
    		}
        }
        
        // Handle the sort style parameter
 		String sortStyleString = request.getParameter("sortby");
 		if (sortStyleString != null) {
 			try {
 			} catch (Exception e) {
 				logger.error("Could not retrieve sort id for this '{}'.",sortStyleString);
 			}
 		}
     		
       
        // Handle the filter parameter
        List<Filter> filterList = new ArrayList<Filter>();
        String[] filterStrings = request.getParameterValues("filter");
        if (action.equals(EXPORTALL_ACTION)) {
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
        if(action.equals(EXPORTALL_ACTION)){
        	
        	EventCriteria eventQueryCriteria = new EventCriteria(eventFilters);
	        Event[] events = m_webEventRepository.getMatchingEvents(eventQueryCriteria);
	        
	        for(Event event : events){
	        	eventList.add(event);
	        }
        }
        
        // Handle the report format and reportId parameter
        String reportId = request.getParameter("reportId");
        String requestFormat = request.getParameter("format");
        
    	if (ReportFormat.PDF == ReportFormat.valueOf(requestFormat)) {
            response.setContentType("application/pdf;charset=UTF-8");
        } else if(ReportFormat.CSV == ReportFormat.valueOf(requestFormat)){
        	response.setContentType("text/csv;charset=UTF-8");
        } else if(ReportFormat.XLS == ReportFormat.valueOf(requestFormat)){
        	response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        } else if(ReportFormat.HTML == ReportFormat.valueOf(requestFormat)){
        	response.setContentType("text/html;charset=UTF-8");
        } else{
        	logger.error("Unknown file format : " + requestFormat);
        }
    	response.setHeader("Content-disposition", "inline; filename=event_report"+new SimpleDateFormat("_MMddyyyy_HHmmss").format(new Date())+"."+requestFormat.toLowerCase());
    	response.setHeader("Pragma", "public");
    	response.setHeader("Cache-Control", "cache");
    	response.setHeader("Cache-Control", "must-revalidate");
        
        List<Integer> eventIds = new ArrayList<Integer>();
        
        for(Event event : eventList){
        	eventIds.add(event.getId());
        }
        
        // Handle the export action
        if (action.equals(EXPORT_ACTION)|| action.equals(EXPORTALL_ACTION)) {
        	try{
        		m_reportWrapperService.getEventReport(eventIds, reportId,
        				ReportFormat.valueOf(requestFormat), response.getOutputStream());
        		ACTION_STATUS = "Y";
        	} catch(final Exception e){
        		ACTION_STATUS = "N";
        	    logger.error("Unable to do export action for this event Id's.", eventIds);
        	}
        } else {
        	logger.error("Unknown event action: " + action);
        }
        
        logger.info("Terminated from the EventReportController action");
        return null;
    }
}
