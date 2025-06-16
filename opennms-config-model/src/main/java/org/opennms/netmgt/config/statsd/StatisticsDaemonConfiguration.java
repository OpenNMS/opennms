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
package org.opennms.netmgt.config.statsd;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Top-level element for the statsd-configuration.xml configuration file.
 */
@XmlRootElement(name = "statistics-daemon-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("statistics-daemon-configuration.xsd")
public class StatisticsDaemonConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Package encapsulating nodes eligible to have
     *  this report run on them.
     */
    @XmlElement(name = "package", required = true)
    private List<Package> m_packages = new ArrayList<>();

    /**
     * Reports
     */
    @XmlElement(name = "report", required = true)
    private List<Report> m_reports = new ArrayList<>();

    public StatisticsDaemonConfiguration() {
    }

    public List<Package> getPackages() {
        return m_packages;
    }

    public void setPackages(final List<Package> packages) {
        ConfigUtils.assertMinimumSize(packages, 1, "package");
        if (packages == m_packages) return;
        m_packages.clear();
        if (packages != null) m_packages.addAll(packages);
    }

    public void addPackage(final Package p) {
        m_packages.add(p);
    }

    public boolean removePackage(final Package p) {
        return m_packages.remove(p);
    }

    public List<Report> getReports() {
        return m_reports;
    }

    public void setReports(final List<Report> reports) {
        ConfigUtils.assertMinimumSize(reports, 1, "report");
        if (reports == m_reports) return;
        m_reports.clear();
        if (reports != null) m_reports.addAll(reports);
    }

    public void addReport(final Report report) {
        m_reports.add(report);
    }

    public boolean removeReport(final Report report) {
        return m_reports.remove(report);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_packages, m_reports);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof StatisticsDaemonConfiguration) {
            final StatisticsDaemonConfiguration that = (StatisticsDaemonConfiguration)obj;
            return Objects.equals(this.m_packages, that.m_packages)
                    && Objects.equals(this.m_reports, that.m_reports);
        }
        return false;
    }

}
