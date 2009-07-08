/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.web.controller.inventory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.web.svclayer.inventory.InventoryService;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class RancidReportExecController extends SimpleFormController {
    
    InventoryService m_inventoryService;
    
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws ServletException, IOException, Exception {

        log().debug("RancidReportExecController ModelAndView onSubmit");
        
        RancidReportExecCommClass bean = (RancidReportExecCommClass) command;
        
        log().debug("RancidReportExecController ModelAndView type" + bean.getReporttype());
        log().debug("RancidReportExecController ModelAndView type" + bean.getFieldhas());

        ModelAndView mav = new ModelAndView(getSuccessView());

        if (bean.getReporttype().compareTo("rancidlist") == 0){
            log().debug("RancidReportExecController rancidlist report ");
            boolean done = m_inventoryService.runRancidListReport(bean.getDate(), bean.getReportfiletype(), bean.getReportemail());
            mav.addObject("type", "Rancid List");
            if (!done){
                log().debug("RancidReportExecController error ");
            }
        } else if (bean.getReporttype().compareTo("inventory") == 0){
            log().debug("RancidReportExecController inventory report ");
            boolean done = m_inventoryService.runNodeBaseInventoryReport(bean.getDate(), bean.getFieldhas(), bean.getReportfiletype(),bean.getReportemail());
            mav.addObject("type", "Inventory Report");
            if (!done){
                log().debug("RancidReportExecController error ");
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
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
        log().debug("RancidReportExecController initBinder");
    }
    
    public InventoryService getInventoryService() {
        return m_inventoryService;
    }

    public void setInventoryService(InventoryService inventoryService) {
        m_inventoryService = inventoryService;
    }

    
    private static Category log() {
        return Logger.getLogger("Rancid");
    }
}
