/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.deviceconfig.monitors;

import static java.util.stream.Collectors.toMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Strings;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.features.deviceconfig.persistence.api.ConfigType;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.retrieval.api.Retriever;
import org.opennms.features.deviceconfig.service.DeviceConfigConstants;
import org.opennms.netmgt.poller.DeviceConfig;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConfigMonitor extends AbstractServiceMonitor {

    public static final String SCRIPT = "script";
    public static final String USERNAME = "username";
    public static final String SSH_PORT = "ssh-port";
    public static final String SSH_TIMEOUT = "ssh-timeout";
    public static final String PASSWORD = "password";
    public static final String LAST_RETRIEVAL = "lastRetrieval";
    public static final String FAILURE_REASON = "failureReason";
    public static final String SCRIPT_FILE = "script-file";

    private static final Logger LOG = LoggerFactory.getLogger(DeviceConfigMonitor.class);
    private Retriever retriever;
    private static final Duration DEFAULT_DURATION = Duration.ofMinutes(1); // 60sec
    private static final int DEFAULT_SSH_PORT = 22;

    private IpInterfaceDao ipInterfaceDao;
    private DeviceConfigDao deviceConfigDao;

    @Override
    public Map<String, Object> getRuntimeAttributes(final MonitoredService svc, final Map<String, Object> parameters) {
        if (ipInterfaceDao == null) {
            ipInterfaceDao = BeanUtils.getBean("daoContext", "ipInterfaceDao", IpInterfaceDao.class);
        }

        if (deviceConfigDao == null) {
            deviceConfigDao = BeanUtils.getBean("daoContext", "deviceConfigDao", DeviceConfigDao.class);
        }

        final Map<String, Object> params = new HashMap<>();
        final OnmsIpInterface ipInterface = ipInterfaceDao.findByNodeIdAndIpAddress(svc.getNodeId(), svc.getIpAddr());
        
        final var deviceConfigOptional = deviceConfigDao.getLatestConfigForInterface(ipInterface, svc.getSvcName());
        deviceConfigOptional.ifPresent(deviceConfig -> params.put(LAST_RETRIEVAL, String.valueOf(deviceConfig.getLastUpdated().getTime())));
        deviceConfigOptional.ifPresent(deviceConfig -> params.put(FAILURE_REASON, deviceConfig.getFailureReason()));
        
        final String scriptFile = getKeyedString(parameters, SCRIPT_FILE, null);
        try {
            if (scriptFile != null) {
                String script = parseScriptFile(scriptFile);
                params.put(SCRIPT, script);
            }
        } catch (Exception e) {
            LOG.error("Error while parsing script file {}", scriptFile, e);
            throw new RuntimeException(e);
        }
        return params;
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
        if (retriever == null) {
            retriever = BeanUtils.getBean("daoContext", "deviceConfigRetriever", Retriever.class);
        }

        boolean triggeredPoll = Boolean.parseBoolean(getKeyedString(parameters, DeviceConfigConstants.TRIGGERED_POLL, "false"));

        final Date lastRun = new Date(getKeyedLong(parameters, LAST_RETRIEVAL, 0L));
        final String cronSchedule = getKeyedString(parameters, DeviceConfigConstants.SCHEDULE, DeviceConfigConstants.DEFAULT_CRON_SCHEDULE);
        final Date nextRun = getNextRunDate(cronSchedule, lastRun);

        if (!triggeredPoll && !nextRun.before(new Date())) {
            // Send previous status back based on failure reason.
            String failureReason = getKeyedString(parameters, FAILURE_REASON, null);
            if (Strings.isNullOrEmpty(failureReason)) {
                return PollStatus.up();
            } else {
                return PollStatus.down(failureReason);
            }
        }

        String script = getObjectAsStringFromParams(parameters, SCRIPT);
        String user = getObjectAsStringFromParams(parameters, USERNAME);
        String password = getObjectAsStringFromParams(parameters, PASSWORD);
        Integer port = getKeyedInteger(parameters, SSH_PORT, DEFAULT_SSH_PORT);
        String configType = getKeyedString(parameters, DeviceConfigConstants.CONFIG_TYPE, ConfigType.Default);
        Long timeout = getKeyedLong(parameters, SSH_TIMEOUT, DEFAULT_DURATION.toMillis());
        var host = svc.getIpAddr();
        var stringParameters = parameters.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
        var future = retriever.retrieveConfig(
                Retriever.Protocol.TFTP,
                script,
                user,
                password,
                host,
                port,
                configType,
                stringParameters,
                Duration.ofMillis(timeout)
        ).thenApply(either ->
                either.fold(
                        failure -> {
                            var reason = "Device config retrieval could not be triggered - host: " + host + ";  message: " + failure.message
                                         + "\nstdout: " + failure.stdout
                                         + "\nstderr: " + failure.stderr;
                            LOG.error(reason);
                            final var pollStatus = PollStatus.unavailable(reason);
                            pollStatus.setDeviceConfig(new DeviceConfig());
                            return pollStatus;
                        },
                        success -> {
                            LOG.debug("Retrieved device configuration - host: " + host);
                            var pollStatus = PollStatus.up();
                            pollStatus.setDeviceConfig(new DeviceConfig(success.config, success.filename));
                            return pollStatus;
                        }
                )
        );

        try {
            return future.toCompletableFuture().get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOG.error("Device config retrieval failed - host: " + host, e);
            return PollStatus.unavailable("Device config retrieval failed - host: " + host + "; message: " + e.getMessage());
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

    private Date getNextRunDate(String cronSchedule, Date lastRun) {
        final Trigger trigger = TriggerBuilder.newTrigger()
            .withSchedule(CronScheduleBuilder.cronSchedule(cronSchedule))
            .startAt(lastRun)
            .build();

        return trigger.getFireTimeAfter(lastRun);
    }

    private String getObjectAsStringFromParams(Map<String, Object> params, String key) {
        Object obj = params.get(key);
        if (obj instanceof String) {
            return (String) obj;
        }
        throw new IllegalArgumentException(key + " is not an instance of String");
    }
}
