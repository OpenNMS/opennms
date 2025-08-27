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

    @Override
    public Map<Integer, Map<InetAddress, Set<String>>> getNodeIPAddressServiceMap(String rule) throws FilterParseException {
        return new HashMap<>();
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

    @Override
    public Map<Integer, String> getNodeLocations() {
        return Map.of();
    }

}
