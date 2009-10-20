package org.opennms.netmgt.provision.adapters.link;

import java.util.Map;

import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.monitors.IPv4Monitor;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LinkStatusMonitor extends IPv4Monitor {
    
    public static final String SNMP_AGENTCONFIG_KEY = "org.opennms.netmgt.snmp.SnmpAgentConfig";
    private EndPointConfigurationDao m_configDao;

    public LinkStatusMonitor() {}
    
    @Override
    public void initialize(Map<String, Object> parameters) {
        ClassPathXmlApplicationContext appContext = BeanUtils.getFactory("linkAdapterPollerContext", ClassPathXmlApplicationContext.class);
        m_configDao = (EndPointConfigurationDao) appContext.getBean("endPointConfigDao");
               
        try {
            SnmpPeerFactory.init();
        } catch (Exception e) {
            LogUtils.debugf(this, e, "Unable to initialize SNMP peer factory");
        }
        
        return;
    }
    
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface iface = svc.getNetInterface();
        SnmpAgentConfig agentConfig = (SnmpAgentConfig) iface.getAttribute(SNMP_AGENTCONFIG_KEY);
        EndPointImpl ep = new EndPointImpl(svc.getAddress(), agentConfig);
        EndPointTypeValidator validator = m_configDao.getValidator();

        try {
            validator.validate(ep);
            return PollStatus.available();
        } catch (EndPointStatusException e) {
            return PollStatus.unavailable(e.getMessage());
        }
    } 
}