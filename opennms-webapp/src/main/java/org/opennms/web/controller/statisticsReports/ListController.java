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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.StatisticsReport;
import org.opennms.web.svclayer.StatisticsReportService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Produce a list of available statistics reports.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class ListController extends AbstractController implements InitializingBean {
    private StatisticsReportService m_statisticsReportService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<StatisticsReport> reports = m_statisticsReportService.getStatisticsReports();
        ArrayList<StatisticsReport> reportsSorted = new ArrayList<StatisticsReport>(reports.size());
        // Reverse the list for intuitive presentation -- it should come out of the DAO
        // in ascending order of ID, but users will expect newer reports to be at the top
        for (int i = reports.size() - 1; i >= 0; i--) {
        	reportsSorted.add(reports.get(i));
        }
        
        return new ModelAndView("statisticsReports/index", "model", reportsSorted);
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_statisticsReportService != null, "property statisticsReportService must be set to a non-null value");
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

}
