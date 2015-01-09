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

package org.opennms.netmgt.provision.detector.endpoint;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.provision.adapters.link.EndPointImpl;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao;
import org.opennms.netmgt.provision.support.SyncAbstractDetector;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <p>EndPointDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Component
@Scope("prototype")
public class EndPointDetector extends SyncAbstractDetector implements InitializingBean {
    
    /** Constant <code>DEFAULT_SERVICE_NAME="EndPoint"</code> */
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

    /**
     * <p>Constructor for EndPointDetector.</p>
     */
    public EndPointDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public EndPointDetector(String serviceName, int port) {
        super(serviceName, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isServiceDetected(InetAddress address) {
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

    /**
     * <p>configureAgentVersion</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
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
    protected String getValue(SnmpAgentConfig agentConfig, String oid) {
        SnmpValue val = SnmpUtils.get(agentConfig, SnmpObjId.get(oid));
        if (val == null || val.isNull() || val.isEndOfMib() || val.isError()) {
            return null;
        }
        else {
            return val.toString();
        }
        
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
     * <p>setAgentConfigFactory</p>
     *
     * @param agentConfigFactory a {@link org.opennms.netmgt.config.api.SnmpAgentConfigFactory} object.
     */
    public void setAgentConfigFactory(SnmpAgentConfigFactory agentConfigFactory) {
        m_agentConfigFactory = agentConfigFactory;
    }
    
    /**
     * <p>getAgentConfigFactory</p>
     *
     * @return a {@link org.opennms.netmgt.config.api.SnmpAgentConfigFactory} object.
     */
    public SnmpAgentConfigFactory getAgentConfigFactory() {
        return m_agentConfigFactory;
    }

    /**
     * <p>setEndPointConfigurationDao</p>
     *
     * @param dao a {@link org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao} object.
     */
    public void setEndPointConfigurationDao(EndPointConfigurationDao dao) {
        m_configDao = dao;
    }
    
    /**
     * <p>getEndPointConfigurationDao</p>
     *
     * @return a {@link org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao} object.
     */
    public EndPointConfigurationDao getEndPointConfigurationDao() {
        return m_configDao;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onInit() {
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
    }
    

}
