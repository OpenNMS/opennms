/**
 * 
 */
package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.adapters.XmlAdapter;

class OperationAdapter extends XmlAdapter<BaseTransactionOperation,Operation> {
	@Override
	public BaseTransactionOperation marshal(Operation v) throws Exception {
		// System.err.println("marshalling object: " + v);
		return (BaseTransactionOperation)v;
	}

	@Override
	public Operation unmarshal(BaseTransactionOperation v) throws Exception {
		// System.err.println("unmarshalling object: " + v);
		return v;
	}
}