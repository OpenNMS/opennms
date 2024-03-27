/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.notifd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SnmpTrapNotificationStrategy class.</p>
 *
 * @author david
 * @version $Id: $
 */
public class SnmpTrapNotificationStrategy implements NotificationStrategy {
    
    private static final Logger LOG = LoggerFactory.getLogger(SnmpTrapNotificationStrategy.class);
    
    private List<Argument> m_arguments;

    /** {@inheritDoc} */
    @Override
    public int send (List<Argument> arguments) {
        
        m_arguments = arguments;
        String argVersion = getVersion();
        
        if (argVersion == null) {
            LOG.info("send: trapVersion paramenter is null, defaulting to \"v1\".");
            argVersion = "v1";
        }

        //determine version of trap and send it.
        try {
            if (argVersion.equals("v1")) {
                sendV1Trap();
            } else if (argVersion.equals("v2c") || argVersion.equals("v2")) {
                sendV2Trap();
            } else if (argVersion.equals("v3")) {
                LOG.info("send: Version3 not supported in notifications, yet, falling back to v2c.");
                sendV2Trap();
            } else {
                LOG.info("send: No version specified in first argument to notification.");
                return 1;
            }
        } catch (Throwable e) {
            LOG.info("send: Exception trying to send trap. ",e);
            return 1;
        }
        
        return 0;
    }
    
    private String getVersion() {
        return getSwitchValue("trapVersion");
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
            LOG.info("getHostInetAddress: trapHost not specified, defaulting to: \"127.0.0.1\".");
            switchValue = "127.0.0.1";
        } else {
            LOG.debug("getHostInetAddress: trapHost argument: {}", switchValue);
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
            LOG.info("getPort: trapPort argument not specified, defaulting to: \"162\".");
            return 162;
        } else {
            LOG.debug("getPort: trapPort argument: {}", Integer.parseInt(switchValue));
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
            LOG.info("getCommunity: trapCommunity not specified, defaulting to: \"public\".");
            switchValue = "public";
        } else {
            LOG.debug("getCommunity: trapCommunity argument: {}", switchValue);
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
            LOG.info("getEnterpriseId: trapEnterprise not specified, defaulting to: \".1.3.6.1.4.1.5813\".");
            switchValue = ".1.3.6.1.4.1.5813";
        } else {
            LOG.debug("getEnterpriseId: trapEnterprise argument: {}", switchValue);
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
            LOG.info("getGenericId: trapGeneric argument not specified, defaulting to: \"6\".");
            return 6;
        } else {
            LOG.debug("getGenericId: trapGeneric argument: {}", switchValue);
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
            LOG.info("getSpecificId: trapSpecific argument not specified, defaulting to: \"1\".");
            return 1;
        } else {
            LOG.debug("getSpecificId: trapSpecific argument: {}", Integer.parseInt(switchValue));
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
            LOG.info("getVarbind: trapVarbind argument not specified, defaulting to: \"OpenNMS Trap Notification\".");
            return "OpenNMS Trap Notification";
        } else {
            LOG.debug("getVarbind: trapVarbind argument: {}", switchValue);
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
