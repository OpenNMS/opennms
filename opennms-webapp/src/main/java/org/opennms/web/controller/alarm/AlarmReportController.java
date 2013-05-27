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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.api.reporting.ReportFormat;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.dao.AlarmRepository;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.event.Event;
import org.opennms.web.event.EventQueryParms;
import org.opennms.web.event.EventUtil;
import org.opennms.web.event.SortStyle;
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
 * This servlet receives an HTTP POST with a list of alarms and export or export all action
 * for the selected alarms. and then it displays the alarm reports on current URL page
 *
 */

public class AlarmReportController extends AbstractController implements InitializingBean {
    
    /** Constant <code>EXPORT_ACTION="1"</code> */
    public final static String EXPORT_ACTION = "1";
    
    /** Constant <code>EXPORTALL_ACTION="2"</code> */
    public final static String EXPORTALL_ACTION = "2";
    
    /** To hold report file name <code>FILE_NAME="EMPTY"</code> */
    public static String FILE_NAME = "EMPTY";
    
	/**
	 * OpenNMS alarm default acknowledge type
	 */
	private org.opennms.web.alarm.AcknowledgeType m_defaultAlarmAcknowledgeType = org.opennms.web.alarm.AcknowledgeType.UNACKNOWLEDGED;
	
	/**
	 * OpenNMS event default acknowledge type
	 */
	private org.opennms.web.event.AcknowledgeType m_defaultEventAcknowledgeType = org.opennms.web.event.AcknowledgeType.UNACKNOWLEDGED;
	
	/**
	 * OpenNMS default sort style
	 */
	private SortStyle m_defaultSortStyle = SortStyle.NODE;

    /**
	 * OpenNMS alarm repository
	 */
    private AlarmRepository m_webAlarmRepository;
    
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
    private Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + AlarmReportController.class.getName());

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
        Assert.notNull(m_webAlarmRepository, "webAlarmRepository must be set");
        Assert.notNull(m_webEventRepository, "webEventRepository must be set");
        Assert.notNull(m_reportWrapperService, "webAlarmRepository must be set");
    }

    /**
     * {@inheritDoc}
     *
     * Export or export all action of the selected alarms specified in the POST and 
     * then display the client to an appropriate URL.
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
    	logger.info("Enter into the AlarmReportController action");
    	
    	// Handle the alarm and actionCode parameter
    	String[] alarmIdStrings = request.getParameterValues("alarm");
        String action = request.getParameter("actionCode");
        
        List<OnmsAlarm> alarmList = new ArrayList<OnmsAlarm>();
        if (alarmIdStrings != null && action.equals(EXPORT_ACTION)) {
        	
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
        
        // Handle the sort style parameter
 		String sortStyleString = request.getParameter("sortby");
 		SortStyle sortStyle = m_defaultSortStyle;
 		if (sortStyleString != null) {
 			try {
 				sortStyle = SortStyle.getSortStyle(sortStyleString);
 			} catch (Exception e) {
 				logger.error("Could not retrieve sort id for this '{}'.",sortStyleString);
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
        if (action.equals(EXPORTALL_ACTION)) {
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
        if(action.equals(EXPORTALL_ACTION)){
        	alarmList.clear();
        	AlarmCriteria alarmQueryCriteria = new AlarmCriteria(alarmAckType,alarmFilters);
        	OnmsAlarm[] alarms = m_webAlarmRepository.getMatchingAlarms(AlarmUtil.getOnmsCriteria(alarmQueryCriteria));
	        
	        for(OnmsAlarm alarm : alarms){
	        	alarmList.add(alarm);
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
    	
    	FILE_NAME =  "alarm_report"+new SimpleDateFormat("_MMddyyyy_HHmmss").format(new Date())+"."+requestFormat.toLowerCase();
    			
    	response.setHeader("Content-disposition", "inline; filename="+FILE_NAME);
    	response.setHeader("Pragma", "public");
    	response.setHeader("Cache-Control", "cache");
    	response.setHeader("Cache-Control", "must-revalidate");
        
        HashMap<Integer, List<Integer>> eventIdsForAlarms = new HashMap<Integer, List<Integer>>();
        List<Integer> alarmIds = new ArrayList<Integer>();
        
        for(OnmsAlarm alarm : alarmList){
        	
	        //Get the default event filters
        	filterList.clear();
        	for (String filterString : m_webAlarmRepository.getFilterStringsForEvent(alarm)) {
        		try{
        			Filter filter= EventUtil.getFilter(filterString, getServletContext());
        			if(filter != null){
        				filterList.add(filter);
        			}
        		} catch(Exception e){
        			logger.error("Could not retrieve filter name for filterString='{}'", filterString);
        		}
            }
        	
	    	Filter[] filters = filterList.toArray(new Filter[0]);
	        List<Integer> eventIdsList = new ArrayList<Integer>();
	        
	        if(alarm != null){
	        	
	        	EventQueryParms parms = new EventQueryParms();
	    		parms.ackType = eventAckType;
	    		parms.filters = filterList;
	    		parms.sortStyle = m_defaultSortStyle;
	    		
	        	//Get the events by event criteria
	        	Event[] events = null;
	        	EventCriteria eventCriteria = new EventCriteria(filters, sortStyle, eventAckType, 0, 0);
	        	try{
	        		events = m_webEventRepository.getMatchingEvents(eventCriteria);
	        	} catch(Exception e){
	        		logger.error("Could not retrieve events for this EventCriteria ='{}'", eventCriteria);
	        	}
		        
		        //Get the event Id's
	        	for(Event event : events){
	        		eventIdsList.add(event.getId());
	    		}
	        }
	        alarmIds.add(alarm.getId());
	        eventIdsForAlarms.put(alarm.getId(), eventIdsList);
		}
        
        // Handle the export action
        if (action.equals(EXPORT_ACTION)|| action.equals(EXPORTALL_ACTION)) {
        	try{
        		m_reportWrapperService.getAlarmReport(alarmIds, eventIdsForAlarms, reportId,
        				ReportFormat.valueOf(requestFormat), response.getOutputStream(), FILE_NAME);
        	} catch(final Exception e){
        	    logger.error("Unable to do export action for this alarm Id's.", alarmIds);
        	}
        } else {
        	logger.error("Unknown alarm action: " + action);
        }
        
        logger.info("Terminated from the AlarmReportController action");
        return null;
    }
}
