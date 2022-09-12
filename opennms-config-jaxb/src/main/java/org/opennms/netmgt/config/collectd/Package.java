/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.collectd;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.network.IPAddress;
import org.opennms.core.network.IpListFromUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Package encapsulating addresses eligible to have SNMP
 *  data collected from them.
 */

@XmlRootElement(name="package")
@XmlAccessorType(XmlAccessType.NONE)
public class Package implements Serializable {
    private static final long serialVersionUID = 1689693370360064016L;

    private static final Logger LOG = LoggerFactory.getLogger(Package.class);

    /**
     * The name or identifier for this package
     */
    @XmlAttribute(name="name")
    private String m_name;

    /**
     * Indicates weather this Package is a remote package.
     * If remote = <code>true</code> it is a remote package.
     * Remote packages are ignored by Colelctd.
     */
    @XmlAttribute(name="remote")
    private boolean m_remote;

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
     * Range of addresses in this package
     */
    @XmlElement(name="include-range")
    private List<IncludeRange> m_includeRanges = new ArrayList<>();

    /**
     * Range of addresses to be excluded from this package
     */
    @XmlElement(name="exclude-range")
    private List<ExcludeRange> m_excludeRanges = new ArrayList<>();

    /**
     * A file URL holding specific addresses to be polled. Each line in the
     * URL file can be one of: "&lt;IP&gt;&lt;space&gt;#&lt;comments&gt;"; "&lt;IP&gt;";
     * "#&lt;comments&gt;"; Lines starting with a '#' are ignored and so are
     * characters after a '&lt;space&gt;#' in a line.
     */
    @XmlElement(name="include-url")
    private List<String> m_includeUrls = new ArrayList<>();

    /**
     * Flag for storing collected data by domain/ifAlias. Defaults to false.
     * Allowable values are true, false.
     */
    @XmlElement(name="storeByIfAlias")
    private String m_storeByIfAlias;

    /**
     * Flag for storing collected data by nodeid/interface name. Defaults to
     * normal. Allowable values are true, false, normal.
     */
    @XmlElement(name="storeByNodeID")
    private String m_storeByNodeID;

    /**
     * The name of the domain covered by this collection package. Defaults to
     * package name.
     */
    @XmlElement(name="ifAliasDomain")
    private String m_ifAliasDomain;

    /**
     * Flag for controlling how interfaces are selected for data collection by
     * domain/ifAlias. If true, storage will occur for any interface on the
     * node found to have an valid ifAlias. Otherwise data will be stored only
     * if the interface is configured for data collection. Defaults to false.
     */
    @XmlElement(name="storFlagOverride")
    private String m_storFlagOverride;

    /**
     * A character or string for terminating ifAlias text. In effect, any text
     * beginning with this character or string becomes a comment and is not
     * considered part of the ifAlias when naming storage files and displaying
     * data. Defaults to null.
     */
    @XmlElement(name="ifAliasComment")
    private String m_ifAliasComment;

    /**
     * Services for which data is to be collected in this package
     */
    @XmlElement(name="service")
    private List<Service> m_services = new ArrayList<>();

    /**
     * Scheduled outages - data collection is not performed during scheduled
     * outages
     */
    @XmlElement(name="outage-calendar")
    private List<String> m_outageCalendar = new ArrayList<>();

    public Package() {
        super();
    }

    /**
     * Copy constructor.
     * 
     * @param pkg
     */
    public Package(Package pkg) {
        super();
        setExcludeRanges(pkg.getExcludeRanges());
        setFilter(pkg.getFilter());
        setIfAliasComment(pkg.getIfAliasComment());
        setIfAliasDomain(pkg.getIfAliasDomain());
        setIncludeRanges(pkg.getIncludeRanges());
        setIncludeUrlCollection(pkg.getIncludeUrls());
        setName(pkg.getName());
        setOutageCalendars(pkg.getOutageCalendars());
        setServices(pkg.getServices());
        setSpecifics(pkg.getSpecifics());
        setStoreByIfAlias(pkg.getStoreByIfAlias());
        setStoreByNodeID(pkg.getStoreByNodeID());
        setStorFlagOverride(pkg.getStorFlagOverride());
    }

    /**
     * The name or identifier for this package
     */
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    /**
     * A rule which addresses belonging to this package must pass. This
     * package is applied only to addresses that pass this filter.
     */
    public Filter getFilter() {
        return m_filter;
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
        m_specifics = new ArrayList<>(specifics);
    }

    public void addSpecific(final String specific) throws IndexOutOfBoundsException {
        m_specifics.add(specific);
    }

    public boolean removeSpecific(final String specific) {
        return m_specifics.remove(specific);
    }

    public boolean hasSpecific(final byte[] addr) {
        final IPAddress ipAddr = new IPAddress(addr);
        for (final String espec : getSpecifics()) {
            if (ipAddr.equals(new IPAddress(espec))) {
                return true;
            }
        }
        return false;
    }

