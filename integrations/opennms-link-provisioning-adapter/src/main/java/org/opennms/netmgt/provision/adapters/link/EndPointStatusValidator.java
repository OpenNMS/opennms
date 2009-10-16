package org.opennms.netmgt.provision.adapters.link;

import java.net.UnknownHostException;

import org.opennms.netmgt.snmp.SnmpAgentConfig;

public interface EndPointStatusValidator{
   public boolean validate(SnmpAgentConfig agentConfig) throws UnknownHostException;
}