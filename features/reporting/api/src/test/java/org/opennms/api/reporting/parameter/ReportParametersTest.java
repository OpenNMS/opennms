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
package org.opennms.api.reporting.parameter;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.api.reporting.ReportParameterBuilder;

import com.google.common.collect.Lists;

public class ReportParametersTest {

    private static Date DATE;

    @BeforeClass
    public static void initClass() throws ParseException {
        DATE = new SimpleDateFormat("yyyy-MM-dd").parse("2019-03-09");
    }

    @Test
    public void verifyAsMap() {
        final ReportParameters parameters = new ReportParameters();
        final Map<String, ReportParm> expectedMap = new HashMap<>();

        // Verify empty
        assertThat(parameters.asMap(), is(expectedMap));

        // Verify float parm
        final ReportFloatParm floatParm = new ReportFloatParm();
        floatParm.setValue(1.234f);
        floatParm.setName("float1");
        parameters.setFloatParms(Lists.newArrayList(floatParm));
        expectedMap.put("float1", floatParm);
        assertThat(parameters.asMap(), is(expectedMap));

        // Verify int parm
        final ReportIntParm intParm = new ReportIntParm();
        intParm.setName("int1");
        intParm.setValue(10002000);
        parameters.setIntParms(Lists.newArrayList(intParm));
        expectedMap.put("int1", intParm);
        assertThat(parameters.asMap(), is(expectedMap));

        // Verify string parm
        final ReportStringParm stringParm = new ReportStringParm();
        stringParm.setName("string1");
        stringParm.setValue("WIUWIU");
        parameters.setStringParms(Lists.newArrayList(stringParm));
        expectedMap.put("string1", stringParm);
        assertThat(parameters.asMap(), is(expectedMap));

        // Verify double parm
        final ReportDoubleParm doubleParm = new ReportDoubleParm();
        doubleParm.setName("double1");
        doubleParm.setValue(Math.PI);
        parameters.setDoubleParms((Lists.newArrayList(doubleParm)));
        expectedMap.put("double1", doubleParm);
        assertThat(parameters.asMap(), is(expectedMap));

        // Verify date parm

        final ReportDateParm dateParm = new ReportDateParm();
        dateParm.setName("date1");
        dateParm.setDate(DATE);
        parameters.setDateParms(Lists.newArrayList(dateParm));
        expectedMap.put("date1", dateParm);
        assertThat(parameters.asMap(), is(expectedMap));
    }

    @Test
    public void verifyApply() {
        final ReportParameters reportParameters = new ReportParameterBuilder()
                .withString("string", "wiuwiu")
                .withDouble("double", Math.PI)
                .withDate("date", DATE)
                .withInteger("integer", 10002000)
                .withFloat("float", 12.13f)
                .build();
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        final Date today = cal.getTime();
        final ReportParameters inputParameters = new ReportParameterBuilder()
                .withString("string", "ulf")
                .withDouble("double", -3.1415)
                .withDate("date", today)
                .withInteger("integer", -1000)
                .withFloat("float", 22.23f)
                .build();
        reportParameters.apply(inputParameters);
        assertThat(reportParameters.asMap().keySet(), is(inputParameters.asMap().keySet()));
        assertThat(reportParameters.getReportParms().values(), containsInAnyOrder(inputParameters.getReportParms().values().toArray()));
    }

    @Test
    public void verifyDateParamsAppliedProperly() {
        final Date date = new Date();
        final ReportParameters reportParameters = new ReportParameterBuilder()
                .withDate("date1", date)
                .withDate("date2", date)
                .withDate("date3", date)
                .build();
        final ReportParameters inputParameters = new ReportParameterBuilder()
                .withDate("date1", date, 10, 15)
                .withDate("date2", ReportParameterBuilder.Intervals.Years, 1)
                .withDate("date3", ReportParameterBuilder.Intervals.Months, 2, 10, 13)
                .build();
        reportParameters.apply(inputParameters);

        // Verify date1
        assertThat(reportParameters.getDateParms().get(0).getName(), is("date1"));
        assertThat(reportParameters.getDateParms().get(0).getUseAbsoluteDate(), is(true));
        assertThat(reportParameters.getDateParms().get(0).getDate(), is(date));
        assertThat(reportParameters.getDateParms().get(0).getHours(), is(10));
        assertThat(reportParameters.getDateParms().get(0).getMinutes(), is(15));

        // Verify date2
        assertThat(reportParameters.getDateParms().get(1).getName(), is("date2"));
        assertThat(reportParameters.getDateParms().get(1).getUseAbsoluteDate(), is(false));
        assertThat(reportParameters.getDateParms().get(1).getDate(), is(nullValue()));
        assertThat(reportParameters.getDateParms().get(1).getInterval(), is(ReportParameterBuilder.Intervals.Years));
        assertThat(reportParameters.getDateParms().get(1).getCount(), is(1));
        assertThat(reportParameters.getDateParms().get(1).getHours(), is(0));
        assertThat(reportParameters.getDateParms().get(1).getMinutes(), is(0));

        // Verify date3
        assertThat(reportParameters.getDateParms().get(2).getName(), is("date3"));
        assertThat(reportParameters.getDateParms().get(2).getUseAbsoluteDate(), is(false));
        assertThat(reportParameters.getDateParms().get(2).getDate(), is(nullValue()));
        assertThat(reportParameters.getDateParms().get(2).getInterval(), is(ReportParameterBuilder.Intervals.Months));
        assertThat(reportParameters.getDateParms().get(2).getCount(), is(2));
        assertThat(reportParameters.getDateParms().get(2).getHours(), is(10));
        assertThat(reportParameters.getDateParms().get(2).getMinutes(), is(13));
    }

}