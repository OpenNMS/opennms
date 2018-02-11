/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.poller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Package encapsulating addresses, services to be polled
 *  for these addresses, etc..
 */

@XmlRootElement(name="package")
@XmlAccessorType(XmlAccessType.NONE)
public class Package implements Serializable {
    private static final long serialVersionUID = 3522040891199184363L;

    /**
     * Name or identifier for this package.
     */
    @XmlAttribute(name="name")
    private String m_name;

    /**
     * Boolean representing whether this is a package for a remote location
     * monitor. If true, this package will be ignored by the OpenNMS daemon
     * poller.
     */
    @XmlAttribute(name="remote")
    private Boolean m_remote;

    /**
     * A rule which addresses belonging to this package must pass. This
     * package is applied only to addresses that pass this filter.
     */
    @XmlElement(name="filter")
    private Filter m_filter;

    /**
     * Addresses in this package
     */
    @XmlElement(name="specific")
    private List<String> m_specifics = new ArrayList<>();

    /**
     * Range of addresses in this package.
     */
    @XmlElement(name="include-range")
    private List<IncludeRange> m_includeRanges = new ArrayList<>();

    /**
     * Range of addresses to be excluded from this package.
     */
    @XmlElement(name="exclude-range")
    private List<ExcludeRange> m_excludeRanges = new ArrayList<>();

    /**
     * A file URL holding specific addresses to be polled. Each line in the
     * URL file can be one of: &lt;IP&gt;&lt;space&gt;#&lt;comments&gt; or &lt;IP&gt; or #&lt;comments&gt;.
     * Lines starting with a '#' are ignored and so are characters after a
     * '&lt;space&gt;#' in a line.
     */
    @XmlElement(name="include-url")
    private List<String> m_includeUrls = new ArrayList<>();

    /**
     * RRD parameters for response time data.
     */
    @XmlElement(name="rrd")
    private Rrd m_rrd;

    /**
     * Services to be polled for addresses belonging to this package.
     */
    @XmlElement(name="service")
    private List<Service> m_services = new ArrayList<>();

    /**
     * Scheduled outages. If a service is found down during this period, it is
     * not reported as down.
     */
    @XmlElement(name="outage-calendar")
    private List<String> m_outageCalendars = new ArrayList<>();

    /**
     * Downtime model. Determines the rate at which addresses are to be polled
     * when they remain down for extended periods.
     */
    @XmlElement(name="downtime")
    private List<Downtime> m_downtimes = new ArrayList<>();

    public Package() {
        super();
    }

    public Package(final String name) {
        this();
        setName(name);
    }

    /**
     * Name or identifier for this package.
     */
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    /**
     * Boolean representing whether this is a package for a remote location
     * monitor. If true, this package will be ignored by the OpenNMS daemon
     * poller.
     */
    public Boolean getRemote() {
        return m_remote == null? false : m_remote;
    }

    public void setRemote(final Boolean remote) {
        m_remote = remote;
    }

    /**
     * A rule which addresses belonging to this package must pass. This
     * package is applied only to addresses that pass this filter.
     */
    public Filter getFilter() {
        return m_filter;
    }

    public void setFilter(final String filter) {
        m_filter = new Filter(filter);
    }

    public void setFilter(final Filter filter) {
        m_filter = filter;
    }
    
