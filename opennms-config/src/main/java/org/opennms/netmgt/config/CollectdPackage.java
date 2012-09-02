/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.core.utils.InetAddressUtils.isInetAddressInRange;
import static org.opennms.core.utils.InetAddressUtils.toIpAddrBytes;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.collectd.ExcludeRange;
import org.opennms.netmgt.config.collectd.IncludeRange;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.filter.FilterDaoFactory;
public class CollectdPackage {
	private Package m_pkg;
	private List<InetAddress> m_ipList;
	private List<IncludeURL> m_includeURLs;
	
	
	/**
	 * <p>Constructor for CollectdPackage.</p>
	 *
	 * @param pkg a {@link org.opennms.netmgt.config.collectd.Package} object.
	 * @param localServer a {@link java.lang.String} object.
	 * @param verifyServer a boolean.
	 */
	public CollectdPackage(Package pkg, String localServer, boolean verifyServer) {
		m_pkg = pkg;
		
		m_includeURLs = new LinkedList<IncludeURL>();
		
		createIpList(localServer, verifyServer);
		
		createIncludeURLs(pkg);
		
	}

	private void createIncludeURLs(Package pkg) {
		Enumeration<String> urlEnum = pkg.enumerateIncludeUrl();
		while (urlEnum.hasMoreElements()) {
			m_includeURLs.add(new IncludeURL(urlEnum.nextElement()));
		}
	}
	
	/**
	 * <p>getPackage</p>
	 *
	 * @return a {@link org.opennms.netmgt.config.collectd.Package} object.
	 */
	public Package getPackage() {
		return m_pkg;
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
		Package pkg = getPackage();
		boolean result = false;
	
		Enumeration<Service> esvcs = pkg.enumerateService();
		while (result == false && esvcs.hasMoreElements()) {
			Service tsvc = esvcs.nextElement();
			if (tsvc.getName().equalsIgnoreCase(svcName)) {
				// Ok its in the package. Now check the
				// status of the service
				String status = tsvc.getStatus();
				if (status.equals("on"))
					result = true;
			}
		}
		return result;
	}

