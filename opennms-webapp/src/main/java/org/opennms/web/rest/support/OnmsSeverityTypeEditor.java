package org.opennms.web.rest.support;

import java.beans.PropertyEditorSupport;

import org.opennms.netmgt.model.OnmsSeverity;

public class OnmsSeverityTypeEditor extends PropertyEditorSupport {

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
