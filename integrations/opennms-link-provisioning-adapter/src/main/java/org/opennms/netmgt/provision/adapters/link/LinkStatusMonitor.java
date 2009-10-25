package org.opennms.netmgt.provision.adapters.link;

import java.io.IOException;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.monitors.IPv4Monitor;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LinkStatusMonitor extends IPv4Monitor {
    
    public static final String SNMP_AGENTCONFIG_KEY = "org.opennms.netmgt.snmp.SnmpAgentConfig";
    private EndPointTypeValidatorFactory m_endPointValidatorFactory;

    public LinkStatusMonitor() {}
    
    @Override
    public void initialize(Map<String, Object> parameters) {
        ClassPathXmlApplicationContext appContext = BeanUtils.getFactory("linkAdapterPollerContext", ClassPathXmlApplicationContext.class);
        m_endPointValidatorFactory = (EndPointTypeValidatorFactory) appContext.getBean("endPointStatusValidatorFactory");
               
        try {
            SnmpPeerFactory.init();
        } catch (MarshalException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ValidationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return;
    }
    
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface iface = svc.getNetInterface();
//        SnmpAgentConfig agentConfig = (SnmpAgentConfig) iface.getAttribute(SNMP_AGENTCONFIG_KEY);
//        SnmpAgentValueGetter valueGetter = new SnmpAgentValueGetter(agentConfig); 
//        
//        String sysOid = valueGetter.getSysOid();
//        
//        EndPointStatusValidator validator;
//        try {
//            validator = m_endPointValidatorFactory.getEndPointStatusValidatorFor(sysOid);
//        }catch(Exception e) {
//            
//            return PollStatus.unavailable("Could not retreive endPoint configuration for EndPoint");
//        }
        throw new UnsupportedOperationException("Not yet implemented");
//        try {
//            if(validator!= null && validator.validate(valueGetter)) {
//                return PollStatus.available();
//            }else {
//                return PollStatus.unavailable();
//            }
//        } catch (UnknownHostException e) {
//            return PollStatus.unavailable();
//        }
        
    } 
    
    private static String getValue(SnmpAgentConfig agentConfig, String oid) {
        SnmpValue val = SnmpUtils.get(agentConfig, SnmpObjId.get(oid));
        if(val == null || val.isNull() || val.isEndOfMib() || val.isError()) {
            return null;
        }else {
            return val.toString();
        }
    }
}