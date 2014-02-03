package org.opennms.netmgt.config.collectd;

import static org.opennms.core.utils.InetAddressUtils.isInetAddressInRange;
import static org.opennms.core.utils.InetAddressUtils.toIpAddrBytes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.IncludeURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Package encapsulating addresses eligible to have SNMP
 *  data collected from them.
 */

@XmlRootElement(name="package")
@XmlAccessorType(XmlAccessType.FIELD)
public class Package implements Serializable {
    private static final long serialVersionUID = 7290975079346639791L;
    private static final Logger LOG = LoggerFactory.getLogger(Package.class);

    /**
     * The name or identifier for this package
     */
    @XmlAttribute(name="name")
    private String m_name;

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
    private List<String> m_specifics = new ArrayList<String>();

    /**
     * Range of addresses in this package
     */
    @XmlElement(name="include-range")
    private List<IncludeRange> m_includeRanges = new ArrayList<IncludeRange>();

    /**
     * Range of addresses to be excluded from this package
     */
    @XmlElement(name="exclude-range")
    private List<ExcludeRange> m_excludeRanges = new ArrayList<ExcludeRange>();

    /**
     * A file URL holding specific addresses to be polled. Each line in the
     * URL file can be one of: "<IP><space>#<comments>"; "<IP>";
     * "#<comments>"; Lines starting with a '#' are ignored and so are
     * characters after a '<space>#' in a line.
     */
    @XmlElement(name="include-url")
    private List<String> m_includeUrls = new ArrayList<String>();

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
    private List<Service> m_services = new ArrayList<Service>();

    /**
     * Scheduled outages - data collection is not performed during scheduled
     * outages
     */
    @XmlElement(name="outage-calendar")
    private List<String> m_outageCalendar = new ArrayList<String>();

    public Package() {
        super();
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
        return Collections.unmodifiableList(m_specifics);
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
        return Collections.unmodifiableList(m_includeRanges);
    }

    public void setIncludeRanges(final List<IncludeRange> ranges) {
        m_includeRanges = new ArrayList<IncludeRange>(ranges);
    }

    public void addIncludeRange(final IncludeRange range) throws IndexOutOfBoundsException {
        m_includeRanges.add(range);
    }

    public boolean removeIncludeRange(final IncludeRange range) {
        return m_includeRanges.remove(range);
    }

    public List<ExcludeRange> getExcludeRanges() {
        return new ArrayList<ExcludeRange>(m_excludeRanges);
    }

    public void setExcludeRanges(final List<ExcludeRange> ranges) {
        m_excludeRanges = new ArrayList<ExcludeRange>(ranges);
    }

    public void addExcludeRange(final ExcludeRange range) throws IndexOutOfBoundsException {
        m_excludeRanges.add(range);
    }

    public boolean removeExcludeRange(final ExcludeRange range) {
        return m_excludeRanges.remove(range);
    }

    public List<String> getIncludeUrls() {
        return Collections.unmodifiableList(m_includeUrls);
    }

    public void setIncludeUrlCollection(final List<String> urls) {
        m_includeUrls = new ArrayList<String>(urls);
    }

    public void addIncludeUrl(final String url) throws IndexOutOfBoundsException {
        m_includeUrls.add(url);
    }

    public boolean removeIncludeUrl(final String url) {
        return m_includeUrls.remove(url);
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
        return Collections.unmodifiableList(m_services);
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

    public List<String> getOutageCalendars() {
        return Collections.unmodifiableList(m_outageCalendar);
    }

    public void setOutageCalendars(final List<String> calendars) {
        m_outageCalendar = new ArrayList<String>(calendars);
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
        final Package other = (Package) obj;
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
        if (m_ifAliasComment == null) {
            if (other.m_ifAliasComment != null) {
                return false;
            }
        } else if (!m_ifAliasComment.equals(other.m_ifAliasComment)) {
            return false;
        }
        if (m_ifAliasDomain == null) {
            if (other.m_ifAliasDomain != null) {
                return false;
            }
        } else if (!m_ifAliasDomain.equals(other.m_ifAliasDomain)) {
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
        if (m_outageCalendar == null) {
            if (other.m_outageCalendar != null) {
                return false;
            }
        } else if (!m_outageCalendar.equals(other.m_outageCalendar)) {
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
        if (m_storFlagOverride == null) {
            if (other.m_storFlagOverride != null) {
                return false;
            }
        } else if (!m_storFlagOverride.equals(other.m_storFlagOverride)) {
            return false;
        }
        if (m_storeByIfAlias == null) {
            if (other.m_storeByIfAlias != null) {
                return false;
            }
        } else if (!m_storeByIfAlias.equals(other.m_storeByIfAlias)) {
            return false;
        }
        if (m_storeByNodeID == null) {
            if (other.m_storeByNodeID != null) {
                return false;
            }
        } else if (!m_storeByNodeID.equals(other.m_storeByNodeID)) {
            return false;
        }
        return true;
    }

    public boolean hasSpecific(byte[] addr) {
        for (final String espec : getSpecifics()) {
            if (new ByteArrayComparator().compare(toIpAddrBytes(espec), addr) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>hasIncludeRange</p>
     *
     * @param addr a long.
     * @return a boolean.
     */
    public boolean hasIncludeRange(String addr) {
        if (getIncludeRanges().size() == 0 && getSpecifics().size() == 0) {
            return true;
        }

        for (final IncludeRange rng : getIncludeRanges()) {
            if (isInetAddressInRange(addr, rng.getBegin(), rng.getEnd())) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>getService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.collectd.Service} object.
     */
    public Service getService(final String svcName) {
        final List<Service> pkgSvcs = getServices();
        
        for (Service svc : pkgSvcs) {
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
    public boolean serviceInPackageAndEnabled(String svcName) {
        for (final Service service : getServices()) {
            if (service.getName().equalsIgnoreCase(svcName)) {
                // OK it's in the package. Now check the
                // status of the service.
                String status = service.getStatus();
                if ("on".equals(status))
                    return true;
            }
        }
        return false;
    }

    public boolean hasExcludeRange(String addr) {
        for (ExcludeRange rng : getExcludeRanges()) {
            if (isInetAddressInRange(addr, rng.getBegin(), rng.getEnd())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSpecificUrl(String iface, boolean hasSpecific) {
        if (hasSpecific) {
            return true;
        } else {
            for (String includeURL : getIncludeUrls()) {
                IncludeURL url = new IncludeURL(includeURL);
                if (url.interfaceInUrl(iface)) {
                    return true;
                }
            }
        }
        return false;
    }

}
