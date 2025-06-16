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
package org.opennms.features.reporting.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Report Configuration for local reports
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "database-reports")
@XmlAccessorType(XmlAccessType.NONE)
public class LocalReports {

    @XmlElement(name = "report")
    private List<Report> m_reportList = new ArrayList<>();

    public List<Report> getReportList() {
        return m_reportList;
    }

    public void setReportList(List<Report> reportList) {
        this.m_reportList = reportList;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof LocalReports)) {
            return false;
        }
        LocalReports castOther = (LocalReports) other;
        return Objects.equals(m_reportList, castOther.m_reportList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_reportList);
    }
}
