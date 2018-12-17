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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.alarms.history.api.AlarmHistoryRepository;
import org.opennms.features.alarms.history.api.AlarmState;
import org.opennms.features.alarms.history.elastic.dto.AlarmDocumentDTO;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.TopHitsAggregation;

public class ElasticAlarmHistoryRepository implements AlarmHistoryRepository {

    private final JestClient client;

    private final QueryProvider queryProvider = new QueryProvider();

    public ElasticAlarmHistoryRepository(JestClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public AlarmState getAlarmWithDbIdAt(long id, long time) {
        return findAlarms(queryProvider.getAlarmByDbIdAt(id, time), false)
                .stream().findFirst()
                .orElse(null);
    }

    @Override
    public AlarmState getAlarmWithReductionKeyIdAt(String reductionKey, long time) {
        return findAlarms(queryProvider.getAlarmByReductionKeyAt(reductionKey, time), false)
                .stream().findFirst()
                .orElse(null);
    }

    @Override
    public List<AlarmState> getStatesForAlarmWithDbId(long id) {
        return findAlarms(queryProvider.getAlarmStatesByDbId(id), false)
                .stream()
                .map(a -> (AlarmState)a)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlarmState> getStatesForAlarmWithReductionKey(String reductionKey) {
        return findAlarms(queryProvider.getAlarmStatesByReductionKey(reductionKey), false)
                .stream()
                .map(a -> (AlarmState)a)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlarmState> getActiveAlarmsAt(long time) {
        return findAlarms(queryProvider.getActiveAlarmsAt(time), true).stream()
                .map(a -> (AlarmState)a)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlarmState> getLastStateOfAllAlarms(long start, long end) {
        return findAlarms(queryProvider.getAllAlarms(), true).stream()
                .map(a -> (AlarmState)a)
                .collect(Collectors.toList());
    }

    @Override
    public long getNumActiveAlarmsAt(long time) {
        // We could improve this to only retrieve the count, instead of
        // actually retrieving and mapping all the documents back
        return getActiveAlarmsAt(time).size();
    }

    @Override
    public List<AlarmState> getActiveAlarmsNow() {
        return getActiveAlarmsAt(System.currentTimeMillis());
    }

    @Override
    public long getNumActiveAlarmsNow() {
        return getNumActiveAlarmsAt(System.currentTimeMillis());
    }

    private List<AlarmDocumentDTO> findAlarms(String query, boolean inAggregation) {
        final Search search = new Search.Builder(query)
                // TODO: Smarter indexing
                .addIndex("opennms-alarms-*")
                .addType(AlarmDocumentDTO.TYPE)
                .build();
        final SearchResult result;
        try {
            result = client.execute(search);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!result.isSucceeded()) {
            throw new RuntimeException(result.getErrorMessage());
        }
        return getAlarmsFromSearchResult(result, inAggregation);
    }

    protected static List<AlarmDocumentDTO> getAlarmsFromSearchResult(SearchResult result, boolean inAggregation) {
        if (!inAggregation) {
            final List<SearchResult.Hit<AlarmDocumentDTO, Void>> hits = result.getHits(AlarmDocumentDTO.class);
            return hits.stream().map(h -> h.source).collect(Collectors.toList());
        } else {
            final List<AlarmDocumentDTO> alarms = new LinkedList<>();
            final CompositeAggregation alarmsById = result.getAggregations().getAggregation("alarms_by_id", CompositeAggregation.class);
            if (alarmsById != null) {
                for (CompositeAggregation.Entry entry : alarmsById.getBuckets()) {
                    final TopHitsAggregation topHitsAggregation = entry.getTopHitsAggregation("latest_alarm");
                    final List<SearchResult.Hit<AlarmDocumentDTO, Void>> hits = topHitsAggregation.getHits(AlarmDocumentDTO.class);
                    hits.stream().map(h -> h.source).forEach(alarms::add);
                }
            }
            return alarms;
        }
    }

}
