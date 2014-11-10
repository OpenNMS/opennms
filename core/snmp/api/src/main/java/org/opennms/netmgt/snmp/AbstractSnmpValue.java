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

package org.opennms.netmgt.snmp;

public abstract class AbstractSnmpValue implements SnmpValue {

    public static boolean allBytesDisplayable(final byte[] bytes) {
        if (allBytesUTF_8(bytes)) {
            return true;
        } else if (allBytesISO_8859_1(bytes)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>Based on a modified version of <a href="http://stackoverflow.com/a/1447720">http://stackoverflow.com/a/1447720</a> for UTF-8 detection.</p>
     */
    public static boolean allBytesUTF_8(final byte[] bytes) {
        int i = 0;
        // Check for BOM
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            i = 3;
        }
        
        int end;
        for (int j = bytes.length; i < j; ++i) {
            int octet = bytes[i];

            // ASCII
            if ((octet & 0x80) == 0) {
                if (octet == 0) {
                    if (i != (j - 1)) {
                        //System.err.println("null found: i=" + i + ", j=" + j + ", octet=" + octet);
                        return false;
                    }
                }
                // control chars that aren't cr/lf and tab
                else if (octet < 32 && octet != 9 && octet != 10 && octet != 13) {
                    //System.err.println("invalid chars: i=" + i + ", j=" + j + ", octet=" + octet);
                    return false;
                }
                // delete
                else if (octet == 127) {
                    //System.err.println("delete found: i=" + i + ", j=" + j + ", octet=" + octet);
                    return false;
                }

                continue;
            }

            // Check for UTF-8 leading byte
            if ((octet & 0xE0) == 0xC0) {
                end = i + 1;
            } else if ((octet & 0xF0) == 0xE0) {
                end = i + 2;
            } else if ((octet & 0xF8) == 0xF0) {
                end = i + 3;
            } else {
                // Java only supports BMP so 3 is max
                //System.err.println("BMP > 3");
                return false;
            }

            while (i < end) {
                i++;
                octet = bytes[i];
                if ((octet & 0xC0) != 0x80) {
                    //System.err.println("Not a valid trailing byte.");
                    // Not a valid trailing byte
                    return false;
                }
            }
        }

        return true;
    }

     /**
      * <p>If the value is in the unprintable ASCII range (&lt; 32) and is not a:</p>
      * 
      * <ul>
      *   <li>Tab (9)</li>
      *   <li>Linefeed (10)</li>
      *   <li>Carriage return (13)</li>
      * </ul>
      * 
      * <p>...or the byte is Delete (127) then this method will return false. Also, if the byte 
      * array has a NULL byte (0) that occurs anywhere besides the last character, return false. 
      * We will allow the NULL byte as a special case at the end of the string.</p>
      */
    public static boolean allBytesISO_8859_1(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            // Null (0)
            if (b == 0) {
                if (i != (bytes.length - 1)) {
                    //System.out.println("INVALID: " + b);
                    return false;
                }
            }
            // Low ASCII (excluding Tab, Carriage Return, and Linefeed)
            else if (b >= 0 && b < 32 && b != 9 && b != 10 && b != 13) {
                //System.out.println("INVALID: " + b);
                return false;
            }
            // Delete (127)
            else if (b == 127) {
                //System.out.println("INVALID: " + b);
                return false;
            }
            // High ASCII values not included in ISO-8859-1
            else if (b >= -128 && b < -96) {
                //System.out.println("INVALID: " + b);
                return false;
            }
        }
        return true;
    }
}
