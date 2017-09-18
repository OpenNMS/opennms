/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.utils.StringUtils;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>ApplicationState class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ApplicationState implements Serializable, IsSerializable {

	private static final long serialVersionUID = 3635173624296588109L;

	private Map<String,ApplicationDetails> m_statuses = new HashMap<String,ApplicationDetails>();

	private StatusDetails m_statusDetails;

	/**
	 * <p>Constructor for ApplicationState.</p>
	 */
	public ApplicationState() {}

	/**
	 * <p>Constructor for ApplicationState.</p>
	 *
	 * @param from a {@link java.util.Date} object.
	 * @param to a {@link java.util.Date} object.
	 * @param applications a {@link java.util.Collection} object.
	 * @param monitors a {@link java.util.List} object.
	 * @param statuses a {@link java.util.Map} object.
	 */
	public ApplicationState(final Date from, final Date to, final Collection<ApplicationInfo> applications, final List<GWTLocationMonitor> monitors, final Map<String, List<GWTLocationSpecificStatus>> statuses) {
		for (final ApplicationInfo app : applications) {
			m_statuses.put(app.getName(), new ApplicationDetails(app, from, to, monitors, statuses.get(app.getName())));
		}
	}

	/**
	 * <p>getStatusDetails</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
	 */
	public StatusDetails getStatusDetails() {
		if (m_statusDetails == null) {
			m_statusDetails = getStatusDetailsUncached();
		}
		return m_statusDetails;
	}

	private StatusDetails getStatusDetailsUncached() {
		if (m_statuses.size() == 0) {
			return StatusDetails.unknown("No applications are currently defined.");
		}
		final List<String> m_applicationsUnknown  = new ArrayList<>();
		final List<String> m_applicationsDown     = new ArrayList<>();
		final List<String> m_applicationsMarginal = new ArrayList<>();
		for (final String appName : m_statuses.keySet()) {
			final ApplicationDetails status = m_statuses.get(appName);
			switch(status.getStatusDetails().getStatus()) {
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
				default: {
				    break;
				}
			}
		}
		if (m_applicationsUnknown.size() > 0) {
			return StatusDetails.unknown("The following applications are reporting an unknown status: " + StringUtils.join(m_applicationsUnknown, ", "));
		}
		if (m_applicationsDown.size() > 0) {
			return StatusDetails.down("The following applications are reported as down: " + StringUtils.join(m_applicationsDown, ", "));
		}
		if (m_applicationsMarginal.size() > 0) {
			return StatusDetails.marginal("The following applications are reported as marginal: " + StringUtils.join(m_applicationsMarginal, ", "));
		}
		return StatusDetails.up();
	}
}
