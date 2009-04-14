//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
package org.opennms.report.availability;

import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.dao.AvailabilityReportLocatorDao;
import org.opennms.netmgt.model.AvailabilityReportLocator;

/**
 * AvailibilityReportLocatorService is used to store, retrieve and delete
 * report locator entries from the report locator table
 * 
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */

public class AvailabilityReportLocatorService implements ReportLocatorService {

    private AvailabilityReportLocatorDao availabilityReportLocatorDao;

    public void setAvailabilityReportLocatorDao(
            AvailabilityReportLocatorDao availabilityReportLocatorDao) {
        this.availabilityReportLocatorDao = availabilityReportLocatorDao;
    }

    /**
     * Returns a collection of ReportLocator objects that represent all the
     * ready run reports available
     * 
     * @return Collection of AvailabilityReportLocator
     */

    public List<AvailabilityReportLocator> locateReports() {
        return availabilityReportLocatorDao.findAll();
    }

    /**
     * Returns a collection of ReportLocator objects that represent all the
     * ready run reports available for a given category
     * 
     * @param catgegoryName
     *            A valid category name (see views.xml)
     * @return Collection of AvailabilityReportLocator applicable to
     *         categoryName
     */

    public List<AvailabilityReportLocator> locateReports(
            String categoryName) {
        return availabilityReportLocatorDao.findByCategory(categoryName);
    }

    /**
     * Returns a single AvailabilityReportLocator
     * 
     * @param id
     * @return AvailabilityReportLocator
     */

    public AvailabilityReportLocator locateReport(int id) {
        return availabilityReportLocatorDao.get(id);
    }

    /**
     * Delete an availability report. Currently only deletes the locator, not
     * the report on disk.
     * 
     * @param id
     */

    public void deleteReport(int id) {
        // TODO Need to add the capability to remove reports from the
        // filsystem, as well as remove them from the locator
        availabilityReportLocatorDao.deleteById(id);
    }
    
    public void deleteReports(Integer[] ids) {
        // TODO Need to add the capability to remove reports from the
        // filsystem, as well as remove them from the locator
        for (Integer id : ids) {
            availabilityReportLocatorDao.deleteById(id); 
        }
    }

    /**
     * Add an availability Report Locator record to the database
     * 
     * @param AvailabilityReportLocator
     */
    
    public void addReport(AvailabilityReportLocator locator) {
        availabilityReportLocatorDao.save(locator);
    }

}
