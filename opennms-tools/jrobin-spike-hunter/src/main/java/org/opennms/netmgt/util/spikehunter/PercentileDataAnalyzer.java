/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
		List<Integer> violatorIndices = new ArrayList<Integer>();
		
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
		ArrayList<Double> sortedValues = new ArrayList<Double>();
		for (double thisVal : values) {
			if (! Double.toString(thisVal).equals(Double.toString(Double.NaN))) {
				sortedValues.add(thisVal);
			}
		}
		if (m_verbose) {
			SpikeHunter.printToUser("After removing NaN values, " + sortedValues.size() + " values are left");
		}
		Collections.sort(sortedValues);
		float N = new Float(sortedValues.size());
		int rankInt = Math.round(new Float((N / 100) * m_percentileNumber + 0.5));
		//BigDecimal rankBD = new BigDecimal((N / 100) * m_percentileNumber + 0.5).round(mc);
		if (m_verbose) {
			SpikeHunter.printToUser("Rank of Nth percentile value (N=" + m_percentileNumber + ") is " + rankInt);
		}
		m_percentileValue = sortedValues.get(rankInt);
		m_lowestValue = sortedValues.get(0);
		m_highestValue = sortedValues.get(sortedValues.size()-1);
	}
	
	public String toString() {
		return "Nth-percentile analyzer (N=" + m_percentileNumber + ", P_N=" + ((m_percentileValue != Double.NaN) ? m_percentileValue : "not yet calculated") + ", lowest=" + m_lowestValue + ", highest=" + m_highestValue + ")";
	}
	
	public void setVerbose(boolean v) {
		m_verbose = v;
	}
}
