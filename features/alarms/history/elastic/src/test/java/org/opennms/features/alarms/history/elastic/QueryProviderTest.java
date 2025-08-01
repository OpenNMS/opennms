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
package org.opennms.features.alarms.history.elastic;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;

public class QueryProviderTest {

    private static final Gson gson = new Gson();

    /**
     * Verifies that the queries generate valid JSON.
     */
    @Test
    public void canGenerateQueries() {
        QueryProvider queryProvider = new QueryProvider();
        validate(queryProvider.getAlarmByDbIdAt(1, new TimeRange(1, 1)));
        validate(queryProvider.getAlarmByReductionKeyAt("string-with-some-special-characters\"\r\n[]{}\t+'", new TimeRange(1, 1)));
        validate(queryProvider.getActiveAlarmsAt(new TimeRange(1, 1), null));
        validate(queryProvider.getAllAlarms(new TimeRange(1, 1), 1));
        validate(queryProvider.getActiveAlarmIdsAtTimeAndExclude(new TimeRange(1, 1), Collections.emptySet(),null));
    }

    private void validate(String query) {
        assertThat(query, not(isEmptyOrNullString()));
        gson.fromJson(query, Map.class);
    }

}
