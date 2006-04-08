package org.opennms.netmgt.model;

/**  Represents a single archive of an RRD file */
public class DataStorageSpec {

	String m_consolidationFunction;
	double m_xfilesFactor;
	int m_steps;
	int m_rows;
	
	public String getConsolidationFunction() {
		return m_consolidationFunction;
	}
	public void setConsolidationFunction(String consolidationFunction) {
		m_consolidationFunction = consolidationFunction;
	}
	public int getRows() {
		return m_rows;
	}
	public void setRows(int rows) {
		m_rows = rows;
	}
	public int getSteps() {
		return m_steps;
	}
	public void setSteps(int steps) {
		m_steps = steps;
	}
	public double getXFilesFactor() {
		return m_xfilesFactor;
	}
	public void setXFilesFactor(double xfilesFactor) {
		m_xfilesFactor = xfilesFactor;
	}
	
	
}
