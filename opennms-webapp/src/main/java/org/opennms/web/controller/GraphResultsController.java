/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.controller;

import org.apache.commons.jexl2.ExpressionImpl;
import org.apache.commons.jexl2.JexlEngine;
import org.jrobin.core.RrdException;
import org.jrobin.core.timespec.TimeParser;
import org.jrobin.core.timespec.TimeSpec;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.web.servlet.MissingParameterException;
import org.opennms.web.svclayer.api.GraphResultsService;
import org.opennms.web.svclayer.model.GraphResults;
import org.opennms.web.svclayer.model.RelativeTimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * <p>GraphResultsController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GraphResultsController extends AbstractController implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + GraphResultsController.class);
    
    private GraphResultsService m_graphResultsService;
    
    private static RelativeTimePeriod[] s_periods = RelativeTimePeriod.getDefaultPeriods();

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String[] requiredParameters = new String[] {
                "resourceId",
                "reports"
        };
        
        for (String requiredParameter : requiredParameters) {
            if (request.getParameter(requiredParameter) == null) {
                throw new MissingParameterException(requiredParameter,
                                                    requiredParameters);
            }
        }

        String[] resourceIds = request.getParameterValues("resourceId");
        String[] reports = request.getParameterValues("reports");
        
        // see if the start and end time were explicitly set as params
        String start = request.getParameter("start");
        String end = request.getParameter("end");

        String relativeTime = request.getParameter("relativetime");
        
        final String startMonth = request.getParameter("startMonth");
        final String startDate = request.getParameter("startDate");
        final String startYear = request.getParameter("startYear");
        final String startHour = request.getParameter("startHour");

        final String endMonth = request.getParameter("endMonth");
        final String endDate = request.getParameter("endDate");
        final String endYear = request.getParameter("endYear");
        final String endHour = request.getParameter("endHour");
        
        long startLong = 0;
        long endLong = 0;

        if (start != null || end != null) {
            String[] ourRequiredParameters = new String[] {
                    "start",
                    "end"
            };
        
            if (start == null) {
                throw new MissingParameterException("start",
                                                    ourRequiredParameters);
            }
            
            if (end == null) {
                throw new MissingParameterException("end",
                                                    ourRequiredParameters);
            }
            //The following is very similar to RrdGraphController.parseTimes, but modified for the local context a bit
            // There's merging possibilities, but I don't know how (common parent class seems wrong; service bean for a single
            // method isn't much better.  Ideas?
            
    		//Try a simple 'long' parsing.  If either fails, do a full parse.  If one is a straight 'long' but the other isn't
    		// that's fine, the TimeParser code will handle it fine (as long as we convert milliseconds to seconds)
            // Indeed, we *have* to use TimeParse for both to ensure any relative references (using "start" or "end") work correctly. 
    		// NB: can't do a "safe" parsing using the WebSecurityUtils; if we did, it would filter out all the possible rrdfetch 
    		// format text and always work :)
    		
        	boolean startIsInteger = false;
        	boolean endIsInteger = false;
        	
        	//If either of start/end *is* a long, convert from the incoming milliseconds to seconds that
        	// is expected for epoch times by TimeParser
        	try {
        		startLong = Long.valueOf(start);
        		startIsInteger = true;
        		start = ""+(startLong/1000);
        	} catch (NumberFormatException e) {
        	}
        	
        	try {
        		endLong = Long.valueOf(end);
        		endIsInteger = true;
        		end = "" +(endLong/1000);
        	} catch (NumberFormatException e) {
        	}
        	
        	if(!endIsInteger || !startIsInteger) {        	
        		//One or both of start/end aren't integers, so we need to do full parsing using TimeParser
        		TimeParser startParser = new TimeParser(start);
        		TimeParser endParser = new TimeParser(end);
	            try {
	
	            	TimeSpec specStart = startParser.parse();
	            	TimeSpec specEnd = endParser.parse();
	            	long[] results = TimeSpec.getTimestamps(specStart, specEnd);
	            	//Multiply by 1000.  TimeSpec returns timestamps in Seconds, not Milliseconds.  
	            	startLong = results[0]*1000;
	            	endLong = results[1]*1000;
	            } catch (RrdException e1) {
	    			throw new IllegalArgumentException("Could not parse start '"+ start+"' and end '"+end+"' as valid time specifications", e1);
	    		}
        	}

        } else if (startMonth != null || startDate != null 
                   || startYear != null || startHour != null
                   || endMonth != null || endDate != null || endYear != null
                   || endHour != null) {
            
            String[] ourRequiredParameters = new String[] {
                    "startMonth",
                    "startDate",
                    "startYear",
                    "startHour",
                    "endMonth",
                    "endDate",
                    "endYear",
                    "endHour"
            };
            
            for (String requiredParameter : ourRequiredParameters) {
                if (request.getParameter(requiredParameter) == null) {
                    throw new MissingParameterException(requiredParameter,
                                                        ourRequiredParameters);
                }
            }

            Calendar startCal = Calendar.getInstance();
            startCal.set(Calendar.MONTH, WebSecurityUtils.safeParseInt(startMonth));
            startCal.set(Calendar.DATE, WebSecurityUtils.safeParseInt(startDate));
            startCal.set(Calendar.YEAR, WebSecurityUtils.safeParseInt(startYear));
            startCal.set(Calendar.HOUR_OF_DAY, WebSecurityUtils.safeParseInt(startHour));
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);

            Calendar endCal = Calendar.getInstance();
            endCal.set(Calendar.MONTH, WebSecurityUtils.safeParseInt(endMonth));
            endCal.set(Calendar.DATE, WebSecurityUtils.safeParseInt(endDate));
            endCal.set(Calendar.YEAR, WebSecurityUtils.safeParseInt(endYear));
            endCal.set(Calendar.HOUR_OF_DAY, WebSecurityUtils.safeParseInt(endHour));
            endCal.set(Calendar.MINUTE, 0);
            endCal.set(Calendar.SECOND, 0);
            endCal.set(Calendar.MILLISECOND, 0);

            startLong = startCal.getTime().getTime();
            endLong = endCal.getTime().getTime();
        } else {
            if (relativeTime == null) {
                relativeTime = s_periods[0].getId();
            }

            RelativeTimePeriod period = RelativeTimePeriod.getPeriodByIdOrDefault(s_periods, relativeTime, s_periods[0]);

            long[] times = period.getStartAndEndTimes();
            startLong = times[0];
            endLong = times[1];
        }

        // The 'matching' parameter is going to work only for one resource.
        String matching = request.getParameter("matching");
        if (matching != null) {
            reports = getSuggestedReports(resourceIds[0], matching);
        }

        GraphResults model = m_graphResultsService.findResults(resourceIds, reports, startLong, endLong, relativeTime);
        
        ModelAndView modelAndView = new ModelAndView("/graph/results", "results", model);

        modelAndView.addObject("loggedIn", request.getRemoteUser() != null);

        return modelAndView;
    }

    /**
     * <p>getSuggestedReports</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
	public String[] getSuggestedReports(String resourceId, String matching) {
		List<String> metricList = new ArrayList<String>();
		JexlEngine expressionParser = new JexlEngine();
		try {
		    ExpressionImpl e = (ExpressionImpl) expressionParser.createExpression(matching);
		    for (List<String> list : e.getVariables()) {
		        if (list.get(0).equalsIgnoreCase("math")) {
		            continue;
		        }
		        if (list.get(0).equalsIgnoreCase("datasources")) {
		            metricList.add(list.get(1).intern());
		        } else {
		            metricList.add(list.get(0).intern());
		        }
		    }
		} catch (Exception e) {
		}
		if (!metricList.isEmpty()) {
		    List<String> templates = new ArrayList<String>();
		    for (PrefabGraph graph : m_graphResultsService.getAllPrefabGraphs(resourceId)) {
		        boolean found = false;
		        for (String c : graph.getColumns()) {
		            if (metricList.contains(c)) {
		                found = true;
		                continue;
		            }
		        }
		        if (found) {
		            templates.add(graph.getName());
		        }
		    }
		    if (!templates.isEmpty()) {
		        return templates.toArray(new String[templates.size()]);
		    }
		}
		return new String[] { "all" };
	}

    /**
     * <p>getGraphResultsService</p>
     *
     * @return a {@link org.opennms.web.svclayer.api.GraphResultsService} object.
     */
    public GraphResultsService getGraphResultsService() {
        return m_graphResultsService;
    }

    /**
     * <p>setGraphResultsService</p>
     *
     * @param graphResultsService a {@link org.opennms.web.svclayer.api.GraphResultsService} object.
     */
    public void setGraphResultsService(GraphResultsService graphResultsService) {
        m_graphResultsService = graphResultsService;
    }

    /**
     * Ensures that required properties are set to valid values.
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_graphResultsService != null, "graphResultsService property must be set to a non-null value");
    }
}
