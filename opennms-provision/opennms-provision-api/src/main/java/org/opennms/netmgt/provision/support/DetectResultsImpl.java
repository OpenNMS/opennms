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
package org.opennms.netmgt.provision.support;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.provision.DetectResults;

public class DetectResultsImpl implements DetectResults {
    private final boolean detected;
    private final Map<String, String> metaData;

    public DetectResultsImpl(boolean detected) {
        this(detected, Collections.emptyMap());
    }

    public DetectResultsImpl(boolean detected, Map<String, String> metaData) {
        this.detected = detected;
        this.metaData = Objects.requireNonNull(metaData);
    }

    @Override
    public boolean isServiceDetected() {
        return detected;
    }

    @Override
    public Map<String, String> getServiceAttributes() {
        return metaData;
    }
}
