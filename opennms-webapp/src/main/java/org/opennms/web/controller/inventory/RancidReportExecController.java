/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.controller.inventory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.report.configuration.svclayer.ConfigurationReportCriteria;
import org.opennms.report.configuration.svclayer.ConfigurationReportService;
import org.opennms.report.inventory.svclayer.InventoryReportCriteria;
import org.opennms.report.inventory.svclayer.InventoryReportService;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * <p>RancidReportExecController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class RancidReportExecController extends SimpleFormController {
    private static final Logger LOG = LoggerFactory.getLogger(RancidReportExecController.class);
    
//    InventoryService m_inventoryService;
    ConfigurationReportService m_configurationReportService;
    InventoryReportService m_inventoryReportService;
    
    
    
    /**
     * <p>getConfigurationReportService</p>
     *
     * @return a {@link org.opennms.report.configuration.svclayer.ConfigurationReportService} object.
     */
    public ConfigurationReportService getConfigurationReportService() {
        return m_configurationReportService;
    }
    /**
     * <p>setConfigurationReportService</p>
     *
     * @param configurationReportService a {@link org.opennms.report.configuration.svclayer.ConfigurationReportService} object.
     */
    public void setConfigurationReportService(
            ConfigurationReportService configurationReportService) {
        m_configurationReportService = configurationReportService;
    }
    /**
     * <p>getInventoryReportService</p>
     *
     * @return a {@link org.opennms.report.inventory.svclayer.InventoryReportService} object.
     */
    public InventoryReportService getInventoryReportService() {
        return m_inventoryReportService;
    }
    /**
     * <p>setInventoryReportService</p>
     *
     * @param inventoryReportService a {@link org.opennms.report.inventory.svclayer.InventoryReportService} object.
     */
    public void setInventoryReportService(
            InventoryReportService inventoryReportService) {
        m_inventoryReportService = inventoryReportService;
    }
    
    /** {@inheritDoc} */
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws ServletException, IOException, Exception {

        LOG.debug("RancidReportExecController ModelAndView onSubmit");
        
        RancidReportExecCommClass bean = (RancidReportExecCommClass) command;
        
        LOG.debug("RancidReportExecController ModelAndView type {}", bean.getReporttype());
        LOG.debug("RancidReportExecController ModelAndView type {}", bean.getFieldhas());
        
        String user = request.getRemoteUser();
        Date reportRequestDate = new Date();

        
        ModelAndView mav = new ModelAndView(getSuccessView());

        if (bean.getReporttype().compareTo("rancidlist") == 0){
            LOG.debug("RancidReportExecController rancidlist report");
            ConfigurationReportCriteria criteria = new ConfigurationReportCriteria(bean.getDate(), bean.getReportfiletype(), bean.getReportemail(), user, reportRequestDate);
//            boolean done = m_inventoryService.runRancidListReport(bean.getDate(), bean.getReportfiletype(), bean.getReportemail(), user, reportRequestDate);
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

        
//        String redirectURL = request.getHeader("Referer");
//        response.sendRedirect(redirectURL);
//        return super.onSubmit(request, response, command, errors);
    }
    /** {@inheritDoc} */
    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
        LOG.debug("RancidReportExecController initBinder");
    }
    
}
