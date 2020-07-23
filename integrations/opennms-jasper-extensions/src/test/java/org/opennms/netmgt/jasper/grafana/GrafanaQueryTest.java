/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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