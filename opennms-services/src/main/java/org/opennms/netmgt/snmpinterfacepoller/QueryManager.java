package org.opennms.netmgt.snmpinterfacepoller;


import java.util.List;

import org.opennms.netmgt.model.OnmsSnmpInterface;

public interface QueryManager {
    
    public List<OnmsSnmpInterface> getSnmpInterfaces(String criteria);
    public void saveSnmpInterface(OnmsSnmpInterface snmpinterfaces);

}
