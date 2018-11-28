/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.features.alarms.history.elastic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;
import org.opennms.features.alarms.history.elastic.dto.AlarmDocumentDTO;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import io.searchbox.core.SearchResult;

public class ElasticAlarmHistoryRepositoryTest {

    @Test
    public void canGetAlarmsFromSearchResult() throws IOException {
        final String json = Resources.toString(Resources.getResource("composite_aggregation_response.json"), StandardCharsets.UTF_8);
        SearchResult result = new SearchResult(new Gson());
        result.setJsonString(json);
        result.setJsonObject(new JsonParser().parse(json).getAsJsonObject());

        final List<AlarmDocumentDTO> alarms = ElasticAlarmHistoryRepository.getAlarmsFromSearchResult(result, true);
        assertThat(alarms, hasSize(equalTo(1)));
    }
}
