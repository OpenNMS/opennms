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
package org.opennms.netmgt.jasper.helper;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRDefaultScriptlet;

/**
 * <p>SnmpInformantOidResolver class.</p>
 *
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 * @version $Id: $
 * @since 1.0-SNAPSHOT
 */
public abstract class SnmpInformantOidResolver extends JRDefaultScriptlet {

    // Logging to reporting log
    private static final Logger logger = LoggerFactory.getLogger(SnmpInformantOidResolver.class);

    /**
     * Convert a name of specific device to a decimal ASCII string as
     * OID. For example: For a drive "C:" the SNMP agent addresses this drive
     * by converting 'C' into ASCII int 67 and ':' into ASCII int 58. The
     * output is used as a OID path to the drive, in this example 67.58
     *
     * @param string2convert String which as to be converted in ASCII integer OID
     * @return converted ASCII OID path
     */
    public static String stringToAsciiOid(String string2convert) {
        final StringBuilder stringBuilder = new StringBuilder();
        int length = 0;

        if (string2convert.length() > 0) {
            length = string2convert.length();
        } else {
            logger.error("String to convert ['{}'] has no length and is forced to 0.", string2convert);
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

    public static String asciiOidToString(String string2convert) {
        final StringBuilder stringBuilder = new StringBuilder();
        StringTokenizer st = new StringTokenizer(string2convert, ".");

        // Skip the first token it is the amount characters
        st.nextToken();
        while (st.hasMoreTokens()) {
            stringBuilder.append((char) Integer.parseInt(st.nextToken()));
        }
        return stringBuilder.toString();
    }
}
