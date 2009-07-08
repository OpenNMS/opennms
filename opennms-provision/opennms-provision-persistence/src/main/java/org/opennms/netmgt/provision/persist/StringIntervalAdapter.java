/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.persist;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class StringIntervalAdapter extends XmlAdapter<String, Duration> {
    public static final PeriodFormatter DEFAULT_PERIOD_FORMATTER = new PeriodFormatterBuilder()
    .appendWeeks().appendSuffix("w").appendSeparator(" ")
    .appendDays().appendSuffix("d").appendSeparator(" ")
    .appendHours().appendSuffix("h").appendSeparator(" ")
    .appendMinutes().appendSuffix("m").appendSeparator(" ")
    .appendSeconds().appendSuffix("s").appendSeparator(" ")
    .appendMillis().appendSuffix("ms")
    .toFormatter();
    
    @Override
    public String marshal(Duration v) {
        Period p = v.toPeriod().normalizedStandard();
        return DEFAULT_PERIOD_FORMATTER.print(p);
    }

    @Override
    public Duration unmarshal(String v) {
        return DEFAULT_PERIOD_FORMATTER.parsePeriod(v).toStandardDuration();
    }

}
