/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.config.tester;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ConfigTesterFilterDao.
 */
public class ConfigTesterFilterDao implements FilterDao {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ConfigTesterFilterDao.class);
    
    /**
     * Instantiates a new configuration tester filter DAO.
     */
    public ConfigTesterFilterDao() {
        super();
        LOG.info("Initializing ConfigTesterFilterDao");
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.filter.api.FilterDao#getNodeMap(java.lang.String)
     */
    @Override
    public SortedMap<Integer, String> getNodeMap(String rule) throws FilterParseException {
        return new TreeMap<Integer, String>();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.filter.api.FilterDao#getIPAddressServiceMap(java.lang.String)
     */
    @Override
    public Map<InetAddress, Set<String>> getIPAddressServiceMap(String rule) throws FilterParseException {
        return new HashMap<InetAddress, Set<String>>();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.filter.api.FilterDao#flushActiveIpAddressListCache()
     */
    @Override
    public void flushActiveIpAddressListCache() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.filter.api.FilterDao#getActiveIPAddressList(java.lang.String)
     */
    @Override
    public List<InetAddress> getActiveIPAddressList(String rule) throws FilterParseException {
        return new ArrayList<InetAddress>();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.filter.api.FilterDao#getIPAddressList(java.lang.String)
     */
    @Override
    public List<InetAddress> getIPAddressList(String rule) throws FilterParseException {
        return new ArrayList<InetAddress>();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.filter.api.FilterDao#isValid(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isValid(String addr, String rule) throws FilterParseException {
        return true;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.filter.api.FilterDao#isRuleMatching(java.lang.String)
     */
    @Override
    public boolean isRuleMatching(String rule) throws FilterParseException {
        return true;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.filter.api.FilterDao#validateRule(java.lang.String)
     */
    @Override
    public void validateRule(String rule) throws FilterParseException {
    }

}
