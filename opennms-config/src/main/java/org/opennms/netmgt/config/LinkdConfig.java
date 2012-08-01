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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.linkd.LinkdConfiguration;
import org.opennms.netmgt.config.linkd.Package;


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
     * @param pkg a {@link org.opennms.netmgt.config.linkd.Package} object.
     * @return a {@link java.util.List} object.
     */
    List<InetAddress> getIpList(Package pkg);
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
    boolean isInterfaceInPackage(InetAddress iface, org.opennms.netmgt.config.linkd.Package pkg);

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
    boolean isInterfaceInPackageRange(InetAddress iface, org.opennms.netmgt.config.linkd.Package pkg);

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
    org.opennms.netmgt.config.linkd.Package getFirstPackageMatch(InetAddress ipaddr);

    /**
     * Returns true if the IP is part of at least one package.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     *
     * @return true if the IP is part of at least one package, false otherwise
     * @param ipAddr a {@link java.lang.String} object.
     */
    List<String> getAllPackageMatches(InetAddress ipAddr);
    
    boolean isAutoDiscoveryEnabled();

    boolean isVlanDiscoveryEnabled();

    /**
     * <p>enumeratePackage</p>
     *
     * @return a {@link java.util.Enumeration} object.
     */
    Enumeration<Package> enumeratePackage();
    
    /**
     * <p>getPackage</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.linkd.Package} object.
     */
    Package getPackage(String pkgName);

    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    int getThreads();

    /**
     * <p>enableDiscoveryDownload</p>
     *
     * @return a boolean.
     */
    boolean enableDiscoveryDownload();

    /**
     * <p>useIpRouteDiscovery</p>
     *
     * @return a boolean.
     */
    boolean useIpRouteDiscovery();

    boolean forceIpRouteDiscoveryOnEthernet();

    /**
     * <p>saveRouteTable</p>
     *
     * @return a boolean.
     */
    boolean saveRouteTable();

    /**
     * <p>useCdpDiscovery</p>
     *
     * @return a boolean.
     */
    boolean useCdpDiscovery();

    /**
     * <p>useBridgeDiscovery</p>
     *
     * @return a boolean.
     */
    boolean useBridgeDiscovery();

    /**
     * <p>saveStpNodeTable</p>
     *
     * @return a boolean.
     */
    boolean saveStpNodeTable();
    /**
     * <p>saveStpInterfaceTable</p>
     *
     * @return a boolean.
     */
    boolean saveStpInterfaceTable();

    /**
     * <p>getInitialSleepTime</p>
     *
     * @return a long.
     */
    long getInitialSleepTime();

    /**
     * <p>getSnmpPollInterval</p>
     *
     * @return a long.
     */
    long getSnmpPollInterval();

    /**
     * <p>getDiscoveryLinkInterval</p>
     *
     * @return a long.
     */
    long getDiscoveryLinkInterval();

    /**
     * <p>reload</p>
     * <p>Reload the configuration file<p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    void reload() throws IOException, MarshalException, ValidationException;
    
    /**
     * <p>save</p>
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    void save() throws MarshalException, IOException, ValidationException;

    /**
     * <p>update</p>
     * <p>update the global helper objects<p>
     * <p>this calls the update of the following maps<p>
     * <p>packageIpListMap<p>
     * <p>urlIpMap<p>
     * <p>IpRouteClassNameMap<p>
     * <p>VlanClassNameMap<p>
     * 
     */
    void update();

    /**
     * <p>getConfiguration</p>
     *
     * @return a {@link org.opennms.netmgt.config.linkd.LinkdConfiguration} object.
     */
    LinkdConfiguration getConfiguration();    
    
    /**
     * <p>getVlanClassName</p>
     *
     * @param sysoid a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getVlanClassName(String sysoid);
    
	/**
	 * <p>hasClassName</p>
	 *
	 * @param sysoid a {@link java.lang.String} object.
	 * @return a boolean.
	 */
    boolean hasClassName(String sysoid);
    
    Lock getReadLock();

    Lock getWriteLock();

    boolean hasIpRouteClassName(String sysoid);

    String getIpRouteClassName(String sysoid);

    String getDefaultIpRouteClassName();
}
