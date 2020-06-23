/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.statisticsReports;

import java.util.Collections;
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
 * @since 1.8.1
 */
public class ListController extends AbstractController implements InitializingBean {
    private StatisticsReportService m_statisticsReportService;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<StatisticsReport> reports = m_statisticsReportService.getStatisticsReports();
        // Reverse the list for intuitive presentation -- it should come out of the DAO
        // in ascending order of ID, but users will expect newer reports to be at the top
        Collections.reverse(reports);
        
        return new ModelAndView("statisticsReports/index", "model", reports);
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
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
