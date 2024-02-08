/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
