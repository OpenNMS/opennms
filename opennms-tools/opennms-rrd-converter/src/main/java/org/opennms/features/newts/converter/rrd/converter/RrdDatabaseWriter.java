/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.newts.converter.rrd.converter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.jrobin.core.Datasource;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;

public class RrdDatabaseWriter {
    private final RrdDb m_rrd;
    private final long m_step;
    private Map<String,Double> m_lastValue = new HashMap<String,Double>();
    private BigDecimal m_doubleMax = BigDecimal.valueOf(Double.MAX_VALUE);

    public RrdDatabaseWriter(final RrdDb rrd) throws IOException {
        m_rrd = rrd;
        m_step = m_rrd.getHeader().getStep();
    }
    
    public void write(final RrdEntry entry) throws IOException, RrdException {
        final Sample s = m_rrd.createSample(entry.getTimestamp());
        final double[] values = new double[entry.getDsNames().size()];
        int i = 0;
        for (final String dsName : entry.getDsNames()) {
            if (dsName != null) {
                Double value = entry.getValue(dsName);
                final Datasource dataSource = m_rrd.getDatasource(dsName);
                if (value != null) {
                    if (dataSource.getDsType().equals("COUNTER")) {
                        final double counterValue = getLastValue(dsName) + (value * m_step);
                        if (Double.isInfinite(counterValue)) {
                            // if we've overrun the counter, calculate our own counter loop
                            final BigDecimal bigValue = BigDecimal.valueOf(value);
                            final BigDecimal bigLastValue = BigDecimal.valueOf(getLastValue(dsName));
                            final BigDecimal bigStep = BigDecimal.valueOf(m_step);
                            final BigDecimal newValue = bigLastValue.multiply(bigStep).add(bigValue).subtract(m_doubleMax);
                            value = newValue.doubleValue();
                        } else {
                            value = counterValue;
                        }
                    }
                    values[i] = value;
                    setLastValue(dsName, value);
                }
            }
            i++;
        }
        s.setValues(values);
//        LogUtils.debugf(this, "writing sample to %s: %s", outputRrd, s);
        s.update();
    }

    private Double getLastValue(final String dsName) {
        Double lastValue = m_lastValue.get(dsName);
        if (lastValue == null) {
            lastValue = 0.0D;
        }
        if (LogUtils.isTraceEnabled(this)) LogUtils.tracef(this, "getLastValue(%s) = %f", dsName, lastValue);
        return lastValue;
    }
    
    private void setLastValue(final String dsName, final Double value) {
        if (value != null && !Double.isNaN(value)) {
            m_lastValue.put(dsName, value);
        }
    }
}
