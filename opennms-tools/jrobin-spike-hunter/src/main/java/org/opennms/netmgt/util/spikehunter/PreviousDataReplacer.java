package org.opennms.netmgt.util.spikehunter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PreviousDataReplacer implements DataReplacer {

	public double[] replaceValues(double[] values, List<Integer> indices) {
		Set<Integer> indexSet = new HashSet<Integer>();
		for (int i : indices) {
			indexSet.add(i);
		}
		
		for (int i : indices) {
			int newIndex = walkBackwards(i, indexSet);
			if (newIndex >= 0) {
				values[i] = newIndex;
			} else {
				values[i] = Double.NaN;
			}
		}
		return null;
	}
	
	private int walkBackwards(int badIndex, Set<Integer> invalidIndices) {
		for (int i = badIndex - 1; i >= 0; i--) {
			if (! invalidIndices.contains(i)) {
				return i;
			}
		}
		return -1;
	}

}
