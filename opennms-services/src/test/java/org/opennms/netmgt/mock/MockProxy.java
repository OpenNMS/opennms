/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.mock;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger LOG = LoggerFactory.getLogger(MockProxy.class);

    private TransportMapping m_transport;
    private Snmp m_snmp;
    private MockAgent m_agent;

    public MockProxy(int port) throws IOException {
        m_transport = new DefaultUdpTransportMapping(new UdpAddress(InetAddress.getLocalHost(), port));
        
        m_snmp = new Snmp(m_transport);
        
        m_snmp.addCommandResponder(this);
        
        m_transport.listen();
        
    }

    @Override
    public void processPdu(CommandResponderEvent e) {
        PDU command = e.getPDU();
        if (command == null) return;
     
        PDU response = processRequest(command);
          if (response == null) return;
          
          StatusInformation statusInformation = new StatusInformation();
          StateReference ref = e.getStateReference();
          try {
              LOG.debug("Replying with: {}", command);
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
              LOG.error("Error while sending response", ex);
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
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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
