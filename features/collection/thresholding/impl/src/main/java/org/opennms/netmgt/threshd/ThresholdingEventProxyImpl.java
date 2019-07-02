/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 * <p>ThresholdingEventProxy class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ThresholdingEventProxyImpl implements ThresholdingEventProxy {
    
    private static final Logger LOG = LoggerFactory.getLogger(ThresholdingEventProxy.class);

    private EventProxy eventMgr;

    @Override
    public void sendEvent(Event event) {
        try {
            eventMgr.send(event);
        } catch (EventProxyException e) {
            LOG.error("Failed to send {} ", event, e);
        }
    }

    @Override
    public void send(Event event) throws EventProxyException {
        eventMgr.send(event);
    }

    @Override
    public void send(Log eventLog) throws EventProxyException {
        eventMgr.send(eventLog);
    }

    @VisibleForTesting
    public void setEventMgr(EventProxy eventMgr) {
        this.eventMgr = eventMgr;
    }

}
