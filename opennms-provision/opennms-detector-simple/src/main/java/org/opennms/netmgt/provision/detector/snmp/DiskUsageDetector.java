/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.detector.snmp;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Map;
import java.util.regex.Pattern;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>DiskUsageDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */

public class DiskUsageDetector extends SnmpDetector {

	private static final Logger LOG = LoggerFactory.getLogger(DiskUsageDetector.class);

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
    
    /**
     * <p>Constructor for DiskUsageDetector.</p>
     */
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
     * @return True if the protocol is supported by the address.
     */
    public boolean isProtocolSupported(InetAddress address) {
        try {
            SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(address);
            return getValue(agentConfig, DEFAULT_OID, isHex()) != null;

        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
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
    public boolean isServiceDetected(final InetAddress address, final SnmpAgentConfig agentConfig) {
        int matchType = MATCH_TYPE_EXACT;

        try {

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
                
                // TODO: Deprecate the snmpv1, snmpv2, snmpv2c, snmpv3 params in favor of more-used v1, v2c, and v3
                // @see http://issues.opennms.org/browse/NMS-7518
                if ("v1".equalsIgnoreCase(version) || "snmpv1".equalsIgnoreCase(version)) {
                    agentConfig.setVersion(SnmpAgentConfig.VERSION1);
                } else if ("v2".equalsIgnoreCase(version) || "v2c".equalsIgnoreCase(version) || "snmpv2".equalsIgnoreCase(version) || "snmpv2c".equalsIgnoreCase(version)) {
                    agentConfig.setVersion(SnmpAgentConfig.VERSION2C);
                } else if ("v3".equalsIgnoreCase(version) || "snmpv3".equalsIgnoreCase(version)) {
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
                LOG.debug("capsd: SNMPwalk succeeded, addr={} oid={} instance={} value={}", InetAddressUtils.str(agentConfig.getAddress()), hrStorageDescrSnmpObject, e.getKey(), e.getValue());
              
                if (isMatch(e.getValue().toString(), getDisk(), matchType)) {
                    LOG.debug("Found disk '{}' (matching hrStorageDescr was '{}')", getDisk(), e.getValue());
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
    
    /**
     * <p>setMatchType</p>
     *
     * @param matchType a {@link java.lang.String} object.
     */
    public void setMatchType(String matchType) {
        m_matchType = matchType;
    }

    /**
     * <p>getMatchType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMatchType() {
        return m_matchType;
    }

    /**
     * <p>setDisk</p>
     *
     * @param disk a {@link java.lang.String} object.
     */
    public void setDisk(String disk) {
        m_disk = disk;
    }

    /**
     * <p>getDisk</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDisk() {
        return m_disk;
    }

    /**
     * <p>Setter for the field <code>hrStorageDescr</code>.</p>
     *
     * @param hrStorageDescr a {@link java.lang.String} object.
     */
    public void setHrStorageDescr(String hrStorageDescr) {
        m_hrStorageDescr = hrStorageDescr;
    }

    /**
     * <p>Getter for the field <code>hrStorageDescr</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHrStorageDescr() {
        return m_hrStorageDescr;
    }
}
