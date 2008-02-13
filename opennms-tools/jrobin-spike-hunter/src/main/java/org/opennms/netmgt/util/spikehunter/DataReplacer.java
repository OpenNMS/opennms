package org.opennms.netmgt.util.spikehunter;

import java.util.List;

public interface DataReplacer {
	public double[] replaceValues(double[] values, List<Integer> indices);
}
