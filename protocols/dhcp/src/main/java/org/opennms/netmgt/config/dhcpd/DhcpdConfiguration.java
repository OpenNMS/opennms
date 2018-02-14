/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.dhcpd;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the dhcpd-configuration.xml
 *  configuration file.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="snmp-config")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class DhcpdConfiguration implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * TCP port number used for internal communication
     *  within OpenNMS between the DHCP poller and other OpenNMS
     * daemons.
     *  The port is set to 5818 in the default configuration
     *  file.
     */
	@XmlAttribute(name="port", required=true)
    private Integer m_port;

    /**
     * Ethernet MAC address used as the client MAC address
     *  in DHCP DISCOVER packets sent by OpenNMS. The MAC address
     * is set to
     *  00:06:0D:BE:9C:B2 in the default configuration file.
     */
	@XmlAttribute(name="macAddress", required=true)
    private String m_macAddress;

    /**
     * IP address used as the relay IP address in DHCP
     *  DISCOVER packets sent by OpenNMS. Setting this attribute to
     *  "broadcast" places the DHCP poller Setting this attribute
     * places the
     *  DHCP poller in "relay" mode instead of the default
     * "broadcast" mode.
     *  In "relay" mode, the DHCP server being polled will unicast
     * its
     *  responses directly back to the specified ip address rather
     * than
     *  broadcasting its responses. This allows DHCP servers to be
     * polled
     *  even though they are not on the same subnet as the OpenNMS
     * server,
     *  and without the aid of an external relay. This is usually
     * set to the
     *  IP address of the OpenNMS server.
     */
	@XmlAttribute(name="myIpAddress")
    private String m_myIpAddress;

    /**
     * Set extended DHCP polling mode. When extendedMode is
     *  false (the default), the DHCP poller will send a DISCOVER
     * and expect
     *  an OFFER in return. When extendedMode is true, the DHCP
     * poller will
     *  first send a DISCOVER. If no valid response is received it
     * will send
     *  an INFORM. If no valid response is received it will then
     * send a
     *  REQUEST. OFFER, ACK, and NAK are all considered valid
     * responses in
     *  extendedMode. Caution on usage: If in extended mode, the
     * time
     *  required to complete the poll for an unresponsive node is
     * increased
     *  by a factor of 3. Thus it is a good idea to limit the
     * number of
     *  retries to a small number.
     */
	@XmlAttribute(name="extendedMode")
    private String m_extendedMode;

    /**
     * Set the IP address to be requested in DHCP REQUEST
     *  queries. This attribute is only used when the extendedMode
     * attribute
     *  is set to "true". If an IP address is specified, it will be
     *  requested in the query. If the string "targetHost" is
     * specified, the
     *  DHCP server's own ip address will be requested. Since a
     * well-managed
     *  server will probably not respond to a request for its own
     * IP
     *  address, this parameter can also be set to the string
     *  "targetSubnet". This is similar to "targetHost", except the
     * last
     *  octet of the DHCP server's IP address is incremented or
     * decremented
     *  by 1 to obtain an IP address that is on the same subnet.
     * The
     *  resulting address will not be on the same subnet if the
     * DHCP
     *  server's subnet length is 31 or 32. Otherwise, the
     * algorithm used
     *  should be reliable.
     */
	@XmlAttribute(name="requestIpAddress")
    private String m_requestIpAddress;


      //----------------/
     //- Constructors -/
    //----------------/

    public DhcpdConfiguration() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method hasPort.
     * 
     * @return true if at least one Port has been added
     */
    public boolean hasPort() {
        return m_port != null;
    }

    /**
     * Returns the value of field 'port'. The field 'port' has the
     * following description: TCP port number used for internal
     * communication
     *  within OpenNMS between the DHCP poller and other OpenNMS
     * daemons.
     *  The port is set to 5818 in the default configuration
     *  file.
     * 
     * @return the value of field 'Port'.
     */
    public Integer getPort() {
        return m_port == null ? 0 : m_port;
    }
    
    /**
     * Sets the value of field 'port'. The field 'port' has the
     * following description: TCP port number used for internal
     * communication
     *  within OpenNMS between the DHCP poller and other OpenNMS
     * daemons.
     *  The port is set to 5818 in the default configuration
     *  file.
     * 
     * @param port the value of field 'port'.
     */
    public void setPort(final Integer port) {
        m_port = port;
    }

    /**
     */
    public void deletePort() {
        m_port = null;
    }

    /**
     * Returns the value of field 'macAddress'. The field
     * 'macAddress' has the following description: Ethernet MAC
     * address used as the client MAC address
     *  in DHCP DISCOVER packets sent by OpenNMS. The MAC address
     * is set to
     *  00:06:0D:BE:9C:B2 in the default configuration file.
     * 
     * @return the value of field 'MacAddress'.
     */
    public String getMacAddress() {
        return m_macAddress;
    }

    /**
     * Sets the value of field 'macAddress'. The field 'macAddress'
     * has the following description: Ethernet MAC address used as
     * the client MAC address
     *  in DHCP DISCOVER packets sent by OpenNMS. The MAC address
     * is set to
     *  00:06:0D:BE:9C:B2 in the default configuration file.
     * 
     * @param macAddress the value of field 'macAddress'.
     */
    public void setMacAddress(final String macAddress) {
        m_macAddress = macAddress;
    }

    /**
     * Returns the value of field 'myIpAddress'. The field
     * 'myIpAddress' has the following description: IP address used
     * as the relay IP address in DHCP
     *  DISCOVER packets sent by OpenNMS. Setting this attribute to
     *  "broadcast" places the DHCP poller Setting this attribute
     * places the
     *  DHCP poller in "relay" mode instead of the default
     * "broadcast" mode.
     *  In "relay" mode, the DHCP server being polled will unicast
     * its
     *  responses directly back to the specified ip address rather
     * than
     *  broadcasting its responses. This allows DHCP servers to be
     * polled
     *  even though they are not on the same subnet as the OpenNMS
     * server,
     *  and without the aid of an external relay. This is usually
     * set to the
     *  IP address of the OpenNMS server.
     * 
     * @return the value of field 'MyIpAddress'.
     */
    public String getMyIpAddress() {
        return m_myIpAddress;
    }
    
    /**
     * Sets the value of field 'myIpAddress'. The field
     * 'myIpAddress' has the following description: IP address used
     * as the relay IP address in DHCP
     *  DISCOVER packets sent by OpenNMS. Setting this attribute to
     *  "broadcast" places the DHCP poller Setting this attribute
     * places the
     *  DHCP poller in "relay" mode instead of the default
     * "broadcast" mode.
     *  In "relay" mode, the DHCP server being polled will unicast
     * its
     *  responses directly back to the specified ip address rather
     * than
     *  broadcasting its responses. This allows DHCP servers to be
     * polled
     *  even though they are not on the same subnet as the OpenNMS
     * server,
     *  and without the aid of an external relay. This is usually
     * set to the
     *  IP address of the OpenNMS server.
     * 
     * @param myIpAddress the value of field 'myIpAddress'.
     */
    public void setMyIpAddress(final String myIpAddress) {
        m_myIpAddress = myIpAddress;
    }

    /**
     * Returns the value of field 'extendedMode'. The field
     * 'extendedMode' has the following description: Set extended
     * DHCP polling mode. When extendedMode is
     *  false (the default), the DHCP poller will send a DISCOVER
     * and expect
     *  an OFFER in return. When extendedMode is true, the DHCP
     * poller will
     *  first send a DISCOVER. If no valid response is received it
     * will send
     *  an INFORM. If no valid response is received it will then
     * send a
     *  REQUEST. OFFER, ACK, and NAK are all considered valid
     * responses in
     *  extendedMode. Caution on usage: If in extended mode, the
     * time
     *  required to complete the poll for an unresponsive node is
     * increased
     *  by a factor of 3. Thus it is a good idea to limit the
     * number of
     *  retries to a small number.
     * 
     * @return the value of field 'ExtendedMode'.
     */
    public String getExtendedMode() {
        return m_extendedMode;
    }

    /**
     * Sets the value of field 'extendedMode'. The field
     * 'extendedMode' has the following description: Set extended
     * DHCP polling mode. When extendedMode is
     *  false (the default), the DHCP poller will send a DISCOVER
     * and expect
     *  an OFFER in return. When extendedMode is true, the DHCP
     * poller will
     *  first send a DISCOVER. If no valid response is received it
     * will send
     *  an INFORM. If no valid response is received it will then
     * send a
     *  REQUEST. OFFER, ACK, and NAK are all considered valid
     * responses in
     *  extendedMode. Caution on usage: If in extended mode, the
     * time
     *  required to complete the poll for an unresponsive node is
     * increased
     *  by a factor of 3. Thus it is a good idea to limit the
     * number of
     *  retries to a small number.
     * 
     * @param extendedMode the value of field 'extendedMode'.
     */
    public void setExtendedMode(final String extendedMode) {
        m_extendedMode = extendedMode;
    }

    /**
     * Returns the value of field 'requestIpAddress'. The field
     * 'requestIpAddress' has the following description: Set the IP
     * address to be requested in DHCP REQUEST
     *  queries. This attribute is only used when the extendedMode
     * attribute
     *  is set to "true". If an IP address is specified, it will be
     *  requested in the query. If the string "targetHost" is
     * specified, the
     *  DHCP server's own ip address will be requested. Since a
     * well-managed
     *  server will probably not respond to a request for its own
     * IP
     *  address, this parameter can also be set to the string
     *  "targetSubnet". This is similar to "targetHost", except the
     * last
     *  octet of the DHCP server's IP address is incremented or
     * decremented
     *  by 1 to obtain an IP address that is on the same subnet.
     * The
     *  resulting address will not be on the same subnet if the
     * DHCP
     *  server's subnet length is 31 or 32. Otherwise, the
     * algorithm used
     *  should be reliable.
     * 
     * @return the value of field 'RequestIpAddress'.
     */
    public String getRequestIpAddress() {
        return m_requestIpAddress;
    }

    /**
     * Sets the value of field 'requestIpAddress'. The field
     * 'requestIpAddress' has the following description: Set the IP
     * address to be requested in DHCP REQUEST
     *  queries. This attribute is only used when the extendedMode
     * attribute
     *  is set to "true". If an IP address is specified, it will be
     *  requested in the query. If the string "targetHost" is
     * specified, the
     *  DHCP server's own ip address will be requested. Since a
     * well-managed
     *  server will probably not respond to a request for its own
     * IP
     *  address, this parameter can also be set to the string
     *  "targetSubnet". This is similar to "targetHost", except the
     * last
     *  octet of the DHCP server's IP address is incremented or
     * decremented
     *  by 1 to obtain an IP address that is on the same subnet.
     * The
     *  resulting address will not be on the same subnet if the
     * DHCP
     *  server's subnet length is 31 or 32. Otherwise, the
     * algorithm used
     *  should be reliable.
     * 
     * @param requestIpAddress the value of field 'requestIpAddress'
     */
    public void setRequestIpAddress(final String requestIpAddress) {
        m_requestIpAddress = requestIpAddress;
    }
    
    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof DhcpdConfiguration) {
        
            DhcpdConfiguration temp = (DhcpdConfiguration)obj;
            if (m_port != temp.m_port)
                return false;
            if (m_macAddress != null) {
                if (temp.m_macAddress == null) return false;
                else if (!(m_macAddress.equals(temp.m_macAddress))) 
                    return false;
            }
            else if (temp.m_macAddress != null)
                return false;
            if (m_myIpAddress != null) {
                if (temp.m_myIpAddress == null) return false;
                else if (!(m_myIpAddress.equals(temp.m_myIpAddress))) 
                    return false;
            }
            else if (temp.m_myIpAddress != null)
                return false;
            if (m_extendedMode != null) {
                if (temp.m_extendedMode == null) return false;
                else if (!(m_extendedMode.equals(temp.m_extendedMode))) 
                    return false;
            }
            else if (temp.m_extendedMode != null)
                return false;
            if (m_requestIpAddress != null) {
                if (temp.m_requestIpAddress == null) return false;
                else if (!(m_requestIpAddress.equals(temp.m_requestIpAddress))) 
                    return false;
            }
            else if (temp.m_requestIpAddress != null)
                return false;
            return true;
        }
        return false;
    }


    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int result = 17;
        
        long tmp;
        result = 37 * result + m_port;
        if (m_macAddress != null) {
           result = 37 * result + m_macAddress.hashCode();
        }
        if (m_myIpAddress != null) {
           result = 37 * result + m_myIpAddress.hashCode();
        }
        if (m_extendedMode != null) {
           result = 37 * result + m_extendedMode.hashCode();
        }
        if (m_requestIpAddress != null) {
           result = 37 * result + m_requestIpAddress.hashCode();
        }
        
        return result;
    }



}
