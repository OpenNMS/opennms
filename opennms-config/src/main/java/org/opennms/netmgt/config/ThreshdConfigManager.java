/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

/**
 * 
 */
package org.opennms.netmgt.config;

import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.core.utils.InetAddressUtils.isInetAddressInRange;
import static org.opennms.core.utils.InetAddressUtils.toIpAddrBytes;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.IpListFromUrl;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.threshd.ExcludeRange;
import org.opennms.netmgt.config.threshd.IncludeRange;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.Service;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.filter.FilterDaoFactory;

/**
 * <p>Abstract ThreshdConfigManager class.</p>
 *
 * @author mhuot
 * @version $Id: $
 */
public abstract class ThreshdConfigManager {

    /**
     * The config class loaded from the config file
     */
    protected ThreshdConfiguration m_config;
    /**
     * A mapping of the configured URLs to a list of the specific IPs configured
     * in each - so as to avoid file reads
     */
    private Map<String, List<String>> m_urlIPMap;
    /**
     * A mapping of the configured package to a list of IPs selected via filter
     * rules, so as to avoid repetitive database access.
     */
    private Map<Package, List<InetAddress>> m_pkgIpMap;
    /**
     * A boolean flag to indicate If a filter rule against the local OpenNMS
     * server has to be used.
     */
    protected boolean m_verifyServer;
    /**
     * The name of the local OpenNMS server
     */
    protected String m_localServer;
    
    /**
     * <p>Constructor for ThreshdConfigManager.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @param localServer a {@link java.lang.String} object.
     * @param verifyServer a boolean.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public ThreshdConfigManager(InputStream stream, String localServer, boolean verifyServer) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(ThreshdConfiguration.class, stream);

        createUrlIpMap();

        m_verifyServer = verifyServer;
        m_localServer = localServer;

        createPackageIpListMap();


    }

    /**
     * Go through the configuration and build a mapping of each configured URL
     * to a list of IPs configured in that URL - done at init() time so that
     * repeated file reads can be avoided
     */
    protected void createUrlIpMap() {
        m_urlIPMap = new HashMap<String, List<String>>();
    
        for (Package pkg : m_config.getPackageCollection()) {
            for (String urlname : pkg.getIncludeUrlCollection()) {
                java.util.List<String> iplist = IpListFromUrl.parse(urlname);
                if (iplist.size() > 0) {
                    m_urlIPMap.put(urlname, iplist);
                }
            }
        }
    }

    /**
     * This method is used to establish package against an iplist iplist mapping,
     * with which, the iplist is selected per package via the configured filter
     * rules from the database.
     */
    protected void createPackageIpListMap() {
        ThreadCategory log = ThreadCategory.getInstance(this.getClass());
    
        m_pkgIpMap = new HashMap<Package, List<InetAddress>>();
    
        Enumeration<org.opennms.netmgt.config.threshd.Package> pkgEnum = m_config.enumeratePackage();
        while (pkgEnum.hasMoreElements()) {
            org.opennms.netmgt.config.threshd.Package pkg = pkgEnum.nextElement();
    
            //
            // Get a list of ipaddress per package agaist the filter rules from
            // database and populate the package, IP list map.
            //
            StringBuffer filterRules = new StringBuffer(pkg.getFilter().getContent());
    
            try {
                if (m_verifyServer) {
                    filterRules.append(" & (serverName == ");
                    filterRules.append('\"');
                    filterRules.append(m_localServer);
                    filterRules.append('\"');
                    filterRules.append(")");
                }
    
                if (log.isDebugEnabled())
                    log.debug("createPackageIpMap: package is " + pkg.getName() + ". filer rules are  " + filterRules.toString());
    
                List<InetAddress> ipList = FilterDaoFactory.getInstance().getActiveIPAddressList(filterRules.toString());
                if (ipList.size() > 0) {
                    m_pkgIpMap.put(pkg, ipList);
                }
            } catch (Throwable t) {
                LogUtils.errorf(this, t, "createPackageIpMap: failed to map package: %s to an IP List with filter \"%s\"", pkg.getName(), pkg.getFilter().getContent());
            }
        }
    }

    /**
     * This nethod is used to rebuild the package agaist iplist mapping when
     * needed. When a node gained service event occurs, threshd has to determine
     * which package the ip/service combination is in, but if the interface is a
     * newly added one, the package iplist should be rebuilt so that threshd
     * could know which package this ip/service pair is in.
     */
    public synchronized void rebuildPackageIpListMap() {
        createPackageIpListMap();
    }

