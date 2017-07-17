/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.snmp;

import static org.opennms.netmgt.snmp.SnmpConfiguration.AUTH_NOPRIV;
import static org.opennms.netmgt.snmp.SnmpConfiguration.AUTH_PRIV;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_AUTH_PROTOCOL;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_CONTEXT_ENGINE_ID;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_CONTEXT_NAME;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_ENGINE_ID;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_MAX_REPETITIONS;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_MAX_REQUEST_SIZE;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_MAX_VARS_PER_PDU;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_PORT;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_PRIV_PROTOCOL;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_READ_COMMUNITY;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_RETRIES;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_SECURITY_NAME;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_TIMEOUT;
import static org.opennms.netmgt.snmp.SnmpConfiguration.DEFAULT_WRITE_COMMUNITY;
import static org.opennms.netmgt.snmp.SnmpConfiguration.NOAUTH_NOPRIV;
import static org.opennms.netmgt.snmp.SnmpConfiguration.VERSION1;
import static org.opennms.netmgt.snmp.SnmpConfiguration.versionToString;

import java.net.InetAddress;
import java.util.List;

import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.IPLike;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LocationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressSnmpConfigVisitor extends AbstractSnmpConfigVisitor implements SnmpConfigVisitor {
    private static final Logger LOG = LoggerFactory.getLogger(AddressSnmpConfigVisitor.class);
    private static final ByteArrayComparator BYTE_ARRAY_COMPARATOR = new ByteArrayComparator();

    private final InetAddress m_address;
    private final String m_location;

    private SnmpConfig m_currentConfig;
    private Definition m_currentDefinition;
    private String m_currentLocation;

    private Definition m_matchedDefinitionAtDefaultLocation;
    private Definition m_matchedDefinitionAtGivenLocation;

    private Definition m_generatedDefinition = null;

    public AddressSnmpConfigVisitor(final InetAddress addr) {
        this(addr, null);
    }

    public AddressSnmpConfigVisitor(final InetAddress addr, final String location) {
        m_address = addr;
        m_location = LocationUtils.getEffectiveLocationName(location);
    }

    @Override
    public void visitSnmpConfig(final SnmpConfig config) {
        m_currentConfig = config;
    }

    @Override
    public void visitDefinition(final Definition definition) {
        //LOG.debug("visitDefinition: {}", definition);
        m_currentDefinition = definition;
        m_currentLocation = LocationUtils.getEffectiveLocationName(definition.getLocation());
    }

    private void handleMatch() {
        if (LocationUtils.isDefaultLocationName(m_currentLocation)) {
            m_matchedDefinitionAtDefaultLocation = m_currentDefinition;
        }
        if (m_location.equals(m_currentLocation)) {
            m_matchedDefinitionAtGivenLocation = m_currentDefinition;
        }
    }

    private Definition getBestMatch() {
        if (m_matchedDefinitionAtGivenLocation != null) {
            return m_matchedDefinitionAtGivenLocation;
        }
        return m_matchedDefinitionAtDefaultLocation;
    }

    private boolean shouldTryToMatch() {
        if (LocationUtils.isDefaultLocationName(m_currentLocation)) {
            // We're currently processing a definition at the default location,
            // try to match if we don't already have a definition here
            return m_matchedDefinitionAtDefaultLocation == null;
        } else if (m_location.equals(m_currentLocation)) {
            // We're currently processing a definition at the target location,
            // try to match if we don't already have a definition here
            return m_matchedDefinitionAtGivenLocation == null;
        } else {
            // We're not interested in definitions at this location
            return false;
        }
    }

    @Override
    public void visitSpecifics(final List<String> specifics) {
        if (!shouldTryToMatch()) return;

        for (final String saddr : specifics) {
            try {
                final InetAddress addr = InetAddressUtils.addr(saddr);
                if (addr != null && addr.equals(m_address)) {
                    //LOG.debug("{} == {}", addr, m_address);
                    handleMatch();
                    return;
                }
            } catch (final IllegalArgumentException e) {
                LOG.info("Error while reading SNMP config <specific> tag: {}", saddr, e);
            }
        }
    }

    @Override
    public void visitRanges(List<Range> ranges) {
        // if we've already matched a specific, don't bother with the ranges
        if (!shouldTryToMatch()) return;

        for (final Range range : ranges) {
            final byte[] addr = m_address.getAddress();
            final byte[] begin = InetAddressUtils.toIpAddrBytes(range.getBegin());
            final byte[] end = InetAddressUtils.toIpAddrBytes(range.getEnd());
    
            final boolean inRange;
            if (BYTE_ARRAY_COMPARATOR.compare(begin, end) <= 0) {
                inRange = InetAddressUtils.isInetAddressInRange(addr, begin, end);
            } else {
                LOG.warn("{} has an 'end' that is earlier than its 'beginning'!", range);
                inRange = InetAddressUtils.isInetAddressInRange(addr, end, begin);
            }
            if (inRange) {
                handleMatch();
                return;
            }
        }
    }

    @Override
    public void visitIpMatches(final List<String> ipMatches) {
        // if we've already matched a specific or range, don't bother with the ipmatches
        if (!shouldTryToMatch()) return;

        for (final String ipMatch : ipMatches) {
            if (IPLike.matches(m_address, ipMatch)) {
                handleMatch();
                return;
            }
        }
    }

    @Override
    public void visitDefinitionFinished() {
        //LOG.debug("matched = {}", m_matchedDefinition);
        m_currentDefinition = null;
    }

    @Override
    public void visitSnmpConfigFinished() {
        final Definition ret = new Definition();

        final Configuration sourceConfig;
        if (getBestMatch() != null) {
            sourceConfig = getBestMatch();
        } else {
            sourceConfig = m_currentConfig;
        }

        if (sourceConfig.getProxyHost() != null) {
            ret.setProxyHost(sourceConfig.getProxyHost());
        } else {
            ret.setProxyHost(m_currentConfig.getProxyHost());
        }

        if (sourceConfig.hasMaxVarsPerPdu()) {
            ret.setMaxVarsPerPdu(sourceConfig.getMaxVarsPerPdu());
        } else if (m_currentConfig.hasMaxVarsPerPdu()) {
            ret.setMaxVarsPerPdu(m_currentConfig.getMaxVarsPerPdu());
        } else {
            ret.setMaxVarsPerPdu(DEFAULT_MAX_VARS_PER_PDU);
        }

        if (sourceConfig.hasMaxRepetitions()) {
            ret.setMaxRepetitions(sourceConfig.getMaxRepetitions());
        } else if (m_currentConfig.hasMaxRepetitions()) {
            ret.setMaxRepetitions(m_currentConfig.getMaxRepetitions());
        } else {
            ret.setMaxRepetitions(DEFAULT_MAX_REPETITIONS);
        }

        if (sourceConfig.hasMaxRequestSize()) {
            ret.setMaxRequestSize(sourceConfig.getMaxRequestSize());
        } else if (m_currentConfig.hasMaxRequestSize()) {
            ret.setMaxRequestSize(m_currentConfig.getMaxRequestSize());
        } else {
            ret.setMaxRequestSize(DEFAULT_MAX_REQUEST_SIZE);
        }

        if (sourceConfig.getSecurityName() != null) {
            ret.setSecurityName(sourceConfig.getSecurityName());
        } else if (m_currentConfig.getSecurityName() != null) {
            ret.setSecurityName(m_currentConfig.getSecurityName());
        } else {
            ret.setSecurityName(DEFAULT_SECURITY_NAME);
        }

        if (sourceConfig.getAuthPassphrase() != null) {
            ret.setAuthPassphrase(sourceConfig.getAuthPassphrase());
        } else if (m_currentConfig.getAuthPassphrase() != null) {
            ret.setAuthPassphrase(m_currentConfig.getAuthPassphrase());
        }
        
        if (sourceConfig.getAuthProtocol() != null) {
            ret.setAuthProtocol(sourceConfig.getAuthProtocol());
        } else if (m_currentConfig.getAuthProtocol() != null) {
            ret.setAuthProtocol(m_currentConfig.getAuthProtocol());
        } else {
            ret.setAuthProtocol(DEFAULT_AUTH_PROTOCOL);
        }
        
        if (sourceConfig.getEngineId() != null) {
            ret.setEngineId(sourceConfig.getEngineId());
        } else if (m_currentConfig.getEngineId() != null) {
            ret.setEngineId(m_currentConfig.getEngineId());
        } else {
            ret.setEngineId(DEFAULT_ENGINE_ID);
        }

        if (sourceConfig.getContextEngineId() != null) {
            ret.setContextEngineId(sourceConfig.getContextEngineId());
        } else if (m_currentConfig.getContextEngineId() != null) {
            ret.setContextEngineId(m_currentConfig.getContextEngineId());
        } else {
            ret.setContextEngineId(DEFAULT_CONTEXT_ENGINE_ID);
        }

        if (sourceConfig.getContextName() != null) {
            ret.setContextName(sourceConfig.getContextName());
        } else if (m_currentConfig.getContextName() != null) {
            ret.setContextName(m_currentConfig.getContextName());
        } else {
            ret.setContextName(DEFAULT_CONTEXT_NAME);
        }

        if (sourceConfig.getPrivacyPassphrase() != null) {
            ret.setPrivacyPassphrase(sourceConfig.getPrivacyPassphrase());
        } else if (m_currentConfig.getPrivacyPassphrase() != null) {
            ret.setPrivacyPassphrase(m_currentConfig.getPrivacyPassphrase());
        }

        if (sourceConfig.getPrivacyProtocol() != null) {
            ret.setPrivacyProtocol(sourceConfig.getPrivacyProtocol());
        } else if (m_currentConfig.getPrivacyProtocol() != null) {
            ret.setPrivacyProtocol(m_currentConfig.getPrivacyProtocol());
        } else {
            ret.setPrivacyProtocol(DEFAULT_PRIV_PROTOCOL);
        }

        if (sourceConfig.getEnterpriseId() != null) {
            ret.setEnterpriseId(sourceConfig.getEnterpriseId());
        } else {
            ret.setEnterpriseId(m_currentConfig.getEnterpriseId());
        }

        if (sourceConfig.getVersion() != null) {
            ret.setVersion(sourceConfig.getVersion());
        } else if (m_currentConfig.getVersion() != null) {
            ret.setVersion(m_currentConfig.getVersion());
        } else {
            ret.setVersion(versionToString(VERSION1));
        }

        if (sourceConfig.getWriteCommunity() != null) {
            ret.setWriteCommunity(sourceConfig.getWriteCommunity());
        } else if (m_currentConfig.getWriteCommunity() != null) {
            ret.setWriteCommunity(m_currentConfig.getWriteCommunity());
        } else {
            ret.setWriteCommunity(DEFAULT_WRITE_COMMUNITY);
        }

        if (sourceConfig.getReadCommunity() != null) {
            ret.setReadCommunity(sourceConfig.getReadCommunity());
        } else if (m_currentConfig.getReadCommunity() != null) {
            ret.setReadCommunity(m_currentConfig.getReadCommunity());
        } else {
            ret.setReadCommunity(DEFAULT_READ_COMMUNITY);
        }

        if (sourceConfig.hasTimeout()) {
            ret.setTimeout(sourceConfig.getTimeout());
        } else if (m_currentConfig.hasTimeout()) {
            ret.setTimeout(m_currentConfig.getTimeout());
        } else {
            ret.setTimeout(DEFAULT_TIMEOUT);
        }

        if (sourceConfig.hasRetry()) {
            ret.setRetry(sourceConfig.getRetry());
        } else if (m_currentConfig.hasRetry()) {
            ret.setRetry(m_currentConfig.getRetry());
        } else {
            ret.setRetry(DEFAULT_RETRIES);
        }

        if (sourceConfig.hasPort()) {
            ret.setPort(sourceConfig.getPort());
        } else if (m_currentConfig.hasPort()) {
            ret.setPort(m_currentConfig.getPort());
        } else {
            ret.setPort(DEFAULT_PORT);
        }

        if (ret.getAuthPassphrase() == null) {
            ret.setAuthProtocol(null);
        }
        if (ret.getPrivacyPassphrase() == null) {
            ret.setPrivacyProtocol(null);
        }
        
        if (sourceConfig.hasSecurityLevel()) {
            //LOG.debug("setSecurityLevel: {}", sourceConfig.getSecurityLevel());
            ret.setSecurityLevel(sourceConfig.getSecurityLevel());
        } else if (m_currentConfig.hasSecurityLevel()) {
            //LOG.debug("setSecurityLevel: {}", m_currentConfig.getSecurityLevel());
            ret.setSecurityLevel(m_currentConfig.getSecurityLevel());
        } else {
            int securityLevel = NOAUTH_NOPRIV;
            if (isBlank(ret.getAuthPassphrase())) {
                //LOG.debug("authPassphrase is null, NOAUTH_NOPRIV");
                securityLevel = NOAUTH_NOPRIV;
            } else if (isBlank(ret.getPrivacyPassphrase())) {
                //LOG.debug("privacyPassphrase is null, AUTH_NOPRIV");
                securityLevel = AUTH_NOPRIV;
            } else {
                //LOG.debug("auth {} privacy {} AUTH_PRIV", ret.getAuthPassphrase(), ret.getPrivacyPassphrase());
                securityLevel = AUTH_PRIV;
            }
            ret.setSecurityLevel(securityLevel);
        }

        //LOG.debug("generated: {}", ret);
        m_generatedDefinition = ret;
    }

    public Definition getDefinition() {
        return m_generatedDefinition;
    }

    private boolean isBlank(final String s) {
        return s == null || s.length() == 0 || s.trim().length() == 0;
    }
}
