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

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Package;
public class CollectdConfig {
    private CollectdConfiguration m_config;
    private Collection<CollectdPackage> m_packages;
    private String m_localServer;
    private boolean m_verifyServer;

    /**
     * Convenience object for CollectdConfiguration.
     *
     * @param config collectd configuration object
     * @param localServer local server name from opennms-server.xml
     * @param verifyServer verify server option from opennms-server.xml
     */
    protected CollectdConfig(CollectdConfiguration config, String localServer, boolean verifyServer) {
        m_config = config;
        m_localServer = localServer;
        m_verifyServer = verifyServer;

//      instantiateCollectors();

        createPackageObjects(localServer, verifyServer);

        initialize(localServer, verifyServer);

    }

    private void createPackageObjects(String localServer, boolean verifyServer) {
        m_packages = new LinkedList<CollectdPackage>();
        Enumeration<Package> pkgEnum = m_config.enumeratePackage();
        while (pkgEnum.hasMoreElements()) {
            Package pkg = pkgEnum.nextElement();
            m_packages.add(new CollectdPackage(pkg, localServer, verifyServer));
        }
    }

    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.collectd.CollectdConfiguration} object.
     */
    public CollectdConfiguration getConfig() {
        return m_config;
    }

    /**
     * <p>getPackages</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<CollectdPackage> getPackages() {
        return m_packages;
    }

    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    public int getThreads() {
        return m_config.getThreads();
    }

    /**
     * This method is used to establish package agaist iplist mapping, with
     * which, the iplist is selected per package via the configured filter rules
     * from the database.
     *
     * @param localServer2
     * @param localServer TODO
     * @param verifyServer2
     * @param verifyServer TODO
     */
    protected void createPackageIpListMap(String localServer, boolean verifyServer) {

        // Multiple threads maybe asking for the m_pkgIpMap field so create
        // with temp map then assign when finished.

        for (Iterator<CollectdPackage> it = getPackages().iterator(); it.hasNext();) {
            CollectdPackage wpkg = it.next();
            wpkg.createIpList(localServer, verifyServer);
        }
    }
    
    /**
     * <p>rebuildPackageIpListMap</p>
     */
    public void rebuildPackageIpListMap() {
        createPackageIpListMap(m_localServer, m_verifyServer);
    }

    /**
     * <p>initialize</p>
     *
     * @param localServer TODO
     * @param verifyServer TODO
     */
    protected void initialize(String localServer, boolean verifyServer)  {
        createPackageIpListMap(localServer, verifyServer);

    }

    /**
     * <p>getPackage</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.CollectdPackage} object.
     */
    public CollectdPackage getPackage(String name) {
        for (Iterator<CollectdPackage> it = getPackages().iterator(); it.hasNext();) {
            CollectdPackage wpkg = it.next();
            if (wpkg.getName().equals(name)) {
                return wpkg;
            }
        }
        return null;
    }

    /**
     * Returns true if collection domain exists
     *
     * @param name
     *            The domain name to check
     * @return True if the domain exists
     */
    public boolean domainExists(String name) {
        for (Iterator<CollectdPackage> it = getPackages().iterator(); it.hasNext();) {
            CollectdPackage wpkg = it.next();
            if ((wpkg.ifAliasDomain() != null)
                    && wpkg.ifAliasDomain().equals(name)) {
                return true;
            }
        }
        return false;
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
     * @return true if Collectd config contains a package which includes the
     *         specified interface and has the specified service enabled.
     */
    public boolean isServiceCollectionEnabled(final String ipAddr, final String svcName) {
        for (final Iterator<CollectdPackage> it = getPackages().iterator(); it.hasNext();) {
        	final CollectdPackage wpkg = it.next();

            // Does the package include the interface?
            //
            if (wpkg.interfaceInPackage(ipAddr)) {
                // Yes, now see if package includes
                // the service and service is enabled
                //
                if (wpkg.serviceInPackageAndEnabled(svcName)) {
                    // Thats all we need to know...
                	return true;
                }
            }
        }

        return false;
    }

}
