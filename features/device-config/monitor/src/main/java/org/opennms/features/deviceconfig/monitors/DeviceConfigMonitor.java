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
package org.opennms.features.deviceconfig.monitors;

import static java.util.stream.Collectors.toMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.features.deviceconfig.persistence.api.ConfigType;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.retrieval.api.Retriever;
import org.opennms.features.deviceconfig.retrieval.api.Retriever.Protocol;
import org.opennms.features.deviceconfig.service.DeviceConfigConstants;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.poller.DeviceConfig;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class DeviceConfigMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceConfigMonitor.class);

    public static final String SCRIPT = "script";
    public static final String USERNAME = "username";
    public static final String SSH_PORT = "ssh-port";
    public static final String SSH_TIMEOUT = "ssh-timeout";
    public static final String PASSWORD = "password";
    public static final String AUTH_KEY = "auth-key";
    public static final String HOST_KEY = "host-key";
    public static final String LAST_RETRIEVAL = "lastRetrieval";
    public static final String SCRIPT_FILE = "script-file";
    private static final String SCRIPT_ERROR = "script-error";
    public static final String SHELL = "shell";

    private static final Duration DEFAULT_DURATION = Duration.ofMinutes(1); // 60sec
    private static final int DEFAULT_SSH_PORT = 22;

    private Retriever retriever;

    private IpInterfaceDao ipInterfaceDao;
    private DeviceConfigDao deviceConfigDao;
    private SessionUtils sessionUtils;

    @Override
    public Map<String, Object> getRuntimeAttributes(final MonitoredService svc, final Map<String, Object> parameters) {

        return sessionUtils.withReadOnlyTransaction(() -> {
            final Map<String, Object> params = new HashMap<>();
            final OnmsIpInterface ipInterface = ipInterfaceDao.findByNodeIdAndIpAddress(svc.getNodeId(), svc.getIpAddr());

            // Publish time of last backup to poller execution.
            // Use creation time of node as fallback to ensure first run scheduled correctly.
            params.put(LAST_RETRIEVAL, Long.toString(this.deviceConfigDao.getLatestConfigForInterface(ipInterface, svc.getSvcName())
                                                                         .map(org.opennms.features.deviceconfig.persistence.api.DeviceConfig::getLastUpdated)
                                                                         .orElse(ipInterface.getNode().getCreateTime())
                                                                         .getTime()));

            final String scriptFile = getKeyedString(parameters, SCRIPT_FILE, null);
            try {
                if (scriptFile != null) {
                    String script = parseScriptFile(scriptFile);
                    params.put(SCRIPT, script);
                }
            } catch (Exception e) {
                LOG.error("Error while parsing script file {}", scriptFile, e);
                // Don't fail fast here, we want to create empty backup entry in DB, just cache the error.
                params.put(SCRIPT_ERROR, e.getMessage());
            }

            return params;
        });
    }

    private static String parseScriptFile(String fileName) throws IOException {
        String opennmsHome = ConfigFileConstants.getHome();
        Path script = Paths.get(opennmsHome, "etc", "device-config", fileName);
        if (script.toFile().exists()) {
            return Files.readString(script);
        } else {
            throw new FileNotFoundException("Couldn't find file " + fileName + " in etc/device-config folder");
        }
    }

    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {

        boolean triggeredPoll = Boolean.parseBoolean(getKeyedString(parameters, DeviceConfigConstants.TRIGGERED_POLL, "false"));

        final Date lastRun = new Date(getKeyedLong(parameters, LAST_RETRIEVAL, 0L));
        final String cronSchedule = getKeyedString(parameters, DeviceConfigConstants.SCHEDULE, DeviceConfigConstants.DEFAULT_CRON_SCHEDULE);

        if (!triggeredPoll) {
            if (Strings.isNullOrEmpty(cronSchedule) || DeviceConfigConstants.NEVER.equalsIgnoreCase(cronSchedule)) {
                return PollStatus.unknown("Not scheduled");
            }

            try {
                final Date nextRun = getNextRunDate(cronSchedule, lastRun);
                if (!nextRun.before(new Date())) {
                    return PollStatus.unknown("Skipping. Next retrieval scheduled for " + nextRun);
                }
            } catch (Exception e) {
                LOG.error("Exception in parsing cron expression {}", cronSchedule, e);
                return PollStatus.down("Invalid cron expression : " + cronSchedule);
            }

        }

        if (parameters.containsKey(SCRIPT_ERROR)) {
            String reason = getObjectAsStringFromParams(parameters, SCRIPT_ERROR);
            var status = PollStatus.unavailable(reason);
            status.setDeviceConfig(new DeviceConfig());
            return status;
        }
        parameters.put(DeviceConfigConstants.PARM_DEVICE_CONFIG_BACKUP_START_TIME, System.currentTimeMillis());
        parameters.put(DeviceConfigConstants.PARM_DEVICE_CONFIG_BACKUP_DATA_PROTOCOL, Protocol.TFTP);
        
        String script = getObjectAsStringFromParams(parameters, SCRIPT);
        String user = getObjectAsStringFromParams(parameters, USERNAME);
        String password = getObjectAsStringFromParams(parameters, PASSWORD);
        String authKey = getObjectAsStringFromParams(parameters, AUTH_KEY);
        Integer port = getKeyedInteger(parameters, SSH_PORT, DEFAULT_SSH_PORT);
        String configType = getKeyedString(parameters, DeviceConfigConstants.CONFIG_TYPE, ConfigType.Default);
        Long timeout = getKeyedLong(parameters, SSH_TIMEOUT, DEFAULT_DURATION.toMillis());
        String hostKeyFingerprint = getKeyedString(parameters, HOST_KEY, null);
        var stringParameters = parameters.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
        String shell = getKeyedString(parameters, SHELL, null);

        final var target = new InetSocketAddress(svc.getAddress(), port);

        var future = retriever.retrieveConfig(
                Retriever.Protocol.TFTP,
                script,
                user,
                password,
                authKey,
                target,
                hostKeyFingerprint,
                shell,
                configType,
                stringParameters,
                Duration.ofMillis(timeout).minusSeconds(1)
        ).thenApply(either ->
                either.fold(
                        failure -> {
                            LOG.error("Device config retrieval could not be triggered - target: {}; script: {};  message: {} \nstdout: {}\nstderr: {}", target, script, failure.message, failure.stdout, failure.stderr);
                            final var pollStatus = PollStatus.unavailable(failure.message);
                            pollStatus.setDeviceConfig(new DeviceConfig(failure.scriptOutput));
                            return pollStatus;
                        },
                        success -> {
                            LOG.debug("Retrieved device configuration - target: " + target);
                            var pollStatus = PollStatus.up();
                            pollStatus.setDeviceConfig(new DeviceConfig(success.config, success.filename, success.scriptOutput));
                            return pollStatus;
                        }
                )
        );

        try {
            LOG.debug("Starting retrieval, waiting at most {} milliseconds", timeout);
            return future.toCompletableFuture().get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOG.error("Device config retrieval failed - target: " + target, e);
            final var pollStatus = PollStatus.unavailable("Device config retrieval failed - target: " + target + "; message: " + e.getMessage());
            pollStatus.setDeviceConfig(new DeviceConfig());
            return pollStatus;
        }
    }

    public void setRetriever(Retriever retriever) {
        this.retriever = retriever;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        this.ipInterfaceDao = ipInterfaceDao;
    }

    public void setDeviceConfigDao(DeviceConfigDao deviceConfigDao) {
        this.deviceConfigDao = deviceConfigDao;
    }

    public void setSessionUtils(final SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }

    private Date getNextRunDate(String cronSchedule, Date lastRun) {
        final Trigger trigger = TriggerBuilder.newTrigger()
            .withSchedule(CronScheduleBuilder.cronSchedule(cronSchedule))
            .startAt(lastRun)
            .build();

        return trigger.getFireTimeAfter(lastRun);
    }

    private String getObjectAsStringFromParams(Map<String, Object> params, String key) {
        Object obj = params.get(key);
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        throw new IllegalArgumentException(key + " is not an instance of String");
    }
}
