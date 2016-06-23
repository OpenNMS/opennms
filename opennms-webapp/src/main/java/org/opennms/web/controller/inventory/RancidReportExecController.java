/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.inventory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.opennms.report.configuration.svclayer.ConfigurationReportCriteria;
import org.opennms.report.configuration.svclayer.ConfigurationReportService;
import org.opennms.report.inventory.svclayer.InventoryReportCriteria;
import org.opennms.report.inventory.svclayer.InventoryReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>RancidReportExecController class.</p>
 */
@Controller
@RequestMapping("/inventory/rancidReportExec.htm")
public class RancidReportExecController {

    private static final Logger LOG = LoggerFactory.getLogger(RancidReportExecController.class);

    @Autowired
    private ConfigurationReportService m_configurationReportService;

    @Autowired
    private InventoryReportService m_inventoryReportService;

    @RequestMapping(method=RequestMethod.POST)
    public ModelAndView onSubmit(HttpServletRequest request, RancidReportExecCommClass bean) {

        LOG.debug("RancidReportExecController ModelAndView onSubmit");
        LOG.debug("RancidReportExecController ModelAndView type {}", bean.getReporttype());
        LOG.debug("RancidReportExecController ModelAndView type {}", bean.getFieldhas());

        String user = request.getRemoteUser();
        Date reportRequestDate = new Date();

        ModelAndView mav = new ModelAndView("inventory/rancidReportExec");

        if (bean.getReporttype().compareTo("rancidlist") == 0){
            LOG.debug("RancidReportExecController rancidlist report");
            ConfigurationReportCriteria criteria = new ConfigurationReportCriteria(bean.getDate(), bean.getReportfiletype(), bean.getReportemail(), user, reportRequestDate);
            // boolean done = m_inventoryService.runRancidListReport(bean.getDate(), bean.getReportfiletype(), bean.getReportemail(), user, reportRequestDate);
            boolean done = m_configurationReportService.runReport(criteria);
            mav.addObject("type", "Rancid List");
            if (!done){
                LOG.debug("RancidReportExecController error");
            }
        } else if (bean.getReporttype().compareTo("inventory") == 0){
            LOG.debug("RancidReportExecController inventory report");
            InventoryReportCriteria criteria = new InventoryReportCriteria(bean.getDate(), bean.getFieldhas(), bean.getReportfiletype(),bean.getReportemail(), user, reportRequestDate);
            boolean done = m_inventoryReportService.runReport(criteria);
            mav.addObject("type", "Inventory Report");
            if (!done){
                LOG.debug("RancidReportExecController error");
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d");
        try {
            mav.addObject("date", format.parse(bean.getDate()));
        }
        catch (ParseException pe){
            mav.addObject("date", format.format(Calendar.getInstance().getTime()));
        }
        mav.addObject("searchfield", bean.getFieldhas());
        if( bean.getReportfiletype().compareTo("pdftype") == 0){
            mav.addObject("reportformat", "PDF");
        } else {
            mav.addObject("reportformat", "HTML");
        }

        return mav;
    }
}
