/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.core.xml.MarshallingExceptionTranslator;
import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.springframework.beans.factory.InitializingBean;

/**
 * DefaultSnmpConfigDao
 *
 * @author brozow
 * @version $Id: $
 */
// TODO MVR what should we do here?
public class FactoryBasedSnmpConfigDao implements SnmpConfigDao, InitializingBean {
    
    private static final MarshallingExceptionTranslator TRANSLATOR = new MarshallingExceptionTranslator();
    
    private SnmpPeerFactory getSnmpPeerFactory() {
        return SnmpPeerFactory.getInstance();
    }
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        SnmpPeerFactory.init();
    }

    /** {@inheritDoc} */
    public SnmpAgentConfig getAgentConfig(final InetAddress agentAddress) {
        return getSnmpPeerFactory().getAgentConfig(agentAddress);
    }
    
    /**
     * <p>getDefaults</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpConfiguration} object.
     */
    public SnmpConfiguration getDefaults() {
    	final SnmpConfig config = getSnmpConfig();

    	final SnmpConfiguration defaults = new SnmpConfiguration();

        if (config.getAuthPassphrase() != null) {
            defaults.setAuthPassPhrase(config.getAuthPassphrase());
        } 
        if (config.getAuthProtocol() != null) {
            defaults.setAuthProtocol(config.getAuthProtocol());
        }
        if (config.hasMaxRepetitions()) {
            defaults.setMaxRepetitions(config.getMaxRepetitions());
        }
        if (config.hasMaxRequestSize()) {
            defaults.setMaxRequestSize(config.getMaxRequestSize());
        }
        if (config.hasMaxVarsPerPdu()) {
            defaults.setMaxVarsPerPdu(config.getMaxVarsPerPdu());
        }
        if (config.hasPort()) {
            defaults.setPort(config.getPort());
        }
        if (config.getPrivacyPassphrase() != null) {
            defaults.setPrivPassPhrase(config.getPrivacyPassphrase());
        }
        if (config.getPrivacyProtocol() != null) {
            defaults.setPrivProtocol(config.getPrivacyProtocol());
        }
        if (config.getReadCommunity() != null) {
            defaults.setReadCommunity(config.getReadCommunity());
        }
        if (config.hasRetry()) {
            defaults.setRetries(config.getRetry());
        }
        if (config.hasSecurityLevel()) {
            defaults.setSecurityLevel(config.getSecurityLevel());
        }
        if (config.getSecurityName() != null) {
            defaults.setSecurityName(config.getSecurityName());
        }
        if (config.hasTimeout()) {
            defaults.setTimeout(config.getTimeout());
        }
        if (config.getVersion() != null) {
            defaults.setVersionAsString(config.getVersion());
        }
        if (config.getWriteCommunity() != null) {
            defaults.setWriteCommunity(config.getWriteCommunity());
        }

        return defaults;
    }
    
    private boolean nullSafeEquals(final String o1, final String o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1.equals(o2)) {
            return true;
        }
        return false;
    }


    /** {@inheritDoc} */
    public void saveAsDefaults(final SnmpConfiguration newDefaults) {
    	final SnmpConfig config = getSnmpConfig();
        
    	final SnmpConfiguration oldDefaults = getDefaults();
        
        if (!nullSafeEquals(oldDefaults.getAuthPassPhrase(), newDefaults.getAuthPassPhrase())) {
            config.setAuthPassphrase(newDefaults.getAuthPassPhrase());
        }
        if (!nullSafeEquals(oldDefaults.getAuthProtocol(), newDefaults.getAuthProtocol())) {
            config.setAuthProtocol(newDefaults.getAuthProtocol());
        }
        if (oldDefaults.getMaxRepetitions() != newDefaults.getMaxRepetitions()) {
            config.setMaxRepetitions(newDefaults.getMaxRepetitions());
        }
        if (oldDefaults.getMaxRequestSize() != newDefaults.getMaxRequestSize()) {
            config.setMaxRequestSize(newDefaults.getMaxRequestSize());
        }
        if (oldDefaults.getMaxVarsPerPdu() != newDefaults.getMaxVarsPerPdu()) {
            config.setMaxVarsPerPdu(newDefaults.getMaxVarsPerPdu());
        }
        if (oldDefaults.getPort() != newDefaults.getPort()) {
            config.setPort(newDefaults.getPort());
        }
        if (!nullSafeEquals(oldDefaults.getPrivPassPhrase(), newDefaults.getPrivPassPhrase())) {
            config.setPrivacyPassphrase(newDefaults.getPrivPassPhrase());
        }
        if (!nullSafeEquals(oldDefaults.getPrivProtocol(), newDefaults.getPrivProtocol())) {
            config.setPrivacyProtocol(newDefaults.getPrivProtocol());
        }
        if (!nullSafeEquals(oldDefaults.getReadCommunity(), newDefaults.getReadCommunity())) {
            config.setReadCommunity(newDefaults.getReadCommunity());
        }
        if (oldDefaults.getRetries() != newDefaults.getRetries()) {
            config.setRetry(newDefaults.getRetries());
        }
        if (oldDefaults.getSecurityLevel() != newDefaults.getSecurityLevel()) {
            config.setSecurityLevel(newDefaults.getSecurityLevel());
        }
        if (!nullSafeEquals(oldDefaults.getSecurityName(), newDefaults.getSecurityName())) {
            config.setSecurityName(newDefaults.getSecurityName());
        }
        if (oldDefaults.getTimeout() != newDefaults.getTimeout()) {
            config.setTimeout(newDefaults.getTimeout());
        }
        if (oldDefaults.getVersion() != newDefaults.getVersion()) {
            config.setVersion(newDefaults.getVersionAsString());
        }
        if (!nullSafeEquals(oldDefaults.getWriteCommunity(), newDefaults.getWriteCommunity())) {
            config.setWriteCommunity(newDefaults.getWriteCommunity());
        }
        
        saveCurrent();
    }
    
    /** {@inheritDoc} */
    public void saveOrUpdate(final SnmpAgentConfig newConfig) {
        
    	final SnmpAgentConfig oldConfig = getAgentConfig(newConfig.getAddress());
        
    	final SnmpEventInfo eventInfo = new SnmpEventInfo();
        eventInfo.setFirstIPAddress(newConfig.getAddress());
        eventInfo.setLastIPAddress(newConfig.getAddress());

        boolean save = false;
        if (!nullSafeEquals(oldConfig.getReadCommunity(), newConfig.getReadCommunity())) {
            eventInfo.setCommunityString(newConfig.getReadCommunity());
            save = true;
        }
        if (oldConfig.getPort() != newConfig.getPort()) {
            eventInfo.setPort(newConfig.getPort());
            save = true;
        }
        if (oldConfig.getRetries() != newConfig.getRetries()) {
            eventInfo.setRetryCount(newConfig.getRetries());
            save = true;
        }
        if (oldConfig.getTimeout() != newConfig.getTimeout()) {
            eventInfo.setTimeout(newConfig.getTimeout());
            save = true;
        }
        if (oldConfig.getVersion() != newConfig.getVersion()) {
            eventInfo.setVersion(newConfig.getVersionAsString());
            save = true;
        }
        
        if (save) {
            getSnmpPeerFactory().define(eventInfo);
            saveCurrent();
        }
        
    }
    
    private SnmpConfig getSnmpConfig() {
        return SnmpPeerFactory.getSnmpConfig();
    }

    private void saveCurrent() {
        try {
            SnmpPeerFactory.saveCurrent();
        } catch (final IOException e) {
            TRANSLATOR.translate("save current SNMP configure", e);
        }
    }

	public void define(final SnmpEventInfo info) {
		getSnmpPeerFactory().define(info);
	}

}
