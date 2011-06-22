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

import java.util.List;

import org.opennms.core.utils.IpListFromUrl;

/**
 * <p>IncludeURL class.</p>
 */
public class IncludeURL {
	
	private String m_urlName;
	private List<String> m_ipList;

	/**
	 * <p>Constructor for IncludeURL.</p>
	 *
	 * @param urlName a {@link java.lang.String} object.
	 */
	public IncludeURL(String urlName) {
		m_urlName = urlName;
		createIpList();
	}
	
	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return m_urlName;
	}

	/**
	 * <p>setIpList</p>
	 *
	 * @param ipList a {@link java.util.List} object.
	 */
	public void setIpList(List<String> ipList) {
		m_ipList = ipList;
	}
	
	/**
	 * <p>getIpList</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<String> getIpList() {
		return m_ipList;
	}

	/**
	 * This method is used to determine if the named interface is included in
	 * the passed package's url includes. If the interface is found in any of
	 * the URL files, then a value of true is returned, else a false value is
	 * returned.
	 * 
	 * <pre>
	 *  The file URL is read and each entry in this file checked. Each line
	 *   in the URL file can be one of -
	 *   &lt;IP&gt;&lt;space&gt;#&lt;comments&gt;
	 *   or
	 *   &lt;IP&gt;
	 *   or
	 *   #&lt;comments&gt;
	 *  
	 *   Lines starting with a '#' are ignored and so are characters after
	 *   a '&lt;space&gt;#' in a line.
	 * </pre>
	 * 
	 * @param url
	 *            The url file to read
	 * @param addr
	 *            The interface to test against the package's URL
	 * @return True if the interface is included in the url, false otherwise.
	 */
	boolean interfaceInUrl(String addr) {
		
		boolean bRet = false;
	
		// get list of IPs in this URL
		List<String> iplist = getIpList();
		if (iplist != null && iplist.size() > 0) {
			bRet = iplist.contains(addr);
		}
	
		return bRet;
	}

	void createIpList() {
		List<String> iplist = IpListFromUrl.parse(getName());
		if (iplist.size() > 0) {
			setIpList(iplist);
		}
	}

}
