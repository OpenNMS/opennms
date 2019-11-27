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

import com.google.common.base.Strings;

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

    private boolean m_isMatchingDefault = true;
    private Definition m_generatedDefinition = null;
    private SnmpProfile m_snmpProfile;
    private String m_currentLabel;
    private String m_matchingProfileLabelAtDefaultLocation;
    private String m_matchingProfileLabelAtGivenLocation;

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
        m_currentDefinition = definition;
        m_currentLocation = LocationUtils.getEffectiveLocationName(definition.getLocation());
        if (!Strings.isNullOrEmpty(definition.getProfileLabel())) {
            m_currentLabel = definition.getProfileLabel();
        }
    }

    private void handleMatch() {
        if (LocationUtils.isDefaultLocationName(m_currentLocation)) {
            m_matchedDefinitionAtDefaultLocation = m_currentDefinition;
            if (!Strings.isNullOrEmpty(m_currentLabel)) {
                m_matchingProfileLabelAtDefaultLocation = m_currentLabel;
            }
        }
        if (m_location.equals(m_currentLocation)) {
            m_matchedDefinitionAtGivenLocation = m_currentDefinition;
            if (!Strings.isNullOrEmpty(m_currentLabel)) {
                m_matchingProfileLabelAtGivenLocation = m_currentLabel;
            }
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
        m_currentDefinition = null;
        m_currentLabel = null;
    }

    @Override
    public void visitSnmpConfigFinished() {

        final Configuration sourceConfig;
        if (getBestMatch() != null) {
            sourceConfig = getBestMatch();
            m_isMatchingDefault = false;
        } else {
            sourceConfig = m_currentConfig;
        }

        m_generatedDefinition = createDefinitionFromConfig(sourceConfig);
    }

    private Definition createDefinitionFromConfig(Configuration sourceConfig) {
        final Definition definition = new Definition();
        if (sourceConfig.getProxyHost() != null) {
            definition.setProxyHost(sourceConfig.getProxyHost());
        } else {
            definition.setProxyHost(m_currentConfig.getProxyHost());
        }

        if (sourceConfig.hasMaxVarsPerPdu()) {
            definition.setMaxVarsPerPdu(sourceConfig.getMaxVarsPerPdu());
        } else if (m_currentConfig.hasMaxVarsPerPdu()) {
            definition.setMaxVarsPerPdu(m_currentConfig.getMaxVarsPerPdu());
        } else {
            definition.setMaxVarsPerPdu(DEFAULT_MAX_VARS_PER_PDU);
        }

        if (sourceConfig.hasMaxRepetitions()) {
            definition.setMaxRepetitions(sourceConfig.getMaxRepetitions());
        } else if (m_currentConfig.hasMaxRepetitions()) {
            definition.setMaxRepetitions(m_currentConfig.getMaxRepetitions());
        } else {
            definition.setMaxRepetitions(DEFAULT_MAX_REPETITIONS);
        }

        if (sourceConfig.hasMaxRequestSize()) {
            definition.setMaxRequestSize(sourceConfig.getMaxRequestSize());
        } else if (m_currentConfig.hasMaxRequestSize()) {
            definition.setMaxRequestSize(m_currentConfig.getMaxRequestSize());
        } else {
            definition.setMaxRequestSize(DEFAULT_MAX_REQUEST_SIZE);
        }

        if (sourceConfig.getSecurityName() != null) {
            definition.setSecurityName(sourceConfig.getSecurityName());
        } else if (m_currentConfig.getSecurityName() != null) {
            definition.setSecurityName(m_currentConfig.getSecurityName());
        } else {
            definition.setSecurityName(DEFAULT_SECURITY_NAME);
        }

        if (sourceConfig.getAuthPassphrase() != null) {
            definition.setAuthPassphrase(sourceConfig.getAuthPassphrase());
        } else if (m_currentConfig.getAuthPassphrase() != null) {
            definition.setAuthPassphrase(m_currentConfig.getAuthPassphrase());
        }

        if (sourceConfig.getAuthProtocol() != null) {
            definition.setAuthProtocol(sourceConfig.getAuthProtocol());
        } else if (m_currentConfig.getAuthProtocol() != null) {
            definition.setAuthProtocol(m_currentConfig.getAuthProtocol());
        } else {
            definition.setAuthProtocol(DEFAULT_AUTH_PROTOCOL);
        }

        if (sourceConfig.getEngineId() != null) {
            definition.setEngineId(sourceConfig.getEngineId());
        } else if (m_currentConfig.getEngineId() != null) {
            definition.setEngineId(m_currentConfig.getEngineId());
        } else {
            definition.setEngineId(DEFAULT_ENGINE_ID);
        }

        if (sourceConfig.getContextEngineId() != null) {
            definition.setContextEngineId(sourceConfig.getContextEngineId());
        } else if (m_currentConfig.getContextEngineId() != null) {
            definition.setContextEngineId(m_currentConfig.getContextEngineId());
        } else {
            definition.setContextEngineId(DEFAULT_CONTEXT_ENGINE_ID);
        }

        if (sourceConfig.getContextName() != null) {
            definition.setContextName(sourceConfig.getContextName());
        } else if (m_currentConfig.getContextName() != null) {
            definition.setContextName(m_currentConfig.getContextName());
        } else {
            definition.setContextName(DEFAULT_CONTEXT_NAME);
        }

        if (sourceConfig.getPrivacyPassphrase() != null) {
            definition.setPrivacyPassphrase(sourceConfig.getPrivacyPassphrase());
        } else if (m_currentConfig.getPrivacyPassphrase() != null) {
            definition.setPrivacyPassphrase(m_currentConfig.getPrivacyPassphrase());
        }

        if (sourceConfig.getPrivacyProtocol() != null) {
            definition.setPrivacyProtocol(sourceConfig.getPrivacyProtocol());
        } else if (m_currentConfig.getPrivacyProtocol() != null) {
            definition.setPrivacyProtocol(m_currentConfig.getPrivacyProtocol());
        } else {
            definition.setPrivacyProtocol(DEFAULT_PRIV_PROTOCOL);
        }

        if (sourceConfig.getEnterpriseId() != null) {
            definition.setEnterpriseId(sourceConfig.getEnterpriseId());
        } else {
            definition.setEnterpriseId(m_currentConfig.getEnterpriseId());
        }

        if (sourceConfig.getVersion() != null) {
            definition.setVersion(sourceConfig.getVersion());
        } else if (m_currentConfig.getVersion() != null) {
            definition.setVersion(m_currentConfig.getVersion());
        } else {
            definition.setVersion(versionToString(VERSION1));
        }

        if (sourceConfig.getWriteCommunity() != null) {
            definition.setWriteCommunity(sourceConfig.getWriteCommunity());
        } else if (m_currentConfig.getWriteCommunity() != null) {
            definition.setWriteCommunity(m_currentConfig.getWriteCommunity());
        } else {
            definition.setWriteCommunity(DEFAULT_WRITE_COMMUNITY);
        }

        if (sourceConfig.getReadCommunity() != null) {
            definition.setReadCommunity(sourceConfig.getReadCommunity());
        } else if (m_currentConfig.getReadCommunity() != null) {
            definition.setReadCommunity(m_currentConfig.getReadCommunity());
        } else {
            definition.setReadCommunity(DEFAULT_READ_COMMUNITY);
        }

        if (sourceConfig.hasTimeout()) {
            definition.setTimeout(sourceConfig.getTimeout());
        } else if (m_currentConfig.hasTimeout()) {
            definition.setTimeout(m_currentConfig.getTimeout());
        } else {
            definition.setTimeout(DEFAULT_TIMEOUT);
        }

        if (sourceConfig.hasRetry()) {
            definition.setRetry(sourceConfig.getRetry());
        } else if (m_currentConfig.hasRetry()) {
            definition.setRetry(m_currentConfig.getRetry());
        } else {
            definition.setRetry(DEFAULT_RETRIES);
        }

        if (sourceConfig.hasPort()) {
            definition.setPort(sourceConfig.getPort());
        } else if (m_currentConfig.hasPort()) {
            definition.setPort(m_currentConfig.getPort());
        } else {
            definition.setPort(DEFAULT_PORT);
        }

        if (definition.getAuthPassphrase() == null) {
            definition.setAuthProtocol(null);
        }
        if (definition.getPrivacyPassphrase() == null) {
            definition.setPrivacyProtocol(null);
        }

        if (sourceConfig.hasSecurityLevel()) {
            definition.setSecurityLevel(sourceConfig.getSecurityLevel());
        } else if (m_currentConfig.hasSecurityLevel()) {
            definition.setSecurityLevel(m_currentConfig.getSecurityLevel());
        } else {
            int securityLevel = NOAUTH_NOPRIV;
            if (isBlank(definition.getAuthPassphrase())) {
                securityLevel = NOAUTH_NOPRIV;
            } else if (isBlank(definition.getPrivacyPassphrase())) {
                securityLevel = AUTH_NOPRIV;
            } else {
                securityLevel = AUTH_PRIV;
            }
            definition.setSecurityLevel(securityLevel);
        }

        if(sourceConfig.hasTTL()) {
            definition.setTTL(sourceConfig.getTTL());
        } else if (m_currentConfig.hasTTL()) {
            definition.setTTL(m_currentConfig.getTTL());
        }

        // Set profile from matching definition else fall back to profile label at default location
        if (!Strings.isNullOrEmpty(m_matchingProfileLabelAtGivenLocation)) {
            definition.setProfileLabel(m_matchingProfileLabelAtGivenLocation);
        } else if(!Strings.isNullOrEmpty(m_matchingProfileLabelAtDefaultLocation)) {
            definition.setProfileLabel(m_matchingProfileLabelAtDefaultLocation);
        }
        return definition;
    }

    @Override
    public void visitSnmpProfile(SnmpProfile snmpProfile) {
        this.m_snmpProfile = snmpProfile;
    }
    @Override
    public void visitSnmpProfileFinished() {
        m_generatedDefinition = createDefinitionFromConfig(m_snmpProfile);
    }

    public Definition getDefinition() {
        return m_generatedDefinition;
    }

    public boolean isMatchingDefaultConfig() {
        return m_isMatchingDefault;
    }

    private boolean isBlank(final String s) {
        return s == null || s.length() == 0 || s.trim().length() == 0;
    }
}
