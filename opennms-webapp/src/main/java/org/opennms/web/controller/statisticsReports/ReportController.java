/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 10: Created this file. - dj@opennms.org
 * 
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.controller.statisticsReports;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.command.StatisticsReportCommand;
import org.opennms.web.svclayer.StatisticsReportService;
import org.opennms.web.svclayer.support.StatisticsReportModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

/**
 * Show a specific statistics report.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class ReportController extends AbstractCommandController implements InitializingBean {
    private StatisticsReportService m_statisticsReportService;
    private String m_successView;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object cmd, BindException errors) throws Exception {
        StatisticsReportCommand command = (StatisticsReportCommand) cmd;

        try {
        	StatisticsReportModel report = m_statisticsReportService.getReport(command, errors);
            return new ModelAndView(getSuccessView(), "model", report);
        } catch (org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException horfe) {
        	throw new StatisticsReportIdNotFoundException("No such report ID", command.getId().toString());
        }
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_statisticsReportService != null, "property statisticsReportService must be set to a non-null value");
        Assert.state(m_successView != null, "property successView must be set to a non-null value");
    }

    /**
     * <p>getStatisticsReportService</p>
     *
     * @return a {@link org.opennms.web.svclayer.StatisticsReportService} object.
     */
    public StatisticsReportService getStatisticsReportService() {
        return m_statisticsReportService;
    }

    /**
     * <p>setStatisticsReportService</p>
     *
     * @param statisticsReportService a {@link org.opennms.web.svclayer.StatisticsReportService} object.
     */
    public void setStatisticsReportService(StatisticsReportService statisticsReportService) {
        m_statisticsReportService = statisticsReportService;
    }

    /**
     * <p>getSuccessView</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSuccessView() {
        return m_successView;
    }

    /**
     * <p>setSuccessView</p>
     *
     * @param successView a {@link java.lang.String} object.
     */
    public void setSuccessView(String successView) {
        m_successView = successView;
    }

}
