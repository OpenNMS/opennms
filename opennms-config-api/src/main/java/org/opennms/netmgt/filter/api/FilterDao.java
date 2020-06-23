/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

}
