/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.backup.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.opennms.features.backup.LocationAwareBackupClient;
import org.opennms.features.backup.api.Config;
import org.opennms.features.backup.service.BackupNetworkDeviceJob;
import org.opennms.features.backup.service.api.NetworkDeviceBackupManager;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsNode;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@EventListener(name = "NetworkDeviceBackupManager", logPrefix = "backupd")
public class NetworkDeviceBackupManagerImpl implements NetworkDeviceBackupManager, SpringServiceDaemon {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkDeviceBackupManagerImpl.class);

    public static final String JOB_IDENTITY = "backup";
    public static final String JOB_GROUP = "backup";

    public static final String CONTEXT = "backup";
    public static final String LATEST = "latest";

    // only use for latest data
    public static final String KEYS_TAG = "KEYS";
    public static final String CONFIG_TAG = "CONFIG";

    private final ObjectMapper mapper = new ObjectMapper();
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private LocationAwareBackupClient backupRpcClient;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private SessionUtils sessionUtils;

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

//    @Autowired
//    private JsonStore jsonStore;

    @Override
    public List<Integer> getBackupedNodeIds() {
        // it is kind of dangerous if jsonStore have a lot of data, suggest create a key only function in kv store
        // Map<String, String> results = jsonStore.enumerateContext(CONTEXT);
        List<OnmsNode> nodes = nodeDao.findNodeWithMetaData(CONTEXT, LATEST, null);
        List<Integer> nodeIds = nodes.stream().collect(Collectors.mapping(OnmsNode::getId, Collectors.toList()));
        return nodeIds;
    }

    @Override
    public List<String> getConfigs(int nodeId) {
        JSONObject jsonObject = this.getLatestConfigJson(nodeId);
        Object tmp = jsonObject.get(KEYS_TAG);
        if (tmp instanceof ArrayList) {
            return (List) tmp;
        } else {
            LOG.warn("nodeId: {} contain invalid configIds. value = {}", nodeId, tmp);
            return new ArrayList<>(0);
        }
    }

    @Override
    public Optional<Config> getConfig(int nodeId) throws JsonProcessingException {
        return this.getConfig(nodeId, LATEST);
    }

    @Override
    public Optional<Config> getConfig(int nodeId, String version) throws JsonProcessingException {
        if (LATEST.equals(version)) {
            JSONObject json = this.getLatestConfigJson(nodeId);
            return Optional.of(mapper.readValue((String) json.get(CONFIG_TAG), Config.class));
        } else {
            //Optional<String> config = jsonStore.get(this.generateKey(nodeId, version), CONTEXT);
            Stream<OnmsMetaData> metas = nodeDao.get(nodeId).getMetaData().stream().filter(
                    meta -> CONTEXT.equals(meta.getContext()) && version.equals(meta.getKey()));
            if (metas.count() != 1) {
                LOG.warn("nodeId: {} do not have ver: {} config stored. metas: {}", nodeId, version, metas);
                return null;
            }
            return Optional.of(mapper.readValue(metas.findFirst().get().getValue(), Config.class));
        }
    }

    private JSONObject getLatestConfigJson(int nodeId) {
        //Optional<String> config = jsonStore.get(this.generateKey(nodeId, LATEST), CONTEXT);
        OnmsNode node = nodeDao.get(nodeId);
        Stream<OnmsMetaData> metas = node.getMetaData().stream().filter(data -> CONTEXT.equals(data.getContext()) && LATEST.equals(data.getKey()));
        if (metas.count() != 1) {
            LOG.warn("nodeId: {} do not have invalid config stored. meta: {}", nodeId, metas);
            return null;
        }
        return new JSONObject(metas.findFirst().get().getValue());
    }



    @Override
    public void saveConfig(int nodeId, Config config) throws Exception {
        JSONObject latest = this.getLatestConfigJson(nodeId);
        List<String> keys;
        if (latest != null) {
            Config latestConfig = mapper.convertValue(latest.get(CONFIG_TAG), Config.class);
            String version = dateFormatter.format(latestConfig.getRetrievedAt());
            this.actualWriteConfig(nodeId, version, mapper.writeValueAsString(latestConfig));
            Object tmp = latest.get(KEYS_TAG);
            if (tmp instanceof ArrayList) {
                keys = (List) tmp;
            } else {
                keys = new ArrayList<>(1);
            }
            keys.add(version);
        } else {
            keys = new ArrayList<>(1);
        }

        JSONObject json = new JSONObject();
        json.put(CONFIG_TAG, config);
        json.put(KEYS_TAG, keys);
        this.actualWriteConfig(nodeId, LATEST, json.toString());
//        jsonStore.put(this.generateKey(nodeId, LATEST), mapper.writeValueAsString(config), CONTEXT);
    }

    private void actualWriteConfig(int nodeId, String version, String configStr) throws JsonProcessingException {
        sessionUtils.withTransaction(() -> {
            OnmsNode node = nodeDao.get(nodeId);
            node.addMetaData(CONTEXT, this.generateKey(nodeId, version), configStr);
            nodeDao.save(node);
        });
        //jsonStore.put(this.generateKey(nodeId, version), mapper.writeValueAsString(config), CONTEXT);
    }

    private String generateKey(int nodeId, String version) {
        //return nodeId + ":" + version;
        return version;
    }

    @Override
    public void backup(int nodeId) throws Exception {
        synchronized (scheduler) {
            Map<String, Object> params = new HashMap<>(1);
            params.put("nodeId", nodeId);
            prepareJobDataMap(params);
            scheduler.triggerJob(new JobKey(JOB_IDENTITY, JOB_GROUP));
        }
    }

    @Override
    public void backup() throws Exception {
        synchronized (scheduler) {
            scheduler.triggerJob(new JobKey(JOB_IDENTITY, JOB_GROUP));
        }
    }

    @Override
    public LocationAwareBackupClient getClient() {
        return backupRpcClient;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void destroy() throws Exception {
        LOG.debug("start: acquiring lock...");
        synchronized (scheduler) {
            scheduler.deleteJob(new JobKey(JOB_IDENTITY, JOB_GROUP));
        }
        LOG.debug("start: lock released (unless reentrant).");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOG.debug("start: acquiring lock...");
        synchronized (scheduler) {
            LOG.info("start: lock acquired (may have reentered), scheduling Reports...");
            this.scheduleBackup();
            LOG.info("start: {} jobs scheduled.", scheduler.getJobKeys(GroupMatcher.groupEquals(JOB_GROUP)).size());
        }
        LOG.debug("start: lock released (unless reentrant).");
    }

    private void scheduleBackup() throws Exception {
        synchronized (scheduler) {
            JobDataMap jobDataMap = this.prepareJobDataMap(null);
            JobDetail jobDetail = JobBuilder.newJob(BackupNetworkDeviceJob.class)
                    .withIdentity(JOB_IDENTITY, JOB_GROUP).setJobData(jobDataMap).build();

            final Trigger trigger = TriggerBuilder
                    .newTrigger().withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?"))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            LOG.debug("Schedule job {} trigger {}", jobDetail, trigger);
        }
    }

    private JobDataMap prepareJobDataMap(Map<String, Object> params) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("nodeDao", nodeDao);
        jobDataMap.put("backupRpcClient", backupRpcClient);
        jobDataMap.put("networkDeviceBackupManager", this);
        jobDataMap.put("sessionUtils", sessionUtils);

        if (params != null) {
            params.forEach((k, v) -> {
                jobDataMap.put(k, v);
            });
        }
        return jobDataMap;
    }
}