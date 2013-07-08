/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



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
public final class DiskUsagePlugin extends AbstractPlugin {
    
    private static final Logger LOG = LoggerFactory.getLogger(DiskUsagePlugin.class);
    
    /**
     * The protocol supported by this plugin
     */
    private static final String PROTOCOL_NAME = "DiskUsage";

    /**
     * The system object identifier to retreive from the remote agent.
     */
    private static final String DEFAULT_OID = ".1.3.6.1.2.1.1.2.0";

    
    private static final String hrStorageDescr = ".1.3.6.1.2.1.25.2.3.1.3";
    
    /**
     * The available match-types for this plugin
     */
    private static final int MATCH_TYPE_EXACT = 0;
    private static final int MATCH_TYPE_STARTSWITH = 1;
    private static final int MATCH_TYPE_ENDSWITH = 2;
    private static final int MATCH_TYPE_REGEX = 3;
    
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
        return (val == null ? null : val.toString());
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
        int matchType = MATCH_TYPE_EXACT;

        try {

            //String oid = ParameterMap.getKeyedString(qualifiers, "vbname", DEFAULT_OID);
        	
        	String disk = ParameterMap.getKeyedString(qualifiers, "disk",null);
        	
            SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(address);
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
                
                // "match-type" parm
                //
                if (qualifiers.get("match-type") != null) {
                    String matchTypeStr = ParameterMap.getKeyedString(qualifiers, "match-type", "exact");
                    if (matchTypeStr.equalsIgnoreCase("exact")) {
                        matchType = MATCH_TYPE_EXACT; 
                    } else if (matchTypeStr.equalsIgnoreCase("startswith")) {
                        matchType = MATCH_TYPE_STARTSWITH;
                    } else if (matchTypeStr.equalsIgnoreCase("endswith")) {
                        matchType = MATCH_TYPE_ENDSWITH;
                    } else if (matchTypeStr.equalsIgnoreCase("regex")) {
                        matchType = MATCH_TYPE_REGEX;
                    } else {
                        throw new RuntimeException("Unknown value '" + matchTypeStr + "' for parameter 'match-type'");
                    }
                }
                
                
            }
                
                SnmpObjId hrStorageDescrSnmpObject = SnmpObjId.get(hrStorageDescr);
                
                Map<SnmpInstId, SnmpValue> descrResults = SnmpUtils.getOidValues(agentConfig, "DiskUsagePoller", hrStorageDescrSnmpObject);
                
                if(descrResults.size() == 0) {
                    return false;
                }

                for (Map.Entry<SnmpInstId, SnmpValue> e : descrResults.entrySet()) { 
                    LOG.debug("capsd: SNMPwalk succeeded, addr={} oid={} instance={} value={}", InetAddressUtils.str(address), hrStorageDescrSnmpObject, e.getKey(), e.getValue());
                  
                    if (isMatch(e.getValue().toString(), disk, matchType)) {
			LOG.debug("Found disk '{}' (matching hrStorageDescr was '{}')", disk, e.getValue());
                    	return true;
                    		
                    }
                     
                }
                
                return false;
        
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
        
    }
    
    private boolean isMatch(String candidate, String target, int matchType) {
        boolean matches = false;
        LOG.debug("isMessage: candidate is '{}', matching against target '{}'", candidate, target);
        if (matchType == MATCH_TYPE_EXACT) {
            LOG.debug("Attempting equality match: candidate '{}', target '{}'", candidate, target);
            matches = candidate.equals(target);
        } else if (matchType == MATCH_TYPE_STARTSWITH) {
            LOG.debug("Attempting startsWith match: candidate '{}', target '{}'", candidate, target);
            matches = candidate.startsWith(target);
        } else if (matchType == MATCH_TYPE_ENDSWITH) {
            LOG.debug("Attempting endsWith match: candidate '{}', target '{}'", candidate, target);
            matches = candidate.endsWith(target);
        } else if (matchType == MATCH_TYPE_REGEX) {
            LOG.debug("Attempting endsWith match: candidate '{}', target '{}'", candidate, target);
            matches = Pattern.compile(target).matcher(candidate).find();
        }
        LOG.debug("isMatch: Match is positive");
        return matches;
    }
}
