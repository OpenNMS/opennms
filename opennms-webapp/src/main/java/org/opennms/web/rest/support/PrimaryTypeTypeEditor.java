package org.opennms.web.rest.support;

import java.beans.PropertyEditorSupport;

import org.opennms.netmgt.model.OnmsIpInterface.PrimaryType;

public class PrimaryTypeTypeEditor extends PropertyEditorSupport {

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
