/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
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
    private String m_firstIPAddress = null;
    private String m_lastIPAddress = null;
    private String m_communityString = null;
    private int m_timeout = 0;
    private int m_retryCount = 0;
    private String m_version = null;
    private int m_port = 0;
    
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
                } else if (parmName.equals(EventConstants.PARM_COMMUNITY_STRING)) {
                    setCommunityString(parmContent);
                } else if (parmName.equals(EventConstants.PARM_RETRY_COUNT)) {
                    setRetryCount(computeIntValue(parmContent));
                } else if (parmName.equals(EventConstants.PARM_TIMEOUT)) {
                    setTimeout(computeIntValue(parmContent));
                } else if (parmName.equals(EventConstants.PARM_VERSION)) {
                    setVersion(parmContent);
                } else if (parmName.equals(EventConstants.PARM_PORT)) {
                    setPort(computeIntValue(parmContent));
                }
            } catch (UnknownHostException e) {
                log().error("SnmpEventInfo constructor: ", e);
                throw new IllegalArgumentException("SnmpEventInfo constructor. "+e.getLocalizedMessage());
            } catch (IllegalArgumentException e) {
                log().error("SnmpEventInfo constructor: ", e);
                throw e;
            }
        }

    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    /**
     * <p>getCommunityString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCommunityString() {
        return m_communityString;
    }
    /**
     * <p>setCommunityString</p>
     *
     * @param communityString a {@link java.lang.String} object.
     */
    public void setCommunityString(String communityString) {
        m_communityString = communityString;
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
    
    /**
     * Creates an SNMP config definition representing the data in this class.
     * The defintion will either have one specific IP element or one Range element.
     *
     * @return a {@link org.opennms.netmgt.config.snmp.Definition} object.
     */
    public Definition createDef() {
        Definition definition = new Definition();
        if (getCommunityString() != null) definition.setReadCommunity(getCommunityString());
        if (getVersion() != null && ("v1".equals(getVersion()) ||"v2c".equals(getVersion()))) {
            definition.setVersion(getVersion());
        }
        if (getRetryCount() != 0) definition.setRetry(getRetryCount());
        if (getTimeout() != 0) definition.setTimeout(getTimeout());
        if (getPort() != 0) definition.setPort(getPort());
        
        if (isSpecific()) {
            definition.addSpecific(getFirstIPAddress());
        } else {
            
            if (BigInteger.ZERO.compareTo(InetAddressUtils.difference(getFirstIPAddress(), getLastIPAddress())) < 0) {
                log().error("createDef: Can not create Definition when specified last is < first IP address: "+ this);
                throw new IllegalArgumentException("First: "+getFirstIPAddress()+" is greater than: "+getLastIPAddress());
            }
            
            Range range = new Range();
            range.setBegin(getFirstIPAddress());
            range.setEnd(getLastIPAddress());
            definition.addRange(range);
        }
        log().debug("createDef: created new Definition from: "+this);
        return definition;
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
    
    private int computeIntValue(String parmContent) throws IllegalArgumentException {
        int val = 0;
        try {
            val = Integer.parseInt(parmContent);
        } catch (NumberFormatException e) {
            log().error("computeIntValue: parm value passed in the event isn't a valid number." ,e);
            throw new IllegalArgumentException(e.getLocalizedMessage());
        }
        return val;
    }
        
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Info: ");
        sb.append("\n\tfirst: ");
        sb.append(getFirstIPAddress());
        sb.append("\n\tlast: ");
        sb.append(getLastIPAddress());
        sb.append("\n\tversion: ");
        sb.append(getVersion());
        sb.append("\n\tcommunity string: ");
        sb.append(getCommunityString());
        sb.append("\n\tport: ");
        sb.append(String.valueOf(getPort()));
        sb.append("\n\tretry count: ");
        sb.append(String.valueOf(getRetryCount()));
        sb.append("\n\ttimeout: ");
        sb.append(getTimeout());
        return sb.toString();
    }
    
}
