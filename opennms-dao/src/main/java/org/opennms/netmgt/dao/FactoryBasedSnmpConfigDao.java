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
package org.opennms.netmgt.dao;

import java.io.IOException;
import java.net.InetAddress;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.dao.castor.CastorExceptionTranslator;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.springframework.beans.factory.InitializingBean;

/**
 * DefaultSnmpConfigDao
 *
 * @author brozow
 */
public class FactoryBasedSnmpConfigDao implements SnmpConfigDao, InitializingBean {
    
    private static final CastorExceptionTranslator TRANSLATOR = new CastorExceptionTranslator();
    
    private SnmpPeerFactory getSnmpPeerFactory() {
        return SnmpPeerFactory.getInstance();
    }
    
    public void afterPropertiesSet() throws Exception {
        SnmpPeerFactory.init();
    }

    public SnmpAgentConfig get(InetAddress agentAddress) {
        return getSnmpPeerFactory().getAgentConfig(agentAddress);
    }

    public void saveAsDefaults(SnmpConfiguration defaults) {
        SnmpConfig config = SnmpPeerFactory.getSnmpConfig();
        
        if (defaults.hasAuthPassPhrase()) {
            config.setAuthPassphrase(defaults.getAuthPassPhrase());
        }
        if (defaults.hasAuthProtocol()) {
            config.setAuthProtocol(defaults.getAuthProtocol());
        }
        if (defaults.hasMaxRepetitions()) {
            config.setMaxRepetitions(defaults.getMaxRepetitions());
        }
        if (defaults.hasMaxRequestSize()) {
            config.setMaxRequestSize(defaults.getMaxRequestSize());
        }
        if (defaults.hasMaxVarsPerPdu()) {
            config.setMaxVarsPerPdu(defaults.getMaxVarsPerPdu());
        }
        if (defaults.hasPort()) {
            config.setPort(defaults.getPort());
        }
        if (defaults.hasPrivPassPhrase()) {
            config.setPrivacyPassphrase(defaults.getPrivPassPhrase());
        }
        if (defaults.hasPrivProtocol()) {
            config.setPrivacyProtocol(defaults.getPrivProtocol());
        }
        if (defaults.hasReadCommunity()) {
            config.setReadCommunity(defaults.getReadCommunity());
        }
        if (defaults.hasRetries()) {
            config.setRetry(defaults.getRetries());
        }
        if (defaults.hasSecurityLevel()) {
            config.setSecurityLevel(defaults.getSecurityLevel());
        }
        if (defaults.hasSecurityName()) {
            config.setSecurityName(defaults.getSecurityName());
        }
        if (defaults.hasTimeout()) {
            config.setTimeout(defaults.getTimeout());
        }
        if (defaults.hasVersion()) {
            config.setVersion(defaults.getVersionAsString());
        }
        if (defaults.hasWriteCommunity()) {
            config.setWriteCommunity(defaults.getWriteCommunity());
        }
        
        saveCurrent();
    }

    public void saveConfigForRange(SnmpConfiguration config, InetAddress beginAddress, InetAddress endAddress) {
        SnmpEventInfo eventInfo = new SnmpEventInfo();
        
        eventInfo.setFirstIPAddress(beginAddress);
        eventInfo.setListIPAddress(endAddress);
        if (config.hasPort()) {
            eventInfo.setPort(config.getPort());
        }
        if (config.hasReadCommunity()) {
            eventInfo.setCommunityString(config.getReadCommunity());
        }
        if (config.hasRetries()) {
            eventInfo.setRetryCount(config.getRetries());
        }
        if (config.hasTimeout()) {
            eventInfo.setTimeout(config.getTimeout());
        }
        if (config.hasVersion()) {
            eventInfo.setVersion(config.getVersionAsString());
        }
        
        getSnmpPeerFactory().define(eventInfo);
        
        saveCurrent();

    }

    public void saveOrUpdate(SnmpAgentConfig config) {
        saveConfigForRange(config, config.getAddress(), config.getAddress());
    }

    private void saveCurrent() {
        try {
            SnmpPeerFactory.saveCurrent();
        } catch (MarshalException e) {
            TRANSLATOR.translate("save current snmp configuraiton", e);
        } catch (ValidationException e) {
            TRANSLATOR.translate("save current snmp configuraiton", e);
        } catch (IOException e) {
            TRANSLATOR.translate("save current snmp configuraiton", e);
        }
    }

}
