/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.core.utils.InetAddressUtils.isInetAddressInRange;
import static org.opennms.core.utils.InetAddressUtils.toIpAddrBytes;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.IpListFromUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.map.adapter.Celement;
import org.opennms.netmgt.config.map.adapter.Cmap;
import org.opennms.netmgt.config.map.adapter.Csubmap;
import org.opennms.netmgt.config.map.adapter.ExcludeRange;
import org.opennms.netmgt.config.map.adapter.IncludeRange;
import org.opennms.netmgt.config.map.adapter.MapsAdapterConfiguration;
import org.opennms.netmgt.config.map.adapter.Package;
import org.opennms.netmgt.filter.FilterDaoFactory;

/**
 * <p>Abstract MapsAdapterConfigManager class.</p>
 *
 * @author <a href="mailto:antonio@openms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
abstract public class MapsAdapterConfigManager implements MapsAdapterConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MapsAdapterConfigManager.class);
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();

   /**
    * <p>Constructor for MapsAdapterConfigManager.</p>
    *
    * @author <a href="mailto:antonio@opennms.org">Antonio Russo</a>
    * @param reader a {@link java.io.InputStream} object.
    * @param verifyServer a boolean.
    * @throws org.exolab.castor.xml.MarshalException if any.
    * @throws org.exolab.castor.xml.ValidationException if any.
    * @throws java.io.IOException if any.
    * @param serverName a {@link java.lang.String} object.
    */
   public MapsAdapterConfigManager(final InputStream reader, final String serverName, final boolean verifyServer) throws MarshalException, ValidationException, IOException {
        m_localServer = serverName;
        m_verifyServer = verifyServer;
        reloadXML(reader);
    }

    /**
     * The config class loaded from the config file
     */
     private MapsAdapterConfiguration m_config;

    /**
     * A boolean flag to indicate If a filter rule against the local OpenNMS
     * server has to be used.
     */
    private static boolean m_verifyServer;

    /**
     * The name of the local OpenNMS server
     */
    private static String m_localServer;

    /**
     * A mapping of the configured URLs to a list of the specific IPs configured
     * in each - so as to avoid file reads
     */
    private Map<String, List<String>> m_urlIPMap;
    
    /**
     * A mapping of the configured package to a list of IPs selected via filter
     * rules, so as to avoid database access.
     */
    private Map<Package, List<InetAddress>> m_pkgIpMap;

    /**
     * A mapping of the configured sub-maps to a list of maps
     */
    private Map<String,List<String>> m_submapNameMapNameMap;
    

    /**
     * A mapping of the configured mapName to cmaps
     */
    private Map<String,Cmap> m_mapNameCmapMap;
    

     /**
      * <p>Constructor for MapsAdapterConfigManager.</p>
      */
     public MapsAdapterConfigManager() {
     }
    
    @Override
     public Lock getReadLock() {
         return m_readLock;
     }
     
    @Override
     public Lock getWriteLock() {
         return m_writeLock;
     }

    /**
     * <p>reloadXML</p>
     *
     * @param reader a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    protected void reloadXML(final InputStream reader) throws MarshalException, ValidationException, IOException {
        getWriteLock().lock();
        try {
            m_config = CastorUtils.unmarshal(MapsAdapterConfiguration.class, reader);
            createUrlIpMap();
            createPackageIpListMap();
            createSubMapMapMap();
            createmapNameCmapMap();
            verifyMapConsistency();
            verifyMapHasLoop();
        } finally {
            getWriteLock().unlock();
        }
    }
    
    private boolean hasCmaps() {
        return (m_config.getCmaps() != null);
    }

    /**
     * Go through the maps adapter configuration and build a mapping of each
     * configured map name to container cmap.
     * 
     */

    private void createmapNameCmapMap() {
        m_mapNameCmapMap = new HashMap<String, Cmap>();
        if (hasCmaps()) {
            for (final Cmap cmap : m_config.getCmaps().getCmapCollection()) {
                m_mapNameCmapMap.put(cmap.getMapName(), cmap);
                LOG.debug("createmapNameCmapMap: Added map: {}", cmap.getMapName());
            }
        }
    }

    
    /**
     * Go through the maps adapter configuration and build a mapping of each
     * configured sub-map to container map.
     * 
     */
    
    private void createSubMapMapMap() {
        m_submapNameMapNameMap = new HashMap<String, List<String>>();
        if (hasCmaps()) {
            for (final Cmap cmap : m_config.getCmaps().getCmapCollection()) {
                for (final Csubmap csubmap : cmap.getCsubmapCollection()) {
                    final String subMapName = csubmap.getName();
                    List<String> containermaps = new ArrayList<String>();
                    if (m_submapNameMapNameMap.containsKey(subMapName)) {
                        containermaps = m_submapNameMapNameMap.get(subMapName);
                    }
                    containermaps.add(cmap.getMapName());
                    m_submapNameMapNameMap.put(subMapName, containermaps);
                    LOG.debug("createSubMapMapMap: added container map: {} to submap: {}", cmap.getMapName(), subMapName);
                }
            }
        }        
    }
    
    /**
      *  Verify that no loop are in maps definition 
    */
    
    private void verifyMapConsistency() throws ValidationException {
        for (final String mapName : m_submapNameMapNameMap.keySet()) {
            // verify cmap exists!
            if (!cmapExist(mapName)) 
                throw new ValidationException("Defined a submap without defining the map: mapName " + mapName);
            
        }
    }

    /**
     *  Verify that all maps are well defined 
   */
   
   private void verifyMapHasLoop() throws ValidationException {
       // TODO use the Floyd's Cycle-Finding Algorithm
       /*       
        * String startnode;
        * String slownode = startnode;
        * String fastnode1 = startnode;
        * String fastnode2 = startnode;
        * while (slownode && fastnode1 = fastnode2.next && fastnode2 = fastnode1.next) {
        *   if (slownode == fastnode1 || slownode == fastnode2) return true;
        *   slownode.next;
         * }
        * return false;
        */
       /*       Iterator<String> ite = m_submapNameMapNameMap.keySet().iterator();
       while (ite.hasNext()) {
           // verify cmap exists!
           String mapName = ite.next();
           Iterator<String> sub_ite = m_submapNameMapNameMap.get(mapName).iterator();
           while (sub_ite.hasNext()) {
               String nextElementMap = sub_ite.next();
           }
       }
       */
   }

    private boolean cmapExist(final String mapName) {
        if (hasCmaps()) {
            for (Cmap cmap : m_config.getCmaps().getCmapCollection()) {
                if (cmap.getMapName().equals(mapName)) return true;
            }
        }
        return false;
    }
    /**
     * Go through the maps adapter configuration and build a mapping of each
     * configured URL to a list of IPs configured in that URL - done at init()
     * time so that repeated file reads can be avoided
     */
    private void createUrlIpMap() {
        m_urlIPMap = new HashMap<String, List<String>>();
    
        for(final Package pkg : packages()) {
            for(final String url : includeURLs(pkg)) {
                final List<String> iplist = IpListFromUrl.parse(url);
                if (iplist.size() > 0) {
                    m_urlIPMap.put(url, iplist);
                }
            }

        }
    }

    /**
     * This method is used to establish package against iplist mapping, with
     * which, the iplist is selected per package via the configured filter rules
     * from the database.
     */
    private void createPackageIpListMap() {
        getWriteLock().lock();
        try {
            m_pkgIpMap = new HashMap<Package, List<InetAddress>>();
            
            for(final Package pkg : packages()) {
        
                // Get a list of IP addresses per package against the filter rules from
                // database and populate the package, IP list map.
                //
                try {
                    final List<InetAddress> ipList = getIpList(pkg);
                    LOG.debug("createPackageIpMap: package {}: ipList size = {}", pkg.getName(), ipList.size());
        
                    if (ipList.size() > 0) {
                        m_pkgIpMap.put(pkg, ipList);
                    }
                } catch (final Throwable t) {
                    LOG.error("createPackageIpMap: failed to map package: {} to an IP List with filter \"{}\"", pkg.getName(), pkg.getFilter().getContent(), t);
                }
    
            }
        } finally {
            getWriteLock().unlock();
        }
    }
    
    private List<InetAddress> getIpList(final Package pkg) {
        final StringBuffer filterRules = new StringBuffer(pkg.getFilter().getContent());
        if (m_verifyServer) {
            filterRules.append(" & (serverName == ");
            filterRules.append('\"');
            filterRules.append(m_localServer);
            filterRules.append('\"');
            filterRules.append(")");
        }
        final String rules = filterRules.toString();
        LOG.debug("createPackageIpMap: package is {}. filter rules are {}", pkg.getName(), rules);
        return FilterDaoFactory.getInstance().getActiveIPAddressList(rules);
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
    private boolean interfaceInPackage(final String iface, final Package pkg) {
        boolean filterPassed = false;
    
        // get list of IPs in this package
        final List<InetAddress> ipList = m_pkgIpMap.get(pkg);
        if (ipList != null && ipList.size() > 0) {
            filterPassed = ipList.contains(addr(iface));
        }
    
        LOG.debug("interfaceInPackage: Interface {} passed filter for package {}?: {}", iface, pkg.getName(), String.valueOf(filterPassed));
    
        if (!filterPassed)
            return false;
    
        //
        // Ensure that the interface is in the specific list or
        // that it is in the include range and is not excluded
        //
        boolean has_specific = false;
        boolean has_range_include = false;
        boolean has_range_exclude = false;
 
        // if there are NO include ranges then treat act as if the user include
        // the range 0.0.0.0 - 255.255.255.255
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
    
        final Enumeration<String> eurl = pkg.enumerateIncludeUrl();
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
     * This method is used to determine if the named interface is included in
     * the passed package's URL includes. If the interface is found in any of
     * the URL files, then a value of true is returned, else a false value is
     * returned.
     * 
     * <pre>
     * 
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
     *  
     * </pre>
     * 
     * @param addr
     *            The interface to test against the package's URL
     * @param url
     *            The URL file to read
     * 
     * @return True if the interface is included in the URL, false otherwise.
     */
    private boolean interfaceInUrl(final String addr, final String url) {
        boolean bRet = false;
    
        // get list of IPs in this URL
        final List<String> iplist = m_urlIPMap.get(url);
        if (iplist != null && iplist.size() > 0) {
            bRet = iplist.contains(addr);
        }
    
        return bRet;
    }
    
    /**
     * Returns a list of package names that the ip belongs to, null if none.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     *
     * @param ipaddr
     *            the interface to check
     * @return a list of package names that the ip belongs to, null if none
     */
    public List<String> getAllPackageMatches(final String ipaddr) {
        getReadLock().lock();
        
        try {
            final List<String> matchingPkgs = new ArrayList<String>();
    
            for(final Package pkg : packages()) {
                final boolean inPkg = interfaceInPackage(ipaddr, pkg);
                if (inPkg) {
                    matchingPkgs.add(pkg.getName());
                }
            }
            return matchingPkgs;
        } finally {
            getReadLock().unlock();
        }
    }



    /**
     * <p>packages</p>
     *
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<Package> packages() {
        getReadLock().lock();
        try {
            return getConfiguration().getPackageCollection();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>includeURLs</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.map.adapter.Package} object.
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<String> includeURLs(final Package pkg) {
        getReadLock().lock();
        try {
            return pkg.getIncludeUrlCollection();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return the poller configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.map.adapter.MapsAdapterConfiguration} object.
     */
    public MapsAdapterConfiguration getConfiguration() {
        getReadLock().lock();
        try {
            return m_config;
        } finally {
            getReadLock().unlock();
        }
    }

// methods from interface
    
    /**
     * <p>getAllMaps</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<Cmap> getAllMaps() {
        getReadLock().lock();
        try {
            if (hasCmaps()) {
                return getConfiguration().getCmaps().getCmapCollection();
            }
            return Collections.emptyList();
        } finally {
            getReadLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Celement> getElementByAddress(final String ipaddr) {
        getReadLock().lock();
        try {
            final Map<String,Celement> mapAndElements = new HashMap<String, Celement>();
            if (hasCmaps()) {
                final List<String> pkgs = getAllPackageMatches(ipaddr);
                for (final Cmap cmap : getConfiguration().getCmaps().getCmapCollection()) {
                    final Iterator<Celement> cels = cmap.getCelementCollection().iterator();
                    boolean found = false;
                    while (cels.hasNext()) {
                        final Celement celement = cels.next();
                        final Iterator<String> pkgname = pkgs.iterator();
                        while (pkgname.hasNext()) {
                            if (pkgname.next().equals(celement.getPackage())) {
                                mapAndElements.put(cmap.getMapName(), celement);
                                found = true;
                                break;
                            }
                        }
                        if (found) break;
                    }
                }
            }
            return mapAndElements;
        } finally {
            getReadLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Csubmap> getSubMaps(final String mapName) {
        getReadLock().lock();
        try {
            if (hasCmaps()) {
                for (final Cmap cmap : getConfiguration().getCmaps().getCmapCollection()) {
                    if (cmap.getMapName().equals(mapName)) return cmap.getCsubmapCollection();
                }
            }
            return Collections.emptyList();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>getMapElementDimension</p>
     *
     * @return a int.
     */
    @Override
    public int getMapElementDimension() {
        getReadLock().lock();
        try {
            return getConfiguration().getElementDimension();
        } finally {
            getReadLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Map<String,Csubmap> getContainerMaps(final String submapName) {
        getReadLock().lock();
        try {
            final Map<String,Csubmap> cmaps = new HashMap<String, Csubmap>();
            if (m_submapNameMapNameMap.containsKey(submapName)) {
                for (final String mapName : m_submapNameMapNameMap.get(submapName)) {
                    final Cmap cmap = m_mapNameCmapMap.get(mapName);
                    for (final Csubmap csubmap : cmap.getCsubmapCollection()) {
                        if (csubmap.getName().equals(submapName)) {
                            cmaps.put(mapName,csubmap);
                            break;
                        }
                    }
                }
            }
            return cmaps;
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>getsubMaps</p>
     *
     * @return a {@link java.util.Map} object.
     */
    @Override
    public Map<String, List<Csubmap>> getsubMaps() {
        getReadLock().lock();
        try {
            final Map<String,List<Csubmap>> csubmaps = new HashMap<String, List<Csubmap>>();
            if (hasCmaps()) {
                for (final Cmap cmap : getConfiguration().getCmaps().getCmapCollection()) {
                    if (cmap.getCsubmapCount() > 0) {
                        csubmaps.put(cmap.getMapName(), cmap.getCsubmapCollection());
                    }
                }
            }
            return Collections.unmodifiableMap(csubmaps);
        } finally {
            getReadLock().unlock();
        }
    }
    
    /**
     * <p>getCelements</p>
     *
     * @return a {@link java.util.Map} object.
     */
    @Override
    public Map<String,List<Celement>> getCelements() {
        getReadLock().lock();
        try {
            final Map<String,List<Celement>> celements = new HashMap<String, List<Celement>>();
            if (hasCmaps()) {
                for (final Cmap cmap : getConfiguration().getCmaps().getCmapCollection()) {
                    if (cmap.getCelementCount() > 0) {
                        celements.put(cmap.getMapName(), cmap.getCelementCollection());
                    }
                }
            }
            return Collections.unmodifiableMap(celements);
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * This method is used to rebuild the package against IP list mapping when
     * needed. When a node gained service event occurs, poller has to determine
     * which package the IP/service combination is in, but if the interface is a
     * newly added one, the package IP list should be rebuilt so that poller
     * could know which package this IP/service pair is in.
     */
    @Override
    public void rebuildPackageIpListMap() {
        createPackageIpListMap();
    }
    
}
