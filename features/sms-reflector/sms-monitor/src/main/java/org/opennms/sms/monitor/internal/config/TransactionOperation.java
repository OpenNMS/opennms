package org.opennms.sms.monitor.internal.config;

import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


@XmlJavaTypeAdapter(TransactionOperationAdapter.class)
public interface TransactionOperation extends Operation {
	public List<Operation> getOperations();
	public void setOperations(List<Operation> operations);
}