    /**
     * Saves the current in-memory configuration to disk and reloads
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public synchronized void saveCurrent() throws MarshalException, IOException, ValidationException {
    
             //marshall to a string first, then write the string to the file. This way the original config
             //isn't lost if the xml from the marshall is hosed.
             StringWriter stringWriter = new StringWriter();
             Marshaller.marshal(m_config, stringWriter);
             
             String xmlString = stringWriter.toString();
            if (xmlString!=null)
             {
                 saveXML(xmlString);
             }
    
             reloadXML();
     }

    /**
     * <p>reloadXML</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public abstract void reloadXML() throws IOException, MarshalException, ValidationException;

    /**
     * <p>saveXML</p>
     *
     * @param xmlString a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected abstract void saveXML(String xmlString) throws IOException;

    /**
     * Return the threshd configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.threshd.ThreshdConfiguration} object.
     */
    public synchronized ThreshdConfiguration getConfiguration() {
        return m_config;
    }

    /**
     * <p>getPackage</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a org$opennms$netmgt$config$threshd$Package object.
     */
    public synchronized org.opennms.netmgt.config.threshd.Package getPackage(String name) {
        for (org.opennms.netmgt.config.threshd.Package thisPackage : m_config.getPackageCollection()) {
            if(thisPackage.getName().equals(name)) {
                return thisPackage;
            }
        }
        return null;
     }

    /**
     * This method is used to determine if the named interface is included in
     * the passed package's url includes. If the interface is found in any of
     * the URL files, then a value of true is returned, else a false value is
     * returned.
     * 
     * <pre>
     * The file URL is read and each entry in this file checked. Each line
     *  in the URL file can be one of -
     *  &lt;IP&gt;&lt;space&gt;#&lt;comments&gt;
     *  or
     *  &lt;IP&gt;
     *  or
     *  #&lt;comments&gt;
     * 
     *  Lines starting with a '#' are ignored and so are characters after
     *  a '&lt;space&gt;#' in a line.
     * </pre>
     * 
     * @param addr
     *            The interface to test against the package's URL
     * @param url
     *            The url file to read
     * 
     * @return True if the interface is included in the url, false otherwise.
     */
    private boolean interfaceInUrl(String addr, String url) {
        boolean bRet = false;
    
        // get list of IPs in this URL
        java.util.List<String> iplist = m_urlIPMap.get(url);
        if (iplist != null && iplist.size() > 0) {
            bRet = iplist.contains(addr);
        }
    
        return bRet;
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
     * @param pkg
     *            The package to check for the inclusion of the interface.
     * @return True if the interface is included in the package, false
     *         otherwise.
     */
    public synchronized boolean interfaceInPackage(String iface, org.opennms.netmgt.config.threshd.Package pkg) {
        ThreadCategory log = ThreadCategory.getInstance(this.getClass());
    
        final InetAddress ifaceAddr = addr(iface);
        boolean filterPassed = false;
    
        // get list of IPs in this package
        java.util.List<InetAddress> ipList = m_pkgIpMap.get(pkg);
        if (ipList != null && ipList.size() > 0) {
			filterPassed = ipList.contains(ifaceAddr);
        }
    
        if (log.isDebugEnabled())
            log.debug("interfaceInPackage: Interface " + iface + " passed filter for package " + pkg.getName() + "?: " + filterPassed);
    
        if (!filterPassed)
            return false;
    
        //
        // Ensure that the interface is in the specific list or
        // that it is in the include range and is not excluded
        //
        boolean has_specific = false;
        boolean has_range_include = false;
        boolean has_range_exclude = false;
        
        has_range_include = pkg.getIncludeRangeCount() == 0 && pkg.getSpecificCount() == 0;
    
        for (IncludeRange rng : pkg.getIncludeRangeCollection()) {
            if (isInetAddressInRange(iface, rng.getBegin(), rng.getEnd())) {
                has_range_include = true;
                break;
            }
        }

        byte[] addr = toIpAddrBytes(iface);

        for (String spec : pkg.getSpecificCollection()) {
            byte[] speca = toIpAddrBytes(spec);
            if (new ByteArrayComparator().compare(speca, addr) == 0) {
                has_specific = true;
                break;
            }
        }

        Enumeration<String> eurl = pkg.enumerateIncludeUrl();
        while (!has_specific && eurl.hasMoreElements()) {
            has_specific = interfaceInUrl(iface, eurl.nextElement());
        }
    
        for (ExcludeRange rng : pkg.getExcludeRangeCollection()) {
            if (isInetAddressInRange(iface, rng.getBegin(), rng.getEnd())) {
                has_range_exclude = true;
                break;
            }
        }
    
        return has_specific || (has_range_include && !has_range_exclude);
    }

    /**
     * Returns true if the service is part of the package and the status of the
     * service is set to "on". Returns false if the service is not in the
     * package or it is but the status of the service is set to "off".
     *
     * @param svcName
     *            The service name to lookup.
     * @param pkg
     *            The package to lookup up service.
     * @return a boolean.
     */
    public synchronized boolean serviceInPackageAndEnabled(String svcName, org.opennms.netmgt.config.threshd.Package pkg) {
        boolean result = false;

        for (Service tsvc : pkg.getServiceCollection()) {
            if (tsvc.getName().equalsIgnoreCase(svcName)) {
                // Ok its in the package. Now check the
                // status of the service
                String status = tsvc.getStatus();
                if (status.equals("on")) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }


}