    public List<String> getSpecifics() {
        if (m_specifics == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_specifics);
        }
    }

    public void setSpecifics(final List<String> specifics) {
        m_specifics = new ArrayList<String>(specifics);
    }

    public void addSpecific(final String specific) throws IndexOutOfBoundsException {
        m_specifics.add(specific);
    }

    public boolean removeSpecific(final String specific) {
        return m_specifics.remove(specific);
    }

    public List<IncludeRange> getIncludeRanges() {
        if (m_includeRanges == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_includeRanges);
        }
    }

    public void setIncludeRanges(final List<IncludeRange> includeRanges) {
        m_includeRanges = new ArrayList<IncludeRange>(includeRanges);
    }

    public void addIncludeRange(final IncludeRange includeRange) throws IndexOutOfBoundsException {
        m_includeRanges.add(includeRange);
    }

    public void addIncludeRange(final String begin, final String end) {
        addIncludeRange(new IncludeRange(begin, end));
    }

    public boolean removeIncludeRange(final IncludeRange includeRange) {
        return m_includeRanges.remove(includeRange);
    }

    public List<ExcludeRange> getExcludeRanges() {
        if (m_excludeRanges == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_excludeRanges);
        }
    }

    public void setExcludeRanges(final List<ExcludeRange> excludeRanges) {
        m_excludeRanges = new ArrayList<ExcludeRange>(excludeRanges);
    }

    public void addExcludeRange(final ExcludeRange excludeRange) throws IndexOutOfBoundsException {
        m_excludeRanges.add(excludeRange);
    }

    public boolean removeExcludeRange(final ExcludeRange excludeRange) {
        return m_excludeRanges.remove(excludeRange);
    }

    public List<String> getIncludeUrls() {
        if (m_includeUrls == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_includeUrls);
        }
    }

    public void setIncludeUrls(final List<String> includeUrls) {
        m_includeUrls = new ArrayList<String>(includeUrls);
    }

    public void addIncludeUrl(final String includeUrl) throws IndexOutOfBoundsException {
        m_includeUrls.add(includeUrl);
    }

    public boolean removeIncludeUrl(final String includeUrl) {
        return m_includeUrls.remove(includeUrl);
    }

    /**
     * RRD parameters for response time data.
     */
    public Rrd getRrd() {
        return m_rrd;
    }

    public void setRrd(final Rrd rrd) {
        m_rrd = rrd;
    }

    public List<Service> getServices() {
        if (m_services == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_services);
        }
    }

    public void setServices(final List<Service> services) {
        m_services = new ArrayList<Service>(services);
    }

    public void addService(final Service service) throws IndexOutOfBoundsException {
        m_services.add(service);
    }

    public boolean removeService(final Service service) {
        return m_services.remove(service);
    }

    public Service getService(final String serviceName) {
        for (final Service service : m_services) {
            if (serviceName.equals(service.getName())) {
                return service;
            }
        }
        return null;
    }

    public List<String> getOutageCalendars() {
        if (m_outageCalendars == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_outageCalendars);
        }
    }

    public void setOutageCalendars(final List<String> outageCalendars) {
        m_outageCalendars = new ArrayList<String>(outageCalendars);
    }

    public void addOutageCalendar(final String outageCalendar) throws IndexOutOfBoundsException {
        m_outageCalendars.add(outageCalendar);
    }

    public boolean removeOutageCalendar(final String outageCalendar) {
        return m_outageCalendars.remove(outageCalendar);
    }

    public List<Downtime> getDowntimes() {
        if (m_downtimes == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_downtimes);
        }
    }

    public void setDowntimes(final List<Downtime> downtimes) {
        m_downtimes = new ArrayList<Downtime>(downtimes);
    }

    public void addDowntime(final Downtime downtime) throws IndexOutOfBoundsException {
        m_downtimes.add(downtime);
    }

    public boolean removeDowntime(final Downtime downtime) {
        return m_downtimes.remove(downtime);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_downtimes == null) ? 0 : m_downtimes.hashCode());
        result = prime * result + ((m_excludeRanges == null) ? 0 : m_excludeRanges.hashCode());
        result = prime * result + ((m_filter == null) ? 0 : m_filter.hashCode());
        result = prime * result + ((m_includeRanges == null) ? 0 : m_includeRanges.hashCode());
        result = prime * result + ((m_includeUrls == null) ? 0 : m_includeUrls.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_outageCalendars == null) ? 0 : m_outageCalendars.hashCode());
        result = prime * result + ((m_remote == null) ? 0 : m_remote.hashCode());
        result = prime * result + ((m_rrd == null) ? 0 : m_rrd.hashCode());
        result = prime * result + ((m_services == null) ? 0 : m_services.hashCode());
        result = prime * result + ((m_specifics == null) ? 0 : m_specifics.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Package)) {
            return false;
        }
        Package other = (Package) obj;
        if (m_downtimes == null) {
            if (other.m_downtimes != null) {
                return false;
            }
        } else if (!m_downtimes.equals(other.m_downtimes)) {
            return false;
        }
        if (m_excludeRanges == null) {
            if (other.m_excludeRanges != null) {
                return false;
            }
        } else if (!m_excludeRanges.equals(other.m_excludeRanges)) {
            return false;
        }
        if (m_filter == null) {
            if (other.m_filter != null) {
                return false;
            }
        } else if (!m_filter.equals(other.m_filter)) {
            return false;
        }
        if (m_includeRanges == null) {
            if (other.m_includeRanges != null) {
                return false;
            }
        } else if (!m_includeRanges.equals(other.m_includeRanges)) {
            return false;
        }
        if (m_includeUrls == null) {
            if (other.m_includeUrls != null) {
                return false;
            }
        } else if (!m_includeUrls.equals(other.m_includeUrls)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_outageCalendars == null) {
            if (other.m_outageCalendars != null) {
                return false;
            }
        } else if (!m_outageCalendars.equals(other.m_outageCalendars)) {
            return false;
        }
        if (m_remote == null) {
            if (other.m_remote != null) {
                return false;
            }
        } else if (!m_remote.equals(other.m_remote)) {
            return false;
        }
        if (m_rrd == null) {
            if (other.m_rrd != null) {
                return false;
            }
        } else if (!m_rrd.equals(other.m_rrd)) {
            return false;
        }
        if (m_services == null) {
            if (other.m_services != null) {
                return false;
            }
        } else if (!m_services.equals(other.m_services)) {
            return false;
        }
        if (m_specifics == null) {
            if (other.m_specifics != null) {
                return false;
            }
        } else if (!m_specifics.equals(other.m_specifics)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Package[name=" + m_name +
                ",remote=" + m_remote +
                ",filter=" + m_filter +
                ",specifics=" + m_specifics +
                ",includeRanges=" + m_includeRanges +
                ",excludeRanges=" + m_excludeRanges +
                ",includeUrls=" + m_includeUrls +
                ",rrd=" + m_rrd +
                ",services=" + m_services +
                ",outageCalendars=" + m_outageCalendars +
                ",downtimes=" + m_downtimes +
                "]";
    }
}
