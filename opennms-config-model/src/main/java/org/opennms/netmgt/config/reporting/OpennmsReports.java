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
package org.opennms.netmgt.config.reporting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Report Configuration for OpenNMS reports including availability
 */
@XmlRootElement(name = "opennms-reports")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("reporting.xsd")
public class OpennmsReports implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * report definition for legacy opennms availability reports
     */
    @XmlElement(name = "report")
    private List<Report> m_reports = new ArrayList<>();

    public List<Report> getReports() {
        return m_reports;
    }

    public void setReports(final List<Report> reports) {
        if (reports == m_reports) return;
        m_reports.clear();
        if (reports != null) m_reports.addAll(reports);
    }

    public void addReport(final Report report) {
        this.m_reports.add(report);
    }

    public boolean removeReport(final Report report) {
        return m_reports.remove(report);
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

        if (obj instanceof OpennmsReports) {
            final OpennmsReports that = (OpennmsReports)obj;
            return Objects.equals(this.m_reports, that.m_reports);
        }
        return false;
    }

}
