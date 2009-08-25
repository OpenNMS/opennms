package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.sms.monitor.OperationExecutor;


@XmlJavaTypeAdapter(OperationAdapter.class)
public interface Operation {
	public String getType();
	
	public String getLabel();
	public void setLabel(String label);

	public OperationExecutor getExecutor();
}
