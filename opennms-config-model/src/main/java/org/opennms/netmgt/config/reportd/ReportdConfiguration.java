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
package org.opennms.netmgt.config.reportd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Behavior configuration for the Enterprise Reporting Daemon
 */
@XmlRootElement(name = "reportd-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("reportd-configuration.xsd")
public class ReportdConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The base directory rendered reports are saved on the file system.
     */
    @XmlAttribute(name = "storage-location", required = true)
    private String m_storageLocation;

    /**
     * Should reports be kept after delivered?
     */
    @XmlAttribute(name = "persist-reports", required = true)
    @XmlJavaTypeAdapter(StupidBooleanAdapter.class)
    private Boolean m_persistReports;

    /**
     * Defines an report schedule with a cron expression
     *  
     *  http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger
     *  Field Name Allowed Values Allowed Special Characters
     *  Seconds 0-59 , - /
     *  Minutes 0-59 , - /
     *  Hours 0-23 , - /
     *  Day-of-month 1-31 , - ? / L W C
     *  Month 1-12 or JAN-DEC , - /
     *  Day-of-Week 1-7 or SUN-SAT , - ? / L C #
     *  Year (Opt) empty, 1970-2099 , - /
     *  
     */
    @XmlElement(name = "report")
    private List<Report> m_reports = new ArrayList<>();

    public void addReport(final Report report) {
        m_reports.add(report);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ReportdConfiguration) {
            final ReportdConfiguration that = (ReportdConfiguration)obj;
            return Objects.equals(this.m_storageLocation, that.m_storageLocation)
                    && Objects.equals(this.m_persistReports, that.m_persistReports)
                    && Objects.equals(this.m_reports, that.m_reports);
        }
        return false;
    }

    public Boolean getPersistReports() {
        return m_persistReports;
    }

    public List<Report> getReports() {
        return m_reports;
    }

    public String getStorageLocation() {
        return m_storageLocation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_storageLocation, 
                            m_persistReports, 
                            m_reports);
    }

    public boolean removeReport(final Report report) {
        return m_reports.remove(report);
    }

    public void setPersistReports(final Boolean persistReports) {
        m_persistReports = ConfigUtils.assertNotNull(persistReports, "persist-reports");
    }

    public void setReport(final List<Report> reports) {
        if (reports == m_reports) return;
        m_reports.clear();
        if (reports != null) m_reports.addAll(reports);
    }

    public void setStorageLocation(final String storageLocation) {
        m_storageLocation = ConfigUtils.assertNotEmpty(storageLocation, "storage-location");
    }

}
