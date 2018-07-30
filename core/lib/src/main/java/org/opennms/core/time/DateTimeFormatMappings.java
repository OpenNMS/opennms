/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.time;

import java.util.LinkedHashMap;
import java.util.Map;

public class DateTimeFormatMappings {

    private final static Map<String, String> angularDateMapping = new LinkedHashMap<>(); // LinkedHashMap to maintain order

    static {
        // see: https://docs.angularjs.org/api/ng/filter/date
        // and: java.time.format.DateTimeFormatter
        // we try to get the format as close as possible. AngularJS has less format features but the missing ones should be
        // mostly irrelevant for practical use. If no close match can be found we substitute with '?'

        angularDateMapping.put("G", "G"); // era			text              AD; Anno Domini; A
        angularDateMapping.put("u", "y"); // year			year              2004; 04
        angularDateMapping.put("y", "y"); // year-of-era        	year              2004; 04
        angularDateMapping.put("D", "?"); // day-of-year        	number            189 => not supported!
        angularDateMapping.put("M", "M"); // month-of-year      	number/text       7; 07; Jul; July; J
        angularDateMapping.put("L", "M"); // month-of-year      	number/text       7; 07; Jul; July; J
        angularDateMapping.put("d", "d"); // day-of-month       	number            10
        angularDateMapping.put("Q", "?"); // quarter-of-year    	number/text       3; 03; Q3; 3rd quarter => not supported!
        angularDateMapping.put("q", "?"); // quarter-of-year    	number/text       3; 03; Q3; 3rd quarter => not supported!
        angularDateMapping.put("Y", "y"); // week-based-year    	year              1996; 96
        angularDateMapping.put("w", "w"); // week-of-week-based-year    number            27
        angularDateMapping.put("W", "?"); // week-of-month              number            4
        angularDateMapping.put("E", "E"); // day-of-week                text              Tue; Tuesday; T
        angularDateMapping.put("e", "E"); // localized day-of-week       number/text       2; 02; Tue; Tuesday; T
        angularDateMapping.put("c", "E"); // localized day-of-week       number/text       2; 02; Tue; Tuesday; T
        angularDateMapping.put("F", "?"); // week-of-month               number            3
        angularDateMapping.put("a", "a"); // am-pm-of-day                text              PM
        angularDateMapping.put("h", "h"); // clock-hour-of-am-pm (1-12)  number            12
        angularDateMapping.put("K", "?"); // hour-of-am-pm (0-11)        number            0
        angularDateMapping.put("k", "?"); // clock-hour-of-am-pm (1-24)  number            0
        angularDateMapping.put("H", "H"); // hour-of-day (0-23)          number            0
        angularDateMapping.put("m", "m"); // minute-of-hour              number            30
        angularDateMapping.put("s", "s"); // second-of-minute            number            55
        angularDateMapping.put("S", "s"); // fraction-of-second          fraction          978
        angularDateMapping.put("A", "?"); // milli-of-day                number            1234
        angularDateMapping.put("n", "?"); // nano-of-second              number            987654321
        angularDateMapping.put("N", "?"); // nano-of-day                 number            1234000000
        angularDateMapping.put("V", "Z"); // time-zone ID                zone-id           America/Los_Angeles; Z; -08:30
        angularDateMapping.put("z", "Z"); // time-zone name              zone-name         Pacific Standard Time; PST
        angularDateMapping.put("O", "Z"); // localized zone-offset       offset-O          GMT+8; GMT+08:00; UTC-08:00;
        angularDateMapping.put("X", "Z"); // zone-offset 'Z' for zero    offset-X          Z; -08; -0830; -08:30; -083015; -08:30:15;
        angularDateMapping.put("xxx", "Z"); // zone-offset                 offset-x          +0000; -08; -0830; -08:30; -083015; -08:30:15;
        angularDateMapping.put("xx", "Z"); // zone-offset                 offset-x          +0000; -08; -0830; -08:30; -083015; -08:30:15;
        angularDateMapping.put("x", "Z"); // zone-offset                 offset-x          +0000; -08; -0830; -08:30; -083015; -08:30:15;
        angularDateMapping.put("Z", "Z"); // zone-offset                 offset-Z          +0000; -0800; -08:00;
        angularDateMapping.put("p", "?"); // pad next                    pad modifier      1
    }

    public static String asAngularJSDate(final String dateTimeFormatterPattern) {
        if (dateTimeFormatterPattern == null) {
            return null;
        }
        String result = dateTimeFormatterPattern;
        for(Map.Entry<String, String> entry : angularDateMapping.entrySet()){
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
