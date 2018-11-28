/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.carbon.adapter;

import java.time.Instant;
import java.util.Objects;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class CarbonMessage {
    public final String metric;
    public final double value;
    public final Instant timestamp;

    public CarbonMessage(final String metric,
                         final double value,
                         final Instant timestamp) {
        this.metric = Objects.requireNonNull(metric);
        this.value = value;
        this.timestamp = Objects.requireNonNull(timestamp);
    }

    public Iterable<String> metricPath() {
        return Splitter.on('.').trimResults().split(this.metric);
    }

    @Override
    public String toString() {
        return Joiner.on(' ').join(
                this.metric,
                Double.toString(this.value),
                this.timestamp.getEpochSecond()
        );
    }
}
