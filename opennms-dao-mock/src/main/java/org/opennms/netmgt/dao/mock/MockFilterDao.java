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
package org.opennms.netmgt.dao.mock;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

public class MockFilterDao implements FilterDao, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(MockFilterDao.class);
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_ipInterfaceDao);
    }

    @Override
    public SortedMap<Integer, String> getNodeMap(final String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Map<InetAddress, Set<String>> getIPAddressServiceMap(final String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Map<Integer, Map<InetAddress, Set<String>>> getNodeIPAddressServiceMap(String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void flushActiveIpAddressListCache() {}

    @Override
    public List<InetAddress> getActiveIPAddressList(final String rule) throws FilterParseException {
        LOG.debug("rule = {}", rule);
        final List<InetAddress> addrs = new ArrayList<>();

        if (rule.equals("IPADDR != '0.0.0.0'")) {
            Assert.notNull(m_ipInterfaceDao);
            final CriteriaBuilder builder = new CriteriaBuilder(OnmsIpInterface.class);
            builder.ne("ipAddress", "0.0.0.0");
            builder.ne("isManaged", "D");
            builder.distinct();
            final Criteria criteria = builder.toCriteria();
            for (final OnmsIpInterface iface : m_ipInterfaceDao.findMatching(criteria)) {
                addrs.add(iface.getIpAddress());
            }
            return addrs;
        }

        if (rule.equals("foreignSource == 'Minions' AND IPADDR != '0.0.0.0'")) {
            Assert.notNull(m_ipInterfaceDao);
            final CriteriaBuilder builder = new CriteriaBuilder(OnmsIpInterface.class);
            builder.ne("ipAddress", "0.0.0.0");
            builder.ne("isManaged", "D");
            builder.eq("node.location.locationName", "Minions");
            builder.distinct();
            final Criteria criteria = builder.toCriteria();
            for (final OnmsIpInterface iface : m_ipInterfaceDao.findMatching(criteria)) {
                addrs.add(iface.getIpAddress());
            }
            return addrs;
        }

        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<InetAddress> getIPAddressList(final String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public boolean isValid(final String addr, final String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public boolean isRuleMatching(final String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void validateRule(final String rule) throws FilterParseException {
        if ("IPADDR != '0.0.0.0'".equals(rule) || "foreignSource == 'Minions' AND IPADDR != '0.0.0.0'".equals(rule))
            return ;

        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }
    public void setIpInterfaceDao(final IpInterfaceDao dao) {
        m_ipInterfaceDao = dao;
    }

}
