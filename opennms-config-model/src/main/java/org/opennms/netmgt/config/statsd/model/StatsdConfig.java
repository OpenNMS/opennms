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

import org.opennms.netmgt.config.statsd.PackageReportStatus;
import org.opennms.netmgt.config.statsd.Parameter;
import org.opennms.netmgt.config.statsd.StatisticsDaemonConfiguration;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * Represents the entire configuration for the statistics daemon.
 * Contains configured reports and packages which select nodes to
 * report on and whicn reports to run on those nodes.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see StastdPackage
 * @see Report
 * @version $Id: $
 */
public class StatsdConfig {
    private StatisticsDaemonConfiguration m_config;
    private List<Report> m_reports = new ArrayList<>();
    private List<StatsdPackage> m_packages = new ArrayList<>();

    /**
     * <p>Constructor for StatsdConfig.</p>
     *
     * @param config a {@link org.opennms.netmgt.config.statsd.StatisticsDaemonConfiguration} object.
     */
    public StatsdConfig(StatisticsDaemonConfiguration config) {
        m_config = config;

        for (org.opennms.netmgt.config.statsd.Report report : getXmlReports()) {
            Report r = new Report();
            r.setName(report.getName());
            r.setClassName(report.getClassName());
            for (Parameter parameter : getParametersForReport(report)) {
                r.addParameter(parameter.getKey(), parameter.getValue());
            }
            m_reports.add(r);
        }
        
        for (org.opennms.netmgt.config.statsd.Package pkg : getXmlPackages()) {
            StatsdPackage p = new StatsdPackage();
            p.setName(pkg.getName());
            if (pkg.getFilter().isPresent() && pkg.getFilter().get().getContent().isPresent()) {
                p.setFilter(pkg.getFilter().get().getContent().get());
            }
            for (org.opennms.netmgt.config.statsd.PackageReport packageReport : getPackageReportForPackage(pkg)) {
                PackageReport r = new PackageReport();
                r.setPackage(p);
                try {
                    r.setReport(loadReport(packageReport.getName()));
                } catch (DataAccessException e) {
                    throw new ObjectRetrievalFailureException("Could not get report named '" + packageReport.getName() + "' for package '" + pkg.getName() + "'", pkg.getName(), null, e);
                }
                r.setDescription(packageReport.getDescription());
                r.setRetainInterval(Long.parseLong(packageReport.getRetainInterval()));
                r.setSchedule(packageReport.getSchedule());
                r.setEnabled(packageReport.getStatus().equals(PackageReportStatus.on));
                for (Parameter parameter : getParametersForPackageReport(packageReport)) {
                    r.addParameter(parameter.getKey(), parameter.getValue());
                }
                p.addReport(r);
            }
            m_packages.add(p);
        }
    }
    
    /**
     * <p>getReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Report> getReports() {
        return m_reports;
    }

    /**
     * <p>getPackages</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<StatsdPackage> getPackages() {
        return m_packages;
    }

    private Report loadReport(String name) {
        for (Report report : m_reports) {
            if (name.equals(report.getName())) {
                return report;
            }
        }
        
        throw new ObjectRetrievalFailureException("There is no report definition named '" + name + "'", name);
    }

    private List<Parameter> getParametersForReport(org.opennms.netmgt.config.statsd.Report report) {
        return report.getParameters();
    }

    private List<org.opennms.netmgt.config.statsd.Report> getXmlReports() {
        return m_config.getReports();
    }

    private List<org.opennms.netmgt.config.statsd.Package> getXmlPackages() {
        return m_config.getPackages();
    }

    private List<org.opennms.netmgt.config.statsd.PackageReport> getPackageReportForPackage(org.opennms.netmgt.config.statsd.Package pkg) {
        return pkg.getPackageReports();
    }

    private List<Parameter> getParametersForPackageReport(org.opennms.netmgt.config.statsd.PackageReport packageReport) {
        return packageReport.getParameters();
    }

}
