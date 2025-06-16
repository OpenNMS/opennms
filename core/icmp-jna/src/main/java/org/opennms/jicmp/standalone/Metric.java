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
