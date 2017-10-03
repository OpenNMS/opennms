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
        } catch (org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException horfe) {
            throw new StatisticsReportIdNotFoundException("No such report ID", command.getId().toString(), horfe);
        }
    }
}
