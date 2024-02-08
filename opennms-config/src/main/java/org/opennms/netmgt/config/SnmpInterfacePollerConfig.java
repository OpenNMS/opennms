/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
     * <p>getUpValues</p>
     * @return a String
     */
    String getUpValues();

    /**
     * <p>getDownValues</p>
     * @return a String
     */
    String getDownValues();
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
     * <p>getUpValues</p>
     * @param pkgName a {@link String} object.
     * @param pkgInterfaceName a {@link String} object.
     * @return a String.
     */
    String getUpValues(String pkgName, String pkgInterfaceName);
    /**
     * <p>getDownValues</p>
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a String.
     */
    String getDownValues(String pkgName,String pkgInterfaceName);
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
