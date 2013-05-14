/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.capsd.plugins;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Map;
import java.util.regex.Pattern;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * This class is used to test passed address for SNMP support. The configuration
 * used to determine the SNMP information is managed by the
 * {@link SnmpPeerFactorySnmpPeerFactory} class.
 *
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 * @version $Id: $
 */
public class SnmpPlugin extends AbstractPlugin {
    /**
     * The protocol supported by this plugin
     */
    private static final String PROTOCOL_NAME = "SNMP";

    /**
     * The system object identifier to retreive from the remote agent.
     */
    private static final String DEFAULT_OID = ".1.3.6.1.2.1.1.2.0";

    /**
     * Returns the name of the protocol that this plugin checks on the target
     * system for support.
     *
     * @return The protocol name for this plugin.
     */
    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     */
    @Override
    public boolean isProtocolSupported(InetAddress address) {
        try {
            SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(address);
            return (getValue(agentConfig, DEFAULT_OID) != null);

        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
        
    }
    
    private String getValue(SnmpAgentConfig agentConfig, String oid) {
        SnmpValue val = SnmpUtils.get(agentConfig, SnmpObjId.get(oid));
        if (val == null || val.isNull() || val.isEndOfMib() || val.isError()) {
            return null;
        }
        else {
            return val.toString();
        }
    }


    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     */
    @Override
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        
        try {

            String oid = ParameterMap.getKeyedString(qualifiers, "vbname", DEFAULT_OID);
            SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(address);
            String expectedValue = null;
            String isTable = null;
            
            if (qualifiers != null) {
                // "port" parm
                //
                if (qualifiers.get("port") != null) {
                    int port = ParameterMap.getKeyedInteger(qualifiers, "port", agentConfig.getPort());
                    agentConfig.setPort(port);
                }
                
                // "timeout" parm
                //
                if (qualifiers.get("timeout") != null) {
                    int timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", agentConfig.getTimeout());
                    agentConfig.setTimeout(timeout);
                }
                
                // "retry" parm
                //
                if (qualifiers.get("retry") != null) {
                    int retry = ParameterMap.getKeyedInteger(qualifiers, "retry", agentConfig.getRetries());
                    agentConfig.setRetries(retry);
                }
                
                // "force version" parm
                //
                if (qualifiers.get("force version") != null) {
                    String version = (String) qualifiers.get("force version");
                    if (version.equalsIgnoreCase("snmpv1"))
                        agentConfig.setVersion(SnmpAgentConfig.VERSION1);
                    else if (version.equalsIgnoreCase("snmpv2") || version.equalsIgnoreCase("snmpv2c"))
                        agentConfig.setVersion(SnmpAgentConfig.VERSION2C);
                    
                    //TODO: make sure JoeSnmpStrategy correctly handles this.
                    else if (version.equalsIgnoreCase("snmpv3"))
                        agentConfig.setVersion(SnmpAgentConfig.VERSION3);
                }
                
                // "vbvalue" parm
                //
                if (qualifiers.get("vbvalue") != null) {
                    expectedValue = (String) qualifiers.get("vbvalue");
                }
                
                if(qualifiers.get("table") != null) {
                	isTable = (String) qualifiers.get("table");
                }
            }
            
            if (isTable != null && isTable.equalsIgnoreCase("true")) {
            	
            	 SnmpObjId snmpObjId = SnmpObjId.get(oid);
            	
            	  Map<SnmpInstId, SnmpValue> table = SnmpUtils.getOidValues(agentConfig, "SnmpPlugin", snmpObjId);
            	  for (Map.Entry<SnmpInstId, SnmpValue> e : table.entrySet()) { 
                      if (e.getValue().toString().equals(expectedValue)) {	
                      	return true;
                      }    
                  }
            }
            
            else { 
            	String retrievedValue = getValue(agentConfig, oid);
            
            	if (retrievedValue != null && expectedValue != null) {
            		return (Pattern.compile(expectedValue).matcher(retrievedValue).find());
            	} else {
            		return (retrievedValue != null);
                
                //return (expectedValue == null ? true : retrievedValue.equals(expectedValue));
            	}
            
            }
            
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
        
        // should never get here.
        return false;
        
    }
}
