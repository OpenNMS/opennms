package org.opennms.sms.monitor.internal.config;

import org.opennms.sms.monitor.OperationExecutor;

public class NullOperationExecutor implements OperationExecutor {
	private boolean m_defaultAnswer = true;

	public NullOperationExecutor() {
		
	}
	
	public NullOperationExecutor(boolean b) {
		m_defaultAnswer = b;
	}

	public boolean execute() {
		return m_defaultAnswer;
	}

}
