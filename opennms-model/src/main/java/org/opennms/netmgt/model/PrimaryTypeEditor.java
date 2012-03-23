package org.opennms.netmgt.model;

import java.beans.PropertyEditorSupport;

public class PrimaryTypeEditor extends PropertyEditorSupport {

	/** {@inheritDoc} */
	@Override
	public String getAsText() {
	    return ((PrimaryType)super.getValue()).getCode();
	}

	/** {@inheritDoc} */
	@Override
	public void setAsText(final String text) throws IllegalArgumentException {
	    super.setValue(PrimaryType.get(text));
	}

}
