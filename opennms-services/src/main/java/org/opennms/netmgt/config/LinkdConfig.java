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
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public interface LinkdConfig {


    /**
     * Determine the list of IPs the filter rule for this package allows
     * @param pkg
     * @return
     */
    List<String> getIpList(Package pkg);
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
     * 
     * @return True if the interface is included in the package, false
     *         otherwise.
     */
    boolean interfaceInPackage(String iface, org.opennms.netmgt.config.linkd.Package pkg);

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
     * 
     * @return True if the interface is included in the package, false
     *         otherwise.
     */
    boolean interfaceInPackageRange(String iface, org.opennms.netmgt.config.linkd.Package pkg);

    /**
     * Returns the first package that the ip belongs to, null if none.
     * 
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     * 
     * @param ipaddr
     *            the interface to check
     * 
     * @return the first package that the ip belongs to, null if none
     */
    org.opennms.netmgt.config.linkd.Package getFirstPackageMatch(String ipaddr);

    /**
     * Returns true if the ip is part of atleast one package.
     * 
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is alrady in the database.
     * 
     * @param ipaddr
     *            the interface to check
     * 
     * @return true if the ip is part of atleast one package, false otherwise
     */
    List<String> getAllPackageMatches(String ipAddr);
    
    Enumeration<Package> enumeratePackage();
    
    Package getPackage(String pkgName);
    
    int getThreads();
    
    boolean enableDiscoveryDownload();
    
    boolean useIpRouteDiscovery();
    
    boolean saveRouteTable();
    
	boolean useCdpDiscovery();

	boolean useBridgeDiscovery();
	
	boolean saveStpNodeTable();
	
	boolean saveStpInterfaceTable();
	
	long getInitialSleepTime();
	
	long getSnmpPollInterval();
	
	long getDiscoveryLinkInterval();
	
	boolean autoDiscovery();
	
	boolean enableVlanDiscovery();
		
	void update() throws IOException, MarshalException, ValidationException;
    
    void save() throws MarshalException, IOException, ValidationException;
    
    LinkdConfiguration getConfiguration();
    
    List<SnmpCollection> getSnmpCollections(String ipaddr, String sysoid);

    SnmpCollection getSnmpCollection(String ipaddr, String sysoid,String pkgName);

    DiscoveryLink getDiscoveryLink(String pkgName);
    
    void createPackageIpListMap();
    
    String getClassName(String sysoid);
    
	boolean hasClassName(String sysoid);
    
}
