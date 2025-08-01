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
package org.opennms.reporting.core.svclayer.support;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.api.reporting.parameter.ReportDateParm;
import org.opennms.api.reporting.parameter.ReportIntParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.api.reporting.parameter.ReportStringParm;
import org.opennms.netmgt.config.reporting.DefaultTime;
import org.opennms.netmgt.config.reporting.Parameters;
import org.opennms.reporting.core.svclayer.ParameterConversionService;

/**
 * <p>DefaultParameterConversionService class.</p>
 */
public class DefaultParameterConversionService implements
        ParameterConversionService {

    /** {@inheritDoc} */
    @Override
    public ReportParameters convert(Parameters configParameters) {

        ReportParameters reportParameters = new ReportParameters();
        
        if (configParameters == null) {
            return reportParameters;
        }
        
        // add date parms to criteria
        final List<ReportDateParm> dateParms = configParameters.getDateParms().stream().map(dp -> {
            final ReportDateParm dateParm = new ReportDateParm();
            dateParm.setUseAbsoluteDate(dp.getUseAbsoluteDate().orElse(null));
            dateParm.setDisplayName(dp.getDisplayName());
            dateParm.setName(dp.getName());
            dateParm.setCount(new Integer((int) dp.getDefaultCount()));
            dateParm.setInterval(dp.getDefaultInterval());
            Calendar cal = Calendar.getInstance();
            if (dp.getDefaultTime().isPresent()) {
                final DefaultTime defaultTime = dp.getDefaultTime().get();
                dateParm.setHours(defaultTime.getHours());
                cal.set(Calendar.HOUR_OF_DAY, dateParm.getHours());
                dateParm.setMinutes(defaultTime.getMinutes());
                cal.set(Calendar.MINUTE, dateParm.getMinutes());
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
            }
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND,0);
            int amount = 0 - dp.getDefaultCount();
            if (dp.getDefaultInterval().equals("year")) {
                cal.add(Calendar.YEAR, amount);
            } else { 
                if (dp.getDefaultInterval().equals("month")) {
                    cal.add(Calendar.MONTH, amount);
                } else {
                    cal.add(Calendar.DATE, amount);
                }
            }
            dateParm.setDate(cal.getTime());
            return dateParm;
        }).collect(Collectors.toList());
        reportParameters.setDateParms(dateParms);
        
        // add string parms to criteria
        final List<ReportStringParm> stringParms = configParameters.getStringParms().stream().map(sp -> {
            final ReportStringParm stringParm = new ReportStringParm();
            stringParm.setDisplayName(sp.getDisplayName());
            stringParm.setName(sp.getName());
            stringParm.setInputType(sp.getInputType());
            stringParm.setValue(sp.getDefault());
            return stringParm;
        }).collect(Collectors.toList());
        reportParameters.setStringParms(stringParms);
        
        // add int parms to criteria
        final List<ReportIntParm> intParms = configParameters.getIntParms().stream().map(ip -> {
            final ReportIntParm intParm = new ReportIntParm();
            intParm.setDisplayName(ip.getDisplayName());
            intParm.setName(ip.getName());
            intParm.setInputType(ip.getInputType());
            intParm.setValue(ip.getDefault());
            return intParm;
        }).collect(Collectors.toList());
        reportParameters.setIntParms(intParms);

        return reportParameters;
    }

}
