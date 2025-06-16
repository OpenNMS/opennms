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
package org.opennms.netmgt.jasper.grafana;

import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class GrafanaQueryTest {

    @Test
    public void canParseQuery() {
        final String queryJson = "{\n" +
                "  \"dashboard\": {\n" +
                "    \"uid\": \"eWsVEL6zz\"\n" +
                "  },\n" +
                "  \"time\": {\n" +
                "    \"from\": 1,\n" +
                "    \"to\": 2\n" +
                "  },\n" +
                "  \"render\": {\n" +
                "    \"width\": 4,\n" +
                "    \"height\": 5,\n" +
                "    \"theme\": \"light\"\n" +
                "  },\n" +
                "  \"variables\": {\n" +
                "    \"node\": \"1\",\n" +
                "    \"interface\": \"2\"\n" +
                "  }\n" +
                "}";
        final GrafanaQuery query = new GrafanaQuery(queryJson);
        assertThat(query.getDashboardUid(), equalTo("eWsVEL6zz"));
        assertThat(query.getFrom().getTime(), equalTo(1L));
        assertThat(query.getTo().getTime(), equalTo(2L));
        assertThat(query.getWidth(), equalTo(4));
        assertThat(query.getHeight(), equalTo(5));
        assertThat(query.getTheme(), equalTo("light"));
        assertThat(query.getVariables(), equalTo(ImmutableMap.builder()
                .put("node", "1")
                .put("interface", "2")
                .build()));
    }

    @Test
    public void canParseQueryWithNoVariables() {
        final String queryJson = "{\n" +
                "  \"dashboard\": {\n" +
                "    \"uid\": \"eWsVEL6zz\"\n" +
                "  },\n" +
                "  \"time\": {\n" +
                "    \"from\": 1,\n" +
                "    \"to\": 2\n" +
                "  },\n" +
                "  \"render\": {\n" +
                "    \"width\": 4,\n" +
                "    \"height\": 5,\n" +
                "    \"theme\": \"light\"\n" +
                "  }\n" +
                "}";
        final GrafanaQuery query = new GrafanaQuery(queryJson);
        assertThat(query.getDashboardUid(), equalTo("eWsVEL6zz"));
        assertThat(query.getVariables(), anEmptyMap());
    }
}