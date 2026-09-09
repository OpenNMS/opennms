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
package org.opennms.netmgt.provision.detector.wsman;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.Optional;

import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.core.wsman.shell.CommandResult;
import org.opennms.core.wsman.utils.ShellCommandUtils;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.DetectResults;
import org.opennms.netmgt.provision.support.DetectResultsImpl;
import org.opennms.netmgt.provision.support.SyncAbstractDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects a service by running a command on the remote host through WinRS
 * (MS-WSMV) and checking its output and, optionally, its exit code.
 *
 * @author dino2gnt
 */
public class WsManShellDetector extends SyncAbstractDetector {
    public static final Logger LOG = LoggerFactory.getLogger(WsManShellDetector.class);

    private static final String PROTOCOL_NAME = "WsManShell";

    private String command;
    private String args;
    private String banner;
    private String exitCode = ShellCommandUtils.EXIT_CODE_ANY;
    private boolean noProfile = true;
    private Integer codepage;
    private String workingDirectory;

    private WSManClientFactory m_factory;

    public WsManShellDetector() {
        super(PROTOCOL_NAME, 0);
    }

    public WsManShellDetector(final String serviceName) {
        super(serviceName, 0);
    }

    @Override
    public DetectResults detect(DetectRequest request) {
        try {
            final WSManEndpoint endpoint = WsmanEndpointUtils.fromMap(request.getRuntimeAttributes());
            return isServiceDetected(request.getAddress(), endpoint);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isServiceDetected(InetAddress address) {
        throw new UnsupportedOperationException("WSManEndpoint is required.");
    }

    public DetectResults isServiceDetected(InetAddress address, WSManEndpoint endpoint) {
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("'command' is required.");
        }
        final String[] arguments = ShellCommandUtils.toArguments(args);
        // No single exchange with the host may outlive the detector timeout
        final WSManEndpoint boundedEndpoint = WsmanEndpointUtils.withTimeouts(endpoint, getTimeout());
        try (WSManClient client = m_factory.getClient(boundedEndpoint)) {
            LOG.debug("Running '{}' with arguments {} on {}", command, arguments, address);
            final CommandResult result = client.runCommand(command, arguments, Duration.ofMillis(getTimeout()),
                    ShellCommandUtils.buildShellOptions(noProfile, codepage, workingDirectory));
            LOG.debug("Command '{}' on {} exited with {}: stdout='{}' stderr='{}'", command, address, result.exitCode(),
                    ShellCommandUtils.excerpt(result.stdout()), ShellCommandUtils.excerpt(result.stderr()));

            final Optional<String> failure = ShellCommandUtils.checkResult(result, exitCode, banner);
            if (failure.isPresent()) {
                LOG.debug("Service not detected on {}: {}", address, failure.get());
            }
            return new DetectResultsImpl(!failure.isPresent());
        } catch (WSManException e) {
            LOG.debug("Running '{}' failed for address '{}' with endpoint '{}'", command, address, endpoint, e);
            return new DetectResultsImpl(false);
        }
    }

    public void setClientFactory(WSManClientFactory factory) {
        m_factory = factory;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getExitCode() {
        return exitCode;
    }

    public void setExitCode(String exitCode) {
        this.exitCode = exitCode;
    }

    public boolean isNoProfile() {
        return noProfile;
    }

    public void setNoProfile(boolean noProfile) {
        this.noProfile = noProfile;
    }

    public Integer getCodepage() {
        return codepage;
    }

    public void setCodepage(Integer codepage) {
        this.codepage = codepage;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    protected void onInit() {
        // pass
    }

    @Override
    public void dispose() {
        // pass
    }
}
