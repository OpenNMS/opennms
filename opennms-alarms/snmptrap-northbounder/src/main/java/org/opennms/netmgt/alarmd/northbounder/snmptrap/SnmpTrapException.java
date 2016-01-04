/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.alarmd.northbounder.snmptrap;

/**
 * The Class SnmpTrapHelperException.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SnmpTrapException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1817203327929032238L;

    /**
     * Instantiates a new SNMP trap helper exception.
     */
    public SnmpTrapException() {
        super();
    }

    /**
     * Instantiates a new SNMP trap helper exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public SnmpTrapException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new SNMP trap helper exception.
     *
     * @param message the message
     */
    public SnmpTrapException(String message) {
        super(message);
    }

    /**
     * Instantiates a new SNMP trap helper exception.
     *
     * @param cause the cause
     */
    public SnmpTrapException(Throwable cause) {
        super(cause);
    }

}
