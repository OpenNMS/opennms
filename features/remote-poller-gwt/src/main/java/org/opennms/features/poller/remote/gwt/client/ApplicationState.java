package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;


import com.google.gwt.user.client.rpc.IsSerializable;

public class ApplicationState implements Serializable, IsSerializable {
	private static final long serialVersionUID = 1L;
	private Map<String,Collection<GWTLocationSpecificStatus>> m_statuses;

	public ApplicationState() {}

	public ApplicationState(final Collection<String> applications, final Map<String, Collection<GWTLocationSpecificStatus>> statuses) {
		m_statuses = statuses;
		for (final String app : applications) {
			if (!m_statuses.containsKey(app)) {
				m_statuses.put(app, null);
			}
		}
	}

	public Status getStatusUncached() {
		return null;
	}
}
