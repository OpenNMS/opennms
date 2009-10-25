package org.opennms.netmgt.provision.adapters.link;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.monitors.IPv4Monitor;

public class LinkStatusMonitor extends IPv4Monitor {
    public List<EndPointStatusValidator> m_validators;
    private EndPointStatusValidator m_endPointValidator;

    public LinkStatusMonitor() {}
    
    private boolean isStatusUp() throws UnknownHostException {
        return m_endPointValidator != null ? m_endPointValidator.validate() : false;          
    }
    
    public void setEndPointValidator(EndPointStatusValidator validator) {
        m_endPointValidator = validator;
    }
    
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        if(parameters != null && parameters.containsKey("validator")) {
            setEndPointValidator( (EndPointStatusValidator) parameters.get("validator") );
        }
        
        try {
            if(isStatusUp()) {
               return PollStatus.up(); 
            }else {
                return PollStatus.down();
            }
        } catch (UnknownHostException e) {
            return PollStatus.down();
        }
    } 
}