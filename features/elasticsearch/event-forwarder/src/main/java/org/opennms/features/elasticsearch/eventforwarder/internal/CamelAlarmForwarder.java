/*******************************************************************************
* This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.elasticsearch.eventforwarder.internal;

import org.apache.camel.InOnly;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;

/**
 * This interface must be {@link InOnly} in order for alarm forwarding to
 * be performed asynchronously.
 * 
 * Created:
 * User: unicoletti
 * Date: 1:48 PM 7/10/15
 */
@InOnly
public interface CamelAlarmForwarder {
    public void sendNow(NorthboundAlarm alarm);
}
