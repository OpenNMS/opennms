/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.opennms.netmgt.model.AvailabilityReportLocator;

public class AvailabilityReportViewerService {

    private AvailabilityReportLocatorService m_reportLocatorService;

    private int m_reportId;

    private String m_baseDir;

    public Source createSource() throws AvailabilityReportException {

        AvailabilityReportLocator locator = m_reportLocatorService.locateReport(m_reportId);

        if (locator == null) {
            throw new AvailabilityReportException(
                                                  "Unable to find a report with that Id");
        } else {
            File xmlFile = new File(locator.getLocation());
            if (xmlFile.canRead() == true) {
                return new StreamSource(xmlFile);
            } else {
                throw new AvailabilityReportException(
                                                      "Found a report with id:  "
                                                              + m_reportId
                                                              + " but xml (plus basedir): "
                                                              + m_baseDir
                                                              + locator.getLocation()
                                                              + " was not readable");
            }
        }

    }

    public AvailabilityReportLocatorService getReportLocatorService() {
        return m_reportLocatorService;
    }

    public void setReportLocatorService(
            AvailabilityReportLocatorService locator) {
        m_reportLocatorService = locator;
    }

    public int getReportId() {
        return m_reportId;
    }

    public void setReportId(int id) {
        m_reportId = id;
    }

    public String getBaseDir() {
        return m_baseDir;
    }

    public void setBaseDir(String dir) {
        m_baseDir = dir;
    }
}
