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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.ExecRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to be used by the service poller framework to test the
 * availability of a generic service by calling an external script or program.
 * The external script or program will be passed two options: --hostname, the IP
 * address of the host to be polled, and --timeout, the timeout in seconds.
 * Additional options or arguments can be specified in the poller configuration.
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mike@opennms.org">Mike </A>
 * @author <A HREF="mailto:ayres@net.orst.edu">Bill Ayres </A>
 */

/**
 *
 * @deprecated Use the SystemExecuteMonitor instead
 */
@Deprecated
final public class GpMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(GpMonitor.class);
    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 3000; // 3 second timeout on
                                                        // read()

    /**
     * {@inheritDoc}
     *
     * Poll the specified address for service availability.
     *
     * During the poll an attempt is made to call the specified external script
     * or program. If the connection request is successful, the banner line
     * returned as standard output by the script or program is parsed for a
     * partial match with the banner string specified in the poller
     * configuration. Provided that the script's response is valid we set the
     * service status to SERVICE_AVAILABLE and return.
     *
     * The timeout is handled by ExecRunner and is also passed as a parameter to
     * the script or program being called.
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        LOG.warn("GpMonitor: This poller monitor is deprecated in favor of SystemExecuteMonitor. GpMonitor will be removed in a future release.");

        //
        // Process parameters
        //
        TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

        String hoption = ParameterMap.getKeyedString(parameters, "hoption", "--hostname");
        String toption = ParameterMap.getKeyedString(parameters, "toption", "--timeout");
        //
        // convert timeout to seconds for ExecRunner
        //
        String args = ParameterMap.getKeyedString(parameters, "args", null);

        // Script
        //
        String script = ParameterMap.getKeyedString(parameters, "script", null);
        if (script == null) {
            throw new RuntimeException("GpMonitor: required parameter 'script' is not present in supplied properties.");
        }

        // BannerMatch
        //
        String strBannerMatch = (String) parameters.get("banner");

        // Script standard output
        //
        String scriptoutput = "";

        // Script error output
        //
        String scripterror = "";

        // Get the address instance.
        //
        InetAddress ipAddr = svc.getAddress();

        final String hostAddress = InetAddressUtils.str(ipAddr);

		LOG.debug("poll: address = {}, script = {}, arguments = {}, {}", hostAddress, script, args, tracker);

        // Give it a whirl
        //
        PollStatus serviceStatus = PollStatus.unavailable();

        for (tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {
            try {
                tracker.startAttempt();

                int exitStatus = 100;

		// Some scripts, such as Nagios check scripts, look for -H and -t versus --hostname and 
		// --timeout. If the optional parameter option-type is set to short, then the former
		// will be used.


                int timeoutInSeconds = (int)tracker.getTimeoutInSeconds();

                ExecRunner er = new ExecRunner();
                er.setMaxRunTimeSecs(timeoutInSeconds);
                if (args == null)
                    exitStatus = er.exec(script + " " + hoption + " " + hostAddress + " " + toption + " " + timeoutInSeconds);
                else
                    exitStatus = er.exec(script + " " + hoption + " " + hostAddress + " " + toption + " " + timeoutInSeconds + " " + args);
                
                double responseTime = tracker.elapsedTimeInMillis();
                
                if (exitStatus != 0) {
                        scriptoutput = er.getOutString();
                        String reason = script + " failed with exit code " + exitStatus + ". Standard out: " + scriptoutput;
                        LOG.debug(reason);
                        serviceStatus = PollStatus.unavailable(reason);
                }
                if (er.isMaxRunTimeExceeded()) {
                	
                	String reason = script + " failed. Timeout exceeded";
                    LOG.debug(reason);
                    serviceStatus = PollStatus.unavailable(reason);

                } else {
                    if (exitStatus == 0) {
                        scriptoutput = er.getOutString();
                        scripterror = er.getErrString();
                        if (!scriptoutput.equals(""))
                            LOG.debug("{} output  = {}", script, scriptoutput);
                        else
                            LOG.debug("{} returned no output", script);
                        if (!scripterror.equals(""))
                            LOG.debug("{} error = {}", script, scripterror);
                        if (strBannerMatch == null || strBannerMatch.equals("*")) {
                        	
                            serviceStatus = PollStatus.available(responseTime);
                            
                        } else {
                            if (scriptoutput.indexOf(strBannerMatch) > -1) {
                                serviceStatus = PollStatus.available(responseTime);
                            } else {
                                serviceStatus = PollStatus.unavailable(script + "banner not contained in output banner='"+strBannerMatch+"' output='"+scriptoutput+"'");
                            }
                        }
                    }
                }

            } catch (ArrayIndexOutOfBoundsException e) {
            	
            	String reason = script + " ArrayIndexOutOfBoundsException";
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            	
            } catch (IOException e) {
            	
            	String reason = "IOException occurred. Check for proper operation of " + script;
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            	
            } catch (Throwable e) {
            	
            	String reason = script + "Exception occurred";
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            	
            }
        }

        //
        // return the status of the service
        //
        LOG.debug("poll: GP - serviceStatus= {} {}", serviceStatus, hostAddress);
        return serviceStatus;
    }
    
}
