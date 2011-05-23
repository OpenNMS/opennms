package org.opennms.netmgt.config.invd;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.opennms.core.utils.IpListFromUrl;

@XmlJavaTypeAdapter(InvdIncludeUrlAdapter.class)
public class InvdIncludeUrl implements Serializable, Comparable<InvdIncludeUrl> {
	private static final long serialVersionUID = -7804934429321236672L;
	private String m_includeUrl;
	private List<String> m_ipList;
	
	public InvdIncludeUrl(String include_url) {
		this.m_includeUrl = include_url;
		
		// Build the IP list.
		createIpList();
	}
	
	public static InvdIncludeUrl getInstance(String include_url) {
		return new InvdIncludeUrl(include_url);
	}

	@XmlTransient
	public String getIncludeUrl() {
		return m_includeUrl;
	}

	public void setIncludeUrl(String includeUrl) {
		m_includeUrl = includeUrl;
	}
	
	@XmlTransient
	public List<String> getIpList() {
		return m_ipList;
	}

	public void setIpList(List<String> ipList) {
		m_ipList = ipList;
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
		List<String> iplist = IpListFromUrl.parse(getIncludeUrl());
		if (iplist.size() > 0) {
			setIpList(iplist);
		}
	}
	
	public int compareTo(InvdIncludeUrl obj) {
        return new CompareToBuilder()
            .append(getIncludeUrl(), obj.getIncludeUrl())            
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InvdIncludeUrl) {
        	InvdIncludeUrl other = (InvdIncludeUrl) obj;
            return new EqualsBuilder()
            	.append(getIncludeUrl(), other.getIncludeUrl())
                .isEquals();
        }
        return false;
    }
}
