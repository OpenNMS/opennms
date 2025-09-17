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
package org.opennms.netmgt.filter.api;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * <p>FilterDao interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public interface FilterDao {

    /**
     * This method returns a map of all node IDs and node labels that match
     * the rule that is passed in, sorted by node ID.
     *
     * @param rule an expression rule to be parsed and executed.
     * @return SortedMap containing all node IDs and node labels selected by the rule.
     * @exception FilterParseException if a rule is syntactically incorrect or failed in
     *                executing the SQL statement
     * @throws FilterParseException if any.
     */
    SortedMap<Integer, String> getNodeMap(String rule) throws FilterParseException;

    /**
     * <p>getIPServiceMap</p>
     *
     * @param rule a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     * @throws FilterParseException if any.
     */
    Map<InetAddress, Set<String>> getIPAddressServiceMap(String rule) throws FilterParseException;

    /**
     * Retrieve a mapping of IP-services scoped by node that match the given rule.
     *
     * @param rule filter rule to evaluate
     * @return map of IP-services
     * @throws FilterParseException if the rule is invalid
     */
    Map<Integer, Map<InetAddress, Set<String>>> getNodeIPAddressServiceMap(String rule) throws FilterParseException;

    void flushActiveIpAddressListCache();

    /**
     * Get the (non-deleted) IP addresses that match the specified rule.
     *
     * @param rule the filter rule
     * @return a {@link java.util.List} of IP addresses.
     * @throws FilterParseException if a rule is syntactically incorrect or failed in executing the SQL statement.
     */
    List<InetAddress> getActiveIPAddressList(String rule) throws FilterParseException;

    /**
     * Get the IP addresses (including deleted) that match the specified rule.
     *
     * @param rule the filter rule
     * @return a {@link java.util.List} of IP addresses.
     * @throws FilterParseException if a rule is syntactically incorrect or failed in executing the SQL statement.
     */
    List<InetAddress> getIPAddressList(String rule) throws FilterParseException;

    /**
     * <p>isValid</p>
     *
     * @param addr a {@link java.lang.String} object.
     * @param rule a {@link java.lang.String} object.
     * @return a boolean.
     * @throws FilterParseException if any.
     */
    boolean isValid(String addr, String rule) throws FilterParseException;

    /**
     * Does this rule match anything in the database?  In particular, does it
     * return at least one record from the database?
     *
     * @param rule rule to match on
     * @return true if there is at least one match, false otherwise
     * @throws FilterParseException if any.
     */
    boolean isRuleMatching(String rule) throws FilterParseException;

    /**
     * <p>validateRule</p>
     *
     * @param rule a {@link java.lang.String} object.
     * @throws FilterParseException if any.
     */
    void validateRule(String rule) throws FilterParseException;

    public Map<Integer, String> getNodeLocations(String rule);
}
