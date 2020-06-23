/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.wmi;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.wmi.WmiAgentConfig;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.support.AgentBasedSyncAbstractDetector;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.WmiManager;
import org.opennms.protocols.wmi.WmiParams;
import org.opennms.protocols.wmi.WmiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WmiDetector extends AgentBasedSyncAbstractDetector<WmiAgentConfig> {

    public static final Logger LOG = LoggerFactory.getLogger(WmiDetector.class);
    
    private static final String PROTOCOL_NAME = "WMI";

    private static final String DEFAULT_WMI_CLASS = "Win32_ComputerSystem";
    private static final String DEFAULT_WMI_OBJECT = "Status";
    private static final String DEFAULT_WMI_COMP_VAL = "OK";
    private static final String DEFAULT_WMI_MATCH_TYPE = "all";
    private static final String DEFAULT_WMI_COMP_OP = "EQ";
    private static final String DEFAULT_WMI_WQL = "NOTSET";
    private static final String DEFAULT_WMI_NAMESPACE = WmiParams.WMI_DEFAULT_NAMESPACE;

    private String m_matchType;

    private String m_compVal;

    private String m_compOp;

    private String m_wmiClass;

    private String m_wmiObject;

    private String m_wmiWqlStr;

    private String m_username;

    private String m_password;

    private String m_domain;
    
    private String m_namespace;
    
    public WmiDetector() {
        super(PROTOCOL_NAME, 0);
    }

    @Override
    protected void onInit() {
        setMatchType(getMatchType() != null ? getMatchType() : DEFAULT_WMI_MATCH_TYPE);
        setCompVal(getCompVal() != null ? getCompVal() : DEFAULT_WMI_COMP_VAL);
        setCompOp(getCompOp() != null ? getCompOp() : DEFAULT_WMI_COMP_OP);
        setWmiClass(getWmiClass() != null ? getWmiClass() : DEFAULT_WMI_CLASS);
        setWmiObject(getWmiObject() != null ? getWmiObject() : DEFAULT_WMI_OBJECT);
        setWmiWqlStr(getWmiWqlStr() != null ? getWmiWqlStr() : DEFAULT_WMI_WQL);
        setNamespace(getNamespace() != null ? getNamespace() : DEFAULT_WMI_NAMESPACE);
    }

    @Override
    public WmiAgentConfig getAgentConfig(DetectRequest request) {
        if (request.getRuntimeAttributes() != null) {
            // All of the keys in the runtime attribute map are used to store the agent configuration
            return WmiAgentConfig.fromMap(request.getRuntimeAttributes());
        } else {
            return new WmiAgentConfig();
        }
    }

    @Override
    public boolean isServiceDetected(final InetAddress address, final WmiAgentConfig agentConfig) {
        WmiParams clientParams = null;

        if(getWmiWqlStr().equals(DEFAULT_WMI_WQL)) {
            // Create the check parameters holder.
            clientParams = new WmiParams(WmiParams.WMI_OPERATION_INSTANCEOF,
                                         getCompVal(), getCompOp(), getWmiClass(), getWmiObject());
        } else {
            // Define the WQL Query.
            clientParams = new WmiParams(WmiParams.WMI_OPERATION_WQL,
                                         getCompVal(), getCompOp(), getWmiWqlStr(), getWmiObject());
        }

        // Use WMI credentials from configuration files, and override values with the detector parameters if they exists.
        if (getUsername() != null)
            agentConfig.setUsername(getUsername());
        if (getPassword() != null)
            agentConfig.setPassword(getPassword());
        if (getDomain() != null)
            agentConfig.setDomain(getDomain());
        if (getRetries() > 0)
            agentConfig.setRetries(getRetries());
        if (getTimeout() > 0)
            agentConfig.setTimeout(getTimeout());

        // Perform the operation specified in the parameters.
        WmiResult result = isServer(address, agentConfig.getUsername(), agentConfig.getPassword(), agentConfig.getDomain(), getNamespace(), getMatchType(),
                agentConfig.getRetries(), agentConfig.getTimeout(), clientParams);

        // Only fail on critical and unknown returns.
        return (result != null && result.getResultCode() != WmiResult.RES_STATE_CRIT
                && result.getResultCode() != WmiResult.RES_STATE_UNKNOWN);
    }
    
    private WmiResult isServer(InetAddress host, String user, String pass,
            String domain, String namespace, String matchType, int retries, int timeout,
            WmiParams params) {
        boolean isAServer = false;

        WmiResult result = null;
        for (int attempts = 0; attempts <= retries && !isAServer; attempts++) {
            WmiManager mgr = null;
            try {
                mgr = new WmiManager(InetAddressUtils.str(host), user,
                        pass, domain, matchType);
                mgr.setNamespace(namespace);

                // Connect to the WMI server.
                mgr.init();

                // Perform the operation specified in the parameters.
                result = mgr.performOp(params);
                if(params.getWmiOperation().equals(WmiParams.WMI_OPERATION_WQL)) {
                    LOG.debug("WmiPlugin: {} : {}", params.getWql(), WmiResult.convertStateToString(result.getResultCode()));
                } else {
                    LOG.debug("WmiPlugin: \\\\{}\\{} : {}", params.getWmiClass(), params.getWmiObject(), WmiResult.convertStateToString(result.getResultCode()));
                }

                isAServer = true;
            } catch (WmiException e) {
                final StringBuilder message = new StringBuilder();
                message.append("WmiPlugin: Check failed... : ");
                message.append(e.getMessage());
                message.append(" : ");
                message.append((e.getCause() == null ? "" : e.getCause().getMessage()));
                LOG.info(message.toString());
                isAServer = false;
            } finally {
                if (mgr != null) {
                    try {
                        mgr.close();
                    } catch (WmiException e) {
                        LOG.warn("an error occurred closing the WMI Manager", e);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void dispose() {
        // pass
    }


    public void setMatchType(String matchType) {
        m_matchType = matchType;
    }


    public String getMatchType() {
        return m_matchType;
    }


    public void setCompVal(String compVal) {
        m_compVal = compVal;
    }


    public String getCompVal() {
        return m_compVal;
    }


    public void setCompOp(String compOp) {
        m_compOp = compOp;
    }


    public String getCompOp() {
        return m_compOp;
    }


    public void setWmiClass(String wmiClass) {
        m_wmiClass = wmiClass;
    }


    public String getWmiClass() {
        return m_wmiClass;
    }


    public void setWmiObject(String wmiObject) {
        m_wmiObject = wmiObject;
    }


    public String getWmiObject() {
        return m_wmiObject;
    }


    public void setWmiWqlStr(String wmiWqlStr) {
        m_wmiWqlStr = wmiWqlStr;
    }


    public String getWmiWqlStr() {
        return m_wmiWqlStr;
    }


    public void setUsername(String username) {
        m_username = username;
    }


    public String getUsername() {
        return m_username;
    }


    public void setPassword(String password) {
        m_password = password;
    }


    public String getPassword() {
        return m_password;
    }


    public void setDomain(String domain) {
        m_domain = domain;
    }


    public String getDomain() {
        return m_domain;
    }

    public void setNamespace(String namespace) {
        m_namespace = namespace;
    }


    public String getNamespace() {
        return m_namespace;
    }
}
