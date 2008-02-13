package org.opennms.netmgt.util.spikehunter;

import java.util.List;

public class NanDataReplacer implements DataReplacer {

	public double[] replaceValues(double[] origValues, List<Integer> indices) {
		double[] newValues = origValues.clone();
		for (int violatorIndex : indices) {
			newValues[violatorIndex] = Double.NaN;
		}
		return newValues;
	}
}
