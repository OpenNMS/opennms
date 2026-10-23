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
package org.opennms.netmgt.config.graphcollections;

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
public class GraphCollectionList implements java.io.Serializable {
    private static final long serialVersionUID = 2L;

    private static final Comparator<GraphCollection> SORT_REPORTS = new Comparator<GraphCollection>() {
        @Override
        public int compare(final GraphCollection o1, final GraphCollection o2) {
            return o1.getTitle().compareTo(o2.getTitle());
        }
    };

    @XmlElement(name = "Report")
    private List<GraphCollection> m_reports = new ArrayList<>();

    public List<GraphCollection> getCollections() {
        return m_reports;
    }

    public void setCollections(final List<GraphCollection> reports) {
        if (reports == m_reports) return;
        m_reports.clear();
        if (reports != null) m_reports.addAll(reports);
        m_reports.sort(SORT_REPORTS);
    }

    public void addCollection(final GraphCollection report) {
        m_reports.add(report);
    }

    public void setCollection(final int index, final GraphCollection report) {
        m_reports.set(index, report);
    }

    public boolean removeCollection(final GraphCollection report) {
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

        if (obj instanceof GraphCollectionList) {
            final GraphCollectionList that = (GraphCollectionList)obj;
            return Objects.equals(this.m_reports, that.m_reports);
        }
        return false;
    }
}
