//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jul 09: Enabled position independent parameters.
// 2003 Jun 29: Added this General Purpose script poller. 
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;
import org.opennms.netmgt.utils.ExecRunner;

/**
 * This class is designed to be used by the service poller framework to test the
 * availability of a generic service by calling an external script or program.
 * The external script or program will be passed two options: --hostname, the IP
 * address of the host to be polled, and --timeout, the timeout in seconds.
 * Additional options or arguments can be specified in the poller configuration.
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mike@opennms.org">Mike </A>
 * @author Weave
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:ayres@net.orst.edu">Bill Ayres </A>
 * 
 */

// this is marked not distributable because it relieds on the dhcpd deamon of opennms
@Distributable(DistributionContext.DAEMON)
final public class GpMonitor extends IPv4Monitor {
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
     * @param parameters
     *            The package parameters (timeout, retry, etc...) to be used for
     *            this poll.
     * @param iface
     *            The network interface to test the service on.
     * @return The availibility of the interface and if a transition event
     *         should be supressed.
     * 
     * @throws java.lang.RuntimeException
     *             Thrown if the interface experiences error during the poll.
     */
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface iface = svc.getNetInterface();

        //
        // Process parameters
        //
        Category log = ThreadCategory.getInstance(getClass());

        //
        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_IPV4)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_IPV4 currently supported");

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

        // Get the address instance.
        //
        InetAddress ipv4Addr = (InetAddress) iface.getAddress();

        if (log.isDebugEnabled())
            log.debug("poll: address = " + ipv4Addr.getHostAddress() + ", script = " + script + ", arguments = " + args + ", " + tracker);

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
                    exitStatus = er.exec(script + " " + hoption + " " + ipv4Addr.getHostAddress() + " " + toption + " " + timeoutInSeconds);
                else
                    exitStatus = er.exec(script + " " + hoption + " " + ipv4Addr.getHostAddress() + " " + toption + " " + timeoutInSeconds + " " + args);
                
                double responseTime = tracker.elapsedTimeInMillis();
                
                if (exitStatus != 0) {
                	
                	serviceStatus = logDown(Level.DEBUG, script + " failed with exit code " + exitStatus);

                }
                if (er.isMaxRunTimeExceeded()) {
                	
                	serviceStatus = logDown(Level.DEBUG, script + " failed. Timeout exceeded");

                } else {
                    if (exitStatus == 0) {
                        String scriptoutput = "";
                        scriptoutput = er.getOutString();
                        String scripterror = "";
                        scripterror = er.getErrString();
                        if (!scriptoutput.equals(""))
                            log.debug(script + " output  = " + scriptoutput);
                        else
                            log.debug(script + " returned no output");
                        if (!scripterror.equals(""))
                            log.debug(script + " error = " + scripterror);
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
            	
            	serviceStatus = logDown(Level.DEBUG, script + " ArrayIndexOutOfBoundsException", e);
            	
            } catch (IOException e) {
            	
            	serviceStatus = logDown(Level.DEBUG, "IOException occurred. Check for proper operation of " + script, e);
            	
            } catch (Exception e) {
            	
            	serviceStatus = logDown(Level.DEBUG, script + "Exception occurred", e);
            	
            }
        }

        //
        // return the status of the service
        //
        log.debug("poll: GP - serviceStatus= " + serviceStatus + "  " + ipv4Addr.getHostAddress());
        return serviceStatus;
    }
    
}
