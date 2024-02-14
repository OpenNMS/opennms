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
