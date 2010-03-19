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
package org.opennms.netmgt.mock;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.StateReference;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * Represents an SNMP Agent for the nodes in a MockNetwork.  SNMP Configuration
 * for the interfaces for the mock network need to be configured to proxy to the
 * host running this agent.
 *
 * @author brozow
 */
public class MockProxy implements CommandResponder {

    private TransportMapping m_transport;
    private Snmp m_snmp;
    private MockAgent m_agent;

    public MockProxy(int port) throws IOException {
        m_transport = new DefaultUdpTransportMapping(new UdpAddress(InetAddress.getLocalHost(), port));
        
        m_snmp = new Snmp(m_transport);
        
        m_snmp.addCommandResponder(this);
        
        m_transport.listen();
        
    }

    public void processPdu(CommandResponderEvent e) {
        PDU command = e.getPDU();
        if (command == null) return;
     
        PDU response = processRequest(command);
          if (response == null) return;
          
          StatusInformation statusInformation = new StatusInformation();
          StateReference ref = e.getStateReference();
          try {
              Logger.getLogger(MockProxy.class).debug("Replying with: "+command);
              e.setProcessed(true);
              e.getMessageDispatcher().returnResponsePdu(e.getMessageProcessingModel(),
                                                         e.getSecurityModel(),
                                                         e.getSecurityName(),
                                                         e.getSecurityLevel(),
                                                         command,
                                                         e.getMaxSizeResponsePDU(),
                                                         ref,
                                                         statusInformation);
          }
          catch (MessageException ex) {
              System.err.println("Error while sending response: "+ex.getMessage());
              Logger.getLogger(MockProxy.class).error(ex);
          }
          
        
    }

    /**
     * @param request
     * @return
     */
    private PDU processRequest(PDU request) {
        if (!isRequestPDU(request)) return null;
        
        switch(request.getType()) {
        case PDU.GET:
            return processGet(request);
        case PDU.GETNEXT:
            return processGetNext(request);
        case PDU.GETBULK:
            return processGetBulk(request);
        case PDU.SET:
            return processSet(request);
        case PDU.INFORM:
            return processInform(request);
        default:
            return processUnhandled(request);
        }
        
    }

    /**
     * @param request
     * @return
     */
    private PDU processUnhandled(PDU request) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param request
     * @return
     */
    private PDU processInform(PDU request) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param request
     * @return
     */
    private PDU processSet(PDU request) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param request
     * @return
     */
    private PDU processGetBulk(PDU request) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param request
     * @return
     */
    private PDU processGet(PDU request) {
        PDU response = request;
        response.setErrorIndex(0);
        response.setErrorStatus(0);
        response.setType(PDU.RESPONSE);
        
        Vector<VariableBinding> varBinds = response.getVariableBindings();
        for(int i = 0; i < varBinds.size(); i++) {
            VariableBinding varBind = varBinds.get(i);
            VariableBinding nextVarBind = m_agent.get(varBind.getOid());
            if (nextVarBind == null) {
                if (response instanceof PDUv1) {
                    if (response.getErrorIndex() == 0) {
                        response.setErrorIndex(i+1);
                        response.setErrorStatus(PDU.noSuchName);
                    } 
                } else {
                    varBind.setVariable(Null.endOfMibView);
                }
            } else {
                response.set(i, nextVarBind);
            }
        }
        
        return response;
    }

    /**
     * @param request
     * @return
     */
    private PDU processGetNext(PDU request) {
        PDU response = request;
        response.setErrorIndex(0);
        response.setErrorStatus(0);
        response.setType(PDU.RESPONSE);
        
        Vector<VariableBinding> varBinds = response.getVariableBindings();
        for(int i = 0; i < varBinds.size(); i++) {
            VariableBinding varBind = varBinds.get(i);
            VariableBinding nextVarBind = m_agent.getNext(varBind.getOid());
            if (nextVarBind == null) {
                if (response instanceof PDUv1) {
                    if (response.getErrorIndex() == 0) {
                        response.setErrorIndex(i+1);
                        response.setErrorStatus(PDU.noSuchName);
                    } 
                } else {
                    varBind.setVariable(Null.endOfMibView);
                }
            } else {
                response.set(i, nextVarBind);
            }
        }
        
        return response;

    }

    /**
     * @param command
     * @return
     */
    private boolean isRequestPDU(PDU command) {
        return (command.getType() != PDU.TRAP) &&
                (command.getType() != PDU.V1TRAP) &&
                (command.getType() != PDU.REPORT) &&
                (command.getType() != PDU.RESPONSE);
    }

    /**
     * @param agent
     */
    public void addAgent(MockAgent agent) {
        m_agent = agent;
    }

    /**
     * 
     */
    public void stop() throws IOException {
        m_snmp.close();
    }


}
