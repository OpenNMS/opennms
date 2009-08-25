package org.opennms.sms.monitor;

import java.util.List;

import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.sms.monitor.internal.config.Operation;
import org.opennms.sms.monitor.internal.config.TransactionOperation;

public class MonitorTransactionProcessor {

	private MonitoredService m_service;
	private boolean m_synchronous = true;
	
	public MonitorTransactionProcessor(MonitoredService svc) {
		m_service = svc;
	}

	public MonitorTransactionProcessor(MonitoredService svc, boolean synchronous) {
		m_service = svc;
		m_synchronous = synchronous;
	}

	public boolean processOperations(List<Operation> operations) {
		Boolean[] passed = new Boolean[operations.size()];

		for (int i = 0; i < operations.size(); i++) {
			boolean worked = true;
			
			Operation o = operations.get(i);

			if (o instanceof TransactionOperation) {
				MonitorTransactionProcessor proc = new MonitorTransactionProcessor(m_service);
				worked = proc.processOperations(((TransactionOperation) o).getOperations());
			} else {
				OperationExecutor oe = o.getExecutor();

				worked = oe.execute();
			
			}
			
			if (!worked) {
				return false;
			}
		}

		return true;
	}


}
