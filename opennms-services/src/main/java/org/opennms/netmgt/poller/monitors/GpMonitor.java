/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.ExecRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;

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

// this is marked not distributable because it relieds on the dhcpd deamon of opennms
@Distributable(DistributionContext.DAEMON)
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
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        //
        // Process parameters
        //

        //
        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_INET)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_INET currently supported");

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
        InetAddress ipv4Addr = (InetAddress) iface.getAddress();

        final String hostAddress = InetAddressUtils.str(ipv4Addr);

		LOG.debug("poll: address = {}, script = {}, arguments = {}, {}", tracker, hostAddress, script, args);

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
        LOG.debug("poll: GP - serviceStatus= {} {}", hostAddress, serviceStatus);
        return serviceStatus;
    }
    
}
