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
// 2003 Jan 31: Added the ability to imbed RRA information in poller packages.
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

import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;

/**
 * <p>
 * This class provides a basic implementation for most of the interface methods
 * of the <code>ServiceMonitor</code> class in addition to methods for
 * creating and updating RRD files with latency information. Since most pollers
 * do not do any special initialization, and only require that the interface is
 * an <code>InetAddress</code> object this class provides eveything by the
 * <code>poll<code> interface.
 *
 * @author <A HREF="mike@opennms.org">Mike</A>
 * @author <A HREF="weave@oculan.com">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
abstract public class IPv4LatencyMonitor extends IPv4Monitor {
    /**
     * RRD data source name which doubles as the RRD file name.
     */
    static String DEFAULT_DSNAME = "response-time";

    private PollerConfig m_pollerConfig;

    /**
     * <P>
     * This method is called after the framework creates an instance of the
     * plug-in. The framework passes the object a proxy object that can be used
     * to retreive configuration information specific to the plug-in.
     * Additionally, any parameters for the plug-in from the package definition
     * are passed using the parameters element.
     * </P>
     * 
     * <P>
     * If there is a critical error, like missing service libraries, the the
     * montior may throw a ServiceMonitorException. If the plug-in throws an
     * exception then the plug-in will be disabled in the framework.
     * </P>
     * 
     * @param parameters
     *            Not currently used
     * 
     * @exception java.lang.RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     * 
     */
    public void initialize(PollerConfig pollerConfig, Map parameters) {
        // Log4j category
        //
        Category log = ThreadCategory.getInstance(getClass());

        m_pollerConfig = pollerConfig;

        try {
            RrdUtils.initialize();
        } catch (RrdException e) {
            if (log.isEnabledFor(Priority.ERROR))
                log.error("initialize: Unable to initialize RrdUtils", e);
            throw new RuntimeException("Unable to initialize RrdUtils", e);
        }

        if (log.isDebugEnabled())
            log.debug("initialize: successfully instantiated JNI interface to RRD...");

        return;
    }

    /**
     * <P>
     * This method is called whenever the plug-in is being unloaded, normally
     * during framework exit. During this time the framework may release any
     * resource and save any state information using the proxy object from the
     * initialization routine.
     * </P>
     * 
     * <P>
     * Even if the plug-in throws a monitor exception, it will not prevent the
     * plug-in from being unloaded. The plug-in should not return until all of
     * its state information is saved. Once the plug-in returns from this call
     * its configuration proxy object is considered invalid.
     * </P>
     * 
     * @exception java.lang.RuntimeException
     *                Thrown if an error occurs during deallocation.
     * 
     */
    public void release() {
        return;
    }

    /**
     * <P>
     * This method is the called whenever an interface is being removed from the
     * scheduler. For example, if a service is determined as being no longer
     * supported then this method will be invoked to cleanup any information
     * associated with this device. This gives the implementor of the interface
     * the ability to serialize any data prior to the interface being discarded.
     * </P>
     * 
     * <P>
     * If an exception is thrown during the release the exception will be
     * logged, but the interface will still be discarded for garbage collection.
     * </P>
     * 
     * @exception java.lang.RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                interface from being monitored.
     */
    public void release(MonitoredService svc) {
        return;
    }

    /**
     * Create an RRD database file for storing latency/response time data.
     * 
     * @param rrdJniInterface
     *            interface used to issue RRD commands.
     * @param repository
     *            path to the RRD file repository
     * @param addr
     *            interface address
     * @param dsName
     *            data source/RRD file name
     * 
     * @return true if RRD file successfully created, false otherwise
     */
    public boolean createRRD(String repository, InetAddress addr, String dsName, org.opennms.netmgt.config.poller.Package pkg) throws RrdException {

        List rraList = getPollerConfig().getRRAList(pkg);

        // add interface address to RRD repository path
        String path = repository + File.separator + addr.getHostAddress();

        return RrdUtils.createRRD(addr.getHostAddress(), path, dsName, getPollerConfig().getStep(pkg), "GAUGE", 600, "U", "U", rraList);

    }

    /**
     * @return
     */
    private PollerConfig getPollerConfig() {
        return m_pollerConfig;
    }

    /**
     * Update an RRD database file with latency/response time data.
     * 
     * @param rrdJniInterface
     *            interface used to issue RRD commands.
     * @param repository
     *            path to the RRD file repository
     * @param addr
     *            interface address
     * @param value
     *            value to update the RRD file with
     * 
     * @return true if RRD file successfully created, false otherwise
     */
    public void updateRRD(String repository, InetAddress addr, String dsName, long value, org.opennms.netmgt.config.poller.Package pkg) {
        Category log = ThreadCategory.getInstance(this.getClass());

        try {
            // Create RRD if it doesn't already exist
            createRRD(repository, addr, dsName, pkg);

            // add interface address to RRD repository path
            String path = repository + File.separator + addr.getHostAddress();

            RrdUtils.updateRRD(addr.getHostAddress(), path, dsName, Long.toString(value));

        } catch (RrdException e) {
            if (log.isEnabledFor(Priority.ERROR)) {
                String msg = e.getMessage();
                log.error(msg);
                throw new RuntimeException(msg, e);
            }
        }
    }

}