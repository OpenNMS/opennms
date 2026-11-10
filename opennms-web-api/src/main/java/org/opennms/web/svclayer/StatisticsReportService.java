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
package org.opennms.web.svclayer;

import java.util.List;

import org.opennms.netmgt.model.StatisticsReport;
import org.opennms.web.svclayer.model.StatisticsReportCommand;
import org.opennms.web.svclayer.model.StatisticsReportModel;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

/**
 * Web service layer for statistics reports.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Transactional(readOnly=true)
public interface StatisticsReportService {
    /**
     * <p>getStatisticsReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<StatisticsReport> getStatisticsReports();

    /**
     * <p>getReport</p>
     *
     * @param command a {@link org.opennms.web.command.StatisticsReportCommand} object.
     * @param errors a {@link org.springframework.validation.BindingResult} object.
     * @return a {@link org.opennms.web.svclayer.model.StatisticsReportModel} object.
     */
    public StatisticsReportModel getReport(StatisticsReportCommand command, BindingResult errors);
}
