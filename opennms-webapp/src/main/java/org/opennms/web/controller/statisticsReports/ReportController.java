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
 */
public class ReportController extends AbstractCommandController implements InitializingBean {
    private StatisticsReportService m_statisticsReportService;
    private String m_successView;

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

    public void afterPropertiesSet() throws Exception {
        Assert.state(m_statisticsReportService != null, "property statisticsReportService must be set to a non-null value");
        Assert.state(m_successView != null, "property successView must be set to a non-null value");
    }

    public StatisticsReportService getStatisticsReportService() {
        return m_statisticsReportService;
    }

    public void setStatisticsReportService(StatisticsReportService statisticsReportService) {
        m_statisticsReportService = statisticsReportService;
    }

    public String getSuccessView() {
        return m_successView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }

}
