package org.opennms.netmgt.dao.jaxb;

import javax.xml.bind.Unmarshaller;


public class JaxbUnmarshallerBundle {

	private Unmarshaller m_unmarshaller;

	public JaxbUnmarshallerBundle(final Unmarshaller unmarshaller) {
		m_unmarshaller = unmarshaller;
	}

	public Unmarshaller getUnmarshaller() {
		return m_unmarshaller;
	}

}
