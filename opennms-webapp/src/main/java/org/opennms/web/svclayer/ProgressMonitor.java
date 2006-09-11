package org.opennms.web.svclayer;

public class ProgressMonitor {
	
	private int m_phaseCount = 1;
	private int m_phase = 0;
	private String m_phaseLabel = "Loading";
	private Object m_result = null;
	private Exception m_exception = null;

	public int getPhaseCount() {
		return m_phaseCount;
	}
	
	public void setPhaseCount(int phaseCount) {
		m_phaseCount = phaseCount;
	}
	
	public String getPhaseLabel() {
		return m_phaseLabel;
	}
	
	public int getPhase() {
		return m_phase;
	}

	public void beginNextPhase(String phaseLabel) {
		m_phaseLabel = phaseLabel;
		m_phase++;
	}

	public void finished(Object result) {
		m_result = result;
		m_phaseLabel = "Done";
		m_phase = m_phaseCount;
	}
	
	public boolean isFinished() {
		return m_result != null;
	}

	public Object getResult() {
		return m_result;
	}

	public boolean isError() {
		return m_exception != null;
	}
	
	public Exception getException() {
		return m_exception;
	}

	public void errorOccurred(Exception e) {
		m_exception = e;
	}

	
}
