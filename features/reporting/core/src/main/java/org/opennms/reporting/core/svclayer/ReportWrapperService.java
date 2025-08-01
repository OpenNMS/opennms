/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
