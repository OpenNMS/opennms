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

package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class LldpSnmpUtils {

    private final static Logger LOG = LoggerFactory.getLogger(LldpSnmpUtils.class);

    public static String getInterface(SnmpValue snmpValue) {
        if (snmpValue == null )
            return "Null";
        if (humanReadable(snmpValue.toDisplayString()) || snmpValue.toDisplayString().contains("-") || snmpValue.toDisplayString().contains("/")) {
            return snmpValue.toDisplayString();
        }
        return getDisplayable(snmpValue);
    }

    public static String getMacAddress(SnmpValue snmpValue) {
        if (InetAddressUtils.isValidBridgeAddress(formatMacAddress(getDisplayable(snmpValue))))
            return formatMacAddress(getDisplayable(snmpValue));
        if (snmpValue.isDisplayable() &&
                InetAddressUtils.isValidBridgeAddress(formatMacAddress(snmpValue.toDisplayString())))
            return formatMacAddress(snmpValue.toDisplayString());
        LOG.error("getMacAddress: not valid mac found: {}", formatMacAddress(getDisplayable(snmpValue)));
        return formatMacAddress(getDisplayable(snmpValue));
    }

    public static String getNetworkAddress(SnmpValue snmpValue) {
        if (snmpValue == null )
            return "Null";
        try {
            return LldpUtils.decodeNetworkAddress(getDisplayable(snmpValue));
        } catch (Exception e) {
            LOG.debug("getNetworkAddress: value not valid {}", getDisplayable(snmpValue));
        }
        try {
            return LldpUtils.decodeNetworkAddress(snmpValue.toDisplayString());
        } catch (Exception e) {
            LOG.debug("getNetworkAddress: not valid {}", snmpValue.toDisplayString());
        }
        return "Not Valid";
    }

    public static boolean isNumber(final String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return input.matches("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?");
    }

    public static boolean humanReadable(final String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile(".*[a-zA-Z].*");
        return pattern.matcher(input).matches();
    }

    public static String getDisplayable(final SnmpValue snmpValue) {
        if (snmpValue == null )
            return "Null";
        try {
            LOG.debug("getDisplayable: displayable {} hex value {}", snmpValue.isDisplayable(),snmpValue.toHexString());
            return snmpValue.toHexString();
        } catch (Exception e) {
            LOG.error("getDisplayable: got not Hex Value {}", e.getMessage(), e);
        }
        try {
            LOG.debug("getDisplayable: displayable value {}", snmpValue.toDisplayString());
            return snmpValue.toDisplayString();
        } catch (Exception e) {
            LOG.error("getDisplayable: got not displayable Value {}", e.getMessage(), e);
        }
        return "Not Displayable";
    }

    private static String formatMacAddress(String mac) {
        return mac
                .replaceAll("\\s+", "")
                .replaceAll(":", "")
                .replaceAll("-", "")
                .toLowerCase(Locale.ROOT);
    }

    public static LldpUtils.LldpChassisIdSubType decodeLldpChassisSubType(Integer subTypeInt) {
        if (subTypeInt == null || subTypeInt == 0 || subTypeInt > 7) {
            return LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL;
        }
        return LldpUtils.LldpChassisIdSubType.get(subTypeInt);
    }

    public static String decodeLldpChassisId(final LldpUtils.LldpChassisIdSubType lldpChassisIdSubType, final SnmpValue snmpValue) {
        switch (lldpChassisIdSubType) {
            case LLDP_CHASSISID_SUBTYPE_MACADDRESS:
                return getMacAddress(snmpValue);
            case LLDP_CHASSISID_SUBTYPE_NETWORKADDRESS:
                return getNetworkAddress(snmpValue);
        }
        if (humanReadable(snmpValue.toDisplayString())) {
            return snmpValue.toDisplayString();
        }

        return getDisplayable(snmpValue);

    }

    public static String decodeLldpPortId(LldpUtils.LldpPortIdSubType type, SnmpValue snmpValue) {
        switch (type) {
            case LLDP_PORTID_SUBTYPE_LOCAL:
                if (isNumber(snmpValue.toDisplayString())) {
                    return snmpValue.toDisplayString();
                }
                return getDisplayable(snmpValue);
            case LLDP_PORTID_SUBTYPE_INTERFACENAME, LLDP_PORTID_SUBTYPE_INTERFACEALIAS:
                return getInterface(snmpValue);
            case LLDP_PORTID_SUBTYPE_MACADDRESS:
                return getMacAddress(snmpValue);
            case LLDP_PORTID_SUBTYPE_NETWORKADDRESS:
                return getNetworkAddress(snmpValue);
       }

       if (humanReadable(getDisplayable(snmpValue))) {
           return getDisplayable(snmpValue);
       }
       return snmpValue.toDisplayString();
    }

    public static LldpUtils.LldpPortIdSubType decodeLldpPortSubType(Integer lldpPortIdSubType, SnmpValue lldpportid) {
        if (lldpPortIdSubType == null || lldpPortIdSubType == 0 || lldpPortIdSubType > 7) {
            try {
                String macAddress = getMacAddress(lldpportid);
                if (InetAddressUtils.isValidBridgeAddress(macAddress)) {
                    return LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS;
                }
            } catch (IllegalArgumentException e) {
                LOG.debug("decodeLldpPortSubType: no mac address: {}", e.getMessage());
            }
            try {
                if (lldpportid.isDisplayable()) {
                    Integer.parseInt(lldpportid.toDisplayString());
                    return LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL;
                }
            } catch (NumberFormatException e) {
                LOG.debug("decodeLldpPortSubType: no LOCAL PORT: {}", lldpportid.toDisplayString());
            }
            try {
                if (lldpportid.isDisplayable()) {
                    lldpportid.toInetAddress();
                    return LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_NETWORKADDRESS;
                }
            } catch (Exception e) {
                LOG.debug("decodeLldpPortSubType: no NETWORK ADDRESS");
            }
            try {
                if (lldpportid.isDisplayable())
                    return LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME;
            } catch (Exception e) {
                LOG.debug("no DISPLAY VALUE");
            }
            return LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_PORTCOMPONENT;
        }
        return LldpUtils.LldpPortIdSubType.get(lldpPortIdSubType);
    }

    public static String decodeTimeTetraLldpPortId(LldpUtils.LldpPortIdSubType portSubType, SnmpValue snmpValue) {
        if (portSubType == LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL) {
                return String.valueOf(Integer.parseInt(decodeLldpPortId( LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL,snmpValue),16));
        }
        return decodeLldpPortId(portSubType,snmpValue);
    }

    public static LldpLink getLldpLink(MtxrLldpRemTableTracker.MtxrLldpRemRow mtxrlldprow, Integer mtxrIndex, Map<Integer, LldpLocalTableTracker.LldpLocalPortRow> mtxrLldpLocalPortMap) {
        LldpLink lldpLink = mtxrlldprow.getLldpLink();
        if (mtxrIndex != null && mtxrLldpLocalPortMap.containsKey(mtxrIndex)) {
            lldpLink.setLldpPortIfindex(mtxrIndex);
            lldpLink.setLldpPortIdSubType(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME);
            lldpLink.setLldpPortId(mtxrLldpLocalPortMap.get(mtxrIndex).getLldpLocPortDesc());
            lldpLink.setLldpPortDescr(mtxrLldpLocalPortMap.get(mtxrIndex).getLldpLocPortDesc());
            LOG.debug("getLldpLink: interfaceId {} -> portId {}", mtxrIndex,lldpLink.getLldpPortId());
        } else {
            lldpLink.setLldpPortIdSubType(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACEALIAS);
            lldpLink.setLldpPortId("\"Not Found On lldpLocPortTable\"");
            lldpLink.setLldpPortDescr("");
            LOG.debug("getLldpLink: setting default not found Values: portidtype \"InterfaceAlias\", portid=\"Not Found On lldpLocPortTable\"");
        }
        return lldpLink;
    }
}
