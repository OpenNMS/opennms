/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

/**
 * Class for handling data passed as parms in a configureSNMP event.  Provides for
 * generating a config package based SNMP Definition class for merging into a current
 * running config.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class SnmpEventInfo {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpEventInfo.class);
    private String m_firstIPAddress = null;
    private String m_lastIPAddress = null;
    private String m_readCommunityString = null;
    private String m_writeCommunityString = null;
    private int m_timeout = 0;
    private int m_retryCount = 0;
    private String m_version = null;
    private int m_port = 0;
    private int m_securityLevel = 0;
    private String m_securityName = null;
    private int m_maxVarsPerPdu = 0;
    private int m_maxRepetitions = 0;
    private int m_maxRequestSize = 0;
    private String m_authPassPhrase = null;
    private String m_authProtocol = null;
    private String m_privProtocol = null;
    private String m_privPassPhrase = null;
    private String m_engineId = null;
    private String m_contextEngineId = null;
    private String m_contextName = null;
    private String m_enterpriseId = null;
    private String m_proxyHost = null;
    
    private static int computeIntValue(String parmContent) throws IllegalArgumentException {
        int val = 0;
        try {
            val = Integer.parseInt(parmContent);
        } catch (NumberFormatException e) {
            LOG.error("computeIntValue: parm value passed in the event isn't a valid number." ,e);
            throw new IllegalArgumentException(e.getLocalizedMessage());
        }
        return val;
    }
    
    /**
     * Default constructor
     */
    public SnmpEventInfo() {
    }
    
    /**
     * <p>Constructor for SnmpEventInfo.</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
	@SuppressWarnings("deprecation")
	public SnmpEventInfo(Event event) {
    	 String parmName = null;
         Value parmValue = null;
         String parmContent = null;
         
         if (!event.getUei().equals(EventConstants.CONFIGURE_SNMP_EVENT_UEI)) {
             throw new IllegalArgumentException("Event is not an a \"configure SNMP\" event: "+event.toString());
         }
         	
         for (Parm parm : event.getParmCollection()) {
            parmName = parm.getParmName();
            parmValue = parm.getValue();
            if (parmValue == null) continue;
            parmContent = parmValue.getContent();
            
            try {
                if (parmName.equals(EventConstants.PARM_FIRST_IP_ADDRESS)) {
                    setFirstIPAddress(parmContent);
                } else if (parmName.equals(EventConstants.PARM_LAST_IP_ADDRESS)) {
                    setLastIPAddress(parmContent);
                } else if (parmName.equals(EventConstants.PARM_COMMUNITY_STRING) || parmName.equals(EventConstants.PARM_SNMP_READ_COMMUNITY_STRING)) {
                    setReadCommunityString(parmContent);
                } else if (parmName.equals(EventConstants.PARM_SNMP_WRITE_COMMUNITY_STRING)) {
                	setWriteCommunityString(parmContent);
                } else if (parmName.equals(EventConstants.PARM_RETRY_COUNT)) {
                    setRetryCount(computeIntValue(parmContent));
                } else if (parmName.equals(EventConstants.PARM_TIMEOUT)) {
                    setTimeout(computeIntValue(parmContent));
                } else if (parmName.equals(EventConstants.PARM_VERSION)) {
                    setVersion(parmContent);
                } else if (parmName.equals(EventConstants.PARM_SNMP_MAX_REPETITIONS)) {
                	setMaxRepetitions(computeIntValue(parmContent));
                } else if (parmName.equals(EventConstants.PARM_SNMP_MAX_REQUEST_SIZE)) {
                	setMaxRequestSize(computeIntValue(parmContent));
                } else if (parmName.equals(EventConstants.PARM_SNMP_MAX_VARS_PER_PDU)) {
                	setMaxVarsPerPdu(computeIntValue(parmContent));
                } else if (parmName.equals(EventConstants.PARM_PORT)) {
                    setPort(computeIntValue(parmContent));
                } else if (parmName.equals(EventConstants.PARM_SNMP_AUTH_PASSPHRASE)) {
                	setAuthPassPhrase(parmContent);
                } else if (parmName.equals(EventConstants.PARM_SNMP_AUTH_PROTOCOL)) {
                	setAuthProtocol(parmContent);
                } else if (parmName.equals(EventConstants.PARM_SNMP_SECURITY_LEVEL)) {
                	setSecurityLevel(computeIntValue(parmContent));
                } else if (parmName.equals(EventConstants.PARM_SNMP_SECURITY_NAME)) {
                	setSecurityName(parmContent);
                } else if (parmName.equals(EventConstants.PARM_SNMP_ENGINE_ID)) {
                	setEngineId(parmContent);
                } else if (parmName.equals(EventConstants.PARM_SNMP_ENTERPRISE_ID)) {
                	setEnterpriseId(parmContent);
                } else if (parmName.equals(EventConstants.PARM_SNMP_CONTEXT_ENGINE_ID)) {
                	setContextEngineId(parmContent);
                } else if (parmName.equals(EventConstants.PARM_SNMP_CONTEXT_NAME)) {
                	setContextName(parmContent);
                } else if (parmName.equals(EventConstants.PARM_SNMP_PRIVACY_PASSPHRASE)) {
                	setPrivPassPhrase(parmContent);
                } else if (parmName.equals(EventConstants.PARM_SNMP_PRIVACY_PROTOCOL)) {
                	setPrivProtocol(parmContent);
                } else if (parmName.equals(EventConstants.PARM_SNMP_PROXY_HOST)) {
                	setProxyHost(parmContent);
                }
            } catch (UnknownHostException e) {
                LOG.error("SnmpEventInfo constructor", e);
                throw new IllegalArgumentException("SnmpEventInfo constructor. "+e.getLocalizedMessage());
            } catch (IllegalArgumentException e) {
            	LOG.error("SnmpEventInfo constructor", e);
                throw e;
            }
        }
	}

	/**
     * Returns the read community string if there is any, otherwise null is returned.
     *
     * @return the read community string if there is any, otherwise null is returned.
     * @deprecated use {@link #getReadCommunityString()} instead.
     */
    @Deprecated
    public String getCommunityString() {
        return getReadCommunityString();
    }
    
    /**
     * <p>sets the read community string.</p>
     *
     * @param communityString a read community string.
     * @deprecated use {@link #setReadCommunityString(String)} instead.
     */
    @Deprecated
    public void setCommunityString(String communityString) {
        setReadCommunityString(communityString);
    }
    
    public void setReadCommunityString(String readCommunityString) {
    	m_readCommunityString = readCommunityString;
    }
    
    public String getReadCommunityString() {
    	return m_readCommunityString;
    }
    
    public void setWriteCommunityString(String writeCommunityString) {
    	m_writeCommunityString = writeCommunityString;
    }
    
    public String getWriteCommunityString() {
    	return m_writeCommunityString;
    }
    
    /**
     * <p>getFirstIPAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFirstIPAddress() {
        return m_firstIPAddress;
    }
    
    /**
     * <p>setFirstIPAddress</p>
     *
     * @param firstIPAddress a {@link java.lang.String} object.
     * @throws java.net.UnknownHostException if any.
     */
    public void setFirstIPAddress(String firstIPAddress) throws UnknownHostException {
        m_firstIPAddress = firstIPAddress;
    }
    
    /**
     * <p>setFirstIPAddress</p>
     *
     * @param firstIPAddress a {@link java.net.InetAddress} object.
     */
    public void setFirstIPAddress(InetAddress firstIPAddress) {
        if (firstIPAddress == null) {
            m_firstIPAddress = null;
        } else {
            m_firstIPAddress = InetAddressUtils.str(firstIPAddress);
        }
    }
    
    /**
     * <p>getLastIPAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLastIPAddress() {
        return m_lastIPAddress;
    }
    
    /**
     * <p>setLastIPAddress</p>
     *
     * @param lastIPAddress a {@link java.lang.String} object.
     * @throws java.net.UnknownHostException if any.
     */
    public void setLastIPAddress(String lastIPAddress) throws UnknownHostException {
    	if (StringUtils.isBlank(lastIPAddress)) {
		} else {
	        m_lastIPAddress = lastIPAddress;
		}
    }
    
    /**
     * <p>setLastIPAddress</p>
     *
     * @param lastIPAddress a {@link java.net.InetAddress} object.
     */
    public void setLastIPAddress(InetAddress lastIPAddress) {
        if (lastIPAddress == null) {
            m_lastIPAddress = null;
        } else {
            m_lastIPAddress = InetAddressUtils.str(lastIPAddress);
        }
    }
    
    public int getMaxVarsPerPdu() {
    	return m_maxVarsPerPdu;
    }
    
    public void setMaxVarsPerPdu(final int maxVarsPerPdu) {
    	m_maxVarsPerPdu = maxVarsPerPdu;
    }
    
    public int getMaxRepetitions() {
    	return m_maxRepetitions;
    }
    
    public void setMaxRepetitions(final int maxRepetitions) {
    	m_maxRepetitions = maxRepetitions;
    }
    
    public String getAuthPassphrase() {
    	return m_authPassPhrase;
    }
    
    public void setAuthPassPhrase(final String authPassPhrase) {
    	m_authPassPhrase = authPassPhrase;
    }
    
    public String getAuthProtocol() {
    	return m_authProtocol;
    }
    
    public void setAuthProtocol(final String authProtocol) {
    	m_authProtocol = authProtocol;
    }

    public void setPrivProtocol(final String privProtocol) {
    	m_privProtocol = privProtocol;
    }
    
    public String getPrivProtocol() {
    	return m_privProtocol;
    }
    
    public String getPrivPassPhrase() {
    	return m_privPassPhrase;
    }
    
    public void setPrivPassPhrase(final String privPassPhrase) {
    	m_privPassPhrase = privPassPhrase;
    }
    
    public String getEngineId() {
    	return m_engineId;
    }
    
    public void setEngineId(final String engineId) {
    	m_engineId = engineId;
    }
    
    public String getContextEngineId() {
    	return m_contextEngineId;
    }
    
    public void setContextEngineId(final String contextEngineId) {
    	m_contextEngineId = contextEngineId;
    }
    
    public void setContextName(final String contextName) {
    	m_contextName = contextName;
    }
    
    public String getContextName() {
    	return m_contextName;
    }
    
    public void setEnterpriseId(final String enterpriseId) {
    	m_enterpriseId = enterpriseId;
    }
    
    public String getEnterpriseId() {
    	return m_enterpriseId;
    }
    
    public String getSecurityName() {
    	return m_securityName;
    }
    
    public void setSecurityName(final String securityName) {
    	m_securityName = securityName;
    }
    
    public void setSecurityLevel(final int securityLevel) {
    	m_securityLevel = securityLevel;
    }
    
    public int getSecurityLevel() {
    	return m_securityLevel;
    }
    
    /**
     * <p>getRetryCount</p>
     *
     * @return a int.
     */
    public int getRetryCount() {
        return m_retryCount;
    }
    /**
     * <p>setRetryCount</p>
     *
     * @param retryCount a int.
     */
    public void setRetryCount(int retryCount) {
        m_retryCount = retryCount;
    }
    /**
     * <p>getTimeout</p>
     *
     * @return a int.
     */
    public int getTimeout() {
        return m_timeout;
    }
    /**
     * <p>setTimeout</p>
     *
     * @param timeout a int.
     */
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }
    /**
     * <p>getVersion</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVersion() {
        return m_version;
    }
    /**
     * <p>setVersion</p>
     *
     * @param version a {@link java.lang.String} object.
     */
    public void setVersion(String version) {
        m_version = version;
    }
    /**
     * <p>getPort</p>
     *
     * @return a int.
     */
    public int getPort() {
        return m_port;
    }
    /**
     * <p>setPort</p>
     *
     * @param port a int.
     */
    public void setPort(int port) {
        m_port  = port;
    }
    
    public int getMaxRequestSize() {
    	return m_maxRequestSize;
    }
    
    public void setMaxRequestSize(int maxRequestSize) {
    	m_maxRequestSize = maxRequestSize;
    }
    
    public String getProxyHost() {
    	return m_proxyHost;
    }
    
    public void setProxyHost(String proxyHost) {
    	m_proxyHost = proxyHost;
    }
    
    /**
     * <p>getRange</p>
     *
     * @return a {@link org.opennms.netmgt.config.common.Range} object.
     */
    public Range getRange() {
        if (isSpecific()) {
            throw new IllegalStateException("Attempted to create range with a specific."+this);
        }
        Range newRange = new Range();
        newRange.setBegin(getFirstIPAddress());
        newRange.setEnd(getLastIPAddress());
        return newRange;
    }
    
    /**
     * Determines if the configureSNMP event is for a specific address.
     *
     * @return true if there is no last IP address specified or if first and last are equal
     */
    public boolean isSpecific() {
        if (getLastIPAddress() == null || getLastIPAddress().equals(getFirstIPAddress())) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Creates an event from <code>this</code>.
     * @param source The source to set in the Event. Must not be null.
     * @return The event which represents <code>this</code>.
     */
    public Event createEvent(final String source) {
		EventBuilder bldr = new EventBuilder(EventConstants.CONFIGURE_SNMP_EVENT_UEI, source);
	    bldr.setInterface(InetAddressUtils.addr(getFirstIPAddress()));
	    bldr.setService("SNMP");

	    bldr.addParam(EventConstants.PARM_FIRST_IP_ADDRESS, getFirstIPAddress());
	    bldr.addParam(EventConstants.PARM_LAST_IP_ADDRESS, getLastIPAddress());
	    
	    if (!StringUtils.isEmpty(getAuthPassphrase())) bldr.addParam(EventConstants.PARM_SNMP_AUTH_PASSPHRASE, getAuthPassphrase());
	    if (!StringUtils.isEmpty(getAuthProtocol())) bldr.addParam(EventConstants.PARM_SNMP_AUTH_PROTOCOL, getAuthProtocol());
	    if (!StringUtils.isEmpty(getContextEngineId())) bldr.addParam(EventConstants.PARM_SNMP_CONTEXT_ENGINE_ID, getContextEngineId());
	    if (!StringUtils.isEmpty(getContextName())) bldr.addParam(EventConstants.PARM_SNMP_CONTEXT_NAME, getContextName());
	    if (!StringUtils.isEmpty(getEngineId())) bldr.addParam(EventConstants.PARM_SNMP_ENGINE_ID, getEngineId());
	    if (!StringUtils.isEmpty(getEnterpriseId())) bldr.addParam(EventConstants.PARM_SNMP_ENTERPRISE_ID, getEnterpriseId());
	    if (getMaxRepetitions() != 0) bldr.addParam(EventConstants.PARM_SNMP_MAX_REPETITIONS, Integer.toString(getMaxRepetitions()));
	    if (getMaxRequestSize() != 0) bldr.addParam(EventConstants.PARM_SNMP_MAX_REQUEST_SIZE, Integer.toString(getMaxRequestSize()));
	    if (getMaxVarsPerPdu() != 0) bldr.addParam(EventConstants.PARM_SNMP_MAX_VARS_PER_PDU, Integer.toString(getMaxVarsPerPdu()));
	    if (getPort() != 0) bldr.addParam(EventConstants.PARM_PORT, Integer.toString(getPort()));
	    if (!StringUtils.isEmpty(getPrivPassPhrase())) bldr.addParam(EventConstants.PARM_SNMP_PRIVACY_PASSPHRASE, getPrivPassPhrase());
	    if (!StringUtils.isEmpty(getPrivProtocol())) bldr.addParam(EventConstants.PARM_SNMP_PRIVACY_PROTOCOL, getPrivProtocol());
	    if (!StringUtils.isEmpty(getProxyHost())) bldr.addParam(EventConstants.PARM_SNMP_PROXY_HOST, getProxyHost());
	    if (!StringUtils.isEmpty(getReadCommunityString())) bldr.addParam(EventConstants.PARM_SNMP_READ_COMMUNITY_STRING, getReadCommunityString());
	    if (!StringUtils.isEmpty(getSecurityName())) bldr.addParam(EventConstants.PARM_SNMP_SECURITY_NAME,getSecurityName());
	    if (getRetryCount() != 0) bldr.addParam(EventConstants.PARM_RETRY_COUNT, Integer.toString(getRetryCount()));
	    if (getSecurityLevel() > 0) bldr.addParam(EventConstants.PARM_SNMP_SECURITY_LEVEL, Integer.toString(getSecurityLevel()));
	    if (getTimeout() != 0) bldr.addParam(EventConstants.PARM_TIMEOUT, Integer.toString(getTimeout()));
	    if (!StringUtils.isEmpty(getVersion())) bldr.addParam(EventConstants.PARM_VERSION, getVersion());
	    if (!StringUtils.isEmpty(getWriteCommunityString())) bldr.addParam(EventConstants.PARM_SNMP_WRITE_COMMUNITY_STRING, getWriteCommunityString());
	    
	    return bldr.getEvent();
    }
    
    /**
     * Creates an SNMP config definition representing the data in this class.
     * The defintion will either have one specific IP element or one Range element.
     *
     * @return a {@link org.opennms.netmgt.config.snmp.Definition} object.
     */
    public Definition createDef() {
        Definition definition = new Definition();
        if (StringUtils.isNotEmpty(getVersion())) definition.setVersion(getVersion());
        if (getRetryCount() != 0) definition.setRetry(Integer.valueOf(getRetryCount()));
        if (getTimeout() != 0) definition.setTimeout(Integer.valueOf(getTimeout()));
        if (getPort() != 0) definition.setPort(Integer.valueOf(getPort()));
        if (getMaxRepetitions() != 0) definition.setMaxRepetitions(Integer.valueOf(getMaxRepetitions()));
    	if (getMaxVarsPerPdu() != 0) definition.setMaxVarsPerPdu(Integer.valueOf(getMaxVarsPerPdu()));
    	if (getMaxRequestSize() != 0) definition.setMaxRequestSize(Integer.valueOf(getMaxRequestSize()));
    	if (StringUtils.isNotEmpty(getProxyHost())) definition.setProxyHost(getProxyHost());
    	
        // version dependend parameters
        if (getVersion() != null && getVersion().equals("v3")) {
        	if (StringUtils.isNotEmpty(getAuthPassphrase())) definition.setAuthPassphrase(getAuthPassphrase());
        	if (StringUtils.isNotEmpty(getAuthProtocol())) definition.setAuthProtocol(getAuthProtocol());
        	if (StringUtils.isNotEmpty(getContextEngineId())) definition.setContextEngineId(getContextEngineId());
        	if (StringUtils.isNotEmpty(getContextName())) definition.setContextName(getContextName());
        	if (StringUtils.isNotEmpty(getEngineId())) definition.setEngineId(getEngineId());
        	if (StringUtils.isNotEmpty(getEnterpriseId())) definition.setEnterpriseId(getEnterpriseId());
        	if (StringUtils.isNotEmpty(getPrivPassPhrase())) definition.setPrivacyPassphrase(getPrivPassPhrase());
        	if (StringUtils.isNotEmpty(getPrivProtocol())) definition.setPrivacyProtocol(getPrivProtocol());
        	if (StringUtils.isNotEmpty(getSecurityName())) definition.setSecurityName(getSecurityName());
        	if (getSecurityLevel() > 0) definition.setSecurityLevel(getSecurityLevel());
        } else { //v1, v2c or invalid version
        	if (getReadCommunityString() != null) definition.setReadCommunity(getReadCommunityString());
        	if (getWriteCommunityString() != null) definition.setWriteCommunity(getWriteCommunityString());
        }
        
        if (isSpecific()) {
            definition.addSpecific(getFirstIPAddress());
        } else {
            
        	// first ip address of range must be < than last ip address of range
            if (BigInteger.ZERO.compareTo(InetAddressUtils.difference(getFirstIPAddress(), getLastIPAddress())) < 0) {
                LOG.error("createDef: Can not create Definition when specified last is < first IP address: {}", this);
                throw new IllegalArgumentException("First: "+getFirstIPAddress()+" is greater than: "+getLastIPAddress());
            }
            
            Range range = new Range();
            range.setBegin(getFirstIPAddress());
            range.setEnd(getLastIPAddress());
            definition.addRange(range);
        }
        LOG.debug("createDef: created new Definition from: {}", this);
        return definition;
    }
    
    @Override
    public boolean equals(Object obj) {
    	return EqualsBuilder.reflectionEquals(this,  obj);
    }

    @Override
    public int hashCode() {
    	return HashCodeBuilder.reflectionHashCode(this);
    }
    
    @Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
