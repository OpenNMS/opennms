/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.health.api;

import java.time.Duration;

/**
 * A Context object to store all parameters required by the {@link HealthCheckService}
 * to calculate the overall {@link Health}.
 *
 * @author mvrueden
 */
public class Context {
    // timout for executing a health check
    private long timeout; // ms
    // the maximum age that cached healthiness responses may have
    private Duration maxAge = Duration.ZERO;

    public void setTimeout(long timeoutInMs) {
        this.timeout = timeoutInMs;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setMaxAge(Duration value) {
        this.maxAge = value;
    }

    public Duration getMaxAge() {
        return maxAge;
    }
}
