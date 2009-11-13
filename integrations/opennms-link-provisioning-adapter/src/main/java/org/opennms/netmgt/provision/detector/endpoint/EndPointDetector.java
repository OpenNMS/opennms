/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
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
package org.opennms.netmgt.provision.detector.endpoint;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;

import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.adapters.link.EndPointImpl;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao;
import org.opennms.netmgt.provision.support.AbstractDetector;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class EndPointDetector extends AbstractDetector {
    
    protected static final String DEFAULT_SERVICE_NAME = "EndPoint";

    /**
     * The system object identifier to retrieve from the remote agent.
     */
    private static final String SYS_OBJECT_ID = ".1.3.6.1.2.1.1.2.0";
    
    //These are -1 so by default we use the AgentConfig 
    private static final int DEFAULT_PORT = -1;
    private static final int DEFAULT_TIMEOUT = -1;
    private static final int DEFAULT_RETRIES = -1;
    
    private String m_forceVersion;
    
    @Autowired
    private SnmpAgentConfigFactory m_agentConfigFactory;

    @Autowired
    private EndPointConfigurationDao m_configDao;

    public EndPointDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     * 
     * @param serviceName
     * @param port
     */
    public EndPointDetector(String serviceName, int port) {
        super(serviceName, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    @Override
    public void init() {}

    @Override
    public boolean isServiceDetected(InetAddress address, DetectorMonitor detectMonitor) {
        try {

            SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(address);
            configureAgentPTR(agentConfig);
            configureAgentVersion(agentConfig);
            String retrievedValue = getValue(agentConfig, SYS_OBJECT_ID);

            EndPointTypeValidator validator = m_configDao.getValidator();
            EndPointImpl ep = new EndPointImpl();
            ep.setSysOid(retrievedValue);

            return validator.hasMatch(ep);
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
    }

    protected void configureAgentVersion(SnmpAgentConfig agentConfig) {
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
    }

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
    
    protected String getValue(SnmpAgentConfig agentConfig, String oid) {
        SnmpValue val = SnmpUtils.get(agentConfig, SnmpObjId.get(oid));
        if (val == null || val.isNull() || val.isEndOfMib() || val.isError()) {
            return null;
        }
        else {
            return val.toString();
        }
        
    }

    public void setForceVersion(String forceVersion) {
        m_forceVersion = forceVersion;
    }

    public String getForceVersion() {
        return m_forceVersion;
    }

    public void setAgentConfigFactory(SnmpAgentConfigFactory agentConfigFactory) {
        m_agentConfigFactory = agentConfigFactory;
    }
    
    public SnmpAgentConfigFactory getAgentConfigFactory() {
        return m_agentConfigFactory;
    }

    public void setEndPointConfigurationDao(EndPointConfigurationDao dao) {
        m_configDao = dao;
    }
    
    public EndPointConfigurationDao getEndPointConfigurationDao() {
        return m_configDao;
    }
    
    @Override
    protected void onInit() {
    }

    @Override
    public void dispose() {
    }
    

}
