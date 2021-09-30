/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.dao.thresholding.impl;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.opennms.core.network.IpListFromUrl;
import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.dao.thresholding.api.ReadableThreshdDao;
import org.opennms.netmgt.config.threshd.ExcludeRange;
import org.opennms.netmgt.config.threshd.IncludeRange;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractThreshdDao implements ReadableThreshdDao {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractThreshdDao.class);
    public static final String JSON_STORE_KEY = "threshd-configuration";

    private IPMap ipMap;
    final JsonStore jsonStore;

    AbstractThreshdDao() {
        jsonStore = null;
        reload();
    }

    AbstractThreshdDao(JsonStore jsonStore) {
        this.jsonStore = Objects.requireNonNull(jsonStore);
    }

    /**
     * Subclasses should call this reload after they have performed their reload logic.
     */
    @Override
    public void reload() {
        ipMap = new IPMap();
    }

    @Override
    public final void rebuildPackageIpListMap() {
        ipMap.rebuildPackageIpListMap();
    }

    @Override
    public final boolean interfaceInPackage(String iface, Package pkg) {
        return ipMap.interfaceInPackage(iface, pkg);
    }

    private class IPMap {
        /**
         * A mapping of the configured URLs to a list of the specific IPs configured
         * in each - so as to avoid file reads
         */
        private final Map<String, List<String>> urlIPMap = new HashMap<>();

        /**
         * A mapping of the configured package to a list of IPs selected via filter
         * rules, so as to avoid repetitive database access.
         */
        private final Map<Package, List<InetAddress>> pkgIpMap = new HashMap<>();

        private IPMap() {
            createUrlIpMap();
            createPackageIpListMap();
        }

        /**
         * Go through the configuration and build a mapping of each configured URL
         * to a list of IPs configured in that URL - done at init() time so that
         * repeated file reads can be avoided
         */
        private synchronized void createUrlIpMap() {
            for (Package pkg : getReadOnlyConfig().getPackages()) {
                for (String urlname : pkg.getIncludeUrls()) {
                    java.util.List<String> iplist = IpListFromUrl.fetch(urlname);
                    if (iplist.size() > 0) {
                        urlIPMap.put(urlname, iplist);
                    }
                }
            }
        }

        /**
         * This method is used to establish package against an iplist iplist mapping,
         * with which, the iplist is selected per package via the configured filter
         * rules from the database.
         */
        private synchronized void createPackageIpListMap() {
            for (final org.opennms.netmgt.config.threshd.Package pkg : getReadOnlyConfig().getPackages()) {
                //
                // Get a list of ipaddress per package agaist the filter rules from
                // database and populate the package, IP list map.
                //
                final StringBuilder filterRules = new StringBuilder();
                if (pkg.getFilter().getContent().isPresent()) {
                    filterRules.append(pkg.getFilter().getContent().get());
                }
                try {
                    LOG.debug("createPackageIpMap: package is {}. filer rules are {}", filterRules, pkg.getName());

                    FilterDaoFactory.getInstance().flushActiveIpAddressListCache();
                    List<InetAddress> ipList =
                            FilterDaoFactory.getInstance().getActiveIPAddressList(filterRules.toString());
                    if (ipList.size() > 0) {
                        pkgIpMap.put(pkg, ipList);
                    }
                } catch (Throwable t) {
                    LOG.error("createPackageIpMap: failed to map package: {} to an IP List with filter \"{}\"",
                            pkg.getName(), pkg.getFilter().getContent().orElse(null), t);
                }
            }
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
         * @param addr The interface to test against the package's URL
         * @param url  The url file to read
         * @return True if the interface is included in the url, false otherwise.
         */
        private synchronized boolean interfaceInUrl(String addr, String url) {
            boolean bRet = false;

            // get list of IPs in this URL
            java.util.List<String> iplist = urlIPMap.get(url);
            if (iplist != null && iplist.size() > 0) {
                bRet = iplist.contains(addr);
            }

            return bRet;
        }

        private synchronized boolean interfaceInPackage(String iface, org.opennms.netmgt.config.threshd.Package pkg) {
            final InetAddress ifaceAddr = InetAddressUtils.addr(iface);
            boolean filterPassed = false;

            // get list of IPs in this package
            java.util.List<InetAddress> ipList = pkgIpMap.get(pkg);
            if (ipList != null && ipList.size() > 0) {
                filterPassed = ipList.contains(ifaceAddr);
            }


            LOG.debug("interfaceInPackage: Interface {} passed filter for package {}?: {}", filterPassed, iface,
                    pkg.getName());

            if (!filterPassed)
                return false;

            //
            // Ensure that the interface is in the specific list or
            // that it is in the include range and is not excluded
            //
            boolean has_specific = false;
            boolean has_range_include = false;
            boolean has_range_exclude = false;

            has_range_include = pkg.getIncludeRanges().size() == 0 && pkg.getSpecifics().size() == 0;

            for (IncludeRange rng : pkg.getIncludeRanges()) {
                if (InetAddressUtils.isInetAddressInRange(iface, rng.getBegin(), rng.getEnd())) {
                    has_range_include = true;
                    break;
                }
            }

            byte[] addr = InetAddressUtils.toIpAddrBytes(iface);

            for (String spec : pkg.getSpecifics()) {
                byte[] speca = InetAddressUtils.toIpAddrBytes(spec);
                if (new ByteArrayComparator().compare(speca, addr) == 0) {
                    has_specific = true;
                    break;
                }
            }

            final Iterator<String> eurl = pkg.getIncludeUrls().iterator();
            while (!has_specific && eurl.hasNext()) {
                has_specific = interfaceInUrl(iface, eurl.next());
            }

            for (ExcludeRange rng : pkg.getExcludeRanges()) {
                if (InetAddressUtils.isInetAddressInRange(iface, rng.getBegin(), rng.getEnd())) {
                    has_range_exclude = true;
                    break;
                }
            }

            return has_specific || (has_range_include && !has_range_exclude);
        }

        private synchronized void rebuildPackageIpListMap() {
            pkgIpMap.clear();
            createPackageIpListMap();
        }
    }
}
