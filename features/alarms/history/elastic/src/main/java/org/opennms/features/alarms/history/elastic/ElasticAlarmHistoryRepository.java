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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.features.alarms.history.api.AlarmHistoryRepository;
import org.opennms.features.alarms.history.api.AlarmState;
import org.opennms.features.alarms.history.elastic.dto.AlarmDocumentDTO;
import org.opennms.features.jest.client.index.IndexSelector;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.TopHitsAggregation;

/**
 * Queries alarm history stored in Elasticsearch.
 */
public class ElasticAlarmHistoryRepository implements AlarmHistoryRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticAlarmHistoryRepository.class);

    public static final long DEFAULT_LOOKBACK_PERIOD_MS = TimeUnit.DAYS.toMillis(7);

    private final JestClient client;

    private final IndexSelector indexSelector;

    private final QueryProvider queryProvider = new QueryProvider();

    private long lookbackPeriodMs = DEFAULT_LOOKBACK_PERIOD_MS;

    public ElasticAlarmHistoryRepository(JestClient client, IndexStrategy indexStrategy, IndexSettings indexSettings) {
        this.client = Objects.requireNonNull(client);
        Objects.requireNonNull(indexStrategy);
        this.indexSelector = new IndexSelector(indexSettings, ElasticAlarmIndexer.INDEX_NAME, indexStrategy, 0);
    }

    @Override
    public Optional<AlarmState> getAlarmWithDbIdAt(long id, long time) {
        final TimeRange timeRange = getTimeRange(time);
        return findAlarms(queryProvider.getAlarmByDbIdAt(id, timeRange), timeRange)
                .stream().findFirst().map(a -> a);
    }

    @Override
    public Optional<AlarmState> getAlarmWithReductionKeyIdAt(String reductionKey, long time) {
        final TimeRange timeRange = getTimeRange(time);
        return findAlarms(queryProvider.getAlarmByReductionKeyAt(reductionKey, timeRange), timeRange)
                .stream().findFirst().map(a -> a);
    }

    @Override
    public List<AlarmState> getStatesForAlarmWithDbId(long id) {
        return findAlarms(queryProvider.getAlarmStatesByDbId(id), null)
                .stream()
                .map(a -> (AlarmState)a)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlarmState> getStatesForAlarmWithReductionKey(String reductionKey) {
        return findAlarms(queryProvider.getAlarmStatesByReductionKey(reductionKey), null)
                .stream()
                .map(a -> (AlarmState)a)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlarmState> getActiveAlarmsAt(long time) {
        final TimeRange timeRange = getTimeRange(time);
        return findAlarmsWithCompositeAggregation((afterAlarmWithId) -> queryProvider.getActiveAlarmsAt(timeRange, afterAlarmWithId), timeRange).stream()
                .map(a -> (AlarmState)a)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlarmState> getLastStateOfAllAlarms(long start, long end) {
        final TimeRange timeRange = new TimeRange(start, end);
        return findAlarmsWithCompositeAggregation((afterAlarmWIthId) -> queryProvider.getAllAlarms(timeRange, afterAlarmWIthId), timeRange).stream()
                .map(a -> (AlarmState)a)
                .collect(Collectors.toList());
    }

    @Override
    public long getNumActiveAlarmsAt(long time) {
        TimeRange timeRange = getTimeRange(time);
        return findAlarmsWithCompositeAggregation((afterAlarmWithId) -> queryProvider.getActiveAlarmIdsAt(timeRange, afterAlarmWithId), timeRange).size();
    }

    @Override
    public List<AlarmState> getActiveAlarmsNow() {
        return getActiveAlarmsAt(System.currentTimeMillis());
    }

    @Override
    public long getNumActiveAlarmsNow() {
        return getNumActiveAlarmsAt(System.currentTimeMillis());
    }

    private List<AlarmDocumentDTO> findAlarmsWithCompositeAggregation(Function<Integer,String> getNextQuery, TimeRange timeRange) {
        final List<AlarmDocumentDTO> alarms = new LinkedList<>();
        Integer afterAlarmWithId = null;
        while (true) {
            final String query = getNextQuery.apply(afterAlarmWithId);
            final Search.Builder search = new Search.Builder(query);
            if (timeRange != null) {
                final List<String> indices = indexSelector.getIndexNames(timeRange.getStart(), timeRange.getEnd());
                search.addIndices(indices);
                search.setParameter("ignore_unavailable", "true"); // ignore unknown index
                LOG.debug("Executing query on {}: {}", indices, query);
            } else {
                search.addIndex("opennms-alarms-*");
                LOG.debug("Executing query on all indices: {}", query);
            }
            final SearchResult result;
            try {
                result = client.execute(search.build());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!result.isSucceeded()) {
                throw new RuntimeException(result.getErrorMessage());
            }

            final CompositeAggregation alarmsById = result.getAggregations().getAggregation("alarms_by_id", CompositeAggregation.class);
            if (alarmsById == null) {
                // No results, we're done
                break;
            } else {
                for (CompositeAggregation.Entry entry : alarmsById.getBuckets()) {
                    final TopHitsAggregation topHitsAggregation = entry.getTopHitsAggregation("latest_alarm");
                    final List<SearchResult.Hit<AlarmDocumentDTO, Void>> hits = topHitsAggregation.getHits(AlarmDocumentDTO.class);
                    hits.stream().map(h -> h.source).forEach(alarms::add);
                }

                if (alarmsById.hasAfterKey()) {
                    // There are more results to page through
                    afterAlarmWithId = alarmsById.getAfterKey().get("alarm_id").getAsInt();
                } else {
                    // There are no more results to page through
                    break;
                }
            } {

            }
        }
        return alarms;
    }

    private TimeRange getTimeRange(long time) {
        return new TimeRange(Math.max(time - lookbackPeriodMs, 0), time);
    }

    private List<AlarmDocumentDTO> findAlarms(String query, TimeRange timeRange) {
        final Search.Builder search = new Search.Builder(query);
        if (timeRange != null) {
            final List<String> indices = indexSelector.getIndexNames(timeRange.getStart(), timeRange.getEnd());
            search.addIndices(indices);
            search.setParameter("ignore_unavailable", "true"); // ignore unknown index
            LOG.debug("Executing query on {}: {}", indices, query);
        } else {
            search.addIndex("opennms-alarms-*");
            LOG.debug("Executing query on all indices: {}", query);
        }

        final SearchResult result;
        try {
            result = client.execute(search.build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!result.isSucceeded()) {
            throw new RuntimeException(result.getErrorMessage());
        }

        final List<SearchResult.Hit<AlarmDocumentDTO, Void>> hits = result.getHits(AlarmDocumentDTO.class);
        return hits.stream().map(h -> h.source).collect(Collectors.toList());
    }

    public void setLookbackPeriodMs(long lookbackPeriodMs) {
        this.lookbackPeriodMs = lookbackPeriodMs;
    }
}
