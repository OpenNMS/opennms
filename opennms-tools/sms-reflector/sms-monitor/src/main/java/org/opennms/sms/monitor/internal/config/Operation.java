package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


@XmlJavaTypeAdapter(OperationAdapter.class)
public interface Operation {
	public String getType();
	
	public String getLabel();
	public void setLabel(String label);
}
