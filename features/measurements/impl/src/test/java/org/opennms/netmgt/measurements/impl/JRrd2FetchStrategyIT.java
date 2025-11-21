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
package org.opennms.netmgt.measurements.impl;

import org.junit.Test;
import org.opennms.netmgt.measurements.model.QueryMetadata;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.rrd.RrdException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JRrd2FetchStrategyIT {
    @Test
    public void checkLongDataSourceName() throws RrdException {
        final JRrd2FetchStrategy fetch = new JRrd2FetchStrategy();
        final Map<Source, String> rrdsBySource = new HashMap<>();

        String rrdFile = new File("src/test/resources/TlmRecordEnrichmentErrors.rrd").getAbsolutePath();
        rrdsBySource.put(new Source("TlmRecordEnrichmentErrors","nodeSource[Minion:node1]",
                "TlmRecordEnrichmentErrors", null, false), rrdFile);

        final Map<String, Object> constants = new HashMap<>();
        final QueryMetadata metadata = new QueryMetadata();

        fetch.fetchMeasurements(1600000500000L, 1600000800000L, 363, 864,
                rrdsBySource, constants, metadata);
    }
}
