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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.opennms.core.utils.ExecRunner;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final public class SystemExecuteMonitor extends AbstractServiceMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemExecuteMonitor.class);

    private static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface on the read method call.
     */
    private static final int DEFAULT_TIMEOUT = 3000;

    /**
     * {@inheritDoc}
     *
     * During the poll an attempt is made to call the specified external script
     * or program. The banner line returned as standard output by the script or program is parsed for a
     * partial match with the banner string specified in the poller configuration.
     * Provided that the script's response is valid we set the service status to SERVICE_AVAILABLE and return.
     * The parameter script
     * The parameter args is handed over to the called script. The following variables will be replaced.
     * ${timeout} will be replaced with the specified timeout in milliseconds.
     * ${timeoutsec} will be replaced with the specified timeout in seconds not in milliseconds.
     * ${retry} will be replaced with the amount off specified retires.
     * ${ipaddr} will be replaced with the ip-address of the interface that holds the polled service at the node.
     * ${nodeid} will be replaced with the nodeid of the node that holds the polled service.
     * ${nodelabel} will be replaced with the nodelabel of the node that holds the polled service.
     * ${svcname} will be replaced with the name of the polled service.
     *
     * The timeout is handled by ExecRunner and is also passed as a parameter to
     * the script or program being called.
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {

        TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

        String script = ParameterMap.getKeyedString(parameters, "script", null);

        String problem = checkScriptFile(script);
        if (problem != null) {
            LOGGER.error(problem);
            return PollStatus.unknown(problem);
        }

        String args = ParameterMap.getKeyedString(parameters, "args", "");
        args = enrichArguments(args, svc, tracker, parameters);


        String strBannerMatch = (String) parameters.get("banner");

        String scriptOutput = "";

        String scriptError = "";

        PollStatus serviceStatus = PollStatus.unavailable();

        for (tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {
            try {
                tracker.startAttempt();

                int exitStatus = 100;

                int timeoutInSeconds = (int) tracker.getTimeoutInSeconds();

                ExecRunner execRunner = new ExecRunner();
                execRunner.setMaxRunTimeSecs(timeoutInSeconds);

                LOGGER.debug("calling: " + script + " " + args);

                exitStatus = execRunner.exec(script + " " + args);

                double responseTime = tracker.elapsedTimeInMillis();

                if (exitStatus != 0) {
                    scriptOutput = execRunner.getOutString();
                    String reason = script + " failed with exit code " + exitStatus + ". Standard out: " + scriptOutput;
                    LOGGER.debug(reason);
                    serviceStatus = PollStatus.unavailable(reason);
                }
                if (execRunner.isMaxRunTimeExceeded()) {

                    String reason = script + " failed. Timeout exceeded";
                    LOGGER.debug(reason);
                    serviceStatus = PollStatus.unavailable(reason);

                } else {
                    if (exitStatus == 0) {
                        scriptOutput = execRunner.getOutString();
                        scriptError = execRunner.getErrString();
                        if (!scriptOutput.equals(""))
                            LOGGER.debug("{} output  = {}", script, scriptOutput);
                        else
                            LOGGER.debug("{} returned no output", script);
                        if (!scriptError.equals(""))
                            LOGGER.debug("{} error = {}", script, scriptError);
                        if (strBannerMatch == null || strBannerMatch.equals("*")) {

                            serviceStatus = PollStatus.available(responseTime);

                        } else {
                            if (scriptOutput.indexOf(strBannerMatch) > -1) {
                                serviceStatus = PollStatus.available(responseTime);
                            } else {
                                serviceStatus = PollStatus.unavailable(script + " banner not contained in output banner='" + strBannerMatch + "' output='" + scriptOutput + "'");
                            }
                        }
                    }
                }

            } catch (ArrayIndexOutOfBoundsException e) {

                String reason = script + " ArrayIndexOutOfBoundsException";
                LOGGER.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);

            } catch (IOException e) {

                String reason = "IOException occurred. Check for proper operation of " + script;
                LOGGER.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);

            } catch (InterruptedException e) {
                LOGGER.debug("Interruption for script " + script, e);
                serviceStatus = PollStatus.unavailable("Interruption for script " + script + " " + e.getMessage());
            }
        }

        LOGGER.debug("Called: '" + script + " " + args + "' Result: " + serviceStatus + " ResponseTime: " + serviceStatus.getResponseTime());
        return serviceStatus;
    }

    private String enrichArguments(String args, MonitoredService svc, TimeoutTracker tracker, Map <String, Object> parameters) {
        String richArgs = args;
        richArgs = richArgs.replace("${timeout}", ((Long)tracker.getTimeoutInMillis()).toString());
        richArgs = richArgs.replace("${timeoutsec}", ((Long)tracker.getTimeoutInSeconds()).toString());
        richArgs = richArgs.replace("${retry}", ParameterMap.getKeyedString(parameters, "retry", ((Integer)DEFAULT_RETRY).toString()));
        richArgs = richArgs.replace("${ipaddr}", svc.getIpAddr());
        richArgs = richArgs.replace("${nodeid}", ((Integer) svc.getNodeId()).toString());
        richArgs = richArgs.replace("${nodelabel}", svc.getNodeLabel());
        richArgs = richArgs.replace("${svcname}", svc.getSvcName());
        return richArgs;
    }

    private String checkScriptFile(String script) {

        if (script == null) {
            return "required parameter script not found";
        }

        File scriptFile = new File(script);
        if (!scriptFile.exists()) {
            return "Script file does not exist: " + script;
        }

        return null;
    }

}
