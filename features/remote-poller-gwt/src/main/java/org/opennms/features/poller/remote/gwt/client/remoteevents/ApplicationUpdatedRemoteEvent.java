/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.poller.remote.gwt.client.remoteevents;

import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;

/**
 * <p>ApplicationUpdatedRemoteEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ApplicationUpdatedRemoteEvent implements MapRemoteEvent {

	private static final long serialVersionUID = -3561142597099593769L;

	private ApplicationInfo m_applicationInfo;

	/**
	 * <p>Constructor for ApplicationUpdatedRemoteEvent.</p>
	 */
	public ApplicationUpdatedRemoteEvent() {}

	/**
	 * <p>Constructor for ApplicationUpdatedRemoteEvent.</p>
	 *
	 * @param item a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
	 */
	public ApplicationUpdatedRemoteEvent(final ApplicationInfo item) {
		m_applicationInfo = item;
	}

	/** {@inheritDoc} */
        @Override
	public void dispatch(final MapRemoteEventHandler presenter) {
		presenter.updateApplication(m_applicationInfo);
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
	    return "ApplicationUpdatedRemoteEvent[applicationInfo=" + m_applicationInfo + "]";
	}
}
