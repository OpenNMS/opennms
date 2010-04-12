package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ApplicationState implements Serializable, IsSerializable {
	private static final long serialVersionUID = 1L;
	private Map<String,GWTApplicationStatus> m_statuses = new HashMap<String,GWTApplicationStatus>();

	private Status m_status;

	public ApplicationState() {}

	public ApplicationState(final Date from, final Date to, final Collection<GWTApplication> applications, final Map<String, List<GWTLocationSpecificStatus>> statuses) {
		for (final GWTApplication app : applications) {
			m_statuses.put(app.getName(), new GWTApplicationStatus(app, from, to, statuses.get(app.getName())));
		}
	}

	public Status getStatus() {
		if (m_status == null) {
			m_status = getStatusUncached();
		}
		return m_status;
	}

	private Status getStatusUncached() {
		if (m_statuses.size() == 0) {
			return Status.unknown("No applications are currently defined.");
		}
		final List<String> m_applicationsUnknown  = new ArrayList<String>();
		final List<String> m_applicationsDown     = new ArrayList<String>();
		final List<String> m_applicationsMarginal = new ArrayList<String>();
		for (final String appName : m_statuses.keySet()) {
			final GWTApplicationStatus status = m_statuses.get(appName);
			switch(status.getStatus()) {
				case UNKNOWN: {
					m_applicationsUnknown.add(appName);
					break;
				}
				case DOWN: {
					m_applicationsDown.add(appName);
					break;
				}
				case MARGINAL: {
					m_applicationsMarginal.add(appName);
					break;
				}
			}
		}
		if (m_applicationsUnknown.size() > 0) {
			return Status.unknown("The following applications are reporting an unknown status: " + Utils.join(m_applicationsUnknown, ", "));
		}
		if (m_applicationsDown.size() > 0) {
			return Status.down("The following applications are reported as down: " + Utils.join(m_applicationsDown, ", "));
		}
		if (m_applicationsMarginal.size() > 0) {
			return Status.marginal("The following applications are reported as marginal: " + Utils.join(m_applicationsMarginal, ", "));
		}
		return Status.UP;
	}
}
