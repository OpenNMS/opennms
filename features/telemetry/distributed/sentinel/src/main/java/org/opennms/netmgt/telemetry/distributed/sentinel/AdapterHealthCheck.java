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
package org.opennms.netmgt.telemetry.distributed.sentinel;

import org.opennms.core.health.api.SimpleHealthCheck;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;

import java.util.Arrays;
import java.util.List;

import static org.opennms.core.health.api.HealthCheckConstants.LOCAL;
import static org.opennms.core.health.api.HealthCheckConstants.TELEMETRY;

public class AdapterHealthCheck extends SimpleHealthCheck {

    public AdapterHealthCheck(AdapterDefinition adapterDef) {
        this(adapterDef.getFullName(), adapterDef.getClassName());
    }

    private AdapterHealthCheck(final String adapterName, final String adapterType) {
        super(() -> "Verifying Adapter " + adapterName + " (" + adapterType + ")");
    }

    @Override
    public List<String> getTags() {
        return Arrays.asList(LOCAL, TELEMETRY);
    }
}
