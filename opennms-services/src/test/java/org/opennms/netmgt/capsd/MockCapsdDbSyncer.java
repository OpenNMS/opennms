package org.opennms.netmgt.capsd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.capsd.CapsdDbSyncer;
import org.opennms.netmgt.config.CapsdConfig;
import org.opennms.netmgt.config.CapsdConfigManager;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

public class MockCapsdDbSyncer implements InitializingBean, CapsdDbSyncer {
    private static final Logger LOG = LoggerFactory.getLogger(MockCapsdDbSyncer.class);

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private CapsdConfigManager m_capsdConfigManager;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    private OpennmsServerConfigFactory m_opennmsServerConfig;
    private CapsdConfig m_capsdConfig;
    private PollerConfig m_pollerConfig;
    private CollectdConfigFactory m_collectdConfig;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_serviceTypeDao);
        Assert.notNull(m_capsdConfigManager);
        Assert.notNull(m_ipInterfaceDao);

        Assert.notNull(m_opennmsServerConfig);
        Assert.notNull(m_capsdConfig);
        Assert.notNull(m_pollerConfig);
        Assert.notNull(m_collectdConfig);
    }

    @Override
    public void syncServices() {
        final List<String> services = syncServicesTable();
        
        final List<String> protocols = m_capsdConfig.getConfiguredProtocols();

        for(final String service : services) {
            if (!protocols.contains(service)) {
                LOG.debug("syncServices: service {} exists in the database but not in the Capsd config file.", service);
                m_serviceTypeDao.delete(m_serviceTypeDao.findByName(service));
            }
        }
    }

    @Override
    public List<String> syncServicesTable() {
        final List<String> serviceNames = new ArrayList<String>();
        for (final OnmsServiceType type : m_serviceTypeDao.findAll()) {
            serviceNames.add(type.getName());
        }

        for (final String protocol : m_capsdConfig.getConfiguredProtocols()) {
            if (!serviceNames.contains(protocol)) {
                LOG.debug("syncServicesTable: protocol '{}' is not in the database... adding.", protocol);
                m_serviceTypeDao.save(new OnmsServiceType(protocol));
                serviceNames.add(protocol);
            }
        }

        return serviceNames;
    }

    @Override
    public void syncManagementState() {
    }

    @Override
    public void syncSnmpPrimaryState() {
    }

    @Override
    public boolean isInterfaceInDB(final InetAddress ifAddress) {
        if (ifAddress == null) return false;
        return m_ipInterfaceDao.findByIpAddress(str(ifAddress)).size() > 0;
    }

    @Override
    public boolean isInterfaceInDB(final Connection dbConn, final InetAddress ifAddress) throws SQLException {
        return isInterfaceInDB(ifAddress);
    }

    @Override
    public Integer getServiceId(String name) {
        if (name == null) return null;
        final OnmsServiceType type = m_serviceTypeDao.findByName(name);
        return type == null? null : type.getId();
    }

    @Override
    public String getServiceName(final Integer id) {
        if (id == null) return null;
        final OnmsServiceType type = m_serviceTypeDao.get(id);
        return type == null? null : type.getName();
    }
    
    public void setOpennmsServerConfig(final OpennmsServerConfigFactory serverConfigFactory) {
        m_opennmsServerConfig = serverConfigFactory;
    }
    public void setCapsdConfig(final CapsdConfig capsdConfig) {
        m_capsdConfig = capsdConfig;
    }
    public void setPollerConfig(final PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }
    public void setCollectdConfig(final CollectdConfigFactory collectdConfig) {
        m_collectdConfig = collectdConfig;
    }
}
