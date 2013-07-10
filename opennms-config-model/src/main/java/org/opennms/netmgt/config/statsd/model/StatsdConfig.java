/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.statsd.model;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.statsd.Parameter;
import org.opennms.netmgt.config.statsd.StatisticsDaemonConfiguration;
import org.opennms.netmgt.config.statsd.types.PackageReportStatusType;
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
    private StatisticsDaemonConfiguration m_castorConfig;
    private List<Report> m_reports = new ArrayList<Report>();
    private List<StatsdPackage> m_packages = new ArrayList<StatsdPackage>();

    /**
     * <p>Constructor for StatsdConfig.</p>
     *
     * @param castorConfig a {@link org.opennms.netmgt.config.statsd.StatisticsDaemonConfiguration} object.
     */
    public StatsdConfig(StatisticsDaemonConfiguration castorConfig) {
        m_castorConfig = castorConfig;

        for (org.opennms.netmgt.config.statsd.Report report : getCastorReports()) {
            Report r = new Report();
            r.setName(report.getName());
            r.setClassName(report.getClassName());
            for (Parameter parameter : getParametersForReport(report)) {
                r.addParameter(parameter.getKey(), parameter.getValue());
            }
            m_reports.add(r);
        }
        
        for (org.opennms.netmgt.config.statsd.Package pkg : getCastorPackages()) {
            StatsdPackage p = new StatsdPackage();
            p.setName(pkg.getName());
            p.setFilter(pkg.getFilter() != null ? pkg.getFilter().getContent() : null);
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
                r.setEnabled(packageReport.getStatus().equals(PackageReportStatusType.ON));
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
        return report.getParameterCollection();
    }

    private List<org.opennms.netmgt.config.statsd.Report> getCastorReports() {
        return m_castorConfig.getReportCollection();
    }

    private List<org.opennms.netmgt.config.statsd.Package> getCastorPackages() {
        return m_castorConfig.getPackageCollection();
    }

    private List<org.opennms.netmgt.config.statsd.PackageReport> getPackageReportForPackage(org.opennms.netmgt.config.statsd.Package pkg) {
        return pkg.getPackageReportCollection();
    }

    private List<Parameter> getParametersForPackageReport(org.opennms.netmgt.config.statsd.PackageReport packageReport) {
        return packageReport.getParameterCollection();
    }

}