	protected boolean hasSpecific(byte[] addr) {
	    for (String espec : getPackage().getSpecific()) {
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
	protected boolean hasIncludeRange(String addr) {
		Package pkg = getPackage();
		if (pkg.getIncludeRangeCount() == 0 && pkg.getSpecificCount() == 0) {
		    return true;
		}

		for (IncludeRange rng : pkg.getIncludeRange()) {
		    if (isInetAddressInRange(addr, rng.getBegin(), rng.getEnd())) {
		        return true;
		    }
		}
		return false;
	}

	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return m_pkg.getName();
	}

	protected boolean hasExcludeRange(String addr, boolean has_specific) {
	    for (ExcludeRange rng : getPackage().getExcludeRange()) {
	        if (isInetAddressInRange(addr, rng.getBegin(), rng.getEnd())) {
	            return true;
	        }
	    }
	    return false;
	}

	/**
	 * <p>putIpList</p>
	 *
	 * @param ipList a {@link java.util.List} object.
	 */
	public void putIpList(List<InetAddress> ipList) {
		m_ipList = ipList;
	}

	/**
	 * <p>getIpList</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	protected List<InetAddress> getIpList() {
		return m_ipList;
	}

	protected boolean interfaceInFilter(String iface) {
		if (iface == null) return false;
		final InetAddress ifaceAddress = addr(iface);

		boolean filterPassed = false;
	
		// get list of IPs in this package
		List<InetAddress> ipList = getIpList();
		if (ipList != null && ipList.size() > 0) {
			filterPassed = ipList.contains(ifaceAddress);
		} else {
			log().debug("interfaceInFilter: ipList contains no data");
		}
	
		if (!filterPassed)
			log().debug("interfaceInFilter: Interface " + iface
					+ " passed filter for package " + getName()
					+ "?: false");
		return filterPassed;
	}

	protected ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}

	/**
	 * <p>getIncludeURLs</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<IncludeURL> getIncludeURLs() {
		return m_includeURLs;
	}

	protected boolean hasSpecificUrl(String iface, boolean has_specific) {
		for (Iterator<IncludeURL> it = getIncludeURLs().iterator(); it.hasNext() && !has_specific;) {
			IncludeURL includeURL = it.next();
			has_specific = includeURL.interfaceInUrl(iface);
		}
		return has_specific;
	}

	/**
	 * This method is used to determine if the named interface is included in
	 * the passed package definition. If the interface belongs to the package
	 * then a value of true is returned. If the interface does not belong to the
	 * package a false value is returned.
	 *
	 * <strong>Note: </strong>Evaluation of the interface against a package
	 * filter will only work if the IP is already in the database.
	 *
	 * @param iface
	 *            The interface to test against the package.
	 * @return True if the interface is included in the package, false
	 *         otherwise.
	 */
	public boolean interfaceInPackage(final String iface) {
		boolean filterPassed = interfaceInFilter(iface);
	
		if (!filterPassed)
			return false;
	
		//
		// Ensure that the interface is in the specific list or
		// that it is in the include range and is not excluded
		//
	
		byte[] addr = toIpAddrBytes(iface);
	
		boolean has_range_include = hasIncludeRange(iface);
		boolean has_specific = hasSpecific(addr);
	
		has_specific = hasSpecificUrl(iface, has_specific);
		boolean has_range_exclude = hasExcludeRange(iface, has_specific);
	
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

	String getFilterRule(String localServer, boolean verifyServer) {
		Package pkg = getPackage();
		String filter = pkg.getFilter().getContent();
		StringBuffer filterRules = new StringBuffer(filter);
	
		if (verifyServer) {
			filterRules.append(" & (serverName == ");
			filterRules.append('\"');
			filterRules.append(localServer);
			filterRules.append('\"');
			filterRules.append(")");
		}
		return filterRules.toString();
	}

	void createIpList(final String localServer, final boolean verifyServer) {
		Package pkg = getPackage();
		//
		// Get a list of ipaddress per package agaist the filter rules from
		// database and populate the package, IP list map.
		//
		String filterRules = getFilterRule(localServer, verifyServer);
	
		if (log().isDebugEnabled())
			log().debug("createPackageIpMap: package is " + pkg.getName()
					+ ". filer rules are  " + filterRules);
		try {
            putIpList(FilterDaoFactory.getInstance().getActiveIPAddressList(filterRules));
		} catch (Throwable t) {
		    LogUtils.errorf(this, t, "createPackageIpMap: failed to map package: %s to an IP List with filter \"%s\"", pkg.getName(), pkg.getFilter().getContent());
		}
	}

	/**
	 * <p>getService</p>
	 *
	 * @param svcName a {@link java.lang.String} object.
	 * @return a {@link org.opennms.netmgt.config.collectd.Service} object.
	 */
	public Service getService(final String svcName) {
        final List<Service> pkgSvcs = m_pkg.getServiceCollection();
        
        for (Service svc : pkgSvcs) {
            if (svc.getName().equalsIgnoreCase(svcName))
                return svc;
        }
		throw new RuntimeException("Service name not part of package!");
	}

	/**
	 * <p>storeByIfAlias</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String storeByIfAlias() {
		return getPackage().getStoreByIfAlias();
	}

	/**
	 * <p>ifAliasComment</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String ifAliasComment() {
		return getPackage().getIfAliasComment();
	}

	/**
	 * <p>getStorFlagOverride</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getStorFlagOverride() {
		return getPackage().getStorFlagOverride();
	}

	/**
	 * <p>ifAliasDomain</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String ifAliasDomain() {
		return getPackage().getIfAliasDomain();
	}

	/**
	 * <p>storeByNodeId</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String storeByNodeId() {
		return getPackage().getStoreByNodeID();
	}
	
}
