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

import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.opennms.netmgt.snmp.TrapProcessor;
import org.opennms.netmgt.snmp.TrapProcessorFactory;
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

/**
 * <p>Snmp4JTrapNotifier class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Snmp4JTrapNotifier implements CommandResponder {

    private TrapProcessorFactory m_trapProcessorFactory;
    private TrapNotificationListener m_listener;

    /**
     * <p>Constructor for Snmp4JTrapNotifier.</p>
     *
     * @param listener a {@link org.opennms.netmgt.snmp.TrapNotificationListener} object.
     * @param processorFactory a {@link org.opennms.netmgt.snmp.TrapProcessorFactory} object.
     */
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

        	if (log().isDebugEnabled()) {
                log().debug("V2 "+m_pduTypeString+" first varbind value: " + getVarBindAt(0).getVariable().toString());
            }

            switch (getVarBindAt(SNMP_SYSUPTIME_OID_INDEX).getVariable().getSyntax()) {
            case SMIConstants.SYNTAX_TIMETICKS:
                log().debug("V2 "+m_pduTypeString+" first varbind value is of type TIMETICKS (correct)");
                return ((TimeTicks) getVarBindAt(SNMP_SYSUPTIME_OID_INDEX).getVariable()).getValue();
            case SMIConstants.SYNTAX_INTEGER32:
                log().debug("V2 "+m_pduTypeString+" first varbind value is of type INTEGER, casting to TIMETICKS");
                return ((Integer32) getVarBindAt(SNMP_SYSUPTIME_OID_INDEX).getVariable()).getValue();
            default:
                throw new IllegalArgumentException("V2 "+m_pduTypeString+" does not have the required first varbind as TIMETICKS - cannot process "+m_pduTypeString);
            }
        }

        protected TrapIdentity getTrapIdentity() {
            // Get the value for the snmpTrapOID
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
            //
            // verify the type
            //
        	int pduType = getPdu().getType();
            if (pduType != PDU.TRAP && pduType != PDU.INFORM) {
                // if not V2 trap or inform, do nothing
                throw new IllegalArgumentException("Received not SNMPv2 Trap|Inform from host " + getTrapAddress() + " PDU Type = " + PDU.getTypeString(getPdu().getType()));
            }
            if (log().isDebugEnabled()) {
                log().debug("V2 "+m_pduTypeString+" numVars or pdu length: " + getPduLength());
            }
            if (getPduLength() < 2) // check number of varbinds
            {
                throw new IllegalArgumentException("V2 "+m_pduTypeString+" from " + getTrapAddress() + " IGNORED due to not having the required varbinds.  Have " + getPduLength() + ", needed at least 2");
            }
            // The first varbind has the sysUpTime
            // Modify the sysUpTime varbind to add the trailing 0 if it is
            // missing
            // The second varbind has the snmpTrapOID
            // Confirm that these two are present
            //
            OID varBindName0 = getVarBindAt(0).getOid();
            OID varBindName1 = getVarBindAt(1).getOid();
            if (varBindName0.equals(EXTREME_SNMP_SYSUPTIME_OID)) {
                log().info("V2 "+m_pduTypeString+" from " + getTrapAddress() + " has been corrected due to the sysUptime.0 varbind not having been sent with a trailing 0.\n\tVarbinds received are : " + varBindName0 + " and " + varBindName1);
                varBindName0 = SNMP_SYSUPTIME_OID;
            }
            if ((!(varBindName0.equals(SNMP_SYSUPTIME_OID))) || (!(varBindName1.equals(SNMP_TRAP_OID)))) {
                throw new IllegalArgumentException("V2 "+m_pduTypeString+" from " + getTrapAddress() + " IGNORED due to not having the required varbinds.\n\tThe first varbind must be sysUpTime.0 and the second snmpTrapOID.0\n\tVarbinds received are : " + varBindName0 + " and " + varBindName1);
            }
        }

        protected void processVarBindAt(int i) {
        	if (i<2) {
                if (i == 0) {
                	log().debug("Skipping processing of varbind it is the sysuptime and the first varbind, it is not processed as a parm per RFC2089");
                } else {
                	log().debug("Skipping processing of varbind it is the trap OID and the second varbind, it is not processed as a parm per RFC2089");				
    			}
        	} else {
        		SnmpObjId name = SnmpObjId.get(getVarBindAt(i).getOid().getValue());
        		SnmpValue value = new Snmp4JValue(getVarBindAt(i).getVariable());
        		processVarBind(name, value);
        	}
        }
    }
        
    

    /** {@inheritDoc} */
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
        			if (log().isDebugEnabled()) {
        				log().debug("Sent RESPONSE PDU to peer " + addr + " acknowledging receipt of INFORM (reqId=" + command.getRequestID() + ")");
        			}
        		} catch (MessageException ex) {
        			log().error("Error while sending RESPONSE PDU to peer " + addr + ": " + ex.getMessage() + "acknowledging receipt of INFORM (reqId=" + command.getRequestID() + ")");
        		}
        	}
        }
        
        if (e.getPDU() instanceof PDUv1)
            m_listener.trapReceived(new Snmp4JV1TrapInformation(addr.getInetAddress(), new String(e.getSecurityName()), (PDUv1)e.getPDU(), m_trapProcessorFactory.createTrapProcessor()));
        else
            m_listener.trapReceived(new Snmp4JV2TrapInformation(addr.getInetAddress(), new String(e.getSecurityName()), e.getPDU(), m_trapProcessorFactory.createTrapProcessor()));
    }
    
    private Category log() {
    	return ThreadCategory.getInstance(getClass());
    }
}
