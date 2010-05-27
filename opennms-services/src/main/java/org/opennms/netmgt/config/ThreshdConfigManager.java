//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
/**
 * 
 */
package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.IPSorter;
import org.opennms.core.utils.IpListFromUrl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.threshd.ExcludeRange;
import org.opennms.netmgt.config.threshd.IncludeRange;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.Service;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.filter.FilterDaoFactory;

/**
 * @author mhuot
 *
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
    private Map<Package, List<String>> m_pkgIpMap;
    /**
     * A boolean flag to indicate If a filter rule against the local OpenNMS
     * server has to be used.
     */
    protected boolean m_verifyServer;
    /**
     * The name of the local OpenNMS server
     */
    protected String m_localServer;
    
    @Deprecated
    public ThreshdConfigManager(Reader rdr, String localServer, boolean verifyServer) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(ThreshdConfiguration.class, rdr);

        createUrlIpMap();

        m_verifyServer = verifyServer;
        m_localServer = localServer;

        createPackageIpListMap();


    }

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
    
        m_pkgIpMap = new HashMap<Package, List<String>>();
    
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
    
                List<String> ipList = FilterDaoFactory.getInstance().getIPList(filterRules.toString());
                if (ipList.size() > 0) {
                    m_pkgIpMap.put(pkg, ipList);
                }
            } catch (Throwable t) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("createPackageIpMap: failed to map package: " + pkg.getName() + " to an IP List", t);
                }
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

    public abstract void reloadXML() throws IOException, MarshalException, ValidationException;

    protected abstract void saveXML(String xmlString) throws IOException;

    /**
     * Return the threshd configuration object.
     */
    public synchronized ThreshdConfiguration getConfiguration() {
        return m_config;
    }

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
     * 
     * @return True if the interface is included in the package, false
     *         otherwise.
     */
    public synchronized boolean interfaceInPackage(String iface, org.opennms.netmgt.config.threshd.Package pkg) {
        ThreadCategory log = ThreadCategory.getInstance(this.getClass());
    
        boolean filterPassed = false;
    
        // get list of IPs in this package
        java.util.List<String> ipList = m_pkgIpMap.get(pkg);
        if (ipList != null && ipList.size() > 0) {
            filterPassed = ipList.contains(iface);
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
    
        long addr = IPSorter.convertToLong(iface);
        Enumeration<IncludeRange> eincs = pkg.enumerateIncludeRange();
        while (!has_range_include && eincs.hasMoreElements()) {
            IncludeRange rng = eincs.nextElement();
            long start = IPSorter.convertToLong(rng.getBegin());
            if (addr > start) {
                long end = IPSorter.convertToLong(rng.getEnd());
                if (addr <= end) {
                    has_range_include = true;
                }
            } else if (addr == start) {
                has_range_include = true;
            }
        }

        Enumeration<String> espec = pkg.enumerateSpecific();
        while (!has_specific && espec.hasMoreElements()) {
            long speca = IPSorter.convertToLong(espec.nextElement());
            if (speca == addr)
                has_specific = true;
        }

        Enumeration<String> eurl = pkg.enumerateIncludeUrl();
        while (!has_specific && eurl.hasMoreElements()) {
            has_specific = interfaceInUrl(iface, eurl.nextElement());
        }
    
        Enumeration<ExcludeRange> eex = pkg.enumerateExcludeRange();
        while (!has_range_exclude && !has_specific && eex.hasMoreElements()) {
            ExcludeRange rng = eex.nextElement();
            long start = IPSorter.convertToLong(rng.getBegin());
            if (addr > start) {
                long end = IPSorter.convertToLong(rng.getEnd());
                if (addr <= end) {
                    has_range_exclude = true;
                }
            } else if (addr == start) {
                has_range_exclude = true;
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
