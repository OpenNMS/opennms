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
import org.opennms.netmgt.filter.FilterDao;
import org.opennms.netmgt.filter.FilterParseException;
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
    public List<InetAddress> getActiveIPAddressList(final String rule) throws FilterParseException {
        LOG.debug("rule = {}", rule);
        final List<InetAddress> addrs = new ArrayList<InetAddress>();
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
