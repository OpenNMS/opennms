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

import org.opennms.api.reporting.ReportService;

/**
 * This class provides a simple mechanism for returning the ReportService bean
 * for a given reportId
 *
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */
public interface ReportServiceLocator  {
    
    /**
     * This method is used to retrieve the ReportService bean associated with
     *
     * @param   reportServiceName the name of the report service as
     *          as defined in database-reports.xml
     * @return  the ReportService bean used to run this report
     * @throws  org.opennms.reporting.core.svclayer.ReportServiceLocatorException if any.
     */
    ReportService getReportService(String reportServiceName) throws ReportServiceLocatorException;
    
    /**
     * This method is used to retrieve the ReportService bean associated with a report Id
     *
     * @param   reportId the reportID defined in database-reports.xml
     * @return  the ReportService bean used to run this report
     * @throws  org.opennms.reporting.core.svclayer.ReportServiceLocatorException if any.
     */
    ReportService getReportServiceForId(String reportId) throws ReportServiceLocatorException;

}
