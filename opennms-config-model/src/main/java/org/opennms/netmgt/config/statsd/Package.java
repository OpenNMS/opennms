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
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Package encapsulating nodes eligible to have
 *  this report run on them.
 */
@XmlRootElement(name = "package")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("statistics-daemon-configuration.xsd")
public class Package implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The name or identifier for this
     *  package
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * A rule which addresses belonging to this package
     *  must pass. This package is applied only to addresses that pass
     *  this filter.
     */
    @XmlElement(name = "filter")
    private Filter m_filter;

    /**
     * Reports to be run on the nodes in this package
     */
    @XmlElement(name = "packageReport")
    private List<PackageReport> m_packageReports = new ArrayList<>();

    public Package() {
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Optional<Filter> getFilter() {
        return Optional.ofNullable(m_filter);
    }

    public void setFilter(final Filter filter) {
        m_filter = filter;
    }

    public List<PackageReport> getPackageReports() {
        return m_packageReports;
    }

    public void setPackageReports(final List<PackageReport> packageReports) {
        if (packageReports == m_packageReports) return;
        m_packageReports.clear();
        if (packageReports != null) m_packageReports.addAll(packageReports);
    }

    public void addPackageReport(final PackageReport packageReport) {
        m_packageReports.add(packageReport);
    }

    public boolean removePackageReport(final PackageReport packageReport) {
        return m_packageReports.remove(packageReport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_filter, 
                            m_packageReports);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Package) {
            final Package that = (Package)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_filter, that.m_filter)
                    && Objects.equals(this.m_packageReports, that.m_packageReports);
        }
        return false;
    }

}
