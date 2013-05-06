/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.helper;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

/**
 * <p>SnmpInformantOidResolver class.</p>
 *
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 * @version $Id: $
 * @since 1.0-SNAPSHOT
 */
public class SnmpInformantOidResolver extends JRDefaultScriptlet {

    // Logging to reporting log
    private final Logger logger = LoggerFactory.getLogger("OpenNMS.Report." + SnmpInformantOidResolver.class.getName());

    /**
     * Convert a name of specific device to a decimal ASCII string as
     * OID. For example: For a drive "C:" the SNMP agent addresses this drive
     * by converting 'C' into ASCII int 67 and ':' into ASCII int 58. The
     * output is used as a OID path to the drive, in this example 67.58
     *
     * @param string2convert String which as to be converted in ASCII integer OID
     * @return converted ASCII OID path
     */
    public String stringToAsciiOid(String string2convert) {
        StringBuilder stringBuilder = new StringBuilder();
        int length = 0;

        if (string2convert.length() > 0) {
            length = string2convert.length();
        } else {
            logger.error("String to convert ['{}'] has no length and is forced to 0.");
        }

        char[] origin = string2convert.toCharArray();
        stringBuilder.append(length + ".");
        for (int i = 0; i < origin.length; i++) {
            stringBuilder.append((int) origin[i]);

            if (i != origin.length - 1) {
                stringBuilder.append(".");
            }
        }

        return stringBuilder.toString();
    }

    public String asciiOidToString(String string2convert) {
        StringBuilder stringBuilder = new StringBuilder();
        StringTokenizer st = new StringTokenizer(string2convert, ".");

        // Skip the first token it is the amount characters
        st.nextToken();
        while (st.hasMoreTokens()) {
            stringBuilder.append((char) Integer.parseInt(st.nextToken()));
        }
        return stringBuilder.toString();
    }
}
