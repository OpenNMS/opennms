/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
