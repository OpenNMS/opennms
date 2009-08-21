/**
 * 
 */
package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.adapters.XmlAdapter;

class SequenceTransactionAdapter extends XmlAdapter<AbstractSequenceTransaction,SequenceTransaction> {
	@Override
	public AbstractSequenceTransaction marshal(SequenceTransaction v) throws Exception {
		/*
		System.err.println("marshalling object: " + v);
		if (v.getType().equals("synchronous")) {
			return (SynchronousSequenceTransaction)v;
		} else if (v.getType().equals("asynchronous")) {
			return (AsynchronousSequenceTransaction)v;
		} else if (v.getType() == null) {
			return (SynchronousSequenceTransaction)v;
		} else if (v.getClass() == SequenceOperation.class) {
			return (SequenceOperation)v;
		}
		System.err.println("WARNING: fell through to abstract transaction: " + v.getClass().getCanonicalName() + " (type = " + v.getType() + ")");
		*/
		return (AbstractSequenceTransaction)v;
	}

	@Override
	public SequenceTransaction unmarshal(AbstractSequenceTransaction v) throws Exception {
		System.err.println("unmarshalling object: " + v);
		return v;
	}
}