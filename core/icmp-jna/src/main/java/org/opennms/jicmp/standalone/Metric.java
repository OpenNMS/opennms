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

package org.opennms.jicmp.standalone;

import java.util.concurrent.TimeUnit;

/**
 * Metric
 *
 * @author brozow
 */
public class Metric {
    
    private int m_count = 0;
    private double m_sumOfSquaresOfDifferences = 0.0;
    private double m_average = 0.0;
    private long m_min = Long.MAX_VALUE;
    private long m_max = Long.MIN_VALUE;
  
    /**
     * @return the count
     */
    public int getCount() {
        return m_count;
    }

    /**
     * @return the sumOfSquaresOfDifferences
     */
    public double getSumOfSquaresOfDifferences() {
        return m_sumOfSquaresOfDifferences;
    }

    /**
     * @return the stdDevElapsedNanos
     */
    public double getStandardDeviation() {
        return m_count == 0 ? 0.0 : Math.sqrt(getSumOfSquaresOfDifferences() / getCount());
    }

    /**
     * @return the avgElapsedNanos
     */
    public double getAverage() {
        return m_average;
    }

    /**
     * @return the minElapsedNanos
     */
    public long getMinimum() {
        return m_min;
    }

    /**
     * @return the maxElapsedNanos
     */
    public long getMaximum() {
        return m_max;
    }

    /**
     * update of nanos value
     */
    public void update(long sample) {
        m_count++;
        double oldAvg = m_average;
        m_average += (sample - oldAvg)/m_count;
        m_sumOfSquaresOfDifferences += (sample - getAverage())*(sample - oldAvg);
        m_min = Math.min(m_min, sample);
        m_max = Math.max(m_max, sample);
    }

    public String getSummary(TimeUnit unit) {
        double nanosPerUnit = TimeUnit.NANOSECONDS.convert(1, unit); 
        return String.format("cnt/min/avg/max/stddev = %d/%.3f/%.3f/%.3f/%.3f",
                getCount(),
                getMinimum()/nanosPerUnit, 
                getAverage()/nanosPerUnit,
                getMaximum()/nanosPerUnit, 
                getStandardDeviation()/nanosPerUnit);
    }


}
