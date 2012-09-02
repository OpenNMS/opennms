/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import org.opennms.core.utils.Argument;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;

/**
 * <p>SnmpTrapNotificationStrategy class.</p>
 *
 * @author david
 * @version $Id: $
 */
public class SnmpTrapNotificationStrategy implements NotificationStrategy {
    
    private List<Argument> m_arguments;

    /** {@inheritDoc} */
    public int send (List<Argument> arguments) {
        
        m_arguments = arguments;
        String argVersion = getVersion();
        
        if (argVersion == null) {
            log().info("send: trapVersion paramenter is null, defaulting to \"v1\".");
            argVersion = "v1";
        }

        //determine version of trap and send it.
        try {
            if (argVersion.equals("v1")) {
                sendV1Trap();
            } else if (argVersion.equals("v2c") || argVersion.equals("v2")) {
                sendV2Trap();
            } else if (argVersion.equals("v3")) {
                log().info("send: Version3 not supported in notifications, yet, falling back to v2c.");
                sendV2Trap();
            } else {
                log().info("send: No version specified in first argument to notification.");
                return 1;
            }
        } catch (Throwable e) {
            log().info("send: Exception trying to send trap. ",e);
            return 1;
        }
        
        return 0;
    }
    
    private String getVersion() {
        return getSwitchValue("trapVersion");
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(this.getClass());
    }

    /**
     * <p>sendV1Trap</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void sendV1Trap() throws Exception {        
        SnmpV1TrapBuilder pdu = SnmpUtils.getV1TrapBuilder();

        pdu.setEnterprise(SnmpObjId.get(getEnterpriseId()));
        
        pdu.setGeneric(getGenericId());
        
        pdu.setSpecific(getSpecificId());
        
        pdu.setTimeStamp(0);
        
        InetAddress agentAddress = getHostInetAddress();
        pdu.setAgentAddress(agentAddress);
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.4.1.5813.20.1"), SnmpUtils.getValueFactory().getOctetString(getVarbind().getBytes()));
        
        pdu.send(InetAddressUtils.str(agentAddress), getPort(), getCommunity());
    }

    /**
     * <p>sendV2Trap</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void sendV2Trap() throws Exception {
        
        SnmpObjId enterpriseId = SnmpObjId.get(getEnterpriseId());
        boolean isGeneric = false;
        
        SnmpObjId trapOID;
        if (SnmpObjId.get(".1.3.6.1.6.3.1.1.5").isPrefixOf(enterpriseId)) {
            isGeneric = true;
            trapOID = enterpriseId;
        } else {
            trapOID = SnmpObjId.get(enterpriseId, new SnmpInstId(getSpecificId()));
            // XXX or should it be this
            // trap OID = enterprise + ".0." + specific;
        }
        
        SnmpTrapBuilder pdu = SnmpUtils.getV2TrapBuilder();
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), SnmpUtils.getValueFactory().getTimeTicks(0));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), SnmpUtils.getValueFactory().getObjectId(trapOID));
        
        if (isGeneric) {
            pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), SnmpUtils.getValueFactory().getObjectId(enterpriseId));
        }
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.4.1.5813.20.1"), SnmpUtils.getValueFactory().getOctetString(getVarbind().getBytes()));

        pdu.send(InetAddressUtils.str(getHostInetAddress()), getPort(), getCommunity());
    }

    /**
     * Helper method to get the Host argument.
     * 
     * @return
     * @throws UnknownHostException
     */
    private InetAddress getHostInetAddress() throws UnknownHostException {
        String switchValue = getSwitchValue("trapHost");
        
        if (switchValue == null) {
            log().info("getHostInetAddress: trapHost not specified, defaulting to: \"127.0.0.1\".");
            switchValue = "127.0.0.1";
        } else {
            log().debug("getHostInetAddress: trapHost argument: "+switchValue);
        }
        return InetAddressUtils.addr(switchValue);
    }

    /**
     * Helper method to get the port argument.
     * @return
     */
    private int getPort() {
        String switchValue = getSwitchValue("trapPort");
        
        if (switchValue == null) {
            log().info("getPort: trapPort argument not specified, defaulting to: \"162\".");
            return 162;
        } else {
            log().debug("getPort: trapPort argument: "+Integer.parseInt(switchValue));
        }
        return Integer.parseInt(switchValue);
    }

    /**
     * Helper method to get the trap community string argument.
     * @return
     */
    private String getCommunity() {
        String switchValue = getSwitchValue("trapCommunity");
        
        if (switchValue == null) {
            log().info("getCommunity: trapCommunity not specified, defaulting to: \"public\".");
            switchValue = "public";
        } else {
            log().debug("getCommunity: trapCommunity argument: "+switchValue);
        }
        return switchValue;
    }

    /**
     * Helper method to get the trap enterprise ID argument.
     * @return
     */
    private String getEnterpriseId() {        
        String switchValue = getSwitchValue("trapEnterprise");
        
        if (switchValue == null) {
            log().info("getEnterpriseId: trapEnterprise not specified, defaulting to: \".1.3.6.1.4.1.5813\".");
            switchValue = ".1.3.6.1.4.1.5813";
        } else {
            log().debug("getEnterpriseId: trapEnterprise argument: "+switchValue);
        }
        return switchValue;
    }

    /**
     * Helper method to get the trap generic ID argument.
     * @return
     */
    private int getGenericId() {
        String switchValue = getSwitchValue("trapGeneric");
        
        if (switchValue == null) {
            log().info("getGenericId: trapGeneric argument not specified, defaulting to: \"6\".");
            return 6;
        } else {
            log().debug("getGenericId: trapGeneric argument: "+switchValue);
        }
        return Integer.parseInt(switchValue);
    }
        
    /**
     * Helper method to get the trap specific id argument.
     * @return
     */
    private int getSpecificId() {
        String switchValue = getSwitchValue("trapSpecific");
        
        if (switchValue == null) {
            log().info("getSpecificId: trapSpecific argument not specified, defaulting to: \"1\".");
            return 1;
        } else {
            log().debug("getSpecificId: trapSpecific argument: "+Integer.parseInt(switchValue));
        }
        return Integer.parseInt(switchValue);
    }

    /**
     * Helper method to get the trap specific id argument.
     * @return
     */
    private String getVarbind() {
        String switchValue = getSwitchValue("trapVarbind");
        
        if (switchValue == null) {
            log().info("getVarbind: trapVarbind argument not specified, defaulting to: \"OpenNMS Trap Notification\".");
            return "OpenNMS Trap Notification";
        } else {
            log().debug("getVarbind: trapVarbind argument: "+switchValue);
        }
        return switchValue;
    }

    /**
     * Helper method to look into the Argument list and return the associaated value.
     * If the value is an empty String, this method returns null.
     * @param argSwitch
     * @return
     */
    private String getSwitchValue(String argSwitch) {
        String value = null;
        for (Iterator<Argument> it = m_arguments.iterator(); it.hasNext();) {
            Argument arg = it.next();
            if (arg.getSwitch().equals(argSwitch)) {
                value = arg.getValue();
            }
        }
        if (value != null && value.equals(""))
            value = null;
        
        return value;
    }
}
