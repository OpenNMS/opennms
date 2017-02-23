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

package org.opennms.api.reporting;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.opennms.api.reporting.parameter.ReportParameters;

/**
 * This interface provides an API for implementing additional database reports
 * inside the opennms webapp
 *
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @version $Id: $
 */
public interface ReportService {

    /**
     * This method validates that the map of report parameters matches the
     * report parameters accepted by the report. Used by the web interface.
     *
     * @param reportParms
     *            hashmap of parameters to be provided at runtime
     * @param reportId
     *            reportId as defined in database-reports.xml
     * @return true if the reportParms supplied match those in the report
     *         definition.
     */
    boolean validate(Map<String, Object> reportParms,
            String reportId);

    /**
     * This method runs the report
     *
     * @param reportParms
     *            hashmap of parameters to be provided at runtime
     * @param reportId
     *            reportId as defined in database-reports.xml
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.api.reporting.ReportException if any.
     */
    String run(Map<String, Object> reportParms,
            String reportId) throws ReportException;

    /**
     * This method provides a list of formats supported by the report
     *
     * @param reportId
     *            reportId as defined in database-reports.xml
     * @return a list of supported formats
     */
    List<ReportFormat> getFormats(String reportId);

    /**
     * This method renders the report into a given output stream.
     *
     * @param ReportId
     *            reportId as defined in database-reports.xml
     * @param location
     *            location of the report on disk
     * @param format
     *            format to render the report
     * @param outputStream
     *            stream to render the resulting report
     * @throws org.opennms.api.reporting.ReportException if any.
     */
    void render(String ReportId, String location,
            ReportFormat format, OutputStream outputStream)
            throws ReportException;

    /**
     * This method runs the report and renders in into the given output stream
     * with no intermediate steps
     *
     * @param ReportId
     *            reportId as defined in database-reports.xml
     * @param format
     *            format to render the report
     * @param outputStream
     *            stream to render the resulting report
     * @param reportParms a {@link java.util.HashMap} object.
     * @throws org.opennms.api.reporting.ReportException if any.
     */
    void runAndRender(Map<String, Object> reportParms,
            String ReportId, ReportFormat format, OutputStream outputStream)
            throws ReportException;

    /**
     * This method retrieves the runtime parameters taken by the report
     *
     * @return a ReportParameters object containing the parameters taken by
     *         the report
     * @param ReportId a {@link java.lang.String} object.
     */
    ReportParameters getParameters(String ReportId)
            throws ReportException;
    
}
