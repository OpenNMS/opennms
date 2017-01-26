/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import static org.opennms.netmgt.eventd.AbstractEventUtil.ATTRIB_DELIM;
import static org.opennms.netmgt.eventd.AbstractEventUtil.escape;

import java.net.InetAddress;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Matcher;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.eventd.processor.expandable.ExpandableParameterResolver;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Tticket;
import org.opennms.netmgt.xml.event.Value;
import org.slf4j.LoggerFactory;

public enum StandardExpandableParameterResolvers implements ExpandableParameterResolver {

    UEI {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_UEI.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return event.getUei();
        }
    },

    DB_ID {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_EVENT_DB_ID.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            if (event.hasDbid()) {
                return Integer.toString(event.getDbid());
            } else {
                return "eventid-unknown";
            }
        }
    },

    SOURCE {
        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_SOURCE.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return event.getSource();
        }
    },

    DPNAME {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_DPNAME.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return event.getDistPoller();
        }
    },

    DESCR {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_DESCR.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return event.getDescr();
        }
    },

    LOGMSG {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_LOGMSG.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return event.getLogmsg().getContent();
        }
    },

    NODE_ID {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_NODEID.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return Long.toString(event.getNodeid());
        }
    },

    TIME {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_TIME.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            Date eventTime = event.getTime(); // This will be in GMT
            if (eventTime != null) {
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
                return df.format(eventTime);
            }
            return null;
        }
    },

    SHORT_TIME {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_SHORT_TIME.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            Date eventTime = event.getTime(); //This will be in GMT
            if (eventTime != null) {
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                return df.format(eventTime);
            }
            return null;
        }
    },

    HOST {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_HOST.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return event.getHost();
        }
    },

    INTERFACE {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_INTERFACE.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return event.getInterface();
        }
    },

    IFINDEX {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_IFINDEX.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            if (event.hasIfIndex()) {
                return Integer.toString(event.getIfIndex());
            }
            return "N/A";
        }
    },

    INTERFACE_ADDRESS {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_INTERFACE_RESOLVE.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            InetAddress addr = event.getInterfaceAddress();
            if (addr != null) {
                return addr.getHostName();
            }
            return null;
        }
    },

    SNMP_HOST {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_SNMPHOST.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return event.getSnmphost();
        }
    },

    SERVICE {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_SERVICE.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return event.getService();
        }
    },

    SNMP {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_SNMP.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            Snmp info = event.getSnmp();
            if (info != null) {
                StringBuffer snmpStr = new StringBuffer(info.getId());
                if (info.getIdtext() != null) {
                    snmpStr.append(ATTRIB_DELIM + escape(info.getIdtext().trim(), ATTRIB_DELIM));
                } else {
                    snmpStr.append(ATTRIB_DELIM + "undefined");
                }

                snmpStr.append(ATTRIB_DELIM + info.getVersion());

                if (info.hasSpecific()) {
                    snmpStr.append(ATTRIB_DELIM + Integer.toString(info.getSpecific()));
                } else {
                    snmpStr.append(ATTRIB_DELIM + "undefined");
                }

                if (info.hasGeneric()) {
                    snmpStr.append(ATTRIB_DELIM + Integer.toString(info.getGeneric()));
                } else {
                    snmpStr.append(ATTRIB_DELIM + "undefined");
                }

                if (info.getCommunity() != null) {
                    snmpStr.append(ATTRIB_DELIM + info.getCommunity().trim());
                } else {
                    snmpStr.append(ATTRIB_DELIM + "undefined");
                }

                return snmpStr.toString();
            }
            return null;
        }
    },

    SNMP_ID {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_SNMP_ID.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            Snmp info = event.getSnmp();
            if (info != null) {
                return info.getId();
            }
            return null;
        }
    },

    SNMP_IDTEXT {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_SNMP_IDTEXT.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            Snmp info = event.getSnmp();
            if (info != null && info.getIdtext() != null) {
                return info.getIdtext();
            }
            return null;
        }
    },

    SNMP_VERSION {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_SNMP_VERSION.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            Snmp info = event.getSnmp();
            if (info != null) {
                return info.getVersion();
            }
            return null;
        }
    },

    SNMP_SPECIFIC {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_SNMP_SPECIFIC.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            Snmp info = event.getSnmp();
            if (info != null && info.hasSpecific()) {
                return Integer.toString(info.getSpecific());
            }
            return null;
        }
    },

    SNMP_GENERIC {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_SNMP_GENERIC.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            Snmp info = event.getSnmp();
            if (info != null && info.hasGeneric()) {
                return Integer.toString(info.getGeneric());
            }
            return null;
        }
    },

    SNMP_COMMUNITY {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_SNMP_COMMUNITY.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            Snmp info = event.getSnmp();
            if (info != null && info.getCommunity() != null) {
                return info.getCommunity();
            }
            return null;
        }
    },

    SEVERITY {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_SEVERITY.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return event.getSeverity();
        }
    },

    OPERINSTRUCT {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_OPERINSTR.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return event.getOperinstruct();
        }
    },

    MOUSE_OVER_TEXT {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_MOUSEOVERTEXT.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return event.getMouseovertext();
        }
    },

    TTICKET_ID {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_TTICKET_ID.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            Tticket ticket = event.getTticket();
            return ticket == null ? "" : ticket.getContent();
        }
    },

    PARMS_VALUES {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.PARMS_VALUES.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return AbstractEventUtil.getAllParmValues(event);
        }
    },

    PARMS_NAMES {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.PARMS_NAMES.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return AbstractEventUtil.getAllParmNames(event);
        }
    },

    PARMS_ALL {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.PARMS_ALL.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return AbstractEventUtil.getAllParamValues(event);
        }
    },

    NUM_PARAMS {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.NUM_PARMS_STR.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return String.valueOf(event.getParmCollection().size());
        }
    },

    PARM_NUM {

        @Override
        public boolean matches(String parm) {
            return parm.startsWith(AbstractEventUtil.PARM_NUM_PREFIX);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return AbstractEventUtil.getNumParmValue(parm, event);
        }
    },

    PARM_NAME_NUMBERED {

        @Override
        public boolean matches(String parm) {
            return parm.startsWith(AbstractEventUtil.PARM_NAME_NUMBERED_PREFIX);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            return AbstractEventUtil.getNumParmName(parm, event);
        }
    },

    PARM {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.PARM_REGEX.matcher(parm).matches();
        }

        @Override
        public String parse(String parm) {
            // Extract the name of the parameter from the 'parm[ZZZ]' string
            final Matcher m = AbstractEventUtil.PARM_REGEX.matcher(parm);
            if (!m.matches()) {
                throw new IllegalStateException("parse() should not be called if matches() returned false");
            }
            return m.group(1);
        }

        @Override
        public String getValue(String parm, String parmName, Event event, EventUtil eventUtil) {
            final Parm evParm = event.getParmTrim(parmName);
            if (evParm != null) {
                final Value eParmVal = evParm.getValue();
                if (eParmVal != null) {
                    return EventConstants.getValueAsString(eParmVal);
                }
            }
            return null;
        }
    },

    HARDWARE {

        @Override
        public boolean matches(String parm) {
            return parm.startsWith(AbstractEventUtil.HARDWARE_BEGIN);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            if (event.getNodeid() != null) {
                String hwFieldValue = eventUtil.getHardwareFieldValue(parm, event.getNodeid());
                if (hwFieldValue != null) {
                    return hwFieldValue;
                }
            }
            return "Unknown";
        }

        @Override
        public boolean requiresTransaction() {
            return true;
        }
    },

    ASSET {

        @Override
        public boolean matches(String parm) {
            return parm.startsWith(AbstractEventUtil.ASSET_BEGIN);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            if (event.getNodeid() != null) {
                String assetFieldValue = eventUtil.getAssetFieldValue(parm, event.getNodeid());
                if (assetFieldValue != null) {
                    return assetFieldValue;
                }
            }
            return "Unknown";
        }

        @Override
        public boolean requiresTransaction() {
            return true;
        }
    },

    NODE_LABEL {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_NODELABEL.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            String nodeLabel = null;
            if (event.hasNodeid()) {
                try {
                    nodeLabel = eventUtil.getNodeLabel(event.getNodeid());
                } catch (SQLException e) {
                    // do nothing
                    LoggerFactory.getLogger(getClass()).info("Node Label unavailable for node with id: {}", event.getNodeid(), e);
                }
            }
            if (nodeLabel != null) {
                return WebSecurityUtils.sanitizeString(nodeLabel);
            } else {
                return "Unknown";
            }
        }


        @Override
        public boolean requiresTransaction() {
            return true;
        }
    },

    NODE_LOCATION {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_NODELOCATION.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            String nodeLocation = null;
            if (event.hasNodeid()) {
                try {
                    nodeLocation = eventUtil.getNodeLocation(event.getNodeid());
                } catch (SQLException e) {
                    // do nothing
                    LoggerFactory.getLogger(getClass()).info("Node Location unavailable for node with id: {}", event.getNodeid(), e);
                }
            }
            if (nodeLocation != null) {
                return WebSecurityUtils.sanitizeString(nodeLocation);
            } else {
                return "Unknown";
            }
        }


        @Override
        public boolean requiresTransaction() {
            return true;
        }
    },

    FOREIGN_SOURCE {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_FOREIGNSOURCE.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            if (event.hasNodeid()) {
                try {
                    String foreignSource = eventUtil.getForeignSource(event.getNodeid());
                    if (foreignSource != null) {
                        return WebSecurityUtils.sanitizeString(foreignSource);
                    }
                } catch (SQLException ex) {
                    // do nothing
                    LoggerFactory.getLogger(getClass()).info("ForeignSource unavailable for node with id:", event.getNodeid(), ex);
                }
            }
            return "";
        }

        @Override
        public boolean requiresTransaction() {
            return true;
        }
    },

    FOREIGN_ID {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_FOREIGNID.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            if (event.hasNodeid()) {
                try {
                    String foreignId = eventUtil.getForeignId(event.getNodeid());
                    if (foreignId != null) {
                        return WebSecurityUtils.sanitizeString(foreignId);
                    }
                } catch (SQLException ex) {
                    // do nothing
                    LoggerFactory.getLogger(getClass()).info("ForeignId unavailable for node with id:", event.getNodeid(), ex);
                }
            }
            return "";
        }

        @Override
        public boolean requiresTransaction() {
            return true;
        }
    },

    IF_ALIAS {

        @Override
        public boolean matches(String parm) {
            return AbstractEventUtil.TAG_IFALIAS.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, EventUtil eventUtil) {
            if (event.getNodeid() > 0 && event.getInterface() != null) {
                try {
                    return eventUtil.getIfAlias(event.getNodeid(), event.getInterface());
                } catch (SQLException e) {
                    // do nothing
                    LoggerFactory.getLogger(getClass()).info("ifAlias Unavailable for {}:{}", event.getNodeid(), event.getInterface(), e);
                }
            }
            return event.getInterface();
        }

        @Override
        public boolean requiresTransaction() {
            return true;
        }

    };

    // By default we don't perform any additional parsing
    @Override
    public String parse(String parm) {
        return null;
    }

    // By default we do not require a transaction
    @Override
    public boolean requiresTransaction() {
        return false;
    }

}
