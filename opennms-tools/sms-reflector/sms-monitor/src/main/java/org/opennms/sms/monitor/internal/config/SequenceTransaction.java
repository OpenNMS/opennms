package org.opennms.sms.monitor.internal.config;

import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


@XmlJavaTypeAdapter(SequenceTransactionAdapter.class)
public interface SequenceTransaction {
	public String getType();
	
	public String getLabel();
	public void setLabel(String label);
	
	public List<SequenceSessionVariable> getSessionVariables();
	public void setSessionVariables(List<SequenceSessionVariable> sessionVariables);
	
	public List<SequenceTransaction> getOperations();
	public void setOperations(List<SequenceTransaction> operations);
}
