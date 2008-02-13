package org.opennms.netmgt.util.spikehunter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NextDataReplacer implements DataReplacer {

	public double[] replaceValues(double[] values, List<Integer> indices) {
		Set<Integer> indexSet = new HashSet<Integer>();
		for (int i : indices) {
			indexSet.add(i);
		}
		
		for (int i : indices) {
			int newIndex = walkForwards(i, indexSet, values.length - 1);
			if (newIndex >= 0) {
				values[i] = newIndex;
			} else {
				values[i] = Double.NaN;
			}
		}
		return null;
	}
	
	private int walkForwards(int badIndex, Set<Integer> invalidIndices, int maxIndex) {
		for (int i = badIndex + 1; i <= maxIndex; i++) {
			if (! invalidIndices.contains(i)) {
				return i;
			}
		}
		return -1;
	}

}
