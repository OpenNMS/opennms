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
import org.opennms.core.backup.client.BackupRpcClient;
import org.opennms.features.backup.api.BackupStrategy;
import org.opennms.features.backup.api.Config;
import org.opennms.features.backup.service.BackupNetworkDeviceJob;
import org.opennms.features.backup.service.api.NetworkDeviceBackupManager;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

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
    private BackupRpcClient backupRpcClient;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private JsonStore jsonStore;

    @Override
    public List<Integer> getBackupedNodeIds() {
        //TODO: it is kind of dangerous if jsonStore have a lot of data, suggest create a key only function in kv store
        Map<String, String> results = jsonStore.enumerateContext(CONTEXT);
        List<Integer> nodeIds = new ArrayList<>();
        Pattern latestPattern = Pattern.compile(":" + LATEST + "$");
        for (String s : results.keySet()) {
            if (s == null || s.indexOf(LATEST) == -1) {
                continue;
            }
            String id = latestPattern.matcher(s).replaceAll("");
            try {
                nodeIds.add(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                LOG.warn("Invalid id found in kv store. key = {}", s);
            }
        }
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
            Optional<String> config = jsonStore.get(this.generateKey(nodeId, version), CONTEXT);
            if (config.isEmpty()) {
                LOG.warn("nodeId: {} do not have ver: {} config stored.", nodeId, version);
                return null;
            }
            return Optional.of(mapper.readValue(config.get(), Config.class));
        }
    }

    private JSONObject getLatestConfigJson(int nodeId) {
        Optional<String> config = jsonStore.get(this.generateKey(nodeId, LATEST), CONTEXT);
        if (config.isEmpty()) {
            LOG.warn("nodeId: {} do not have any config stored.", nodeId);
            return null;
        }
        return new JSONObject(config.get());
    }

    @Override
    public void saveConfig(int nodeId, Config config) throws Exception {
        JSONObject latest = this.getLatestConfigJson(nodeId);
        List<String> keys;
        if (latest != null) {
            Config latestConfig = mapper.convertValue(latest.get(CONFIG_TAG), Config.class);
            String version = dateFormatter.format(latestConfig.getRetrievedAt());
            this.archiveConfig(nodeId, version, latestConfig);
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
        jsonStore.put(this.generateKey(nodeId, LATEST), mapper.writeValueAsString(config), CONTEXT);
    }

    private void archiveConfig(int nodeId, String version, Config config) throws JsonProcessingException {
        jsonStore.put(this.generateKey(nodeId, version), mapper.writeValueAsString(config), CONTEXT);
    }

    private String generateKey(int nodeId, String version) {
        return nodeId + ":" + version;
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
            CronTriggerFactoryBean cronReportTrigger = new CronTriggerFactoryBean();
            cronReportTrigger.setJobDetail(jobDetail);
            cronReportTrigger.setCronExpression("*/1 * * * *");

            scheduler.scheduleJob(jobDetail, cronReportTrigger.getObject());
            LOG.debug("Schedule report {}", cronReportTrigger);
        }
    }

    private JobDataMap prepareJobDataMap(Map<String, Object> params) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("nodeDao", nodeDao);
        jobDataMap.put("backupRpcClient", backupRpcClient);
        jobDataMap.put("networkDeviceBackupManager", this);

        params.forEach((k, v) -> {
            jobDataMap.put(k, v);
        });
        return jobDataMap;
    }
}