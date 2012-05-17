package org.opennms.core.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StringTrimAdapter extends XmlAdapter<String,String> {

	@Override
	public String marshal(final String value) throws Exception {
		return value;
	}

	@Override
	public String unmarshal(final String value) throws Exception {
		if (value == null) return null;
		return value.trim();
	}

}
