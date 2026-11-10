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
package org.opennms.netmgt.config.statsd.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a package that selects nodes and lists reports to
 * run on them.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see PackageReport
 * @version $Id: $
 */
public class StatsdPackage {
    private String m_name;
    private String m_filter;
    private List<PackageReport> m_reports = new ArrayList<>();
    
    /**
     * <p>getFilter</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFilter() {
        return m_filter;
    }
    /**
     * <p>setFilter</p>
     *
     * @param filter a {@link java.lang.String} object.
     */
    public void setFilter(String filter) {
        m_filter = filter;
    }
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }
    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }
    /**
     * <p>getReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<PackageReport> getReports() {
        return m_reports;
    }
    /**
     * <p>setReports</p>
     *
     * @param reports a {@link java.util.List} object.
     */
    public void setReports(List<PackageReport> reports) {
        m_reports = reports;
    }
    /**
     * <p>addReport</p>
     *
     * @param report a {@link org.opennms.netmgt.config.statsd.model.PackageReport} object.
     */
    public void addReport(PackageReport report) {
        m_reports.add(report);
    }
}
