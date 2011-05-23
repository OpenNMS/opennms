package org.opennms.netmgt.config.invd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.opennms.core.utils.IPSorter;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.filter.FilterDaoFactory;

@XmlRootElement(name="package")
public class InvdPackage implements Serializable, Comparable<InvdPackage> {
	private static final long serialVersionUID = -8960663103686221278L;
	
	private static final InvdIncludeRange[] OF_INCLUDE_RANGES = new InvdIncludeRange[0];
	private static final InvdService[] OF_SERVICES = new InvdService[0];
	private static final InvdIncludeUrl[] OF_INCLUDE_URLS = new InvdIncludeUrl[0];
	private static final String[] OF_STRINGS = new String[0];
	
	@XmlAttribute(name="name",required=true)
	private String m_name;
	
	@XmlElement(name="filter",required=true)
	private String m_filter;
	
	@XmlElement(name="specific")
	private List<String> m_specifics = new ArrayList<String>();
	
	@XmlElement(name="include-range")
	private List<InvdIncludeRange> m_includeRanges = new ArrayList<InvdIncludeRange>();
	
	@XmlElement(name="exclude-range")
	private List<InvdIncludeRange> m_excludeRanges = new ArrayList<InvdIncludeRange>();
	
	@XmlElement(name="include-url")
	private List<InvdIncludeUrl> m_includeUrls = new ArrayList<InvdIncludeUrl>();
	
	@XmlElement(name="service")
	private List<InvdService> m_services = new ArrayList<InvdService>();
	
	@XmlElement(name="outage-calendar")
	private List<String> m_outageCalendars = new ArrayList<String>();
	
	@XmlTransient
	private List<String> m_ipList;

	@XmlTransient
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	@XmlTransient
	public String getFilter() {
		return m_filter;
	}

	public void setFilter(String filter) {
		m_filter = filter;
	}

	@XmlTransient
	public List<String> getSpecifics() {
		return m_specifics;
	}

	public void setSpecifics(List<String> specifics) {
		m_specifics = specifics;
	}

	public void addSpecific(String specific) {
		m_specifics.add(specific);
	}
	@XmlTransient
	public List<InvdIncludeRange> getIncludeRanges() {
		return m_includeRanges;
	}

	public void setIncludeRanges(List<InvdIncludeRange> includeRanges) {
		m_includeRanges = includeRanges;
	}
	
	public void addIncludeRange(InvdIncludeRange range) {
		m_includeRanges.add(range);
	}

	@XmlTransient
	public List<InvdIncludeRange> getExcludeRanges() {
		return m_excludeRanges;
	}

	public void setExcludeRanges(List<InvdIncludeRange> excludeRanges) {
		m_excludeRanges = excludeRanges;
	}
	
	public void addExcludeRange(InvdIncludeRange range) {
		m_excludeRanges.add(range);
	}

	@XmlTransient
	public List<InvdIncludeUrl> getIncludeUrls() {
		return m_includeUrls;
	}

	public void setIncludeUrls(List<InvdIncludeUrl> includeUrls) {
		m_includeUrls = includeUrls;
	}
	
	public void addIncludeUrl(InvdIncludeUrl includeUrl) {
		m_includeUrls.add(includeUrl);
	}
	
	public void addIncludeUrl(String includeUrl) {
		m_includeUrls.add(new InvdIncludeUrl(includeUrl));
	}

	@XmlTransient
	public List<String> getOutageCalendars() {
		return m_outageCalendars;
	}

	public void setOutageCalendars(List<String> outageCalendars) {
		m_outageCalendars = outageCalendars;
	}
	
	public void addOutageCalendar(String outageCalendar) {
		m_outageCalendars.add(outageCalendar);
	}

	@XmlTransient
	public List<InvdService> getServices() {
		return m_services;
	}

	public void setServices(List<InvdService> services) {
		m_services = services;
	}	
	
	public InvdService getServiceByName(String name) {
		for(InvdService svc : getServices()) {
			if(svc.getName().equals(name))
				return svc;
		}
		return null;
	}
	
	public void addService(InvdService service) {
		m_services.add(service);
	}
	
	@XmlTransient
	public List<String> getIpList() {
		return m_ipList;
	}

	public void setIpList(List<String> ipList) {
		m_ipList = ipList;
	}

	//// Utility methods.
	private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
	
	// TODO this needs to be called before packages can be used.
	public void createIpList() {
		//
		// Get a list of IP Address per package against the filter rules from
		// database and populate the package, IP list map.
		//
		String filterRules = getFilter();

		if (log().isDebugEnabled())
			log().debug("createPackageIpMap: package is " + getName()
					+ ". filer rules are  " + filterRules);
		try {
            List<String> ipList = FilterDaoFactory.getInstance().getIPList(filterRules);
			if (ipList.size() > 0) {
				setIpList(ipList);
			}
		} catch (Throwable t) {
		    log().error("createPackageIpMap: failed to map package: "
		            + getName() + " to an IP List: " + t, t);
		}
	}
	
	boolean hasSpecific(long addr) {
		boolean has_specific = false;
		for(String espec : getSpecifics()) {
			long speca = IPSorter.convertToLong(espec);
			if(speca == addr)
				has_specific = true;
		}
		return has_specific;
	}
	
	public boolean hasIncludeRange(long addr) {
		boolean has_range_include = getIncludeRanges().size() == 0 && getSpecifics().size() == 0;

		for(InvdIncludeRange rng : getIncludeRanges()) {		
			long start = IPSorter.convertToLong(rng.getBegin());
			if (addr > start) {
				long end = IPSorter.convertToLong(rng.getEnd());
				if (addr <= end) {
					has_range_include = true;
				}
			} else if (addr == start) {
				has_range_include = true;
			}
		}
		return has_range_include;
	}
	
