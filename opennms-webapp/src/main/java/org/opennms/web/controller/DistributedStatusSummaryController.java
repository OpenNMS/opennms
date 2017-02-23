/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.DistributedStatusService;
import org.opennms.web.svclayer.model.SimpleWebTable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>DistributedStatusSummaryController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DistributedStatusSummaryController extends AbstractController {
    
    private DistributedStatusService m_distributedStatusService;

    /**
     * <p>getDistributedStatusService</p>
     *
     * @return a {@link org.opennms.web.svclayer.DistributedStatusService} object.
     */
    public DistributedStatusService getDistributedStatusService() {
        return m_distributedStatusService;
    }

    /**
     * <p>setDistributedStatusService</p>
     *
     * @param statusService a {@link org.opennms.web.svclayer.DistributedStatusService} object.
     */
    public void setDistributedStatusService(DistributedStatusService statusService) {
        m_distributedStatusService = statusService;
    }

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        // Calculate a date that equals midnight of the current day (00:00:00 AM)
        GregorianCalendar calendar = new GregorianCalendar();
        Date endDate = new Date(calendar.getTimeInMillis());
        
        calendar.set(Calendar.HOUR_OF_DAY, 0); 
        calendar.set(Calendar.MINUTE, 0); 
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0); 
        Date startDate = new Date(calendar.getTimeInMillis());
        
        if (m_distributedStatusService.getApplicationCount() <= 0) {
            return new ModelAndView("distributedStatusSummaryError", "error", createError("No Applications Defined", "No applications have been defined for this system so a summary of application status is impossible to display."));
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
