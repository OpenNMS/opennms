/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
