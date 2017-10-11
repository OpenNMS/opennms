/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.util.spikehunter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PercentileDataAnalyzer implements DataAnalyzer {
	private Double m_thresholdMultiplier;
	private int m_percentileNumber;		// e.g. 95 for 95th percentile
	private double m_percentileValue = Double.NaN;
	private boolean m_verbose = false;
	
	private double m_lowestValue = Double.NaN;
	private double m_highestValue = Double.NaN;

	public PercentileDataAnalyzer(List<Double> operands) {
		setParms(operands);
	}
	
	public List<Integer> findSamplesInViolation(double[] values) {
		List<Integer> violatorIndices = new ArrayList<>();
		
		calculatePercentile(values);
		double absThreshold = m_percentileValue * m_thresholdMultiplier;
		for (int i = 0; i < values.length; i++) {
			if (Double.toString(values[i]).equals(Double.toString(Double.NaN))) {
				continue;
			}
			if (values[i] > absThreshold) {
				violatorIndices.add(i);
			}
		}		
		return violatorIndices;
	}
	
	public void setParms(List<Double> parms) {
		m_percentileNumber = parms.get(0).intValue();
		m_thresholdMultiplier = parms.get(1);
	}

	private void calculatePercentile(double[] values) {	
		if (m_verbose) {
			SpikeHunter.printToUser("Before removing NaN values, " + values.length + " values are in the set");
		}
		ArrayList<Double> sortedValues = new ArrayList<>();
		for (double thisVal : values) {
			if (! Double.toString(thisVal).equals(Double.toString(Double.NaN))) {
				sortedValues.add(thisVal);
			}
		}
		if (m_verbose) {
			SpikeHunter.printToUser("After removing NaN values, " + sortedValues.size() + " values are left");
		}
		if (sortedValues.isEmpty()) {
		    m_percentileValue = 0;
		    m_lowestValue = 0;
		    m_highestValue = 0;
		    return;
		}
		Collections.sort(sortedValues);
		float N = new Float(sortedValues.size());
		int rankInt = getPercentilePossition(N);
		if (m_verbose) {
			SpikeHunter.printToUser("Rank of Nth percentile value (N=" + m_percentileNumber + ") is " + rankInt);
		}
		m_percentileValue = sortedValues.get(rankInt);
		m_lowestValue = sortedValues.get(0);
		m_highestValue = sortedValues.get(sortedValues.size()-1);
	}
	
	/*
	 * According with the Apache Commons Math (3.0-SNAPSHOT), the percentile should be calculated according with the following rules:
	 * 
	 * 1) Let n be the length of the (sorted) array and 0 < p <= 100 be the desired percentile.
	 * 2) If n = 1 return the unique array element (regardless of the value of p); otherwise
	 * 3) Compute the estimated percentile position pos = p * (n + 1) / 100 and the difference, d between pos and floor(pos) (i.e. the fractional part of pos).
	 * 4) If pos < 1 return the smallest element in the array.
	 * 5) Else if pos >= n return the largest element in the array.
	 * 6) Else let lower be the element in position floor(pos) in the array and let upper be the next element in the array. Return lower + d * (upper - lower)
	 * 
	 * Source: http://commons.apache.org/math/apidocs/org/apache/commons/math/stat/descriptive/rank/Percentile.html
	 * 
	 */
	public int getPercentilePossition(float n) {
	    if (n == 0)
	        return 0;
	    if (n == 1)
	        return 1;
	    double pos = m_percentileNumber * (n + 1) / 100;
	    if (pos < 1) {
	        return 0;
	    }
	    if (pos >= n) {
	        return (int)n - 1;
	    }
	    return Math.round(new Float((n / 100) * m_percentileNumber + 0.5));
	}

	public String toString() {
		return "Nth-percentile analyzer (N=" + m_percentileNumber + ", P_N=" + ((m_percentileValue != Double.NaN) ? m_percentileValue : "not yet calculated") + ", lowest=" + m_lowestValue + ", highest=" + m_highestValue + ")";
	}
	
	public void setVerbose(boolean v) {
		m_verbose = v;
	}
}
