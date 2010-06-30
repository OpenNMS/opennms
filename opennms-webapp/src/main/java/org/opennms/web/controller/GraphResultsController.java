//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Sep 28: Handle XSS security issues - ranger@opennms.org
// 2007 Apr 05: Implement InitializingBean, make m_periods static, and eliminate
//              RrdStrategy (the needed data is in GraphResults). - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.controller;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.XssRequestWrapper;
import org.opennms.web.graph.GraphResults;
import org.opennms.web.graph.RelativeTimePeriod;
import org.opennms.web.svclayer.GraphResultsService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


/**
 * <p>GraphResultsController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class GraphResultsController extends AbstractController implements InitializingBean {
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

        String[] resourceIds = WebSecurityUtils.sanitizeString(request.getParameterValues("resourceId"));
        String[] reports = WebSecurityUtils.sanitizeString(request.getParameterValues("reports"));
        
        // see if the start and end time were explicitly set as params
        String start = WebSecurityUtils.sanitizeString(request.getParameter("start"));
        String end = WebSecurityUtils.sanitizeString(request.getParameter("end"));

        String relativeTime = WebSecurityUtils.sanitizeString(request.getParameter("relativetime"));
        
        String startMonth = WebSecurityUtils.sanitizeString(request.getParameter("startMonth"));
        String startDate = WebSecurityUtils.sanitizeString(request.getParameter("startDate"));
        String startYear = WebSecurityUtils.sanitizeString(request.getParameter("startYear"));
        String startHour = WebSecurityUtils.sanitizeString(request.getParameter("startHour"));

        String endMonth = WebSecurityUtils.sanitizeString(request.getParameter("endMonth"));
        String endDate = WebSecurityUtils.sanitizeString(request.getParameter("endDate"));
        String endYear = WebSecurityUtils.sanitizeString(request.getParameter("endYear"));
        String endHour = WebSecurityUtils.sanitizeString(request.getParameter("endHour"));
        
        long startLong;
        long endLong;

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
            
            // XXX could use some error checking
            startLong = WebSecurityUtils.safeParseLong(start);
            endLong = WebSecurityUtils.safeParseLong(end);
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

            RelativeTimePeriod period = RelativeTimePeriod.getPeriodByIdOrDefault(
                                                                                  s_periods,
                                                                                  relativeTime,
                                                                                  s_periods[0]);

            long[] times = period.getStartAndEndTimes();
            startLong = times[0];
            endLong = times[1];
        }
        
        GraphResults model =
            m_graphResultsService.findResults(resourceIds,
                                              reports, startLong,
                                              endLong, relativeTime);

        return new ModelAndView("/graph/results", "results", model);
    }

    /**
     * <p>getGraphResultsService</p>
     *
     * @return a {@link org.opennms.web.svclayer.GraphResultsService} object.
     */
    public GraphResultsService getGraphResultsService() {
        return m_graphResultsService;
    }

    /**
     * <p>setGraphResultsService</p>
     *
     * @param graphResultsService a {@link org.opennms.web.svclayer.GraphResultsService} object.
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
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_graphResultsService != null, "graphResultsService property must be set to a non-null value");
    }
}
