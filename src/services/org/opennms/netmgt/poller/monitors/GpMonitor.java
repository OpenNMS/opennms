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
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;
import org.opennms.netmgt.utils.ExecRunner;
import org.opennms.netmgt.utils.ParameterMap;

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
final public class GpMonitor extends IPv4LatencyMonitor {
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
     * 
     * @return The availibility of the interface and if a transition event
     *         should be supressed.
     * 
     * @throws java.lang.RuntimeException
     *             Thrown if the interface experiences error during the poll.
     */
    public int checkStatus(MonitoredService svc, Map parameters, org.opennms.netmgt.config.poller.Package pkg) {
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

        int retry = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
        int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);
        //
        // convert timeout to seconds for ExecRunner
        //
        if (0 < timeout && timeout < 1000)
            timeout = 1;
        else
            timeout = timeout / 1000;
        String rrdPath = ParameterMap.getKeyedString(parameters, "rrd-repository", null);
        String dsName = ParameterMap.getKeyedString(parameters, "ds-name", null);

        if (rrdPath == null || dsName == null) {
            log.info("poll: RRD repository and/or ds-name not specified in parameters, latency data will not be stored.");
        }

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
            log.debug("poll: address = " + ipv4Addr.getHostAddress() + ", script = " + script + ", arguments = " + args + ", timeout(seconds) = " + timeout + ", retry = " + retry);

        // Give it a whirl
        //
        int serviceStatus = SERVICE_UNAVAILABLE;
        long responseTime = -1;

        for (int attempts = 0; attempts <= retry && serviceStatus != SERVICE_AVAILABLE; attempts++) {
            try {
                long sentTime = System.currentTimeMillis();

                int exitStatus = 100;
                ExecRunner er = new ExecRunner();
                er.setMaxRunTimeSecs(timeout);
                if (args == null)
                    exitStatus = er.exec(script + " --hostname " + ipv4Addr.getHostAddress() + " --timeout " + timeout);
                else
                    exitStatus = er.exec(script + " --hostname " + ipv4Addr.getHostAddress() + " --timeout " + timeout + " " + args);
                if (exitStatus != 0) {
                    log.debug(script + " failed with exit code " + exitStatus);
                    serviceStatus = SERVICE_UNAVAILABLE;
                }
                if (er.isMaxRunTimeExceeded()) {
                    log.debug(script + " failed. Timeout exceeded");
                    serviceStatus = SERVICE_UNAVAILABLE;
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
                        if (strBannerMatch == null || strBannerMatch.equals("*"))
                            serviceStatus = SERVICE_AVAILABLE;
                        else {
                            if (scriptoutput.indexOf(strBannerMatch) > -1) {
                                serviceStatus = SERVICE_AVAILABLE;
                            } else
                                serviceStatus = SERVICE_UNRESPONSIVE;
                        }
                        if (serviceStatus == SERVICE_AVAILABLE) {
                            responseTime = System.currentTimeMillis() - sentTime;
                            if (log.isDebugEnabled()) {
                                log.debug("poll: responseTime = " + responseTime + "ms");
                            }
                            if (responseTime >= 0 && rrdPath != null && dsName != null) {
                                this.updateRRD(rrdPath, ipv4Addr, dsName, responseTime, pkg);
                            }
                        }
                    }
                }

            } catch (ArrayIndexOutOfBoundsException e) {
                e.fillInStackTrace();
                log.debug(script + " ArrayIndexOutOfBoundsException");
            } catch (IOException e) {
                e.fillInStackTrace();
                log.debug("IOException occurred. Check for proper operation of " + script);
            } catch (Exception e) {
                e.fillInStackTrace();
                log.debug(script + "Exception occurred");
            }
        }

        //
        // return the status of the service
        //
        log.debug("poll: GP - serviceStatus= " + serviceStatus + "  " + ipv4Addr.getHostAddress());
        return serviceStatus;
    }
    
}
