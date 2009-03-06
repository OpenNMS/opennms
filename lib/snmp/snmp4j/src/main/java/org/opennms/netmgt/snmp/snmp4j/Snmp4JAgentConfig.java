//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 23: Format code, move SNMP session creation, PDU creation,
//              and version string into this class. - dj@opennms.org
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.snmp.snmp4j;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Snmp4JAgentConfig {
    
    private SnmpAgentConfig m_config;
    
    public Snmp4JAgentConfig(SnmpAgentConfig config) {
        m_config = config;
    }

    public InetAddress getInetAddress() {
        return m_config.getAddress();
    }
    
    public OctetString getAuthPassPhrase() {
        return createOctetString(m_config.getAuthPassPhrase());
    }

    public OID getAuthProtocol() {
        return convertAuthProtocol(m_config.getAuthProtocol());
    }

    public int getMaxRequestSize() {
        return m_config.getMaxRequestSize();
    }

    public int getMaxVarsPerPdu() {
        return m_config.getMaxVarsPerPdu();
    }
    
    public int getMaxRepetitions() {
        return m_config.getMaxRepetitions();
    }

    public int getPort() {
        return m_config.getPort();
    }

    public int getRetries() {
        return m_config.getRetries();
    }

    public int getSecurityLevel() {
        return convertSecurityLevel(m_config.getSecurityLevel());
    }

    public OctetString getSecurityName() {
        return convertSecurityName(m_config.getSecurityName());
    }

    public int getTimeout() {
        return m_config.getTimeout();
    }

    public int getVersion() {
        return convertVersion(m_config.getVersion());
    }

    /**
     * Returns a string representation of the SNMP4J version constant
     * @param version
     * @return
     */
    public String getVersionString() {
        switch (getVersion()) {
        case SnmpConstants.version1:
            return "SNMPv1";
        case SnmpConstants.version2c:
            return "SNMPv2c";
        case SnmpConstants.version3:
            return "SNMPv3";
        default:
            return "unknown: " + getVersion();
        }
    }

    public String getWriteCommunity() {
        return m_config.getWriteCommunity();
    }

    public String toString() {
        return m_config.toString();
    }

    /**
     * This method converts an InetAddress to an implementation of an SNMP4J Address
     * (UdpAddress or TcpAddress)
     * 
     * TODO: This needs to be updated when the protocol flag is added to the SNMP Config
     * so that UDP or TCP can be used in v3 operations.
     */
    private Address convertAddress(InetAddress address, int port) {
        String transportAddress = address.getHostAddress();
        transportAddress += "/" + port;
        Address targetAddress = new UdpAddress(transportAddress);
        return targetAddress;
    }

    /**
     * This method adapts the OpenNMS SNMP version constants
     * to SNMP4J defined constants.
     * 
     * @param version
     * @return
     */
    private int convertVersion(int version) {
        switch (version) {
        case SnmpAgentConfig.VERSION3:
            return SnmpConstants.version3;
        case SnmpAgentConfig.VERSION2C:
            return SnmpConstants.version2c;
        default:
            return SnmpConstants.version1;
        }
    }

    private OctetString createOctetString(String s) {
        
        if (s == null) {
            return null;
        }
        
        OctetString octetString;
        if (s.startsWith("0x")) {
            octetString = OctetString.fromHexString(s.substring(2), ':');
        } else {
            octetString = new OctetString(s);
        }
        return octetString;
    }
    
    /**
     * Adapts the OpenNMS SNMPv3 security name to an SNMP4J compatible
     * security name (String -> OctetString)
     * 
     * @param securityName
     * @return
     */
    private OctetString convertSecurityName(String securityName) {
        return new OctetString(securityName);
    }

    private OID convertPrivProtocol(String privProtocol) {
        /*
         * Returning null here is okay because the SNMP4J library supports
         * this value as null when creating the Snmp session.
         */
        if (privProtocol == null) {
            return null;
        }
        
        if (privProtocol.equals("DES")) {
            return PrivDES.ID;
        } else if ((privProtocol.equals("AES128")) || (privProtocol.equals("AES"))) {
            return PrivAES128.ID;
        } else if (privProtocol.equals("AES192")) {
            return PrivAES192.ID;
        } else if (privProtocol.equals("AES256")) {
            return PrivAES256.ID;
        } else {
            throw new IllegalArgumentException("Privacy protocol " + privProtocol + " not supported");
        }
    
    }

    /**
     * Adapts the OpenNMS SNMPv3 community name to an SNMP4J compatible
     * community name (String -> OctetString)
     * 
     * @param agentConfig
     * @return
     */
    private OctetString convertCommunity(String community) {
        return new OctetString(community);
    }

    private OID convertAuthProtocol(String authProtocol) {
        /*
         * Returning null here is okay because the SNMP4J library supports
         * this value as null when creating the Snmp session.
         */
        if (authProtocol == null) {
            return null;
        }
        
        if (authProtocol.equals("MD5")) {
            return AuthMD5.ID;
        } else if (authProtocol.equals("SHA")) {
            return AuthSHA.ID;
        } else {
            throw new IllegalArgumentException("Authentication protocol unsupported: " + authProtocol);
        }            
    }

    protected Target getTarget() {
        Target target = createTarget();
        target.setVersion(getVersion());
        target.setRetries(getRetries());
        target.setTimeout(getTimeout());
        target.setAddress(getAddress());
        target.setMaxSizeRequestPDU(getMaxRequestSize());
            
        return target;
    }

    private Target createTarget() {
        return (isSnmpV3() ? createUserTarget() : createCommunityTarget());
    }

    boolean isSnmpV3() {
        return m_config.getVersion() == SnmpConstants.version3;
    }

    private Target createCommunityTarget() {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(getReadCommunity());
        return target;
    }

    private Target createUserTarget() {
        UserTarget target = new UserTarget();
        target.setSecurityLevel(getSecurityLevel());
        target.setSecurityName(getSecurityName());
        return target;
    }

    private OctetString getReadCommunity() {
        return convertCommunity(m_config.getReadCommunity());
    }

    private Address getAddress() {
        return convertAddress(getInetAddress(), getPort());
    }

    public OID getPrivProtocol() {
        return convertPrivProtocol(m_config.getPrivProtocol());
    }

    public OctetString getPrivPassPhrase() {
        return createOctetString(m_config.getPrivPassPhrase());
    }

    /**
     * This method adapts the OpenNMS SNMPv3 security level constants
     * to SNMP4J defined constants.
     * 
     * @param securityLevel
     * @return
     */
    private int convertSecurityLevel(int securityLevel) {
        switch (securityLevel) {
        case SnmpAgentConfig.AUTH_NOPRIV :
            securityLevel = SecurityLevel.AUTH_NOPRIV;
            break;
        case SnmpAgentConfig.AUTH_PRIV :
            securityLevel = SecurityLevel.AUTH_PRIV;
            break;
        case SnmpAgentConfig.NOAUTH_NOPRIV :
            securityLevel = SecurityLevel.NOAUTH_NOPRIV;
            break;
        default :
           securityLevel = SecurityLevel.NOAUTH_NOPRIV;
        }
        
        return securityLevel;
    }

    public Snmp createSnmpSession() throws IOException {
        TransportMapping transport = new DefaultUdpTransportMapping();
        Snmp session = new Snmp(transport);
        
        if (isSnmpV3()) {
            session.getUSM().addUser(getSecurityName(),
                    new UsmUser(getSecurityName(),
                            getAuthProtocol(),
                            getAuthPassPhrase(),
                            getPrivProtocol(),
                            getPrivPassPhrase()));
        }
        
        return session;
    }
    
    /**
     * Creates an SNMP4J PDU based on the SNMP4J version constants.
     * A v3 request requires a ScopedPDU.
     * 
     * @param type
     * @return
     */
    public PDU createPdu(int type) {
        PDU pdu = getVersion() == SnmpConstants.version3 ? new ScopedPDU() : new PDU();
        pdu.setType(type);
        return pdu;
    }

}
