/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.kscReports;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the ksc-performance-reports.xml
 *  configuration file. 
 */
@XmlRootElement(name = "ReportsList")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("ksc-performance-reports.xsd")
public class ReportsList implements java.io.Serializable {
    private static final long serialVersionUID = 2L;

    private static final Comparator<Report> SORT_REPORTS = new Comparator<Report>() {
        @Override
        public int compare(final Report o1, final Report o2) {
            return o1.getTitle().compareTo(o2.getTitle());
        }
    };

    @XmlElement(name = "Report")
    private List<Report> m_reports = new ArrayList<>();

    public List<Report> getReports() {
        return m_reports;
    }

    public void setReports(final List<Report> reports) {
        if (reports == m_reports) return;
        m_reports.clear();
        if (reports != null) m_reports.addAll(reports);
        m_reports.sort(SORT_REPORTS);
    }

    public void addReport(final Report report) {
        m_reports.add(report);
    }

    public void setReport(final int index, final Report report) {
        m_reports.set(index, report);
    }

    public boolean removeReport(final Report report) {
        return m_reports.remove(report);
    }

    public void sort() {
        m_reports.sort(SORT_REPORTS);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_reports);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ReportsList) {
            final ReportsList that = (ReportsList)obj;
            return Objects.equals(this.m_reports, that.m_reports);
        }
        return false;
    }
}
