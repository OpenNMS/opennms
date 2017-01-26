/*******************************************************************************
* This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.amqp.alarmnorthbounder.internal;

import org.apache.camel.Produce;
import org.opennms.core.camel.DefaultDispatcher;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAlarmForwarder extends DefaultDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAlarmForwarder.class);

    @Produce(property="endpointUri")
    CamelAlarmForwarder m_proxy;

    public DefaultAlarmForwarder(final String endpointUri) {
        super(endpointUri);
    }

    public void sendNow(NorthboundAlarm alarm) {
        LOG.debug("Forwarding alarm {}", alarm);
        m_proxy.sendNow(alarm);
    }
}
