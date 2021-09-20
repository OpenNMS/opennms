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
