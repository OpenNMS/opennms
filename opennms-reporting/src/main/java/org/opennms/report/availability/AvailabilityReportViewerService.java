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

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.opennms.netmgt.model.AvailabilityReportLocator;

/**
 * <p>AvailabilityReportViewerService class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AvailabilityReportViewerService {

    private AvailabilityReportLocatorService m_reportLocatorService;

    private int m_reportId;

    private String m_baseDir;

    /**
     * <p>createSource</p>
     *
     * @return a {@link javax.xml.transform.Source} object.
     * @throws org.opennms.report.availability.AvailabilityReportException if any.
     */
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

    /**
     * <p>getReportLocatorService</p>
     *
     * @return a {@link org.opennms.report.availability.AvailabilityReportLocatorService} object.
     */
    public AvailabilityReportLocatorService getReportLocatorService() {
        return m_reportLocatorService;
    }

    /**
     * <p>setReportLocatorService</p>
     *
     * @param locator a {@link org.opennms.report.availability.AvailabilityReportLocatorService} object.
     */
    public void setReportLocatorService(
            AvailabilityReportLocatorService locator) {
        m_reportLocatorService = locator;
    }

    /**
     * <p>getReportId</p>
     *
     * @return a int.
     */
    public int getReportId() {
        return m_reportId;
    }

    /**
     * <p>setReportId</p>
     *
     * @param id a int.
     */
    public void setReportId(int id) {
        m_reportId = id;
    }

    /**
     * <p>getBaseDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBaseDir() {
        return m_baseDir;
    }

    /**
     * <p>setBaseDir</p>
     *
     * @param dir a {@link java.lang.String} object.
     */
    public void setBaseDir(String dir) {
        m_baseDir = dir;
    }
}
