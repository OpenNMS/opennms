/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
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
