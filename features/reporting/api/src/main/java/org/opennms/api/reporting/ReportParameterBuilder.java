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
        return this.withDate(name, value, hours, minutes, false);
    }

    public ReportParameterBuilder withDate(String name, Date value, int hours, int minutes, boolean adjusted) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);

        final ReportDateParm parm = new ReportDateParm();
        parm.setName(name);
        parm.setDate(value);
        parm.setUseAbsoluteDate(true);
        parm.setHours(hours);
        parm.setMinutes(minutes);
        parm.setIsAdjustedDate(adjusted);

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
