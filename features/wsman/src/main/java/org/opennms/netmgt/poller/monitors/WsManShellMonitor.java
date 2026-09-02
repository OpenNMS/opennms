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
package org.opennms.netmgt.poller.monitors;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.core.wsman.shell.CommandResult;
import org.opennms.core.wsman.shell.ShellOptions;
import org.opennms.core.wsman.utils.CachingWSManClientFactory;
import org.opennms.core.wsman.utils.ShellCommandUtils;
import org.opennms.netmgt.config.wsman.credentials.WsmanAgentConfig;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.monitors.support.ParameterSubstitutingMonitor;
import org.opennms.netmgt.provision.detector.wsman.WsmanEndpointUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WS-Man Shell Monitor
 *
 * Runs a command on the remote host through WinRS (MS-WSMV) and marks the service
 * up when its output matches the configured banner and, if an exit code is
 * configured, the command exits with that code.
 *
 * The endpoint details come from wsman-config.xml, resolved on the core in
 * {@link #getRuntimeAttributes} so the poll itself can run on a Minion.
 */
public class WsManShellMonitor extends ParameterSubstitutingMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(WsManShellMonitor.class);

    public static final String COMMAND_PARAM = "command";
    public static final String ARGS_PARAM = "args";
    public static final String BANNER_PARAM = "banner";
    public static final String EXIT_CODE_PARAM = "exit-code";
    public static final String NO_PROFILE_PARAM = "no-profile";
    public static final String CODEPAGE_PARAM = "codepage";
    public static final String WORKING_DIRECTORY_PARAM = "working-directory";

    private static final String RETRY_KEY = "retry";

    static final int DEFAULT_TIMEOUT = 3000;
    static final int DEFAULT_RETRY = 0;
    /** By default the exit code is not checked; set exit-code to require a specific value. */
    static final String DEFAULT_EXIT_CODE = ShellCommandUtils.EXIT_CODE_ANY;

    private WSManClientFactory m_factory = new CachingWSManClientFactory();

    private WSManConfigDao m_wsManConfigDao;

    @Override
    public Map<String, Object> getRuntimeAttributes(MonitoredService svc, Map<String, Object> parameters) {
        final Map<String, Object> runtimeAttributes = super.getRuntimeAttributes(svc, parameters);

        if (m_wsManConfigDao == null) {
            m_wsManConfigDao = BeanUtils.getBean("daoContext", "wsManConfigDao", WSManConfigDao.class);
        }

        final WsmanAgentConfig config = m_wsManConfigDao.getAgentConfig(svc.getAddress());
        WsmanEndpointUtils.toMap(WSManConfigDao.getEndpoint(config, svc.getAddress()))
                .forEach((key, value) -> runtimeAttributes.put(key, Interpolator.pleaseInterpolate(value)));

        // The service definition's retry wins over the wsman-config default
        if (!parameters.containsKey(RETRY_KEY) && config.getRetry() != null) {
            runtimeAttributes.put(RETRY_KEY, config.getRetry());
        }
        return runtimeAttributes;
    }

    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        final String command = resolveKeyedString(parameters, COMMAND_PARAM, null);
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("'" + COMMAND_PARAM + "' parameter is required.");
        }
        final String[] args = ShellCommandUtils.toArguments(resolveKeyedString(parameters, ARGS_PARAM, null));
        final String banner = resolveKeyedString(parameters, BANNER_PARAM, null);
        final String expectedExitCode = ParameterMap.getKeyedString(parameters, EXIT_CODE_PARAM, DEFAULT_EXIT_CODE);
        final ShellOptions shellOptions = ShellCommandUtils.buildShellOptions(
                ParameterMap.getKeyedBoolean(parameters, NO_PROFILE_PARAM, true),
                parameters.get(CODEPAGE_PARAM) != null ? ParameterMap.getKeyedInteger(parameters, CODEPAGE_PARAM, 0) : null,
                ParameterMap.getKeyedString(parameters, WORKING_DIRECTORY_PARAM, null));

        final WSManEndpoint endpoint;
        try {
            final Map<String, String> filteredMap = parameters.entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().toString()));
            endpoint = WsmanEndpointUtils.fromMap(filteredMap);
        } catch (MalformedURLException e) {
            return PollStatus.down(e.getMessage());
        }

        final TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);
        final Duration commandTimeout = Duration.ofMillis(tracker.getTimeoutInMillis());

        PollStatus status = PollStatus.unavailable();
        try (WSManClient client = m_factory.getClient(endpoint)) {
            for (tracker.reset(); tracker.shouldRetry() && !status.isAvailable(); tracker.nextAttempt()) {
                tracker.startAttempt();
                try {
                    LOG.debug("Running '{}' with arguments {} on {}", command, args, svc.getIpAddr());
                    final CommandResult result = client.runCommand(command, args, commandTimeout, shellOptions);
                    final double responseTime = tracker.elapsedTimeInMillis();
                    LOG.debug("Command '{}' on {} exited with {}: stdout='{}' stderr='{}'", command, svc.getIpAddr(),
                            result.exitCode(), ShellCommandUtils.excerpt(result.stdout()), ShellCommandUtils.excerpt(result.stderr()));

                    final Optional<String> failure = ShellCommandUtils.checkResult(result, expectedExitCode, banner);
                    status = failure.isPresent() ? PollStatus.unavailable(failure.get()) : PollStatus.available(responseTime);
                } catch (WSManException e) {
                    LOG.debug("Command '{}' failed on {}", command, svc.getIpAddr(), e);
                    status = PollStatus.unavailable(String.format("Running '%s' failed: %s", command, e.getMessage()));
                }
            }
        }
        return status;
    }

    public void setWSManConfigDao(WSManConfigDao wsManConfigDao) {
        m_wsManConfigDao = Objects.requireNonNull(wsManConfigDao);
    }

    public void setWSManClientFactory(WSManClientFactory factory) {
        m_factory = Objects.requireNonNull(factory);
    }
}
