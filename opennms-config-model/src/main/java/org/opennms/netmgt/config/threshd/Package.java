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
package org.opennms.netmgt.config.threshd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Package encapsulating addresses eligible for
 *  thresholding.
 */
@XmlRootElement(name = "package")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("thresholding.xsd")
public class Package implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Name or identifier for this package
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * A rule which addresses belonging to this package
     *  must pass. This package is applied only to addresses that pass
     *  this filter
     */
    @XmlElement(name = "filter", required = true)
    private Filter m_filter;

    /**
     * Adresses in this package
     */
    @XmlElement(name = "specific")
    private List<String> m_specifics = new ArrayList<>();

    /**
     * Range of addresses in this package
     */
    @XmlElement(name = "include-range")
    private List<IncludeRange> m_includeRanges = new ArrayList<>();

    /**
     * Range of addresses to be excluded from this
     *  package
     */
    @XmlElement(name = "exclude-range")
    private List<ExcludeRange> m_excludeRanges = new ArrayList<>();

    /**
     * A file URL holding specific addresses to be polled.
     *  Each line in the URL file can be one of:
     *  <IP><space>#<comments>, or <IP>, or
     *  #<comments>. Lines starting with a '#' are ignored and so
     *  are characters after a '<space>#' in a line.
     */
    @XmlElement(name = "include-url")
    private List<String> m_includeUrls = new ArrayList<>();

    /**
     * Services for which thresholding is to occur in this package
     */
    @XmlElement(name = "service")
    private List<Service> m_services = new ArrayList<>();

    /**
     * Scheduled outages. Thresholding is not performed
     *  during scheduled outages.
     */
    @XmlElement(name = "outage-calendar")
    private List<String> m_outageCalendars = new ArrayList<>();

    public Package() { }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Filter getFilter() {
        return m_filter;
    }

    public void setFilter(final Filter filter) {
        m_filter = ConfigUtils.assertNotNull(filter, "filter");
    }

    public List<String> getSpecifics() {
        return m_specifics;
    }

    public void setSpecifics(final List<String> specifics) {
        if (specifics == m_specifics) return;
        m_specifics.clear();
        if (specifics != null) m_specifics.addAll(specifics);
    }

    public void addSpecific(final String specific) {
        m_specifics.add(specific);
    }

    public boolean removeSpecific(final String specific) {
        return m_specifics.remove(specific);
    }

    public List<IncludeRange> getIncludeRanges() {
        return m_includeRanges;
    }

    public void setIncludeRanges(final List<IncludeRange> includeRanges) {
        if (includeRanges == m_includeRanges) return;
        m_includeRanges.clear();
        if (includeRanges != null) m_includeRanges.addAll(includeRanges);
    }

    public void addIncludeRange(final IncludeRange includeRange) {
        m_includeRanges.add(includeRange);
    }

    public boolean removeIncludeRange(final IncludeRange includeRange) {
        return m_includeRanges.remove(includeRange);
    }

    public List<ExcludeRange> getExcludeRanges() {
        return m_excludeRanges;
    }

    public void setExcludeRanges(final List<ExcludeRange> excludeRanges) {
        if (excludeRanges == m_excludeRanges) return;
        m_excludeRanges.clear();
        if (excludeRanges != null) m_excludeRanges.addAll(excludeRanges);
    }

    public void addExcludeRange(final ExcludeRange excludeRange) {
        m_excludeRanges.add(excludeRange);
    }

    public boolean removeExcludeRange(final ExcludeRange excludeRange) {
        return m_excludeRanges.remove(excludeRange);
    }

    public List<String> getIncludeUrls() {
        return m_includeUrls;
    }

    public void setIncludeUrls(final List<String> includeUrls) {
        if (includeUrls == m_includeUrls) return;
        m_includeUrls.clear();
        if (includeUrls != null) m_includeUrls.addAll(includeUrls);
    }

    public void addIncludeUrl(final String includeUrl) {
        m_includeUrls.add(includeUrl);
    }

    public boolean removeIncludeUrl(final String includeUrl) {
        return m_includeUrls.remove(includeUrl);
    }
    
    public List<Service> getServices() {
        return m_services;
    }

    public void setServices(final List<Service> services) {
        if (services == m_services) return;
        m_services.clear();
        if (services != null) m_services.addAll(services);
    }

    public void addService(final Service service) {
        m_services.add(service);
    }

    public boolean removeService(final Service service) {
        return m_services.remove(service);
    }

    public List<String> getOutageCalendars() {
        return m_outageCalendars;
    }

    public void setOutageCalendars(final List<String> outageCalendars) {
        if (outageCalendars == m_outageCalendars) return;
        m_outageCalendars.clear();
        if (outageCalendars != null) m_outageCalendars.addAll(outageCalendars);
    }

    public void addOutageCalendar(final String outageCalendar) {
        m_outageCalendars.add(outageCalendar);
    }

    public boolean removeOutageCalendar(final String outageCalendar) {
        return m_outageCalendars.remove(outageCalendar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_filter, 
                            m_specifics, 
                            m_includeRanges, 
                            m_excludeRanges, 
                            m_includeUrls, 
                            m_services, 
                            m_outageCalendars);
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
                    && Objects.equals(this.m_specifics, that.m_specifics)
                    && Objects.equals(this.m_includeRanges, that.m_includeRanges)
                    && Objects.equals(this.m_excludeRanges, that.m_excludeRanges)
                    && Objects.equals(this.m_includeUrls, that.m_includeUrls)
                    && Objects.equals(this.m_services, that.m_services)
                    && Objects.equals(this.m_outageCalendars, that.m_outageCalendars);
        }
        return false;
    }

}
