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
package org.opennms.core.utils;

/**
 * <P>
 * This class contains static functions used to convert time related string
 * values into numeric values to be used in computations.
 * </P>
 *
 * @author <A HREF="mike@opennms.org">Mike </A>
 */
public abstract class TimeConverter {
    /**
     * <P>
     * Converts the passed time string to a time value that is measured in
     * milliseconds. The following extension are considered when converting the
     * string:
     * </P>
     *
     * <TABLE BORDER=0 summary="Extensions">
     * <TR>
     * <TH>Extension</TH>
     * <TH>Conversion Value</TH>
     * </TR>
     * <TR>
     * <TD>us</TD>
     * <TD>Microseconds</TD>
     * </TR>
     * <TR>
     * <TD>ms</TD>
     * <TD>Milliseconds</TD>
     * </TR>
     * <TR>
     * <TD>s</TD>
     * <TD>Seconds</TD>
     * </TR>
     * <TR>
     * <TD>m</TD>
     * <TD>Minutes</TD>
     * </TR>
     * <TR>
     * <TD>h</TD>
     * <TD>Hours</TD>
     * </TR>
     * <TR>
     * <TD>d</TD>
     * <TD>Days</TD>
     * </TR>
     * </TABLE>
     *
     * <P>
     * A number entered with out any units is considered to be in milliseconds.
     * </P>
     *
     * @param valueToConvert
     *            The string to convert to milliseconds.
     * @return Returns the string converted to a millisecond value.
     * @throws java.lang.NumberFormatException if the string is malformed and a number cannot be
     *                extracted from the value..
     */
    public static long convertToMillis(String valueToConvert) throws NumberFormatException {
        valueToConvert = valueToConvert.trim();
        String timeVal = valueToConvert.toLowerCase();
        int index = 0;
        float factor = 1.0f;

        if (timeVal.endsWith("us")) {
            factor = 0.001f;
            index = timeVal.indexOf("us");
        } else if (timeVal.endsWith("ms")) {
            factor = 1.0f;
            index = timeVal.indexOf("ms");
        } else if (timeVal.endsWith("s")) {
            factor = 1000.0f;
            index = timeVal.indexOf('s');
        } else if (timeVal.endsWith("m")) {
            factor = 1000.0f * 60.0f;
            index = timeVal.indexOf('m');
        } else if (timeVal.endsWith("h")) {
            factor = 1000.0f * 60.0f * 60.0f;
            index = timeVal.indexOf('h');
        } else if (timeVal.endsWith("d")) {
            factor = 1000.0f * 60.0f * 60.0f * 24.0f;
            index = timeVal.indexOf('d');
        }

        if (index == 0) {
            index = timeVal.length();
        }

        Float fVal = new Float(timeVal.substring(0, index));
        return ((long) (fVal.floatValue() * factor));

    } // end timeToMillis()

}
