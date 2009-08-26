package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.Task;


@XmlJavaTypeAdapter(OperationAdapter.class)
public interface Operation {
	public String getType();
	
	public String getLabel();
	public void setLabel(String label);

	public Task createTask(DefaultTaskCoordinator coordinator);
}
