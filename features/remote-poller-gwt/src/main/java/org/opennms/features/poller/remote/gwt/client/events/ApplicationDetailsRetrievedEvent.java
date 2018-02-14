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

package org.opennms.features.poller.remote.gwt.client.events;

import org.opennms.features.poller.remote.gwt.client.ApplicationDetails;

import com.google.gwt.event.shared.GwtEvent;

/**
 * <p>ApplicationDetailsRetrievedEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ApplicationDetailsRetrievedEvent extends GwtEvent<ApplicationDetailsRetrievedEventHandler> {

    /** Constant <code>TYPE</code> */
    public final static Type<ApplicationDetailsRetrievedEventHandler> TYPE = new Type<>();
    private ApplicationDetails m_applicationDetails;

    /**
     * <p>Constructor for ApplicationDetailsRetrievedEvent.</p>
     *
     * @param details a {@link org.opennms.features.poller.remote.gwt.client.ApplicationDetails} object.
     */
    public ApplicationDetailsRetrievedEvent(final ApplicationDetails details) {
        setApplicationDetails(details);
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(final ApplicationDetailsRetrievedEventHandler handler) {
        handler.onApplicationDetailsRetrieved(this);

    }

    /** {@inheritDoc} */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ApplicationDetailsRetrievedEventHandler> getAssociatedType() {
        return TYPE;
    }

    private void setApplicationDetails(final ApplicationDetails applicationDetails) {
        m_applicationDetails = applicationDetails;
    }

    /**
     * <p>getApplicationDetails</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationDetails} object.
     */
    public ApplicationDetails getApplicationDetails() {
        return m_applicationDetails;
    }

}
