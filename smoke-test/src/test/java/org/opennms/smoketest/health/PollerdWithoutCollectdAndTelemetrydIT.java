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
package org.opennms.smoketest.health;

import java.util.List;

import org.junit.ClassRule;
import org.opennms.smoketest.stacks.OpenNMSStack;

/**
 * Verifies that Pollerd runs and the health check succeeds when Collectd and Telemetryd are disabled.
 */
public class PollerdWithoutCollectdAndTelemetrydIT extends AbstractDisabledServicesHealthCheckIT {

    private static final List<String> DISABLED_DAEMONS = List.of("Collectd", "Telemetryd");

    @ClassRule
    public static final OpenNMSStack stack = stackWithDisabledServices(DISABLED_DAEMONS);

    @Override
    protected OpenNMSStack stack() {
        return stack;
    }

    @Override
    protected List<String> disabledDaemons() {
        return DISABLED_DAEMONS;
    }

    @Override
    protected List<String> requiredDaemons() {
        return List.of("Pollerd");
    }
}
