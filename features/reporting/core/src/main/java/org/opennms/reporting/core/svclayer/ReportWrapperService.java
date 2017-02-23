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

package org.opennms.reporting.core.svclayer;

import java.io.OutputStream;
import java.util.List;

import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.reporting.core.DeliveryOptions;

/**
 * Interface that finds and executes individual reportServices.
 * Always run a report service via this wrapper as the implementation will find
 * the correct service for the reportId and wrap it as necessary.
 *
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */
public interface ReportWrapperService {

    /**
     * This method validates that the map of report parameters matches the report
     * parameters accepted by the report. Used by the web interface.
     *
     * @param parameters runtime report parameters
     * @param reportId reportId as defined in database-reports.xml
     * @return true if the reportParms supplied match those in the report definition.
     */
    boolean validate(ReportParameters parameters, String reportId);
    
    /**
     * This method runs the report
     *
     * @param parameters runtime report parameters
     * @param deliveryOptions delivery options for the report
     * @param reportId reportId as defined in database-reports.xml
     * @param mode in which to run the report (ONLINE, BATCH or IMMEDIATE)
     */
    void run(ReportParameters parameters, ReportMode mode, DeliveryOptions deliveryOptions, String reportId);

    /**
     * This method returns the delivery options for the report. Providing a userID will
     * allow the report service to pre-populate the destination address
     *
     * @param userId a {@link java.lang.String} object.
     * @param reportId a {@link java.lang.String} object.
     * @return a delivery options object containing information that describes how the report might
     *         be delivered.
     */
    DeliveryOptions getDeliveryOptions(String userId, String reportId);

    /**
     * This method provides a list of formats supported by the report
     *
     * @param reportId reportId as defined in database-reports.xml
     * @return a list of supported formats
     */
    List<ReportFormat> getFormats(String reportId);
    
    /**
     * This method runs the report and renders in into the given output stream
     * with no intermediate steps
     *
     * @param parameters runtime report parameters
     * @param outputStream stream to render the resulting report
     * @param mode in which to run the report (ONLINE, BATCH or IMMEDIATE)
     */
    void runAndRender(ReportParameters parameters, ReportMode mode, OutputStream outputStream) throws ReportException;

    /**
     * This method renders the report into a given output stream.
     *
     * @param ReportId reportId as defined in database-reports.xml
     * @param location location of the report on disk
     * @param format format to render the report
     * @param outputStream stream to render the resulting report
     */
    void render(String ReportId, String location, ReportFormat format, OutputStream outputStream);


    /**
     * This method is used to determine whether the report takes any parameters
     *
     *  @return true if the report takes parameters, false if not.
     * @param ReportId a {@link java.lang.String} object.
     */
    Boolean hasParameters(String ReportId);

    /**
     * This method retrieves the runtime parameters taken by the report
     *
     * @return a ReportParameters object containing the parameters taken by the report
     * @param ReportId a {@link java.lang.String} object.
     */
    ReportParameters getParameters(String ReportId);
}
