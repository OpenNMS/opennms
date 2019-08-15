/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.api.reporting;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.opennms.api.reporting.parameter.ReportDateParm;
import org.opennms.api.reporting.parameter.ReportDoubleParm;
import org.opennms.api.reporting.parameter.ReportFloatParm;
import org.opennms.api.reporting.parameter.ReportIntParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.api.reporting.parameter.ReportStringParm;
import org.opennms.api.reporting.parameter.ReportTimezoneParm;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ReportParameterBuilder {

    public interface Intervals {
        String Years = "year";
        String Months = "month";
        String Days = "day";
        List<String> ALL = Lists.newArrayList(Years, Months, Days);
    }

    private final List<ReportStringParm> stringParams = Lists.newArrayList();
    private final List<ReportIntParm> intParams = Lists.newArrayList();
    private final List<ReportFloatParm> floatParams = Lists.newArrayList();
    private final List<ReportDateParm> dateParams = Lists.newArrayList();
    private final List<ReportDoubleParm> doubleParms = Lists.newArrayList();
    private final List<ReportTimezoneParm> timezoneParms = Lists.newArrayList();

    public ReportParameterBuilder withString(String name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);

        final ReportStringParm stringParm = new ReportStringParm();
        stringParm.setName(name);
        stringParm.setValue(value);

        stringParams.add(stringParm);
        return this;
    }

    public ReportParameterBuilder withInteger(String name, Integer value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);

        final ReportIntParm intParm = new ReportIntParm();
        intParm.setName(name);
        intParm.setValue(value);

        intParams.add(intParm);
        return this;
    }

    public ReportParameterBuilder withFloat(String name, Float value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);

        final ReportFloatParm parm = new ReportFloatParm();
        parm.setName(name);
        parm.setValue(value);

        floatParams.add(parm);
        return this;
    }

    public ReportParameterBuilder withDouble(String name, Double value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);

        final ReportDoubleParm parm = new ReportDoubleParm();
        parm.setName(name);
        parm.setValue(value);

        doubleParms.add(parm);
        return this;
    }

    public ReportParameterBuilder withDate(String name, String interval, int count) {
        return withDate(name, interval, count, 0, 0);
    }

    public ReportParameterBuilder withDate(String name, String interval, int count, int hours, int minutes) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(interval);
        Preconditions.checkArgument(hours >= 0 && hours <= 23, "Hours must be >= 0 and <= 23");
        Preconditions.checkArgument(minutes >= 0 && minutes <= 59, "Minutes must be >= 0 and <= 59");

        final ReportDateParm parm = new ReportDateParm();
        parm.setName(name);
        parm.setInterval(interval);
        parm.setCount(count);
        parm.setUseAbsoluteDate(false);
        parm.setHours(hours);
        parm.setMinutes(minutes);

        dateParams.add(parm);

        return this;
    }

    public ReportParameterBuilder withDate(String name, Date value) {
        return withDate(name, value, 0, 0);

    }

    public ReportParameterBuilder withDate(String name, Date value, int hours, int minutes) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);

        final ReportDateParm parm = new ReportDateParm();
        parm.setName(name);
        parm.setDate(value);
        parm.setUseAbsoluteDate(true);
        parm.setHours(hours);
        parm.setMinutes(minutes);

        dateParams.add(parm);
        return this;
    }

    public ReportParameterBuilder withTimezone(String name, ZoneId zoneId) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(zoneId);

        final ReportTimezoneParm parm = new ReportTimezoneParm();
        parm.setName(name);
        parm.setValue(zoneId);

        timezoneParms.add(parm);
        return this;
    }

    public ReportParameters build() {
        final ReportParameters parameters = new ReportParameters();
        parameters.setStringParms(stringParams);
        parameters.setIntParms(intParams);
        parameters.setFloatParms(floatParams);
        parameters.setDateParms(dateParams);
        parameters.setDoubleParms(doubleParms);
        parameters.setTimezoneParms(timezoneParms);
        return parameters;
    }
}
