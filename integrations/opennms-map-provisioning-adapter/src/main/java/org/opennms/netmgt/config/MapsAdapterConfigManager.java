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
// 2007 May 06: Eliminate a warning. - dj@opennms.org
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
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.IPSorter;
import org.opennms.core.utils.IpListFromUrl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.map.adapter.Celement;
import org.opennms.netmgt.config.map.adapter.Cmap;
import org.opennms.netmgt.config.map.adapter.Csubmap;
import org.opennms.netmgt.config.map.adapter.Package;
import org.opennms.netmgt.config.map.adapter.MapsAdapterConfiguration;
import org.opennms.netmgt.config.map.adapter.ExcludeRange;
import org.opennms.netmgt.config.map.adapter.IncludeRange;

import org.opennms.netmgt.filter.FilterDaoFactory;

/**
 * 
 * @author <a href="mailto:antonio@openms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
abstract public class MapsAdapterConfigManager implements MapsAdapterConfig {
     
    /**
     * @author <a href="mailto:antonio@opennms.org">Antonio Russo</a>
     * @param reader
     * @param localServer
     * @param verifyServer
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException
     */
   public MapsAdapterConfigManager(Reader reader,String serverName, boolean verifyServer) throws MarshalException, ValidationException, IOException {
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
    private Map<Package, List<String>> m_pkgIpMap;

    /**
     * A mapping of the configured sub-maps to a list of maps
     */
    private Map<String,List<String>> m_submapNameMapNameMap;
    

    /**
     * A mapping of the configured mapName to cmaps
     */
    private Map<String,Cmap> m_mapNameCmapMap;
    

     public MapsAdapterConfigManager() {
     }
    

    protected synchronized void reloadXML(Reader reader) throws MarshalException, ValidationException, IOException {
        m_config = (MapsAdapterConfiguration) Unmarshaller.unmarshal(MapsAdapterConfiguration.class, reader);
        createUrlIpMap();
        createPackageIpListMap();
        createSubMapMapMap();
        createmapNameCmapMap();
        verifyMapConsistency();
        verifyMapHasLoop();
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
            Iterator<Cmap> ite = m_config.getCmaps().iterateCmap();
            while (ite.hasNext()) {
                Cmap cmap = ite.next();
                m_mapNameCmapMap.put(cmap.getMapName(), cmap);
                log().debug("createmapNameCmapMap: Added map: " +cmap.getMapName());
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
            Iterator<Cmap> ite = m_config.getCmaps().iterateCmap();
            while (ite.hasNext()) {
                Cmap cmap = ite.next();
                Iterator<Csubmap> sub_ite = cmap.iterateCsubmap();
                while (sub_ite.hasNext()) {
                    Csubmap csubmap = sub_ite.next();
                    String subMapName = csubmap.getName();
                    List<String> containermaps = new ArrayList<String>();
                    if (m_submapNameMapNameMap.containsKey(subMapName)) {
                        containermaps = m_submapNameMapNameMap.get(subMapName);
                    }
                    containermaps.add(cmap.getMapName());
                    m_submapNameMapNameMap.put(subMapName, containermaps);
                    log().debug("createSubMapMapMap: added container map: " + cmap.getMapName() + " to submap: " + subMapName);
                }
            }
        }        
    }
    
    /**
      *  Verify that no loop are in maps definition 
    */
    
    private void verifyMapConsistency() throws ValidationException {
        Iterator<String> ite = m_submapNameMapNameMap.keySet().iterator();
        while (ite.hasNext()) {
            // verify cmap exists!
            String mapName = ite.next();
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

    private boolean cmapExist(String mapName) {
        if (hasCmaps()) {
            Iterator<Cmap> ite = m_config.getCmaps().iterateCmap();
            while (ite.hasNext()) {
                if (ite.next().getMapName().equals(mapName)) return true;
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
    
        for(Package pkg : packages()) {
    
            for(String url : includeURLs(pkg)) {
    
                List<String> iplist = IpListFromUrl.parse(url);
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
    private synchronized void createPackageIpListMap() {
        m_pkgIpMap = new HashMap<Package, List<String>>();
        
        for(Package pkg : packages()) {
    
            // Get a list of IP addresses per package against the filter rules from
            // database and populate the package, IP list map.
            //
            try {
                List<String> ipList = getIpList(pkg);
                log().debug("createPackageIpMap: package " + pkg.getName() + ": ipList size =  " + ipList.size());
    
                if (ipList.size() > 0) {
                    m_pkgIpMap.put(pkg, ipList);
                }
            } catch (Throwable t) {
                log().error("createPackageIpMap: failed to map package: " + pkg.getName() + " to an IP List: " + t, t);
            }

        }
    }
    
    private List<String> getIpList(Package pkg) {
        StringBuffer filterRules = new StringBuffer(pkg.getFilter().getContent());
        if (m_verifyServer) {
            filterRules.append(" & (serverName == ");
            filterRules.append('\"');
            filterRules.append(m_localServer);
            filterRules.append('\"');
            filterRules.append(")");
        }
        log().debug("createPackageIpMap: package is " + pkg.getName() + ". filer rules are  " + filterRules.toString());
        List<String> ipList = FilterDaoFactory.getInstance().getIPList(filterRules.toString());
        return ipList;
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
    private synchronized boolean interfaceInPackage(String iface, Package pkg) {
        Category log = log();
    
        boolean filterPassed = false;
    
        // get list of IPs in this package
        List<String> ipList = m_pkgIpMap.get(pkg);
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
 
        // if there are NO include ranges then treat act as if the user include
        // the range 0.0.0.0 - 255.255.255.255
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
    private boolean interfaceInUrl(String addr, String url) {
        boolean bRet = false;
    
        // get list of IPs in this URL
        List<String> iplist = m_urlIPMap.get(url);
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
     *
     * @return a list of package names that the ip belongs to, null if none
     */
    public synchronized List<String> getAllPackageMatches(String ipaddr) {
    
        List<String> matchingPkgs = new ArrayList<String>();

        for(Package pkg : packages()) {

            boolean inPkg = interfaceInPackage(ipaddr, pkg);
            if (inPkg) {
                matchingPkgs.add(pkg.getName());
            }

        }
    
        return matchingPkgs;
    }



    public Iterable<Package> packages() {
        return getConfiguration().getPackageCollection();
    }

    public Iterable<String> includeURLs(Package pkg) {
        return pkg.getIncludeUrlCollection();
    }

    /**
     * Return the poller configuration object.
     */
    public synchronized MapsAdapterConfiguration getConfiguration() {
        return m_config;
    }

    
 
    private Category log() {
        return ThreadCategory.getInstance(this.getClass());
    }

// methods from interface
    
    public List<Cmap> getAllMaps() {
        if (hasCmaps()) {
            return getConfiguration().getCmaps().getCmapCollection();
        }
        return new ArrayList<Cmap>();
    }

    public Map<String, Celement> getElementByAddress(String ipaddr) {
        Map<String,Celement> mapAndElements = new HashMap<String, Celement>();
        if (hasCmaps()) {
            List<String> pkgs = getAllPackageMatches(ipaddr);
            Iterator<Cmap> ite = getConfiguration().getCmaps().getCmapCollection().iterator();
            while (ite.hasNext()) {
                Cmap cmap = ite.next();
                Iterator<Celement> cels = cmap.getCelementCollection().iterator();
                boolean found = false;
                while (cels.hasNext()) {
                     Celement celement = cels.next();
                     Iterator<String> pkgname = pkgs.iterator();
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
    }

    public List<Csubmap> getSubMaps(String mapName) {
        if (hasCmaps()) {
            Iterator<Cmap> ite = getConfiguration().getCmaps().getCmapCollection().iterator();
            while (ite.hasNext()) {
                Cmap cmap = ite.next();
                if (cmap.getMapName().equals(mapName))
                    return cmap.getCsubmapCollection();
            }
        }
        return new ArrayList<Csubmap>();
    }

    public int getMapElementDimension() {
        return getConfiguration().getElementDimension();
    }

    public int getOperationNumberBeforeSync() {        
        return getConfiguration().getOperationNumberBeforeSync();
    }
    
    public Map<String,Csubmap> getContainerMaps(String submapName) {
        Map<String,Csubmap> cmaps = new HashMap<String, Csubmap>();
        if (m_submapNameMapNameMap.containsKey(submapName)) {
            Iterator<String> ite = m_submapNameMapNameMap.get(submapName).iterator();
            while (ite.hasNext()) {
                String mapName = ite.next();
                Cmap cmap = m_mapNameCmapMap.get(mapName);
                Iterator<Csubmap> sub_ite = cmap.iterateCsubmap();
                while (sub_ite.hasNext()) {
                    Csubmap csubmap = sub_ite.next();
                    if (csubmap.getName().equals(submapName)) {
                        cmaps.put(mapName,csubmap);
                        break;
                    }
                }
            }
        }
        return cmaps;
    }

    public Map<String, List<Csubmap>> getsubMaps() {
        Map<String,List<Csubmap>> csubmaps = new HashMap<String, List<Csubmap>>();
        if (hasCmaps()) {
            Iterator<Cmap> ite = getConfiguration().getCmaps().getCmapCollection().iterator();
            while (ite.hasNext()) {
                Cmap cmap = ite.next();
                if (cmap.getCsubmapCount() > 0) {
                    csubmaps.put(cmap.getMapName(), cmap.getCsubmapCollection());
                }
            }
        }
        return csubmaps;
    }
    
    public Map<String,List<Celement>> getCelements() {
        Map<String,List<Celement>> celements = new HashMap<String, List<Celement>>();
        if (hasCmaps()) {
            Iterator<Cmap> ite = getConfiguration().getCmaps().getCmapCollection().iterator();
            while (ite.hasNext()) {
                Cmap cmap = ite.next();
                if (cmap.getCelementCount() > 0) {
                    celements.put(cmap.getMapName(), cmap.getCelementCollection());
                }
            }
        }
     
        return celements;
    }

    /**
     * This method is used to rebuild the package against IP list mapping when
     * needed. When a node gained service event occurs, poller has to determine
     * which package the IP/service combination is in, but if the interface is a
     * newly added one, the package IP list should be rebuilt so that poller
     * could know which package this IP/service pair is in.
     */
    public synchronized void rebuildPackageIpListMap() {
        createPackageIpListMap();
    }
    
}
