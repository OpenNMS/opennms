/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.api;

import java.util.List;
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
     * <p>getResourcesFromGraphs</p>
     *
     * @param graphs a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource>getResourcesFromGraphs(List<Graph> graphs);
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
