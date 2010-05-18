//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Dec 01: enhanced configure SNMP handler
// 2005 Mar 08: Added configure SNMP handler
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Category;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.common.Range;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
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
    private long m_first = 0;
    private long m_last = 0;
    
    /**
     * Default constructor
     *
     */
    public SnmpEventInfo() {
    }
    
    /**
     * 
     * @param event
     */
    public SnmpEventInfo(Event event) {
        
        String parmName = null;
        Value parmValue = null;
        String parmContent = null;

        Parms parms = event.getParms();
        
        if (parms == null) {
            throw new IllegalArgumentException("SnmpEventInfo constructor: Can't construct class with event containing no parameters. "+ event.toString());
        }
        
        if (!event.getUei().equals(EventConstants.CONFIGURE_SNMP_EVENT_UEI)) {
            throw new IllegalArgumentException("Event not an a configure snmp event: "+event.toString());
        }
        
        for (Parm parm : parms.getParmCollection()) {
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
    
    public String getCommunityString() {
        return m_communityString;
    }
    public void setCommunityString(String communityString) {
        m_communityString = communityString;
    }
    public String getFirstIPAddress() {
        return m_firstIPAddress;
    }
    public void setFirstIPAddress(String firstIPAddress) throws UnknownHostException {
        m_firstIPAddress = firstIPAddress;
        m_first = InetAddressUtils.toIpAddrLong(InetAddress.getByName(firstIPAddress));
    }
    
    public void setFirstIPAddress(InetAddress firstIPAddress) {
        if (firstIPAddress == null) {
            m_firstIPAddress = null;
            m_first = 0;
        } else {
            m_firstIPAddress = firstIPAddress.getHostAddress();
            m_first = InetAddressUtils.toIpAddrLong(firstIPAddress);
        }
    }
    public String getLastIPAddress() {
        return m_lastIPAddress;
    }
    public void setLastIPAddress(String lastIPAddress) throws UnknownHostException {
    	if (StringUtils.isBlank(lastIPAddress)) {
			m_last = 0;
		} else {
	        m_lastIPAddress = lastIPAddress;
	        m_last = InetAddressUtils.toIpAddrLong(InetAddress.getByName(lastIPAddress));
		}
    }
    
    public void setListIPAddress(InetAddress lastIPAddress) {
        if (lastIPAddress == null) {
            m_lastIPAddress = null;
            m_last = 0;
        } else {
            m_lastIPAddress = lastIPAddress.getHostAddress();
            m_last = InetAddressUtils.toIpAddrLong(lastIPAddress);
        }
    }
    public long getFirst() {
        return m_first;
    }
    public long getLast() {
        return m_last;
    }
    public Range getRange() {
        if (isSpecific()) {
            throw new IllegalStateException("Attempted to create range with a specific."+this);
        }
        Range newRange = new Range();
        newRange.setBegin(getFirstIPAddress());
        newRange.setEnd(getLastIPAddress());
        return newRange;
    }
    public int getRetryCount() {
        return m_retryCount;
    }
    public void setRetryCount(int retryCount) {
        m_retryCount = retryCount;
    }
    public int getTimeout() {
        return m_timeout;
    }
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }
    public String getVersion() {
        return m_version;
    }
    public void setVersion(String version) {
        m_version = version;
    }
    public int getPort() {
        return m_port;
    }
    public void setPort(int port) {
        m_port  = port;
    }
    
    /**
     * Creates an SNMP config definition representing the data in this class.
     * The defintion will either have one specific IP element or one Range element.
     * @return
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
            
            if (getFirst() > getLast()) {
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
        if (getLast() == 0 ||  getFirst() == getLast()) {
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
