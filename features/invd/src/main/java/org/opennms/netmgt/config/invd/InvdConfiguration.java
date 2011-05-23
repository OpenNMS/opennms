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
import org.opennms.netmgt.config.invd.InvdPackage;

@XmlRootElement(name="invd-configuration")
public class InvdConfiguration implements Serializable, Comparable<InvdConfiguration> {
	private static final long serialVersionUID = -826647119091179797L;

	private static final InvdPackage[] OF_PACKAGES = new InvdPackage[0];
	private static final InvdScanner[] OF_SCANNERS = new InvdScanner[0];
	
	@XmlAttribute(name="threads",required=true)
	private Integer m_threadCount;
	
	@XmlElement(name="package")
	private List<InvdPackage> m_packages = new ArrayList<InvdPackage>();
	
	@XmlElement(name="scanner")
	private List<InvdScanner> m_scanners = new ArrayList<InvdScanner>();

	//// Bean methods
	
	@XmlTransient
	public Integer getThreadCount() {
		return m_threadCount;
	}

	public void setThreadCount(Integer threadCount) {
		m_threadCount = threadCount;
	}

	@XmlTransient
	public List<InvdPackage> getPackages() {
		return m_packages;
	}

	public void setPackages(List<InvdPackage> packages) {
		m_packages = packages;
	}
	
	public void addPackage(InvdPackage pkg) {
		m_packages.add(pkg);
	}

	@XmlTransient
	public List<InvdScanner> getScanners() {
		return m_scanners;
	}

	public void setScanners(List<InvdScanner> scanners) {
		m_scanners = scanners;
	}
	
	public void addScanner(InvdScanner scanner) {
		m_scanners.add(scanner);
	}
	
	//// Utility methods.
	
	public InvdPackage getPackage(String name) {
        for(InvdPackage wpkg : getPackages()) {
        	if(wpkg.getName().equals(name)) {
        		return wpkg;
        	}
        }
        return null;
    }
	
	public void createIpLists() {
		for(InvdPackage pkg : getPackages()) {
			pkg.createIpList();
		}
	}
	
	/**
     * Returns true if the specified interface is included by at least one
     * package which has the specified service and that service is enabled (set
     * to "on").
     *
     * @param ipAddr
     *            IP address of the interface to lookup
     * @param svcName
     *            The service name to lookup
     * @return true if Invd config contains a package which includes the
     *         specified interface and has the specified service enabled.
     */
    public boolean isServiceCollectionEnabled(String ipAddr, String svcName) {
    	for(InvdPackage pkg : getPackages()) {
    		if(pkg.interfaceInPackage(ipAddr)) {
    			if(pkg.serviceInPackageAndEnabled(svcName)) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    /**
     * This method is used to establish package agaist iplist mapping, with
     * which, the iplist is selected per package via the configured filter rules
     * from the database.
     */
    public void createPackageIpListMap() {
        // Multiple threads maybe asking for the m_pkgIpMap field so create
        // with temp map then assign when finished.
    	for(InvdPackage pkg : getPackages()) {
    		pkg.createIpList();
    	}
    }
	
	public int compareTo(InvdConfiguration obj) {
        return new CompareToBuilder()
            .append(getThreadCount(), obj.getThreadCount())
            .append(getPackages().toArray(OF_PACKAGES), obj.getPackages().toArray(OF_PACKAGES))
            .append(getScanners().toArray(OF_SCANNERS), obj.getScanners().toArray(OF_SCANNERS))
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InvdConfiguration) {
        	InvdConfiguration other = (InvdConfiguration) obj;
            return new EqualsBuilder()
            	.append(getThreadCount(), other.getThreadCount())
            	.append(getPackages().toArray(OF_PACKAGES), other.getPackages().toArray(OF_PACKAGES))
            	.append(getScanners().toArray(OF_SCANNERS), other.getScanners().toArray(OF_SCANNERS))
                .isEquals();
        }
        return false;
    }
}
