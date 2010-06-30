//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Apr 27: Added support for pathOutageEnabled
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.config;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.config.linkd.LinkdConfiguration;
import org.opennms.netmgt.linkd.DiscoveryLink;
import org.opennms.netmgt.linkd.SnmpCollection;


/**
 * <p>LinkdConfig interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public interface LinkdConfig {


    /**
     * Determine the list of IPs the filter rule for this package allows
     *
     * @param pkg a org$opennms$netmgt$config$linkd$Package object.
     * @return a {@link java.util.List} object.
     */
    public abstract List getIpList(Package pkg);
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
     * @param pkg
     *            The package to check for the inclusion of the interface.
     * @return True if the interface is included in the package, false
     *         otherwise.
     */
    public abstract boolean interfaceInPackage(String iface, org.opennms.netmgt.config.linkd.Package pkg);

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
     * @param pkg
     *            The package to check for the inclusion of the interface.
     * @return True if the interface is included in the package, false
     *         otherwise.
     */
    public abstract boolean interfaceInPackageRange(String iface, org.opennms.netmgt.config.linkd.Package pkg);

    /**
     * Returns the first package that the ip belongs to, null if none.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     *
     * @param ipaddr
     *            the interface to check
     * @return the first package that the ip belongs to, null if none
     */
    public abstract org.opennms.netmgt.config.linkd.Package getFirstPackageMatch(String ipaddr);

    /**
     * Returns true if the ip is part of atleast one package.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     *
     * @return true if the ip is part of atleast one package, false otherwise
     * @param ipAddr a {@link java.lang.String} object.
     */
    public abstract List getAllPackageMatches(String ipAddr);
    
    /**
     * <p>enumeratePackage</p>
     *
     * @return a {@link java.util.Enumeration} object.
     */
    public Enumeration enumeratePackage();
    
    /**
     * <p>getPackage</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @return a org$opennms$netmgt$config$linkd$Package object.
     */
    public Package getPackage(String pkgName);
    
    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    public int getThreads();
    
    /**
     * <p>enableDiscoveryDownload</p>
     *
     * @return a boolean.
     */
    public boolean enableDiscoveryDownload();
    
    /**
     * <p>useIpRouteDiscovery</p>
     *
     * @return a boolean.
     */
    public boolean useIpRouteDiscovery();
    
    /**
     * <p>saveRouteTable</p>
     *
     * @return a boolean.
     */
    public boolean saveRouteTable();
    
	/**
	 * <p>useCdpDiscovery</p>
	 *
	 * @return a boolean.
	 */
	public boolean useCdpDiscovery();

	/**
	 * <p>useBridgeDiscovery</p>
	 *
	 * @return a boolean.
	 */
	public boolean useBridgeDiscovery();
	
	/**
	 * <p>saveStpNodeTable</p>
	 *
	 * @return a boolean.
	 */
	public boolean saveStpNodeTable();
	
	/**
	 * <p>saveStpInterfaceTable</p>
	 *
	 * @return a boolean.
	 */
	public boolean saveStpInterfaceTable();
	
	/**
	 * <p>getInitialSleepTime</p>
	 *
	 * @return a long.
	 */
	public long getInitialSleepTime();
	
	/**
	 * <p>getSnmpPollInterval</p>
	 *
	 * @return a long.
	 */
	public long getSnmpPollInterval();
	
	/**
	 * <p>getDiscoveryLinkInterval</p>
	 *
	 * @return a long.
	 */
	public long getDiscoveryLinkInterval();
	
	/**
	 * <p>autoDiscovery</p>
	 *
	 * @return a boolean.
	 */
	public boolean autoDiscovery();
	
	/**
	 * <p>enableVlanDiscovery</p>
	 *
	 * @return a boolean.
	 */
	public boolean enableVlanDiscovery();
		
	/**
	 * <p>update</p>
	 *
	 * @throws java.io.IOException if any.
	 * @throws org.exolab.castor.xml.MarshalException if any.
	 * @throws org.exolab.castor.xml.ValidationException if any.
	 */
	public void update() throws IOException, MarshalException, ValidationException;
    
    /**
     * <p>save</p>
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void save() throws MarshalException, IOException, ValidationException;
    
    /**
     * <p>getConfiguration</p>
     *
     * @return a {@link org.opennms.netmgt.config.linkd.LinkdConfiguration} object.
     */
    public abstract LinkdConfiguration getConfiguration();
    
    /**
     * <p>getSnmpCollections</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     * @param sysoid a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public abstract List<SnmpCollection> getSnmpCollections(String ipaddr, String sysoid);

    /**
     * <p>getSnmpCollection</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     * @param sysoid a {@link java.lang.String} object.
     * @param pkgName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.linkd.SnmpCollection} object.
     */
    public abstract SnmpCollection getSnmpCollection(String ipaddr, String sysoid,String pkgName);

    /**
     * <p>getDiscoveryLink</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.linkd.DiscoveryLink} object.
     */
    public abstract DiscoveryLink getDiscoveryLink(String pkgName);
    
    /**
     * <p>createPackageIpListMap</p>
     */
    public void createPackageIpListMap();
    
    /**
     * <p>getClassName</p>
     *
     * @param sysoid a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public abstract String getClassName(String sysoid);
    
	/**
	 * <p>hasClassName</p>
	 *
	 * @param sysoid a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean hasClassName(String sysoid);
    
}
