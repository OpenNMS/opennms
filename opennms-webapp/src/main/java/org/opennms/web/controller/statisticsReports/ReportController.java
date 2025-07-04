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

import org.opennms.web.svclayer.StatisticsReportService;
import org.opennms.web.svclayer.model.StatisticsReportCommand;
import org.opennms.web.svclayer.model.StatisticsReportModel;
import org.opennms.web.validator.StatisticsReportCommandValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Show a specific statistics report.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@Controller
@RequestMapping("/statisticsReports/report.htm")
public class ReportController {

    @Autowired
    private StatisticsReportService m_statisticsReportService;

    @Autowired
    private StatisticsReportCommandValidator m_validator;

    @RequestMapping(method={ RequestMethod.GET, RequestMethod.POST })
    public ModelAndView handle(@ModelAttribute("command") StatisticsReportCommand command, BindingResult errors) {
        m_validator.validate(command, errors);
        try {
            StatisticsReportModel report = m_statisticsReportService.getReport(command, errors);
            if (report == null) {
                throw new StatisticsReportIdNotFoundException("No such report ID", command.getId().toString(), null);
            } else {
                return new ModelAndView("statisticsReports/report", "model", report);
            }
        } catch (org.springframework.orm.hibernate5.HibernateObjectRetrievalFailureException horfe) {
            throw new StatisticsReportIdNotFoundException("No such report ID", command.getId().toString(), horfe);
        }
    }
}
