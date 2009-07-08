/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.web.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.DistributedStatusService;
import org.opennms.web.svclayer.SimpleWebTable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class DistributedStatusSummaryController extends AbstractController {
    
    private DistributedStatusService m_distributedStatusService;

    public DistributedStatusService getDistributedStatusService() {
        return m_distributedStatusService;
    }

    public void setDistributedStatusService(DistributedStatusService statusService) {
        m_distributedStatusService = statusService;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        GregorianCalendar calendar = new GregorianCalendar();
        Date endDate = new Date(calendar.getTimeInMillis());
        
        calendar.set(Calendar.HOUR_OF_DAY, 0); 
        calendar.set(Calendar.MINUTE, 0); 
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0); 
        Date startDate = new Date(calendar.getTimeInMillis());
        
        if (m_distributedStatusService.getApplicationCount() <= 0) {
            return new ModelAndView("distributedStatusSummaryError", "error", createError("No Applications Defined", "No have applications have been defined for this system so a summary of application status is impossbile to display."));
        }
        
        SimpleWebTable table = m_distributedStatusService.createFacilityStatusTable(startDate, endDate);
        return new ModelAndView("distributedStatusSummary", "webTable", table);
    }

    private Object createError(String shortDescr, String longDescr) {
        Map<String, String> error = new HashMap<String, String>();
        error.put("shortDescr", shortDescr);
        error.put("longDescr", longDescr);
        return error;
    }

}
