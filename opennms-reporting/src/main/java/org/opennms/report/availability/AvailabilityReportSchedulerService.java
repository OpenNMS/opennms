/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
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

package org.opennms.report.availability;

import java.util.Date;

import org.opennms.netmgt.dao.AvailabilityReportLocatorDao;
import org.opennms.netmgt.model.AvailabilityReportLocator;

public class AvailabilityReportSchedulerService implements
        ReportSchedulerService {

    private AvailabilityReportLocatorService m_locatorService;

    private AvailabilityReportLocatorDao m_availabilityReportLocatorDao;

    public void setAvailabilityReportLocatorDao(
            AvailabilityReportLocatorDao dao) {
        m_availabilityReportLocatorDao = dao;
    }

    public void Schedule(String category, String type, String format,
            Date date) {

        // TODO: All this does right now is add a locator entry, it does
        // not actually shedule anything.

        AvailabilityReportLocator locator = new AvailabilityReportLocator();
        locator.setCategory(category);
        locator.setFormat(format);
        locator.setType(type);
        locator.setDate(date);
        locator.setLocation("not yet available");
        locator.setAvailable(false);
        m_locatorService = new AvailabilityReportLocatorService();
        m_locatorService.setAvailabilityReportLocatorDao(m_availabilityReportLocatorDao);
        m_locatorService.addReport(locator);
    }

}
