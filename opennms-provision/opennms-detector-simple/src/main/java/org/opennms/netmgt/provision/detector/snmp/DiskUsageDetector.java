/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.snmp;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DiskUsageDetector extends SnmpDetector {
    /**
     * The protocol supported by this plugin
     */
    private static final String PROTOCOL_NAME = "DiskUsage";

    /**
     * The system object identifier to retrieve from the remote agent.
     */
    private static final String DEFAULT_OID = ".1.3.6.1.2.1.1.2.0";

    
    private static final String hrStorageDescr = ".1.3.6.1.2.1.25.2.3.1.3";
    
    /**
     * The available match-types for this detector
     */
    private static final int MATCH_TYPE_EXACT = 0;
    private static final int MATCH_TYPE_STARTSWITH = 1;
    private static final int MATCH_TYPE_ENDSWITH = 2;
    private static final int MATCH_TYPE_REGEX = 3;
    
    private String m_matchType = "";
    private String m_disk;
    private String m_hrStorageDescr;
    
    public DiskUsageDetector(){
        setServiceName(PROTOCOL_NAME);
        setOid(DEFAULT_OID);
        setHrStorageDescr(hrStorageDescr);
    }
    
    /**
     * Returns the name of the protocol that this plugin checks on the target
     * system for support.
     * 
     * @return The protocol name for this plugin.
     */
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * 
     * @param address
     *            The address to check for support.
     * 
     * @return True if the protocol is supported by the address.
     */
    public boolean isProtocolSupported(InetAddress address) {
        try {
            SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(address);
            return (getValue(agentConfig, DEFAULT_OID) != null);

        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
        
    }

    /**
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     * 
     * @param address
     *            The address to check for support.
     * @param qualifiers
     *            The map where qualification are set by the plugin.
     * 
     * @return True if the protocol is supported by the address.
     */
    public boolean isServiceDetected(InetAddress address, DetectorMonitor detectMonitor) {
        int matchType = MATCH_TYPE_EXACT;

        try {

            SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(address);
            String expectedValue = null;
            
            if (getPort() > 0) {
                agentConfig.setPort(getPort());
            }
            
            if (getTimeout() > 0) {
                agentConfig.setTimeout(getTimeout());
            }
            
            if (getRetries() > -1) {
                agentConfig.setRetries(getRetries());
            }
            
            if (getForceVersion() != null) {
                String version = getForceVersion();
                
                if (version.equalsIgnoreCase("snmpv1")) {
                    agentConfig.setVersion(SnmpAgentConfig.VERSION1);
                } else if (version.equalsIgnoreCase("snmpv2") || version.equalsIgnoreCase("snmpv2c")) {
                    agentConfig.setVersion(SnmpAgentConfig.VERSION2C);
                } else if (version.equalsIgnoreCase("snmpv3")) {
                    agentConfig.setVersion(SnmpAgentConfig.VERSION3);
                }
            }
                
            // "match-type" parm
            //
            if (!"".equals(getMatchType())) {
                String matchTypeStr = getMatchType();
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

            SnmpObjId hrStorageDescrSnmpObject = SnmpObjId.get(getHrStorageDescr());
            
            Map<SnmpInstId, SnmpValue> descrResults = SnmpUtils.getOidValues(agentConfig, "DiskUsagePoller", hrStorageDescrSnmpObject);
            
            if(descrResults.size() == 0) {
                return false;
            }

            for (Map.Entry<SnmpInstId, SnmpValue> e : descrResults.entrySet()) { 
                log().debug("capsd: SNMPwalk succeeded, addr=" + address.getHostAddress() + " oid=" + hrStorageDescrSnmpObject + " instance=" + e.getKey() + " value=" + e.getValue());
              
                if (isMatch(e.getValue().toString(), getDisk(), matchType)) {
                    log().debug("Found disk '" + getDisk() + "' (matching hrStorageDescr was '" + e.getValue().toString() + "'");
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
        log().debug("isMessage: candidate is '" + candidate + "', matching against target '" + target + "'");
        if (matchType == MATCH_TYPE_EXACT) {
            log().debug("Attempting equality match: candidate '" + candidate + "', target '" + target + "'");
            matches = candidate.equals(target);
        } else if (matchType == MATCH_TYPE_STARTSWITH) {
            log().debug("Attempting startsWith match: candidate '" + candidate + "', target '" + target + "'");
            matches = candidate.startsWith(target);
        } else if (matchType == MATCH_TYPE_ENDSWITH) {
            log().debug("Attempting endsWith match: candidate '" + candidate + "', target '" + target + "'");
            matches = candidate.endsWith(target);
        } else if (matchType == MATCH_TYPE_REGEX) {
            log().debug("Attempting endsWith match: candidate '" + candidate + "', target '" + target + "'");
            matches = Pattern.compile(target).matcher(candidate).find();
        }
        log().debug("isMatch: Match is positive");
        return matches;
    }
    
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void setMatchType(String matchType) {
        m_matchType = matchType;
    }

    public String getMatchType() {
        return m_matchType;
    }

    public void setDisk(String disk) {
        m_disk = disk;
    }

    public String getDisk() {
        return m_disk;
    }

    public void setHrStorageDescr(String hrStorageDescr) {
        m_hrStorageDescr = hrStorageDescr;
    }

    public String getHrStorageDescr() {
        return m_hrStorageDescr;
    }
}
