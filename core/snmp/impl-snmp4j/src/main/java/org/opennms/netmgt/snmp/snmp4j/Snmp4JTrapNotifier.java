/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.snmp4j;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.opennms.netmgt.snmp.TrapProcessor;
import org.opennms.netmgt.snmp.TrapProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.mp.StateReference;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

public class Snmp4JTrapNotifier implements CommandResponder {
	
	public static final transient Logger LOG = LoggerFactory.getLogger(Snmp4JTrapNotifier.class);

    private TrapProcessorFactory m_trapProcessorFactory;
    private TrapNotificationListener m_listener;

    public Snmp4JTrapNotifier(TrapNotificationListener listener, TrapProcessorFactory processorFactory) {
        m_listener = listener;
        m_trapProcessorFactory = processorFactory;
    }
    
    public static class Snmp4JV1TrapInformation extends TrapInformation {

        private PDUv1 m_pdu;

        protected Snmp4JV1TrapInformation(InetAddress agent, String community, PDUv1 pdu, TrapProcessor trapProcessor) {
            super(agent, community, trapProcessor);
            m_pdu = pdu;
        }

        protected InetAddress getTrapAddress() {
            return m_pdu.getAgentAddress().getInetAddress();
        }

        protected String getVersion() {
            return "v1";
        }

        protected int getPduLength() {
            return m_pdu.getVariableBindings().size();
        }

        protected long getTimeStamp() {
            return m_pdu.getTimestamp();
        }

        protected TrapIdentity getTrapIdentity() {
            return new TrapIdentity(SnmpObjId.get(m_pdu.getEnterprise().getValue()), m_pdu.getGenericTrap(), m_pdu.getSpecificTrap());
        }
        
        protected VariableBinding getVarBindAt(int i) {
            return m_pdu.get(i);
        }

        protected void processVarBindAt(int i) {
            SnmpObjId name = SnmpObjId.get(getVarBindAt(i).getOid().getValue());
            SnmpValue value = new Snmp4JValue(getVarBindAt(i).getVariable());
            processVarBind(name, value);
        }
    }

    public static class Snmp4JV2TrapInformation extends TrapInformation {
        /**
         * The received PDU
         */
        private PDU m_pdu;
        
        /**
         * The name of the PDU's type
         */
        private String m_pduTypeString;

        /**
         * The snmp sysUpTime OID is the first varbind
         */
        static final int SNMP_SYSUPTIME_OID_INDEX = 0;

        /**
         * The snmp trap OID is the second varbind
         */
        static final int SNMP_TRAP_OID_INDEX = 1;

        /**
         * The sysUpTimeOID, which should be the first varbind in a V2 trap
         */
        static final OID SNMP_SYSUPTIME_OID = new OID(".1.3.6.1.2.1.1.3.0");

        /**
         * The sysUpTimeOID, which should be the first varbind in a V2 trap, but in
         * the case of Extreme Networks only mostly
         */
        static final OID EXTREME_SNMP_SYSUPTIME_OID = new OID(".1.3.6.1.2.1.1.3");

        /**
         * The snmpTrapOID, which should be the second varbind in a V2 trap
         */
        static final OID SNMP_TRAP_OID = new OID(".1.3.6.1.6.3.1.1.4.1.0");

        /**
         * Constructs a new trap information instance that contains the sending
         * agent, the community string, and the Protocol Data Unit.
         * 
         * @param agent
         *            The sending agent's address
         * @param community
         *            The community string from the SNMP packet.
         * @param pdu
         *            The encapsulated Protocol Data Unit.
         * @param trapProcessor The trap processor used to process the trap data
         * 
         */
        public Snmp4JV2TrapInformation(InetAddress agent, String community, PDU pdu, TrapProcessor trapProcessor) {
            super(agent, community, trapProcessor);
            m_pdu = pdu;
            m_pduTypeString = PDU.getTypeString(m_pdu.getType());
        }

        /**
         * Returns the Protocol Data Unit that was encapsulated within the SNMP
         * Trap message
         */
        private PDU getPdu() {
            return m_pdu;
        }
        
        protected int getPduLength() {
            return getPdu().size();
        }
        
        protected long getTimeStamp() {

        	LOG.debug("V2 {}"+m_pduTypeString+" first varbind value: {}", m_pduTypeString, getVarBindAt(0).getVariable());

            switch (getVarBindAt(SNMP_SYSUPTIME_OID_INDEX).getVariable().getSyntax()) {
            case SMIConstants.SYNTAX_TIMETICKS:
                LOG.debug("V2 {} first varbind value is of type TIMETICKS (correct)", m_pduTypeString);
                return ((TimeTicks) getVarBindAt(SNMP_SYSUPTIME_OID_INDEX).getVariable()).getValue();
            case SMIConstants.SYNTAX_INTEGER32:
                LOG.debug("V2 {} first varbind value is of type INTEGER, casting to TIMETICKS", m_pduTypeString);
                return ((Integer32) getVarBindAt(SNMP_SYSUPTIME_OID_INDEX).getVariable()).getValue();
            default:
                throw new IllegalArgumentException("V2 "+m_pduTypeString+" does not have the required first varbind as TIMETICKS - cannot process "+m_pduTypeString);
            }
        }

