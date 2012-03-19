package org.opennms.netmgt.model;

import java.beans.PropertyEditorSupport;


public class OnmsSeverityEditor extends PropertyEditorSupport {

	/** {@inheritDoc} */
	@Override
	public String getAsText() {
		final OnmsSeverity severity = (OnmsSeverity)super.getValue();
		return severity.name();
	}

	/** {@inheritDoc} */
	@Override
	public void setAsText(final String text) throws IllegalArgumentException {
		final OnmsSeverity severity = OnmsSeverity.get(text);
		super.setValue(severity);
	}

}
