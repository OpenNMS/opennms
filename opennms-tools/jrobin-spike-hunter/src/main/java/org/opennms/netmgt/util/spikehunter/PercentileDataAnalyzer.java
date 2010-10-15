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
