package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.log4j.Category;
import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.Task;
import org.opennms.core.utils.ThreadCategory;

public abstract class BaseOperation implements Operation {
	private String m_type;
	private String m_label;

	@XmlAttribute(name="type", required=true)
	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}
	
	@XmlAttribute(name="label", required=false)
	public String getLabel() {
		return m_label;
	}

	public void setLabel(String label) {
		m_label = label;
	}

	public abstract Task createTask(DefaultTaskCoordinator coordinator, ContainerTask parent);

	public Category log() {
		return ThreadCategory.getInstance(getClass());
	}
}
