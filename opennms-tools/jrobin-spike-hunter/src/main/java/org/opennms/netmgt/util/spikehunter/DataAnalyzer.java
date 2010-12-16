package org.opennms.netmgt.util.spikehunter;

import java.util.List;

public interface DataAnalyzer {
	public List<Integer> findSamplesInViolation(double[] values);
	public void setParms(List<Double> parms);
	public void setVerbose(boolean v);
}
