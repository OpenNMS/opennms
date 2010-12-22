package org.opennms.netmgt.provision.adapters.link;

import java.util.Map;

import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.monitors.AbstractServiceMonitor;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <p>EndPointMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class EndPointMonitor extends AbstractServiceMonitor {
    
    /** Constant <code>SNMP_AGENTCONFIG_KEY="org.opennms.netmgt.snmp.SnmpAgentConfig"</code> */
    public static final String SNMP_AGENTCONFIG_KEY = "org.opennms.netmgt.snmp.SnmpAgentConfig";
    private EndPointConfigurationDao m_configDao;
    private NodeDao m_nodeDao;
    private SnmpAgentConfigFactory m_agentConfigFactory;

    /**
     * <p>Constructor for EndPointMonitor.</p>
     */
    public EndPointMonitor() {}
    
    /** {@inheritDoc} */
    @Override
    public void initialize(Map<String, Object> parameters) {
        ClassPathXmlApplicationContext appContext = BeanUtils.getFactory("linkAdapterPollerContext", ClassPathXmlApplicationContext.class);
        m_configDao = (EndPointConfigurationDao) appContext.getBean("endPointConfigDao");
        m_nodeDao = (NodeDao) appContext.getBean("nodeDao");
        m_agentConfigFactory = (SnmpAgentConfigFactory) appContext.getBean("snmpPeerFactory", SnmpAgentConfigFactory.class);
               
    }
    
    /** {@inheritDoc} */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        SnmpAgentConfig agentConfig = m_agentConfigFactory.getAgentConfig(svc.getAddress());
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
