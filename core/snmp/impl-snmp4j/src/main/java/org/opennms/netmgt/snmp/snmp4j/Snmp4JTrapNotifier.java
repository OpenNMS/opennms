/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.snmp4j;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpException;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpVarBindDTO;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.TrapNotificationListener;
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

    private TrapNotificationListener m_listener;

    public Snmp4JTrapNotifier(TrapNotificationListener listener) {
        m_listener = listener;
    }
    
    public static class Snmp4JV1TrapInformation extends TrapInformation {

        private PDUv1 m_pdu;

        public Snmp4JV1TrapInformation(InetAddress agent, String community, PDUv1 pdu) {
            super(agent, community);
            m_pdu = pdu;
        }
        
        /**
         * Returns the Protocol Data Unit that was encapsulated within the SNMP
         * Trap message
         */
        public PDUv1 getPdu() {
            return m_pdu;
        }

        /**
         * @return The {@link InetAddress} of the agent that generated the trap
         * as found in the SNMPv1 AgentAddress field. This can vary from the value
         * of {@link #getAgentAddress()} if the SNMPv1 trap has been forwarded.
         */
        @Override
        public InetAddress getTrapAddress() {
            return m_pdu.getAgentAddress().getInetAddress();
        }

        @Override
        public String getVersion() {
            return "v1";
        }

        @Override
        public int getPduLength() {
            return m_pdu.getVariableBindings().size();
        }

        @Override
        public long getTimeStamp() {
            return m_pdu.getTimestamp();
        }

        @Override
        public TrapIdentity getTrapIdentity() {
            return new TrapIdentity(SnmpObjId.get(m_pdu.getEnterprise().getValue()), m_pdu.getGenericTrap(), m_pdu.getSpecificTrap());
        }

        protected VariableBinding getVarBindAt(int i) {
            return m_pdu.get(i);
        }

        @Override
        public SnmpVarBindDTO getSnmpVarBindDTO(int i) {
            SnmpObjId name = SnmpObjId.get(getVarBindAt(i).getOid().getValue());
            SnmpValue value = new Snmp4JValue(getVarBindAt(i).getVariable());
            return new SnmpVarBindDTO(name, value);
        }

		@Override
		protected Integer getRequestId() {
			return m_pdu.getRequestID().toInt();
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("[");
			sb.append("Version=").append(getVersion())
				.append(", Agent-Addr=").append(getTrapAddress().getHostAddress())
				.append(", Length=").append(getPduLength())
				.append(", Identity=").append(getTrapIdentity().toString())
				.append(", Request-ID=").append(getRequestId())
				.append("]");
			return sb.toString();
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
         */
        public Snmp4JV2TrapInformation(InetAddress agent, String community, PDU pdu) {
            super(agent, community);
            m_pdu = pdu;
            m_pduTypeString = PDU.getTypeString(m_pdu.getType());
        }

        /**
         * Returns the Protocol Data Unit that was encapsulated within the SNMP
         * Trap message
         */
        public PDU getPdu() {
            return m_pdu;
        }
        
        @Override
        public int getPduLength() {
            return getPdu().size();
        }
        
        @Override
        public long getTimeStamp() {

            LOG.debug("V2 {} first varbind value: {}", m_pduTypeString, getVarBindAt(0).getVariable());

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

        @Override
        public TrapIdentity getTrapIdentity() {
            OID snmpTrapOid = (OID) getVarBindAt(SNMP_TRAP_OID_INDEX).getVariable();
            OID lastVarBindOid = getVarBindAt(getPduLength() - 1).getOid();
            Variable lastVarBindValue = getVarBindAt(getPduLength() - 1).getVariable();
            return new TrapIdentity(SnmpObjId.get(snmpTrapOid.getValue()), SnmpObjId.get(lastVarBindOid.getValue()), new Snmp4JValue(lastVarBindValue));
        }

        /**
         *  For SNMPv2 traps, this returns the same value as {@link #getAgentAddress()}.
         */
        @Override
        public InetAddress getTrapAddress() {
            return getAgentAddress();
        }

        protected VariableBinding getVarBindAt(int index) {
            return getPdu().get(index);
        }

        @Override
        public String getVersion() {
            return "v2";
        }

        @Override
        public void validate() throws SnmpException {
        	int pduType = getPdu().getType();
            if (pduType != PDU.TRAP && pduType != PDU.INFORM) {
                throw new SnmpException("Received not SNMPv2 Trap|Inform from host " + getTrapAddress() + " PDU Type = " + PDU.getTypeString(getPdu().getType()));
            }
            
            LOG.debug("V2 {} numVars or pdu length: {}", m_pduTypeString, getPduLength());
            
            if (getPduLength() < 2) {
                throw new SnmpException("V2 "+m_pduTypeString+" from " + getTrapAddress() + " IGNORED due to not having the required varbinds.  Have " + getPduLength() + ", needed at least 2");
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
                throw new SnmpException("V2 "+m_pduTypeString+" from " + getTrapAddress() + " IGNORED due to not having the required varbinds.\n\tThe first varbind must be sysUpTime.0 and the second snmpTrapOID.0\n\tVarbinds received are : " + varBindName0 + " and " + varBindName1);
            }
        }

        @Override
        public SnmpVarBindDTO getSnmpVarBindDTO(int i) {
            if (i == 0) {
                LOG.debug("Skipping processing of varbind {}: it is sysuptime and the first varbind, and is not processed as a parm per RFC2089", i);
                return null;
            } else if (i == 1) {
                LOG.debug("Skipping processing of varbind {}: it is the trap OID and the second varbind, and is not processed as a parm per RFC2089", i);
                return null;
            } else {
                SnmpObjId name = SnmpObjId.get(getVarBindAt(i).getOid().getValue());
                SnmpValue value = new Snmp4JValue(getVarBindAt(i).getVariable());
                return new SnmpVarBindDTO(name, value);
            }
        }

		@Override
		protected Integer getRequestId() {
			return m_pdu.getRequestID().toInt();
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("[");
			sb.append("Version=").append(getVersion())
				.append(", Source-Addr=").append(getTrapAddress().getHostAddress())
				.append(", Length=").append(getPduLength())
				.append(", Identity=").append(getTrapIdentity().toString())
				.append(", Request-ID=").append(getRequestId())
				.append("]");
			return sb.toString();
		}
    }
        
    

    @Override
    public void processPdu(CommandResponderEvent e) {
        PDU command = e.getPDU();
        if (command != null) {
            IpAddress addr = ((IpAddress)e.getPeerAddress());
            if (command.getType() == PDU.INFORM) {
                // Backing up original content
                int errorIndex = command.getErrorIndex();
                int errorStatus = command.getErrorStatus();
                int type = command.getType();
                // Prepare resopnse
                command.setErrorIndex(0);
                command.setErrorStatus(0);
                command.setType(PDU.RESPONSE);
                StatusInformation statusInformation = new StatusInformation();
                StateReference ref = e.getStateReference();
                // Send the response
                try {
                    e.getMessageDispatcher().returnResponsePdu(e.getMessageProcessingModel(),
                                                               e.getSecurityModel(),
                                                               e.getSecurityName(),
                                                               e.getSecurityLevel(),
                                                               command,
                                                               e.getMaxSizeResponsePDU(),
                                                               ref,
                                                               statusInformation);
                    LOG.debug("Sent RESPONSE PDU to peer {} acknowledging receipt of INFORM (reqId={})", addr, command.getRequestID());
                } catch (MessageException ex) {
                    LOG.error("Error while sending RESPONSE PDU to peer {}: {} acknowledging receipt of INFORM (reqId={})", addr, ex.getMessage(), command.getRequestID());
                } finally {
                    // Restoring original settings
                    command.setErrorIndex(errorIndex);
                    command.setErrorStatus(errorStatus);
                    command.setType(type);
                }
            }
            if (command instanceof PDUv1) {
                m_listener.trapReceived(new Snmp4JV1TrapInformation(addr.getInetAddress(), new String(e.getSecurityName()), (PDUv1)command));
            } else {
                m_listener.trapReceived(new Snmp4JV2TrapInformation(addr.getInetAddress(), new String(e.getSecurityName()), command));
            }
        }
    }
}
