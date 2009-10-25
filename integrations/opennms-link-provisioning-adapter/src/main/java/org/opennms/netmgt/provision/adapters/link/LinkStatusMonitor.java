package org.opennms.netmgt.provision.adapters.link;

import java.util.Map;

import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
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
    private NodeDao m_nodeDao;

    public LinkStatusMonitor() {}
    
    @Override
    public void initialize(Map<String, Object> parameters) {
        ClassPathXmlApplicationContext appContext = BeanUtils.getFactory("linkAdapterPollerContext", ClassPathXmlApplicationContext.class);
        m_configDao = (EndPointConfigurationDao) appContext.getBean("endPointConfigDao");
        m_nodeDao = (NodeDao) appContext.getBean("nodeDao");
               
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
        EndPointTypeValidator validator = m_configDao.getValidator();

        EndPointImpl ep = new EndPointImpl(svc.getAddress(), agentConfig);
        OnmsNode node = m_nodeDao.get(svc.getNodeId());
        ep.setSysOid(node.getSysObjectId());

        try {
            validator.validate(ep);
            return PollStatus.available();
        } catch (EndPointStatusException e) {
            return PollStatus.unavailable(e.getMessage());
        }
    } 
}