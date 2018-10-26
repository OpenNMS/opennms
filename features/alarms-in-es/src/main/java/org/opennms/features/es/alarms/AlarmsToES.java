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

package org.opennms.features.es.alarms;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.opennms.core.cache.Cache;
import org.opennms.core.cache.CacheBuilder;
import org.opennms.core.cache.CacheConfig;
import org.opennms.core.time.PseudoClock;
import org.opennms.features.es.alarms.dto.AlarmDocumentDTO;
import org.opennms.features.es.alarms.dto.NodeDocumentDTO;
import org.opennms.features.es.alarms.tasks.BulkDeleteTask;
import org.opennms.features.es.alarms.tasks.DeleteAlarmTask;
import org.opennms.features.es.alarms.tasks.IndexAlarmsTask;
import org.opennms.features.es.alarms.tasks.Task;
import org.opennms.features.es.alarms.tasks.TaskVisitor;
import org.opennms.netmgt.alarmd.api.AlarmLifecycleListener;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.plugins.elasticsearch.rest.bulk.BulkException;
import org.opennms.plugins.elasticsearch.rest.bulk.BulkRequest;
import org.opennms.plugins.elasticsearch.rest.bulk.BulkWrapper;
import org.opennms.plugins.elasticsearch.rest.bulk.FailedItem;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;
import org.opennms.plugins.elasticsearch.rest.template.TemplateInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;

import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import io.searchbox.core.UpdateByQuery;
import io.searchbox.core.UpdateByQueryResult;

/**
 * TODO: Review log levels.
 * TODO: Handle different prefixes i.e. many OpenNMS systems using the same ES
 */