    public List<IncludeRange> getIncludeRanges() {
        if (m_includeRanges == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_includeRanges);
        }
    }

    public void setIncludeRanges(final List<IncludeRange> ranges) {
        m_includeRanges = new ArrayList<>(ranges);
    }

    public void addIncludeRange(final IncludeRange range) throws IndexOutOfBoundsException {
        m_includeRanges.add(range);
    }

    public boolean removeIncludeRange(final IncludeRange range) {
        return m_includeRanges.remove(range);
    }

    public boolean hasIncludeRange(final String addr) {
        if (getIncludeRanges().isEmpty() && getSpecifics().isEmpty()) {
            return true;
        }

        final IPAddress ipAddr = new IPAddress(addr);

        for (final IncludeRange rng : getIncludeRanges()) {
            final IPAddress begin = rng.getBeginAsAddress();
            final IPAddress end   = rng.getEndAsAddress();
            if (ipAddr.isGreaterThanOrEqualTo(begin) && ipAddr.isLessThanOrEqualTo(end)) {
                return true;
            }
        }
        return false;
    }

    public List<ExcludeRange> getExcludeRanges() {
        if (m_excludeRanges == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_excludeRanges);
        }
    }

    public void setExcludeRanges(final List<ExcludeRange> ranges) {
        m_excludeRanges = new ArrayList<>(ranges);
    }

    public void addExcludeRange(final ExcludeRange range) throws IndexOutOfBoundsException {
        m_excludeRanges.add(range);
    }

    public boolean removeExcludeRange(final ExcludeRange range) {
        return m_excludeRanges.remove(range);
    }

    public boolean hasExcludeRange(final String addr) {
        final IPAddress ipAddr = new IPAddress(addr);

        for (final ExcludeRange rng : getExcludeRanges()) {
            final IPAddress begin = rng.getBeginAsAddress();
            final IPAddress end   = rng.getEndAsAddress();

            if (ipAddr.isGreaterThanOrEqualTo(begin) && ipAddr.isLessThanOrEqualTo(end)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getIncludeUrls() {
        if (m_includeUrls == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_includeUrls);
        }
    }

    public void setIncludeUrlCollection(final List<String> urls) {
        m_includeUrls = new ArrayList<>(urls);
    }

    public void addIncludeUrl(final String url) throws IndexOutOfBoundsException {
        m_includeUrls.add(url);
    }

    public boolean removeIncludeUrl(final String url) {
        return m_includeUrls.remove(url);
    }

    public boolean hasSpecificUrl(final String iface, final boolean hasSpecific) {
        if (hasSpecific) {
            return true;
        } else {
            final IPAddress addr = new IPAddress(iface);
            for (final String includeURL : getIncludeUrls()) {
                try {
                    final List<String> ips = IpListFromUrl.fetch(includeURL);
                    for (final String includeAddr : ips) {
                        if (new IPAddress(includeAddr).equals(addr)) {
                            return true;
                        }
                    }
                } catch (final IOException e) {
                    LOG.warn("Failed to process IPs from {}", includeURL, e);
                }
            }
        }
        return false;
    }

    /**
     * Flag for storing collected data by domain/ifAlias. Defaults to false.
     * Allowable values are true, false.
     */
    public String getStoreByIfAlias() {
        return m_storeByIfAlias;
    }

    public void setStoreByIfAlias(final String storeByIfAlias) {
        m_storeByIfAlias = storeByIfAlias;
    }

    /**
     * Flag for storing collected data by nodeid/interface name. Defaults to
     * normal. Allowable values are true, false, normal.
     */
    public String getStoreByNodeID() {
        return m_storeByNodeID;
    }

    public void setStoreByNodeID(final String storeByNodeID) {
        m_storeByNodeID = storeByNodeID;
    }

    /**
     * RFlag for controlling how interfaces are selected for data collection
     * by domain/ifAlias. If true, storage will occur for any interface on the
     * node found to have an valid ifAlias. Otherwise data will be stored only
     * if the interface is configured for data collection. Defaults to false.
     */
    public String getStorFlagOverride() {
        return m_storFlagOverride;
    }

    public void setStorFlagOverride(final String storFlagOverride) {
        m_storFlagOverride = storFlagOverride;
    }

    /**
     * The name of the domain covered by this collection package. Defaults to
     * package name.
     */
    public String getIfAliasDomain() {
        return m_ifAliasDomain;
    }

    public void setIfAliasDomain(final String ifAliasDomain) {
        m_ifAliasDomain = ifAliasDomain;
    }

    /**
     * A character or string for terminating ifAlias text. In effect, any text
     * beginning with this character or string becomes a comment and is not
     * considered part of the ifAlias when naming storage files and displaying
     * data. Defaults to null.
     */
    public String getIfAliasComment() {
        return m_ifAliasComment;
    }

    public void setIfAliasComment(final String ifAliasComment) {
        m_ifAliasComment = ifAliasComment;
    }

    public List<Service> getServices() {
        if (m_services == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_services);
        }
    }

    public void setServices(final List<Service> services) {
        m_services = new ArrayList<>(services);
    }

    public void addService(final Service service) throws IndexOutOfBoundsException {
        m_services.add(service);
    }

    public boolean removeService(final Service service) {
        return m_services.remove(service);
    }

    public Service getService(final String svcName) {
        final List<Service> pkgSvcs = getServices();

        for (final Service svc : pkgSvcs) {
            if (svc.getName().equalsIgnoreCase(svcName))
                return svc;
        }
        throw new IllegalArgumentException("Service name not part of package!");
    }

    /**
     * Returns true if the service is part of the package and the status of the
     * service is set to "on". Returns false if the service is not in the
     * package or it is but the status of the service is set to "off".
     *
     * @param svcName
     *            The service name to lookup.
     * @return a boolean.
     */
    public boolean serviceInPackageAndEnabled(final String svcName) {
        for (final Service service : getServices()) {
            if (service.getName().equalsIgnoreCase(svcName)) {
                // OK it's in the package. Now check the status of the service.
                return "on".equals(service.getStatus());
            }
        }
        return false;
    }

    public List<String> getOutageCalendars() {
        if (m_outageCalendar == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_outageCalendar);
        }
    }

    public void setOutageCalendars(final List<String> calendars) {
        m_outageCalendar = new ArrayList<>(calendars);
    }

    public void addOutageCalendar(final String calendar) throws IndexOutOfBoundsException {
        m_outageCalendar.add(calendar);
    }

    public boolean removeOutageCalendar(final String calendar) {
        return m_outageCalendar.remove(calendar);
    }

    @Override
    public int hashCode() {
        final int prime = 727;
        int result = 1;
        result = prime * result + ((m_excludeRanges == null) ? 0 : m_excludeRanges.hashCode());
        result = prime * result + ((m_filter == null) ? 0 : m_filter.hashCode());
        result = prime * result + ((m_ifAliasComment == null) ? 0 : m_ifAliasComment.hashCode());
        result = prime * result + ((m_ifAliasDomain == null) ? 0 : m_ifAliasDomain.hashCode());
        result = prime * result + ((m_includeRanges == null) ? 0 : m_includeRanges.hashCode());
        result = prime * result + ((m_includeUrls == null) ? 0 : m_includeUrls.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_outageCalendar == null) ? 0 : m_outageCalendar.hashCode());
        result = prime * result + ((m_services == null) ? 0 : m_services.hashCode());
        result = prime * result + ((m_specifics == null) ? 0 : m_specifics.hashCode());
        result = prime * result + ((m_storFlagOverride == null) ? 0 : m_storFlagOverride.hashCode());
        result = prime * result + ((m_storeByIfAlias == null) ? 0 : m_storeByIfAlias.hashCode());
        result = prime * result + ((m_storeByNodeID == null) ? 0 : m_storeByNodeID.hashCode());
        result = prime * result + Boolean.valueOf(m_remote).hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Package)) {
            return false;
        }
        final Package that = (Package) obj;
        return Objects.equals(this.m_excludeRanges, that.m_excludeRanges) &&
                Objects.equals(this.m_filter, that.m_filter) &&
                Objects.equals(this.m_ifAliasComment, that.m_ifAliasComment) &&
                Objects.equals(this.m_ifAliasDomain, that.m_ifAliasDomain) &&
                Objects.equals(this.m_includeRanges, that.m_includeRanges) &&
                Objects.equals(this.m_includeUrls, that.m_includeUrls) &&
                Objects.equals(this.m_name, that.m_name) &&
                Objects.equals(this.m_outageCalendar, that.m_outageCalendar) &&
                Objects.equals(this.m_services, that.m_services) &&
                Objects.equals(this.m_specifics, that.m_specifics) &&
                Objects.equals(this.m_storFlagOverride, that.m_storFlagOverride) &&
                Objects.equals(this.m_storeByIfAlias, that.m_storeByIfAlias) &&
                Objects.equals(this.m_storeByNodeID, that.m_storeByNodeID) &&
                Objects.equals(this.m_remote, that.m_remote);
    }

    public boolean isRemote() {
        return m_remote;
    }

    public void setRemote(boolean remote) {
        m_remote = remote;
    }

    @Override
    public String toString() {
        return "Package [name=" + m_name + ", filter=" + m_filter + ", specifics=" + m_specifics + ", includeRanges=" + m_includeRanges + ", excludeRanges=" + m_excludeRanges
                + ", includeUrls=" + m_includeUrls + ", storeByIfAlias=" + m_storeByIfAlias + ", storeByNodeID=" + m_storeByNodeID + ", ifAliasDomain=" + m_ifAliasDomain
                + ", storFlagOverride=" + m_storFlagOverride + ", ifAliasComment=" + m_ifAliasComment + ", services=" + m_services + ", outageCalendar=" + m_outageCalendar
                + ", remote=" + m_remote + "]";
    }

}
