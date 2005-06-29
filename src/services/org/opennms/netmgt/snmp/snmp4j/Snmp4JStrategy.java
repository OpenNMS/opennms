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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.snmp.snmp4j;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpStrategy;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JWalker.Snmp4JValue;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.log.Log4jLogFactory;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Snmp4JStrategy implements SnmpStrategy {
    
    //Initialize for v3 communications
    static {
        LogFactory.setLogFactory(new Log4jLogFactory());
        MPv3.setEnterpriseID(5813);
        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);
    }
    
    /**
     * SNMP4J createWalker implemenetation.
     * 
     * @param agentConfig
     * @param name
     * @param tracker
     */
    public SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, CollectionTracker tracker) {
        return new Snmp4JWalker(agentConfig, name, tracker);
    }
    
    /**
     * Not yet implemented.  Use a walker.
     */
    public SnmpValue[] getBulk(SnmpAgentConfig agentConfig, SnmpObjId[] oid) {
        return null;
    }

    /**
     * SNMP4J get implementation.
     * 
     * @param agentConfig
     * @param oid
     *
     */
    public SnmpValue get(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        if (log().isDebugEnabled())
            log().debug("get: OID: "+oid+" for Agent:"+agentConfig);
        
        if (!agentConfig.isAdapted())
            adaptConfig(agentConfig);
        
        agentConfig.setPduType(PDU.GET);
        return send(agentConfig, oid);
    }
    
    /**
     * SNMP4J getNext implementation
     * 
     * @param agentConfig
     * @param oid
     * 
     */
    public SnmpValue getNext(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        if (log().isDebugEnabled())
            log().debug("getNext: OID: "+oid+" for Agent:"+agentConfig);
        
        if (!agentConfig.isAdapted())
            adaptConfig(agentConfig);
        
        agentConfig.setPduType(PDU.GETNEXT);
        
        return send(agentConfig, oid);
    }
    
    /**
     * This method determines whether to send v1 or v2 request and is used
     * in testing only.
     * 
     * @deprecated
     * @param agentConfig
     * @param oid
     * @return
     */
    private SnmpValue versionSend(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        if (agentConfig.getVersion() != SnmpAgentConfig.VERSION3) {
            return sendV1_2(agentConfig, oid);
        } else {
            return send(agentConfig, oid);
        }
    }
    
    /**
     * This method sends an SNMP4J v1/v2 request and uses no helper methods and
     * is used in testing only.
     * 
     * @deprecated
     * @param agentConfig
     * @param oid
     * @return
     */
    private SnmpValue sendV1_2(SnmpAgentConfig agentConfig, SnmpObjId oid) {

        TransportMapping transport = null;
        try {
            transport = new DefaultUdpTransportMapping();
            Snmp session = new Snmp(transport);
            session.listen();
            
            CommunityTarget target = new CommunityTarget();
            ((CommunityTarget)target).setCommunity(createOctetString(agentConfig.getReadCommunity()));
            target.setAddress(convertAddress(agentConfig.getAddress(), agentConfig.getPort()));
            target.setRetries(agentConfig.getRetries());
            target.setTimeout(agentConfig.getTimeout());
            target.setVersion(convertVersion(agentConfig.getVersion()));
            
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid.toString())));
            pdu.setType(convertPduType(agentConfig.getPduType()));
            
            ResponseEvent responseEvent = null;
            responseEvent = session.send(pdu, target);
            PDU response = responseEvent.getResponse();
            if (response.size() < 1) {
                return null;
            } else {
                return new Snmp4JValue(response.get(0).getVariable());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                transport.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }
    
    /*
     * TODO: make send take a target and a pdu so that this can be called
     * from getBulk after the pdu is built
     * TODO: Write tests for null SnmpValues
     */
    private SnmpValue send(SnmpAgentConfig agentConfig, SnmpObjId oid) {
        
        SnmpValue value = null;
        Snmp session = null;
        
        try {
            Address targetAddress = GenericAddress.parse("udp:"+agentConfig.getAddress().getHostAddress()+"/"+agentConfig.getPort());
            session = new Snmp(new DefaultUdpTransportMapping());
            session.listen();
            
            session.getUSM().addUser((createOctetString(agentConfig.getSecurityName())),
                    new UsmUser(createOctetString(agentConfig.getSecurityName()),
                            convertAuthProtocol(agentConfig.getAuthProtocol()),
                            createOctetString(agentConfig.getAuthPassPhrase()),
                            convertPrivProtocol(agentConfig.getAuthPrivProtocol()),
                            createOctetString(agentConfig.getPrivPassPhrase())));
            
            Target target = getTarget(agentConfig);
            PDU pdu = SnmpHelpers.createPDU(agentConfig);
            pdu.add(new VariableBinding(new OID(oid.toString())));
            ResponseEvent responseEvent = session.send(pdu, target);
            
            if (responseEvent.getResponse() == null) {
                log().warn("Timeout.  Agent: "+agentConfig);
            } else if (responseEvent.getError() != null) {
                log().warn("Error during getNext operation.  Error: "+responseEvent.getError().getLocalizedMessage());
            } else if (responseEvent.getResponse().getVariableBindings().size() < 1) {
                log().warn("send: Received PDU with 0 varbinds.");
            } else {
                value = new Snmp4JValue(responseEvent.getResponse().get(0).getVariable());
                if (log().isDebugEnabled())
                    log().debug("send: Snmp operation successful. Value: "+value);
            }
            
            
        } catch (IOException e) {
            log().error("getNext: Could not create Snmp session for Agent: "+agentConfig+". "+e);
        } finally {
            try {
                session.close();
                return value;
            } catch (IOException e) {
                log().error("send: Error closinging SNMP connection: "+e);
            }
        }
        return value;
        
    }

    private OID convertAuthProtocol(String authProtocol) {
        if (authProtocol == null)
            return null;
        
            if (authProtocol.equals("MD5")) {
                return AuthMD5.ID;
            } else if (authProtocol.equals("SHA")) {
                return AuthSHA.ID;
            } else {
                throw new IllegalArgumentException("Authentication protocol unsupported: " + authProtocol);
            }            
    }

    private OID convertPrivProtocol(String privProtocol) {
        if (privProtocol == null)
            return null;
        
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
     * Adapts the agent's values defined by SnmpAgentConfig's constants
     * to SNMP4J compatible constants.
     * @param agentConfig
     */
    public static void adaptConfig(SnmpAgentConfig agentConfig) {
        
        //fail-safe for those not checking prior to this call
        if(agentConfig.isAdapted())
            return;
        
        agentConfig.setPduType(convertPduType(agentConfig.getPduType()));
        agentConfig.setSecurityLevel(convertSecurityLevel(agentConfig.getSecurityLevel()));
        agentConfig.setVersion(convertVersion(agentConfig.getVersion()));
        agentConfig.setAdapted(true);
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    /**
     * Adapts the OpenNMS SNMPv3 community name to an SNMP4J compatible
     * community name (String -> OctetString)
     * 
     * @param agentConfig
     * @return
     */
    public static OctetString convertCommunity(String community) {
        return new OctetString(community);
    }

    /**
     * This method adapts the OpenNMS SNMP version constants
     * to SNMP4J defined constants.
     * 
     * @param version
     * @return
     */
    public static int convertVersion(int version) {

        switch (version) {
        case SnmpAgentConfig.VERSION3 :
            return SnmpConstants.version3;
        case SnmpAgentConfig.VERSION2C :
            return SnmpConstants.version2c;
        default :
            return SnmpConstants.version1;
        }
    }

    /**
     * Adapts the OpenNMS SNMPv3 security name to an SNMP4J compatible
     * security name (String -> OctetString)
     * 
     * @param securityName
     * @return
     */
    public static OctetString convertSecurityName(String securityName) {
        return new OctetString(securityName);
    }

    /**
     * This method adapts the OpenNMS SNMPv3 security level constants
     * to SNMP4J defined constants.
     * 
     * @param securityLevel
     * @return
     */
    public static int convertSecurityLevel(int securityLevel) {
        
    
        switch (securityLevel) {
        case SnmpAgentConfig.AUTH_NOPRIV :
            securityLevel = SecurityLevel.AUTH_NOPRIV;
        case SnmpAgentConfig.AUTH_PRIV :
            securityLevel = SecurityLevel.AUTH_PRIV;
        case SnmpAgentConfig.NOAUTH_NO_PRIV :
            securityLevel = SecurityLevel.NOAUTH_NOPRIV;
        default :
           securityLevel = SecurityLevel.NOAUTH_NOPRIV;
        }
        
        return securityLevel;
    }

    /**
     * This method converts an InetAddress to an implementation of an SNMP4J Address
     * (UdpAddress or TcpAddress)
     * 
     * TODO: This needs to be updated when the protocol flag is added to the SNMP Config
     * so that UDP or TCP can be used in v3 operations.
     */
    public static Address convertAddress(InetAddress address, int port) {
        String transportAddress = address.getHostAddress();
        transportAddress += "/" + port;
        Address targetAddress = new UdpAddress(transportAddress);
        return targetAddress;
    }
    
    /**
     * Converts OpenNMS PDU type constant to that value used in the SNMP4J library.
     * 
     * @param agentConfig
     * @return
     */
    public static int convertPduType(int pduType) {
        
        switch (pduType) {
        case SnmpAgentConfig.GET_PDU :
            return PDU.GET;
        case SnmpAgentConfig.GETNEXT_PDU :
            return PDU.GETNEXT;
        case SnmpAgentConfig.GETBULK_PDU :
            return PDU.GETBULK;
        case SnmpAgentConfig.SET_PDU :
            return PDU.SET;
        default :
            ThreadCategory.getInstance(Snmp4JStrategy.class).warn("convertPduType: Unknown SnmpAgentConfig PDU type requested...Defaulting to GET. Type requested: "+pduType);
            return PDU.SET;
        }
    }

    protected static Target getTarget(SnmpAgentConfig agentConfig) {
    
        Target target = null;
        
        //TODO: Need to do something better than this.
        if (!agentConfig.isAdapted()) {
            return target;
        }
        
        if (agentConfig.getVersion() == SnmpConstants.version3) {
            target = new UserTarget();
            ((UserTarget)target).setSecurityLevel(agentConfig.getVersion());
            ((UserTarget)target).setSecurityName(convertSecurityName(agentConfig.getSecurityName()));
        } else {
            target = new CommunityTarget();
            ((CommunityTarget)target).setCommunity(convertCommunity(agentConfig.getReadCommunity()));
        }
    
        target.setVersion((agentConfig.getVersion()));
        target.setRetries(agentConfig.getRetries());
        target.setTimeout(agentConfig.getTimeout());
        target.setAddress(convertAddress(agentConfig.getAddress(), agentConfig.getPort()));
        target.setMaxSizeRequestPDU(agentConfig.getMaxRequestSize());
            
        return target;
    }
    
    private static OctetString createOctetString(String s) {
        OctetString octetString;
        if (s.startsWith("0x")) {
            octetString = OctetString.fromHexString(s.substring(2), ':');
        } else {
            octetString = new OctetString(s);
        }
        return octetString;
    }

}