public class AlarmsToES implements AlarmLifecycleListener, Runnable  {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmsToES.class);
    private static final Gson gson = new Gson();

    private final JestClient client;
    private final TemplateInitializer templateInitializer;
    private IndexStrategy indexStrategy = IndexStrategy.MONTHLY;
    private int bulkRetryCount = 3;
    private int batchSize = 200;
    private final String alarmIndexPrefix = "opennms-alarms";
    private boolean usePseudoClock = false;

    private final QueryProvider queryProvider = new QueryProvider();

    private final List<AlarmDocumentDTO> alarmDocumentsToIndex = new LinkedList<>();

    private Map<Integer, AlarmDocumentDTO> alarmDocumentsById = new LinkedHashMap<>();
    private final BlockingQueue<Task> taskQueue = new ArrayBlockingQueue<>(10000, true);
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setNameFormat("alarms-to-es")
            .build());
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private java.util.Timer timer;
    private final DocumentMapper documentMapper;

    private final Cache<Integer, Optional<NodeDocumentDTO>> nodeInfoCache;
    private final Histogram batchSizeHistogram;
    private final Timer batchTimer;

    public AlarmsToES(MetricRegistry metrics, JestClient client, TemplateInitializer templateInitializer) {
        this(metrics, client, templateInitializer, new CacheConfig("nodes-for-alarms-in-es"));
    }

    public AlarmsToES(MetricRegistry metrics, JestClient client, TemplateInitializer templateInitializer, CacheConfig nodeCacheConfig) {
        this.client = Objects.requireNonNull(client);
        this.templateInitializer = Objects.requireNonNull(templateInitializer);
        this.nodeInfoCache = new CacheBuilder<>()
                .withConfig(nodeCacheConfig)
                .withCacheLoader(new CacheLoader<Integer, Optional<NodeDocumentDTO>>() {
                    @Override
                    public Optional<NodeDocumentDTO> load(Integer nodeId) {
                        return Optional.empty();
                    }
                }).build();

        batchSizeHistogram = metrics.histogram("batch-size");
        batchTimer = metrics.timer("batch-timer");
        metrics.register("task-queue-size", (Gauge<Integer>) taskQueue::size);

        documentMapper = new DocumentMapper(nodeInfoCache, this::getCurrentTimeMillis);
    }

    public void init() {
        if (stopped.get()) {
            throw new IllegalStateException("Already destroyed.");
        }
        executor.execute(this);
        timer = new java.util.Timer("alarms-to-es");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    flushDocumentsToIndexToTaskQueue();
                } catch (Exception e) {
                    LOG.error("Flush failed.", e);
                }
            }
        }, 500, 500);
    }

    public void destroy() {
        stopped.set(true);
        timer.cancel();
        executor.shutdown();
    }

    private Collection<String> getIndicesForDeleteAt(long time) {
        // TODO: improve this!
        return Collections.singletonList(indexStrategy.getIndex(alarmIndexPrefix, Instant.ofEpochMilli(time)));
    }

    @Override
    public void run() {
        templateInitializer.initialize();
        while(!stopped.get()) {
            try {
                final Task task = taskQueue.take();
                task.visit(new TaskVisitor() {
                    @Override
                    public void indexAlarms(List<AlarmDocumentDTO> docs) {
                        LOG.error("Performing index task on {}", docs.stream().map(AlarmDocumentDTO::getId).collect(Collectors.toList()));
                        try {
                            bulkInsert(docs);
                        } catch (PersistenceException|IOException e) {
                            LOG.error("Persisting one or more documents failed.", e);
                        }
                    }

                    @Override
                    public void deleteAlarm(int alarmId, long time) {
                        LOG.error("Performing delete task on alarm with id {} at {}", alarmId, time);
                        final String query = queryProvider.markAlarmAsDeleted(alarmId, time);
                        final Collection<String> indices = getIndicesForDeleteAt(time);
                        final UpdateByQuery updateByQuery = new UpdateByQuery.Builder(query)
                                .addIndices(getIndicesForDeleteAt(time))
                                .setParameter("conflicts", "proceed")
                                .addType(AlarmDocumentDTO.TYPE)
                                .build();

                        try {
                            UpdateByQueryResult result = client.execute(updateByQuery);

                            if (!result.isSucceeded()) {
                                LOG.error("Update by query failed with: {}", result.getErrorMessage());
                            } else if (result.getUpdatedCount() < 1) {
                                LOG.warn("Did not find any documents for alarm with id: {} to delete in indices: {}.",
                                        alarmId, indices);
                            } else {
                                LOG.warn("Delete successful!");
                            }
                        } catch (IOException e) {
                            // TODO: Retries?
                            LOG.error("Marking alarm with id: {} as deleted for time: {} failed.", alarmId, time, e);
                        }
                    }

                    @Override
                    public void deleteAlarmsWithoutIdsIn(Set<Integer> alarmIdsToKeep, long time) {
                        LOG.error("Performing bulk delete task for alarms without ids in: {}, at time: {}", alarmIdsToKeep, time);
                        // TODO: Should only be applied to documents created before the given time
                        final String query = queryProvider.markOtherAlarmsAsDeleted(alarmIdsToKeep, time);
                        final UpdateByQuery updateByQuery = new UpdateByQuery.Builder(query)
                                .addIndices(getIndicesForDeleteAt(time))
                                .setParameter("conflicts", "proceed")
                                .addType(AlarmDocumentDTO.TYPE)
                                .build();

                        try {
                            UpdateByQueryResult result = client.execute(updateByQuery);
                            if (!result.isSucceeded()) {
                                LOG.error("Update by query failed with: {}", result.getErrorMessage());
                            } else {
                                LOG.error("Marked {} documents as deleted.", result.getUpdatedCount());
                            }
                        } catch (IOException e) {
                            // TODO: Retries? - use the LimitedRetriesRequestExecutor
                            LOG.error("Marking alarms without ids in: {} as deleted for time: {} failed.", alarmIdsToKeep, time, e);
                        }
                    }
                });
                LOG.warn("Task complete.");
            } catch (InterruptedException e) {
                LOG.info("Interrupted. Stopping.");
                return;
            } catch (Exception e) {
                LOG.error("Persisting one or more documents failed.", e);
            }
        }
    }

    public void bulkInsert(List<AlarmDocumentDTO> alarmDocuments) throws PersistenceException, IOException {
        final BulkRequest<AlarmDocumentDTO> bulkRequest = new BulkRequest<>(client, alarmDocuments, (documents) -> {
            final Bulk.Builder bulkBuilder = new Bulk.Builder();
            for (AlarmDocumentDTO alarmDocument : alarmDocuments) {
                final String index = indexStrategy.getIndex(alarmIndexPrefix, Instant.ofEpochMilli(alarmDocument.getUpdateTime()));
                final Index.Builder indexBuilder = new Index.Builder(alarmDocument)
                        .index(index)
                        .type(AlarmDocumentDTO.TYPE);
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Adding index action on index: {} with type: {} and payload: {}",
                            index, AlarmDocumentDTO.TYPE, gson.toJson(alarmDocument));
                }
                bulkBuilder.addAction(indexBuilder.build());
            }
            return new BulkWrapper(bulkBuilder);
        }, bulkRetryCount);

        try {
            // the bulk request considers retries
            bulkRequest.execute();
        } catch (BulkException ex) {
            final List<FailedItem<AlarmDocumentDTO>> failedItems;
            if (ex.getBulkResult() != null) {
                failedItems = ex.getBulkResult().getFailedItems();
            } else {
                failedItems = Collections.emptyList();
            }
            throw new PersistenceException(ex.getMessage(), failedItems);
        } catch (IOException ex) {
            LOG.error("An error occurred while executing the given request: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    // TODO: Remove synchronized safely?
    @Override
    public synchronized void handleAlarmSnapshot(List<OnmsAlarm> alarms) {
        LOG.error("Got snapshot with: {}", alarms);
        flushDocumentsToIndexToTaskQueue();
        // Index/update documents as necessary
        final List<AlarmDocumentDTO> alarmDocuments = alarms.stream()
                .map(this::getDocumentIfNeedsIndexing)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (alarmDocuments.size() > 0) {
            taskQueue.add(new IndexAlarmsTask(alarmDocuments));
        }

        // Bulk delete alarms that are not yet marked as deleted in ES, and are not present in the given list
        final Set<Integer> alarmIds = alarms.stream().map(OnmsAlarm::getId)
                .collect(Collectors.toSet());
        taskQueue.add(new BulkDeleteTask(alarmIds, getCurrentTimeMillis()));
        alarmDocumentsById.keySet().removeIf(alarmId -> !alarmIds.contains(alarmId));
    }

    @Override
    public synchronized void handleNewOrUpdatedAlarm(OnmsAlarm alarm) {
        LOG.error("Got new or updated with: {}", alarm);
        final AlarmDocumentDTO alarmDocument = getDocumentIfNeedsIndexing(alarm);
        if (alarmDocument != null) {
            alarmDocumentsToIndex.add(alarmDocument);
            if (alarmDocumentsToIndex.size() >= batchSize) {
                flushDocumentsToIndexToTaskQueue();
            }
        }
    }

    @Override
    public synchronized void handleDeletedAlarm(int alarmId, String reductionKey) {
        LOG.error("Got delete for: {}", alarmId);
        flushDocumentsToIndexToTaskQueue();
        taskQueue.add(new DeleteAlarmTask(alarmId, getCurrentTimeMillis()));
        alarmDocumentsById.remove(alarmId);
    }

    private synchronized void flushDocumentsToIndexToTaskQueue() {
        if (alarmDocumentsToIndex.size() > 0) {
            taskQueue.add(new IndexAlarmsTask(new ArrayList<>(alarmDocumentsToIndex)));
            alarmDocumentsToIndex.clear();
        }
    }

    private AlarmDocumentDTO getDocumentIfNeedsIndexing(OnmsAlarm alarm) {
        final AlarmDocumentDTO existingAlarmDocument = alarmDocumentsById.get(alarm.getId());

        // These conditions could be combined in a single mega-conditiational
        // but I find it easier to follow if kept separated
        boolean needsIndexing = false;
        if (existingAlarmDocument == null) {
            needsIndexing = true;
        } else if (getCurrentTimeMillis() - existingAlarmDocument.getUpdateTime() >= 5*60*1000) {
            // TODO: Move to constant
            needsIndexing = true;
        } else if (!Objects.equals(existingAlarmDocument.getReductionKey(), alarm.getReductionKey())
                || !Objects.equals(existingAlarmDocument.getAckTime(), alarm.getAckTime() != null ? alarm.getAckTime().getTime() : null)
                || !Objects.equals(existingAlarmDocument.getSeverityId(), alarm.getSeverity().getId())
                || !Objects.equals(new HashSet<>(existingAlarmDocument.getRelatedAlarmIds()), alarm.getRelatedAlarmIds())) {
            // TODO: Update list of "interesting" fields
            // sticky + journal memos
            needsIndexing = true;
        }

        if (!needsIndexing) {
            return null;
        }

        final AlarmDocumentDTO doc = documentMapper.toDocument(alarm);
        alarmDocumentsById.put(alarm.getId(), doc);
        return doc;
    }

    private long getCurrentTimeMillis() {
        if (usePseudoClock) {
            return PseudoClock.getInstance().getTime();
        } else {
            return System.currentTimeMillis();
        }
    }

    public void setBulkRetryCount(int bulkRetryCount) {
        this.bulkRetryCount = bulkRetryCount;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setIndexStrategy(IndexStrategy indexStrategy) {
        this.indexStrategy = indexStrategy;
    }

    public void setUsePseudoClock(boolean usePseudoClock) {
        this.usePseudoClock = usePseudoClock;
    }


}
