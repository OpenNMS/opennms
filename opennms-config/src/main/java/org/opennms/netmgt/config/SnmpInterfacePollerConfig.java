/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * <p>SnmpInterfacePollerConfig interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface SnmpInterfacePollerConfig {
	/**
	 * 
	 * @return the Default interval
	 * 
	 */
	long getInterval();
    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    int getThreads();
    /**
     * <p>useCriteriaFilters</p>
     *
     * @return a boolean.
     */
    boolean useCriteriaFilters();
    /**
     * <p>getService</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getService();
    /**
     * <p>getCriticalServiceIds</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    String[] getCriticalServiceIds();
    /**
     * <p>getAllPackageMatches</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<String> getAllPackageMatches(String ipaddr);
    /**
     * <p>getPackageName</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getPackageName(String ipaddr);
    /**
     * <p>getInterfaceOnPackage</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @return a {@link java.util.Set} object.
     */
    Set<String> getInterfaceOnPackage(String pkgName);
    /**
     * <p>getStatus</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean getStatus(String pkgName,String pkgInterfaceName);
    /**
     * <p>getInterval</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a long.
     */
    long getInterval(String pkgName,String pkgInterfaceName);
    /**
     * <p>getCriteria</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    Optional<String> getCriteria(String pkgName,String pkgInterfaceName);
    /**
     * <p>hasPort</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean hasPort(String pkgName,String pkgInterfaceName);
    /**
     * <p>getPort</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a int.
     */
    Optional<Integer> getPort(String pkgName,String pkgInterfaceName);
    /**
     * <p>hasTimeout</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean hasTimeout(String pkgName,String pkgInterfaceName);
    /**
     * <p>getTimeout</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a int.
     */
    Optional<Integer> getTimeout(String pkgName,String pkgInterfaceName);
    /**
     * <p>hasRetries</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean hasRetries(String pkgName,String pkgInterfaceName);
    /**
     * <p>getRetries</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a int.
     */
    Optional<Integer> getRetries(String pkgName,String pkgInterfaceName);
    /**
     * <p>hasMaxVarsPerPdu</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean hasMaxVarsPerPdu(String pkgName,String pkgInterfaceName);
    /**
     * <p>getMaxVarsPerPdu</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a int.
     */
    Integer getMaxVarsPerPdu(String pkgName,String pkgInterfaceName);
    /**
     * <p>rebuildPackageIpListMap</p>
     */
    void rebuildPackageIpListMap();
    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     */
    void update() throws IOException;
}
