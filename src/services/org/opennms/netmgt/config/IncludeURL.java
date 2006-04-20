package org.opennms.netmgt.config;

import java.util.List;

public class IncludeURL {
	
	private String m_urlName;
	private List m_ipList;

	public IncludeURL(String urlName) {
		m_urlName = urlName;
	}
	
	public String getName() {
		return m_urlName;
	}

	public void setIpList(List ipList) {
		m_ipList = ipList;
	}
	
	public List getIpList() {
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
		List iplist = getIpList();
		if (iplist != null && iplist.size() > 0) {
			bRet = iplist.contains(addr);
		}
	
		return bRet;
	}

}
