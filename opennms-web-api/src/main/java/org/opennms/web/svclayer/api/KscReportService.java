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
package org.opennms.web.svclayer.api;

import java.util.Map;

import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.model.OnmsResource;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>KscReportService interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Transactional(readOnly = true)
public interface KscReportService {
    /**
     * <p>buildNodeReport</p>
     *
     * @param nodeId a int.
     * @return a {@link org.opennms.netmgt.config.kscReports.Report} object.
     */
    public Report buildNodeReport(int nodeId);
    /**
     * <p>buildNodeSourceReport</p>
     *
     * @param nodeSource a String.
     * @return a {@link org.opennms.netmgt.config.kscReports.Report} object.
     */
    public Report buildNodeSourceReport(String nodeSource);
    /**
     * <p>buildDomainReport</p>
     *
     * @param domain a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.kscReports.Report} object.
     */
    public Report buildDomainReport(String domain);
    /**
     * <p>getResourceFromGraph</p>
     *
     * @param graph a {@link org.opennms.netmgt.config.kscReports.Graph} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public OnmsResource getResourceFromGraph(Graph graph);
    /**
     * <p>getTimeSpans</p>
     *
     * @param includeNone a boolean.
     * @return a {@link java.util.Map} object.
     */
    public Map<String, String> getTimeSpans(boolean includeNone);
    /**
     * <p>getReportList</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<Integer, String> getReportList();

    /**
     * <p>getReportMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<Integer, Report> getReportMap();
}
