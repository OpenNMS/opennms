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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.capsd;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.utils.ParameterMap;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Snmpv3PluginTest extends TestCase {

    private static final String DEFAULT_PORT = "161";
    private static final String DEFAULT_TIMEOUT = "3000";
    private static final String DEFAULT_RETRY = "2";

    private static final OctetString DEFAULT_SECURITY_NAME = new OctetString("opennms");
    private static final OID DEFAULT_AUTH_PROTOCOL = AuthMD5.ID;
    private static final OctetString DEFAULT_AUTH_PASSPHRASE = new OctetString("opennms");
    private static final OID DEFAULT_PRIV_PROTOCOL = PrivDES.ID;
    private static final OctetString DEFAULT_PRIV_PASSPHRASE = new OctetString("opennms");
    private static final String DEFAULT_VERSION = "snmpv3";

    /**
     * The system object identifier to retreive from the remote agent.
     */
    private static final String DEFAULT_OID = ".1.3.6.1.2.1.1.2.0";

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testIsProtocolSupported() {
        
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
            address = InetAddress.getByName("192.168.0.100");
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        assertTrue(isProtocolSupported(address, new HashMap()));
    }
    
    private PDU createPDU(Target target) {
        PDU request;
        request = new ScopedPDU();
        ScopedPDU scopedPDU = (ScopedPDU) request;
//      scopedPDU.setContextEngineID(contextEngineID);
//      scopedPDU.setContextName(contextName);
        request.setType(PDU.GET);
        return request;
    }

    
    private Snmp createSnmpSession() throws IOException {
        TransportMapping transport;
        
        transport = new DefaultUdpTransportMapping();
        
        Snmp snmp = new Snmp(transport);
        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);
//      UsmUser user = new UsmUser(DEFAULT_SECURITY_NAME, DEFAULT_PRIV_PROTOCOL, DEFAULT_AUTH_PASSPHRASE, DEFAULT_PRIV_PROTOCOL, DEFAULT_PRIV_PASSPHRASE);
        UsmUser user = new UsmUser(DEFAULT_SECURITY_NAME, null, null, null, null);
        snmp.getUSM().addUser(DEFAULT_SECURITY_NAME, user);

        return snmp;
    }

    public boolean isProtocolSupported(InetAddress address, Map qualifiers) {

        InetAddress inetAddress = address;
        String port =  (qualifiers.get("port") == null ? DEFAULT_PORT : (String)qualifiers.get("port"));
        String timeout = (qualifiers.get("timeout") == null ? DEFAULT_TIMEOUT : (String)qualifiers.get("timeout"));
        String retry = (qualifiers.get("retry") == null ? DEFAULT_RETRY : (String)qualifiers.get("retry"));
        String forceVersion = (qualifiers.get("forced version") == null ? DEFAULT_VERSION : (String)qualifiers.get("forced version"));
        String vbValue = (String)qualifiers.get("vbvalue");
        String oid = ParameterMap.getKeyedString(qualifiers, "vbname", DEFAULT_OID);
        
        boolean isSupported = false;
        
        MPv3.setEnterpriseID(5813);
        
        String transportAddress = inetAddress.getHostAddress() + "/" + DEFAULT_PORT;
        
        Address targetAddress = new UdpAddress(transportAddress);
        Snmp snmp = null;
        try {
            snmp = createSnmpSession();
            UserTarget target = new UserTarget();
            target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
            target.setVersion(SnmpConstants.version3);
            target.setAddress(targetAddress);
            target.setRetries(Integer.parseInt(retry));
            target.setTimeout(Integer.parseInt(timeout));
            target.setSecurityName(DEFAULT_SECURITY_NAME);
            snmp.listen();
            PDU request = createPDU(target);
            VariableBinding vb = new VariableBinding(new OID(DEFAULT_OID));
            request.add(vb);
            
            PDU response = null;
            ResponseEvent responseEvent;
            responseEvent = snmp.send(request, target);
            snmp.close();
            
            if (responseEvent.getResponse() != null) {
                MockUtil.println(responseEvent.getResponse().toString());
                return true;
            } else {
                return false;
            }

            
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isSupported;
    }


}