	boolean hasExcludeRange(long addr, boolean has_specific) {
		boolean has_range_exclude = false;
		for(InvdIncludeRange rng : getExcludeRanges()) {
			long start = IPSorter.convertToLong(rng.getBegin());
			if (addr > start) {
				long end = IPSorter.convertToLong(rng.getEnd());
				if (addr <= end) {
					has_range_exclude = true;
				}
			} else if (addr == start) {
				has_range_exclude = true;
			}
		}
		return has_range_exclude;
	}

	/**
	 * This method is used to determine if the named interface is included in
	 * the passed package definition. If the interface belongs to the package
	 * then a value of true is returned. If the interface does not belong to the
	 * package a false value is returned.
	 *
	 * <strong>Note: </strong>Evaluation of the interface against a package
	 * filter will only work if the IP is already in the database.
	 * @param iface
	 *            The interface to test against the package.
	 *
	 * @return True if the interface is included in the package, false
	 *         otherwise.
	 */
	public boolean interfaceInPackage(String iface) {
		boolean filterPassed = interfaceInFilter(iface);

		if (!filterPassed)
			return false;

		//
		// Ensure that the interface is in the specific list or
		// that it is in the include range and is not excluded
		//

		long addr = IPSorter.convertToLong(iface);

		boolean has_range_include = hasIncludeRange(addr);
		boolean has_specific = hasSpecific(addr);

		has_specific = hasSpecificUrl(iface, has_specific);
		boolean has_range_exclude = hasExcludeRange(addr, has_specific);

		boolean packagePassed = has_specific
				|| (has_range_include && !has_range_exclude);
                if(packagePassed) {
		    log().info("interfaceInPackage: Interface " + iface
				+ " passed filter and specific/range for package "
				+ getName() + "?: " + packagePassed);
                } else {
		    log().debug("interfaceInPackage: Interface " + iface
				+ " passed filter and specific/range for package "
				+ getName() + "?: " + packagePassed);
                }
		return packagePassed;
	}
	
	/**
	 * Returns true if the service is part of the package and the status of the
	 * service is set to "on". Returns false if the service is not in the
	 * package or it is but the status of the service is set to "off".
	 *
	 * @param svcName
	 *            The service name to lookup.
     * @return Returns true if the service is part of the package and the status is
     *         set to 'on.'
	 */
	public boolean serviceInPackageAndEnabled(String svcName) {
		for(InvdService tsvc : getServices()) {			
			if(tsvc.getName().equalsIgnoreCase(svcName)) {
				String status = tsvc.getStatus();
				if(status.equals("on"))
					return true;
			}
		}		
		return false;
	}
	
	boolean interfaceInFilter(String iface) {
		boolean filterPassed = false;

		// get list of IPs in this package
		List<String> ipList = getIpList();
		if (ipList != null && ipList.size() > 0) {
			filterPassed = ipList.contains(iface);
		} else {
			log().debug("interfaceInFilter: ipList contains no data");
		}

		if (!filterPassed)
			log().debug("interfaceInFilter: Interface " + iface
					+ " passed filter for package " + getName()
					+ "?: false");
		return filterPassed;
	}
	
	boolean hasSpecificUrl(String iface, boolean has_specific) {
		for(InvdIncludeUrl url : getIncludeUrls()) {
			if(has_specific) break;
			has_specific = url.interfaceInUrl(iface);
		}
		
		return has_specific;
	}
    
	public int compareTo(InvdPackage obj) {
        return new CompareToBuilder()
            .append(getName(), obj.getName())
            .append(getFilter(), obj.getFilter())
            .append(getSpecifics().toArray(OF_STRINGS), obj.getSpecifics().toArray(OF_STRINGS))
            .append(getIncludeRanges().toArray(OF_INCLUDE_RANGES), obj.getIncludeRanges().toArray(OF_INCLUDE_RANGES))
            .append(getExcludeRanges().toArray(OF_INCLUDE_RANGES), obj.getExcludeRanges().toArray(OF_INCLUDE_RANGES))
            .append(getIncludeUrls().toArray(OF_INCLUDE_URLS), obj.getIncludeUrls().toArray(OF_INCLUDE_URLS))
            .append(getOutageCalendars().toArray(OF_STRINGS), obj.getOutageCalendars().toArray(OF_STRINGS))
            .append(getServices().toArray(OF_SERVICES), obj.getServices().toArray(OF_SERVICES))
            .toComparison();
    }
	
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InvdPackage) {
        	InvdPackage other = (InvdPackage) obj;
            return new EqualsBuilder()
            	.append(getName(), other.getName())
            	.append(getFilter(), other.getFilter())
            	.append(getSpecifics().toArray(OF_STRINGS), other.getSpecifics().toArray(OF_STRINGS))
            	.append(getIncludeRanges().toArray(OF_INCLUDE_RANGES), other.getIncludeRanges().toArray(OF_INCLUDE_RANGES))
            	.append(getExcludeRanges().toArray(OF_INCLUDE_RANGES), other.getExcludeRanges().toArray(OF_INCLUDE_RANGES))
            	.append(getIncludeUrls().toArray(OF_INCLUDE_URLS), other.getIncludeUrls().toArray(OF_INCLUDE_URLS))
            	.append(getOutageCalendars().toArray(OF_STRINGS), other.getOutageCalendars().toArray(OF_STRINGS))
            	.append(getServices().toArray(OF_SERVICES), other.getServices().toArray(OF_SERVICES))
                .isEquals();
        }
        return false;
    }
}
