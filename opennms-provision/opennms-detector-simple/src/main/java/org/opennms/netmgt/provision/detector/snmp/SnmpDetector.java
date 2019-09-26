/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.snmp;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.support.AgentBasedSyncAbstractDetector;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * <p>SnmpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SnmpDetector extends AgentBasedSyncAbstractDetector<SnmpAgentConfig>  {

    public enum MatchType {
        // Service detected if 1 or more entries match an expected value
        Any {
            @Override
            boolean isServiceDetected(List<String> retrievedValues, String expectedValuePattern) {
                Pattern expectedPattern = Pattern.compile(Objects.requireNonNull(expectedValuePattern));
                retrievedValues = removeNullElements(retrievedValues);
                if (retrievedValues.isEmpty()) {
                    return false;
                }
                boolean anyMatch = retrievedValues
                        .stream()
                        .anyMatch(eachRetrievedValue -> expectedPattern.matcher(eachRetrievedValue).matches());
                return anyMatch;
            }
        },
        // Service detected if ALL entries match an expected value
        All {
            @Override
            boolean isServiceDetected(List<String> retrievedValues, String expectedValuePattern) {
                Pattern expectedPattern = Pattern.compile(Objects.requireNonNull(expectedValuePattern));
                retrievedValues = removeNullElements(retrievedValues);
                if (retrievedValues.isEmpty()) {
                    return false;
                }
                boolean allMatch = retrievedValues
                        .stream()
                        .allMatch(eachRetrievedValue -> expectedPattern.matcher(eachRetrievedValue).matches());
                return allMatch;
            }
        },
        // Service detected in the meaning of invert Any
        None {
            @Override
            boolean isServiceDetected(List<String> retrievedValues, String expectedValuePattern) {
                return !Any.isServiceDetected(retrievedValues, expectedValuePattern);
            }
        },
        // Service detected if the table exist
        Exist {
            @Override
            boolean isServiceDetected(List<String> retrievedValues, String expectedValuePattern) {
                return !removeNullElements(retrievedValues).isEmpty();
            }
        };

        abstract boolean isServiceDetected(List<String> retrievedValues, String expectedValuePattern);

        private static List<String> removeNullElements(List<String> input) {
            return input
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        public static MatchType createFrom(String input) {
            Objects.requireNonNull(input);
            for (MatchType eachType : values()) {
                if (eachType.name().equalsIgnoreCase(input)) {
                    return eachType;
                }
            }
            throw new IllegalArgumentException("No MatchType found for name " + input);
        }
    }
    
    /** Constant <code>DEFAULT_SERVICE_NAME="SNMP"</code> */
    protected static final String DEFAULT_SERVICE_NAME = "SNMP";

    private static final Logger LOG = LoggerFactory.getLogger(SnmpDetector.class);

    /**
     * The system object identifier to retrieve from the remote agent.
     */
    private static final String DEFAULT_OID = ".1.3.6.1.2.1.1.2.0";
    
    //These are -1 so by default we use the AgentConfig
    private static final int DEFAULT_PORT = -1;
    private static final int DEFAULT_TIMEOUT = -1;
    private static final int DEFAULT_RETRIES = -1;
    
    private String m_oid = DEFAULT_OID;
    private boolean m_isTable = false;
    private boolean m_hex = false;

    private String m_forceVersion;
    private String m_vbvalue;

    private MatchType matchType;

    /**
     * <p>Constructor for SnmpDetector.</p>
     */
    public SnmpDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public SnmpDetector(String serviceName, int port) {
        super(serviceName, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    public String getIsTable() {
        return String.valueOf(m_isTable);
    }

    public void setIsTable(String table) {
        m_isTable = "true".equalsIgnoreCase(table);
    }

    public void setHex(String hex) {
        m_hex = "true".equalsIgnoreCase(hex);
    }

    public String getHex() {
        return String.valueOf(m_hex);
    }

    protected boolean isHex() {
        return m_hex;
    }

    @Override
    public SnmpAgentConfig getAgentConfig(DetectRequest request) {
        if (request.getRuntimeAttributes() != null) {
            // All of the keys in the runtime attribute map are used to store the agent configuration
            return SnmpAgentConfig.fromMap(request.getRuntimeAttributes());
        } else {
            return new SnmpAgentConfig();
        }
    }

    @Override
    public boolean isServiceDetected(InetAddress address, SnmpAgentConfig agentConfig) {
        try {
            configureAgentPTR(agentConfig);
            configureAgentVersion(agentConfig);

            final String expectedValue = getVbvalue();
            if (this.m_isTable) {
                LOG.debug(getServiceName() + ": table detect enabled");
                final SnmpObjId snmpObjId = SnmpObjId.get(getOid());
                final Map<SnmpInstId, SnmpValue> table = SnmpUtils.getOidValues(agentConfig, DEFAULT_SERVICE_NAME, snmpObjId);
                final List<String> retrievedValues = table.values().stream().map(snmpValue -> m_hex ? snmpValue.toHexString() : snmpValue.toString()).collect(Collectors.toList());
                return isServiceDetected(this.matchType, retrievedValues, expectedValue);
            } else {
                final String retrievedValue = getValue(agentConfig, getOid(), m_hex);
                // we have to ensure that if expectedValue is defined, we use ANY, this is due to backwards compatibility
                MatchType matchType = this.matchType;
                if (matchType == null && expectedValue != null) {
                    matchType = MatchType.Any;
                }
                return isServiceDetected(matchType, Lists.newArrayList(retrievedValue), expectedValue);
            }
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
    }

    private boolean isServiceDetected(MatchType matchType, List<String> retrievedValues, String expectedValue) {
        matchType = matchType == null ? MatchType.Exist : matchType;
        // If matchType is NOT Exist, than we need an expectedValue
        if (matchType != MatchType.Exist && expectedValue == null) {
            throw new IllegalArgumentException(getServiceName() + ": expectedValue was not defined using matchType=" + matchType + " but is required. Otherwise set matchType to " + MatchType.Exist);
        }
        boolean isServiceDetected = matchType.isServiceDetected(retrievedValues, expectedValue);
        LOG.debug(getServiceName() + ": services detected {} using matchType={}, expectedValue={}, retrievedValues={}",isServiceDetected, matchType, expectedValue, retrievedValues);
        return isServiceDetected;
    }

    /**
     * <p>configureAgentVersion</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    protected void configureAgentVersion(SnmpAgentConfig agentConfig) {
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
    }

    /**
     * <p>configureAgentPTR</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    protected void configureAgentPTR(SnmpAgentConfig agentConfig) {
        if (getPort() > 0) {
            agentConfig.setPort(getPort());
        }
        
        if (getTimeout() > 0) {
            agentConfig.setTimeout(getTimeout());
        }
        
        if (getRetries() > -1) {
            agentConfig.setRetries(getRetries());
        }
    }
    
    /**
     * <p>getValue</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oid a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected  static String getValue(SnmpAgentConfig agentConfig, String oid, boolean hex) {
        SnmpValue val = SnmpUtils.get(agentConfig, SnmpObjId.get(oid));
        if (val == null || val.isNull() || val.isEndOfMib() || val.isError()) {
            return null;
        }  else {
            return hex ? val.toHexString() : val.toString();
        }
        
    }

    /**
     * <p>setOid</p>
     *
     * @param oid a {@link java.lang.String} object.
     */
    public void setOid(String oid) {
        m_oid = oid;
    }

    /**
     * <p>getOid</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOid() {
        return m_oid;
    }

    /**
     * <p>setForceVersion</p>
     *
     * @param forceVersion a {@link java.lang.String} object.
     */
    public void setForceVersion(String forceVersion) {
        m_forceVersion = forceVersion;
    }

    /**
     * <p>getForceVersion</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForceVersion() {
        return m_forceVersion;
    }

    /**
     * <p>setVbvalue</p>
     *
     * @param vbvalue a {@link java.lang.String} object.
     */
    public void setVbvalue(String vbvalue) {
        m_vbvalue = vbvalue;
    }

    /**
     * <p>getVbvalue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVbvalue() {
        return m_vbvalue;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.detector.AbstractDetector#onInit()
     */
    /** {@inheritDoc} */
    @Override
    protected void onInit() {
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
    }

    public void setMatchType(String matchType) {
        this.matchType = MatchType.createFrom(matchType);
    }

    public String getMatchType() {
        return matchType.name();
    }
}