        protected TrapIdentity getTrapIdentity() {
            OID snmpTrapOid = (OID) getVarBindAt(SNMP_TRAP_OID_INDEX).getVariable();
            OID lastVarBindOid = getVarBindAt(getPduLength() - 1).getOid();
            Variable lastVarBindValue = getVarBindAt(getPduLength() - 1).getVariable();
            return new TrapIdentity(SnmpObjId.get(snmpTrapOid.getValue()), SnmpObjId.get(lastVarBindOid.getValue()), new Snmp4JValue(lastVarBindValue));
        }

        public InetAddress getTrapAddress() {
            return getAgentAddress();
        }

        protected VariableBinding getVarBindAt(int index) {
            return getPdu().get(index);
        }

        protected String getVersion() {
            return "v2";
        }

        protected void validate() {
        	int pduType = getPdu().getType();
            if (pduType != PDU.TRAP && pduType != PDU.INFORM) {
                throw new IllegalArgumentException("Received not SNMPv2 Trap|Inform from host " + getTrapAddress() + " PDU Type = " + PDU.getTypeString(getPdu().getType()));
            }
            
            LOG.debug("V2 {} numVars or pdu length: {}", m_pduTypeString, getPduLength());
            
            if (getPduLength() < 2) {
                throw new IllegalArgumentException("V2 "+m_pduTypeString+" from " + getTrapAddress() + " IGNORED due to not having the required varbinds.  Have " + getPduLength() + ", needed at least 2");
            }
            
            OID varBindName0 = getVarBindAt(0).getOid();
            OID varBindName1 = getVarBindAt(1).getOid();

            /*
             * Modify the sysUpTime varbind OID to add the trailing 0 if it is
             * missing, which is seen with some Extreme equipment.
             */
            if (varBindName0.equals(EXTREME_SNMP_SYSUPTIME_OID)) {
                LOG.info("V2 {} from {} has been corrected due to the sysUptime.0 varbind not having been sent with a trailing 0.\n\tVarbinds received are : {} and {}",
                		m_pduTypeString,
                		getTrapAddress(),
                		varBindName0,
                		varBindName1
                		);
                varBindName0 = SNMP_SYSUPTIME_OID;
            }
            
            /*
             * Confirm that the two required varbinds (sysUpTime and
             * snmpTrapOID) are present and in that order.
             */
            if ((!(varBindName0.equals(SNMP_SYSUPTIME_OID))) || (!(varBindName1.equals(SNMP_TRAP_OID)))) {
                throw new IllegalArgumentException("V2 "+m_pduTypeString+" from " + getTrapAddress() + " IGNORED due to not having the required varbinds.\n\tThe first varbind must be sysUpTime.0 and the second snmpTrapOID.0\n\tVarbinds received are : " + varBindName0 + " and " + varBindName1);
            }
        }

        protected void processVarBindAt(int i) {
            if (i == 0) {
                LOG.debug("Skipping processing of varbind {}: it is sysuptime and the first varbind, and is not processed as a parm per RFC2089", i);
            } else if (i == 1) {
                LOG.debug("Skipping processing of varbind {}: it is the trap OID and the second varbind, and is not processed as a parm per RFC2089", i);				
            } else {
                SnmpObjId name = SnmpObjId.get(getVarBindAt(i).getOid().getValue());
                SnmpValue value = new Snmp4JValue(getVarBindAt(i).getVariable());
                processVarBind(name, value);
            }
        }
    }
        
    

    @Override
    public void processPdu(CommandResponderEvent e) {
    	PDU command = new PDU(e.getPDU());
        IpAddress addr = ((IpAddress)e.getPeerAddress());
        
        if (command != null) {
        	if (command.getType() == PDU.INFORM) {
        		PDU response = new PDU(command);
        		response.setErrorIndex(0);
        		response.setErrorStatus(0);
        		response.setType(PDU.RESPONSE);
        		StatusInformation statusInformation = new StatusInformation();
        		StateReference ref = e.getStateReference();
        		try {
        			e.getMessageDispatcher().returnResponsePdu(e.getMessageProcessingModel(),
        														e.getSecurityModel(),
        														e.getSecurityName(),
        														e.getSecurityLevel(),
        														response,
        														e.getMaxSizeResponsePDU(),
        														ref,
        														statusInformation);
        			LOG.debug("Sent RESPONSE PDU to peer {} acknowledging receipt of INFORM (reqId={})", addr, command.getRequestID());
        		} catch (MessageException ex) {
        			LOG.error("Error while sending RESPONSE PDU to peer " + addr + ": " + ex.getMessage() + "acknowledging receipt of INFORM (reqId=" + command.getRequestID() + ")");
        		}
        	}
        }
        
        if (e.getPDU() instanceof PDUv1) {
            m_listener.trapReceived(new Snmp4JV1TrapInformation(addr.getInetAddress(), new String(e.getSecurityName()), (PDUv1)e.getPDU(), m_trapProcessorFactory.createTrapProcessor()));
        } else {
            m_listener.trapReceived(new Snmp4JV2TrapInformation(addr.getInetAddress(), new String(e.getSecurityName()), e.getPDU(), m_trapProcessorFactory.createTrapProcessor()));
        }
    }
}
