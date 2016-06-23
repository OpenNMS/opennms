package org.opennms.core.camel;

import org.apache.camel.InOnly;

@InOnly
public class DefaultDispatcher {

	private final String m_endpointUri;

	public DefaultDispatcher(final String endpointUri) {
		m_endpointUri = endpointUri;
	}

	public String getEndpointUri() {
		return m_endpointUri;
	}
}
