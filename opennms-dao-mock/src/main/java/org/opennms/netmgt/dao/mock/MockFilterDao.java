/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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
        throw new UnsupportedOperationException("Not yet implemented!");
        
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }
    public void setIpInterfaceDao(final IpInterfaceDao dao) {
        m_ipInterfaceDao = dao;
    }

}
