/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

public abstract class AbstractSnmpValue implements SnmpValue {
    
    /**
     * <p>If the value is in the unprintable ASCII range (< 32) and is not a:</p>
     * <ul>
     *   <li>Tab (9)</li>
     *   <li>Linefeed (10)</li>
     *   <li>Carriage return (13)</li>
     * <ul>
     * <p>or the byte is Delete (127) then this method will return false. Also, if the byte 
     * array has a NULL byte (0) that occurs anywhere besides the last character, return false. 
     * We will allow the NULL byte as a special case at the end of the string.</p>
     */
    protected boolean allBytesDisplayable(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            // Null (0)
            if (b == 0) {
                if (i != (bytes.length - 1)) {
                    return false;
                }
            }
            // Low or high ASCII (excluding Tab, Carriage Return, and Linefeed)
            else if (b < 32 && b != 9 && b != 10 && b != 13) {
                return false;
            }
            // Delete (127)
            else if (b == 127) {
                return false;
            }
        }
        return true;
    }


}
