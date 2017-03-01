/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.elasticsearch.eventforwarder;

import org.opennms.features.elasticsearch.eventforwarder.internal.DefaultAlarmForwarder;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Registers as an alam northbounder and forwards alarm to ES
 *
 * Created:
 * User: unicoletti
 * Date: 1:25 PM 7/10/15
 */
public class ElasticsearchNorthbounder extends AbstractNorthbounder {

    private volatile DefaultAlarmForwarder alarmForwarder;

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchNorthbounder.class);

    public ElasticsearchNorthbounder() {
        super("ElasticsearchNorthbounder");
        LOG.debug("ElasticsearchNorthbounder created");
    }

    @Override
    protected boolean accepts(NorthboundAlarm alarm) {
        return true; // accept ANY alarm
    }

    @Override
    public void forwardAlarms(List<NorthboundAlarm> alarms) throws NorthbounderException {
        for(NorthboundAlarm alarm: alarms) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("ElasticsearchNorthbounder Forwarding alarm: "+alarm);
            }
            alarmForwarder.sendNow(alarm);
        }
    }

    public DefaultAlarmForwarder getAlarmForwarder() {
        return alarmForwarder;
    }

    public void setAlarmForwarder(DefaultAlarmForwarder alarmForwarder) {
        this.alarmForwarder = alarmForwarder;
    }
}
