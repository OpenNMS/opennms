/**
 * 
 */
package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;

class TransactionOperationAdapter extends XmlAdapter<BaseTransactionOperation,Operation> {
	@Override
	public BaseTransactionOperation marshal(Operation v) throws Exception {
		Logger log = log();
		if (log.isDebugEnabled()) {
			log.debug("marshalling object: " + v);
		}
		return (BaseTransactionOperation)v;
	}

	@Override
	public Operation unmarshal(BaseTransactionOperation v) throws Exception {
		Logger log = log();
		if (log.isDebugEnabled()) {
			log.debug("unmarshalling object: " + v);
		}
		return v;
	}

	private Logger log() {
		return ThreadCategory.getInstance(TransactionOperationAdapter.class);
	}

}