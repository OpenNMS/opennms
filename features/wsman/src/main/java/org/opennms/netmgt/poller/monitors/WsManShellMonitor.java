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
 * <p>The endpoint details come from wsman-config.xml, resolved on the core in
 * {@link #getRuntimeAttributes} so the poll itself can run on a Minion. The
 * {@code timeout} and {@code retry} values from wsman-config.xml serve as defaults
 * that a service definition can override, as with snmp-config.xml.
 *
 * <p>The {@code command} and {@code args} parameters are deliberately excluded from
 * placeholder substitution: they are passed to the Windows command interpreter
 * verbatim, so substituting node-controlled text such as the node label into them
 * would let whoever controls that text run commands on the host. The {@code banner}
 * parameter, which is only ever matched against output, does support substitution.
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

    static final String RETRY_KEY = "retry";
    static final String TIMEOUT_KEY = "timeout";

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

        // wsman-config.xml provides the defaults; the service definition wins when it sets its own
        if (!parameters.containsKey(RETRY_KEY) && config.getRetry() != null) {
            runtimeAttributes.put(RETRY_KEY, config.getRetry());
        }
        if (!parameters.containsKey(TIMEOUT_KEY) && config.getTimeout() != null) {
            runtimeAttributes.put(TIMEOUT_KEY, config.getTimeout());
        }
        return runtimeAttributes;
    }

    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        // command and args intentionally bypass placeholder substitution, see the class comment
        final String command = ParameterMap.getKeyedString(parameters, COMMAND_PARAM, null);
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("'" + COMMAND_PARAM + "' parameter is required.");
        }
        final String[] args = ShellCommandUtils.toArguments(ParameterMap.getKeyedString(parameters, ARGS_PARAM, null));
        final String banner = resolveKeyedString(parameters, BANNER_PARAM, null);
        final String expectedExitCode = ParameterMap.getKeyedString(parameters, EXIT_CODE_PARAM, DEFAULT_EXIT_CODE);
        final ShellOptions shellOptions = ShellCommandUtils.buildShellOptions(
                ParameterMap.getKeyedBoolean(parameters, NO_PROFILE_PARAM, true),
                parseCodepage(ParameterMap.getKeyedString(parameters, CODEPAGE_PARAM, null)),
                ParameterMap.getKeyedString(parameters, WORKING_DIRECTORY_PARAM, null));

        final TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);
        final int timeoutMillis = (int) tracker.getTimeoutInMillis();

        final WSManEndpoint endpoint;
        try {
            final Map<String, String> filteredMap = parameters.entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().toString()));
            // The timeout bounds every exchange with the host (connect, shell creation, output,
            // shell deletion) as well as the wait for the command itself
            endpoint = WsmanEndpointUtils.withTimeouts(WsmanEndpointUtils.fromMap(filteredMap), timeoutMillis);
        } catch (MalformedURLException e) {
            return PollStatus.down(e.getMessage());
        }
        final Duration commandTimeout = Duration.ofMillis(timeoutMillis);

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
        } catch (WSManException e) {
            return PollStatus.unavailable(String.format("Could not create WS-Man client for %s: %s", svc.getIpAddr(), e.getMessage()));
        }
        return status;
    }

    private static Integer parseCodepage(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            final int codepage = Integer.parseInt(value.trim());
            if (codepage <= 0) {
                throw new IllegalArgumentException("'" + CODEPAGE_PARAM + "' must be a positive code page number, got: " + value);
            }
            return codepage;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + CODEPAGE_PARAM + "' must be a positive code page number, got: " + value);
        }
    }

    public void setWSManConfigDao(WSManConfigDao wsManConfigDao) {
        m_wsManConfigDao = Objects.requireNonNull(wsManConfigDao);
    }

    public void setWSManClientFactory(WSManClientFactory factory) {
        m_factory = Objects.requireNonNull(factory);
    }

    /**
     * Releases any clients the factory is holding on to. Called by the blueprint
     * container when the bundle stops.
     */
    public void destroy() {
        if (m_factory instanceof AutoCloseable) {
            try {
                ((AutoCloseable) m_factory).close();
            } catch (Exception e) {
                LOG.debug("Error closing WS-Man client factory", e);
            }
        }
    }
}
