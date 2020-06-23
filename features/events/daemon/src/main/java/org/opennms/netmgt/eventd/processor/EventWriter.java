/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.processor;

import org.opennms.netmgt.events.api.EventProcessor;

/**
 * EventWriter stores the information for each 'Event' into the database.
 *
 * While loading multiple values of the same element into a single DB column, the
 * multiple values are delimited by MULTIPLE_VAL_DELIM.
 *
 * When an element and its attribute are loaded into a single DB column, the
 * value and the attribute are separated by a DB_ATTRIB_DELIM.
 *
 * When using delimiters to append values, if the values already have the
 * delimiter, the delimiter in the value is escaped as in URLs.
 *
 * Values for the ' <parms>' block are loaded with each parm name and parm value
 * delimited with the NAME_VAL_DELIM.
 *
 * @see org.opennms.netmgt.model.events.Constants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.model.events.Constants#NAME_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.model.events.Constants#NAME_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.model.events.Constants#NAME_VAL_DELIM
 * 
 * Changes:
 *
 * - Alarm persisting added (many moons ago)
 * - Alarm persisting now removes oldest events by default.  Use "auto-clean" attribute
 *   in eventconf files.
 * 
 * @author Sowmya Nataraj </A>
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 *
 */
public interface EventWriter extends EventProcessor {
    
    // Field sizes in the events table
    /** Constant <code>EVENT_UEI_FIELD_SIZE=256</code> */
	public static final int EVENT_UEI_FIELD_SIZE = 256;

    /** Constant <code>EVENT_HOST_FIELD_SIZE=256</code> */
	public static final int EVENT_HOST_FIELD_SIZE = 256;

    /** 
     * Constant <code>EVENT_INTERFACE_FIELD_SIZE=50</code>.
     * This value must be long enough to accommodate an IPv6 address
     * with scope identifier suffix (if present). Basic IPv6 addresses
     * are 39 characters so this will accommodate a 10-digit scope
     * identifier (any 32-bit decimal value). 
     */
    public static final int EVENT_INTERFACE_FIELD_SIZE = 50;

    /** Constant <code>EVENT_SNMPHOST_FIELD_SIZE=256</code> */
    public static final int EVENT_SNMPHOST_FIELD_SIZE = 256;

    /** Constant <code>EVENT_SNMP_FIELD_SIZE=256</code> */
    public static final int EVENT_SNMP_FIELD_SIZE = 256;

    /** Constant <code>EVENT_LOGGRP_FIELD_SIZE=32</code> */
    public static final int EVENT_LOGGRP_FIELD_SIZE = 32;

    /** Constant <code>EVENT_PATHOUTAGE_FIELD_SIZE=1024</code> */
    public static final int EVENT_PATHOUTAGE_FIELD_SIZE = 1024;

    /** Constant <code>EVENT_CORRELATION_FIELD_SIZE=1024</code> */
    public static final int EVENT_CORRELATION_FIELD_SIZE = 1024;

    /** Constant <code>EVENT_OPERINSTRUCT_FIELD_SIZE=1024</code> */
//    public static final int EVENT_OPERINSTRUCT_FIELD_SIZE = 1024;

    /** Constant <code>EVENT_AUTOACTION_FIELD_SIZE=256</code> */
    public static final int EVENT_AUTOACTION_FIELD_SIZE = 256;

    /** Constant <code>EVENT_OPERACTION_FIELD_SIZE=256</code> */
    public static final int EVENT_OPERACTION_FIELD_SIZE = 256;

    /** Constant <code>EVENT_OPERACTION_MENU_FIELD_SIZE=64</code> */
    public static final int EVENT_OPERACTION_MENU_FIELD_SIZE = 64;

//    public static final int EVENT_NOTIFICATION_FIELD_SIZE = 128;

    /** Constant <code>EVENT_TTICKET_FIELD_SIZE=128</code> */
    public static final int EVENT_TTICKET_FIELD_SIZE = 128;

    /** Constant <code>EVENT_FORWARD_FIELD_SIZE=256</code> */
    public static final int EVENT_FORWARD_FIELD_SIZE = 256;

    /** Constant <code>EVENT_MOUSEOVERTEXT_FIELD_SIZE=64</code> */
    public static final int EVENT_MOUSEOVERTEXT_FIELD_SIZE = 64;

    /** Constant <code>EVENT_ACKUSER_FIELD_SIZE=256</code> */
    public static final int EVENT_ACKUSER_FIELD_SIZE = 256;

    /** Constant <code>EVENT_SOURCE_FIELD_SIZE=128</code> */
    public static final int EVENT_SOURCE_FIELD_SIZE = 128;
    
    /** Constant <code>EVENT_X733_ALARMTYPE_SIZE=31</code> */
    public static final int EVENT_X733_ALARMTYPE_SIZE = 31;

    /**
     * The character to put in if the log or display is to be set to yes
     */
    public static final char MSG_YES = 'Y';

    /**
     * The character to put in if the log or display is to be set to no
     */
    public static final char MSG_NO = 'N';
}
