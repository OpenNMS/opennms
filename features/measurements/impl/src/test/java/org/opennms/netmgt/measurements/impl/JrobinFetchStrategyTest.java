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

import org.jrobin.core.RrdException;
import org.junit.Test;
import org.opennms.netmgt.measurements.model.QueryMetadata;
import org.opennms.netmgt.measurements.model.Source;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JrobinFetchStrategyTest {
    @Test
    public void checkLongDataSourceName() throws RrdException {
        final JrobinFetchStrategy fetch = new JrobinFetchStrategy();
        final Map<Source, String> rrdsBySource = new HashMap<>();

        String rrdFile = new File("src/test/resources/TlmRecordEnrichmentErrors.jrb").getAbsolutePath();
        rrdsBySource.put(new Source("TlmRecordEnrichmentErrors","nodeSource[Minion:node1]",
                "TlmRecordEnrichmentErrors", null, false), rrdFile);

        final Map<String, Object> constants = new HashMap<>();
        final QueryMetadata metadata = new QueryMetadata();

        fetch.fetchMeasurements(1607394840000L, 1607999580000L, 363, 864,
                rrdsBySource, constants, metadata);
    }
}
