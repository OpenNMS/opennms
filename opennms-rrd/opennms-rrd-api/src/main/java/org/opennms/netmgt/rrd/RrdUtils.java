//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 05: Use Java 5 generics.
// 2004 Jun 24: Created this file.
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

package org.opennms.netmgt.rrd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Provides static methods for interacting with round robin files. Supports JNI
 * and JRobin based files and provides queuing for managing differences in
 * collection speed and disk write speed. This behaviour is implemented using
 * the Strategy pattern with a different RrdStrategy for JRobin and JNI as well
 * as a Strategy that provides Queueing on top of either one.
 * 
 * The following System properties select which strategy is in use.
 * 
 * <pre>
 * 
 *  org.opennms.rrd.usejni: (defaults to true)
 *   true - use the existing RRDTool code via the JNI interface @see JniRrdStrategy
 *   false - use the pure java JRobin interface @see JRobinRrdStrategy
 *  
 *  org.opennms.rrd.usequeue: (defaults to true)
 *    use the queueing that allows collection to occur even though the disks are
 *    keeping up. @see QueuingRrdStrategy  
 *  
 *  
 * </pre>
 */
public abstract class RrdUtils {

    private static RrdStrategy m_rrdStrategy = null;

    private static BeanFactory m_context = new ClassPathXmlApplicationContext(new String[] {
            // Default RRD configuration context
            "org/opennms/netmgt/rrd/rrd-configuration.xml"
    }); 

    public static RrdStrategy getStrategy() {
        RrdStrategy retval = null;
        if (m_rrdStrategy == null) {
            if ((Boolean)m_context.getBean("useQueue")) {
                if ((Boolean)m_context.getBean("useTcp")) {
                    retval = (RrdStrategy)m_context.getBean("tcpAndQueuingRrdStrategy");
                } else {
                    retval = (RrdStrategy)m_context.getBean("queuingRrdStrategy");
                }
            } else {
                if ((Boolean)m_context.getBean("useTcp")) {
                    retval = (RrdStrategy)m_context.getBean("tcpAndBasicRrdStrategy");
                } else {
                    retval = (RrdStrategy)m_context.getBean("basicRrdStrategy");
                }
            }
        } else {
            retval = m_rrdStrategy;
        }

        if (retval == null) {
            throw new IllegalStateException("RrdUtils not initialized");
        }
        return retval;
    }

    public static void setStrategy(RrdStrategy strategy) {
        m_rrdStrategy = strategy;
    }

    /**
     * Create a round robin database file. See the man page for rrdtool create
     * for definitions of each of these.
     * 
     * @param creator -
     *            A string representing who is creating this file for use in log
     *            msgs
     * @param directory -
     *            The directory to create the file in
     * @param dsName -
     *            The datasource name for use in the round robin database
     * @param step -
     *            the step for the database
     * @param dsType -
     *            the type for the datasource
     * @param dsHeartbeat -
     *            the heartbeat for the datasouce
     * @param dsMin -
     *            the minimum allowable value for the datasource
     * @param dsMax -
     *            the maximum allowable value for the datasouce
     * @param rraList -
     *            a List of the round robin archives to create in the database
     * @return true if the file was actually created, false otherwise
     */
    public static boolean createRRD(String creator, String directory, String dsName, int step, String dsType, int dsHeartbeat, String dsMin, String dsMax, List<String> rraList) throws RrdException {
        return createRRD(creator, directory, dsName, step, Collections.singletonList(new RrdDataSource(dsName, dsType, dsHeartbeat, dsMin, dsMax)), rraList);
    }

    public static boolean createRRD(String creator, String directory, String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList) throws RrdException {
        String fileName = rrdName + getExtension();

        String completePath = directory + File.separator + fileName;

        log().info("createRRD: creating RRD file " + completePath);

        try {
            Object def = getStrategy().createDefinition(creator, directory, rrdName, step, dataSources, rraList);
            getStrategy().createFile(def);
            return true;
        } catch (Exception e) {
            log().error("createRRD: An error occured creating rrdfile " + completePath + ": "  + e, e);
            throw new org.opennms.netmgt.rrd.RrdException("An error occured creating rrdfile " + completePath + ": " + e, e);
        }
    }

    private static Category log() {
        return ThreadCategory.getInstance(RrdUtils.class);
    }

    /**
     * Add datapoints to a round robin database using the current system time as the timestamp for the values
     * 
     * @param owner
     *            the owner of the file. This is used in log messages
     * @param repositoryDir
     *            the directory the file resides in
     * @param rrdName
     *            the name for the rrd file.
     * @param val
     *            a colon separated list of values representing the updates for datasources for this rrd
     *            
     * @throws RrdException
     */
    public static void updateRRD(String owner, String repositoryDir, String rrdName, String val) throws RrdException {
        updateRRD(owner, repositoryDir, rrdName, System.currentTimeMillis(), val);
    }

    /**
     * Add datapoints to a round robin database.
     * 
     * @param owner
     *            the owner of the file. This is used in log messages
     * @param repositoryDir
     *            the directory the file resides in
     * @param rrdName
     *            the name for the rrd file.
     * @param timestamp
     *            the timestamp in millis to use for the rrd update (this gets rounded to the nearest second)
     * @param val
     *            a colon separated list of values representing the updates for datasources for this rrd
     *            
     * @throws RrdException
     */
    public static void updateRRD(String owner, String repositoryDir, String rrdName, long timestamp, String val) throws RrdException {
        // Issue the RRD update
        String rrdFile = repositoryDir + File.separator + rrdName + getExtension();
        long time = (timestamp + 500L) / 1000L;

        String updateVal = Long.toString(time) + ":" + val;

        log().info("updateRRD: updating RRD file " + rrdFile + " with values '" + updateVal + "'");

        Object rrd = null;
        try {
            rrd = getStrategy().openFile(rrdFile);
            getStrategy().updateFile(rrd, owner, updateVal);
        } catch (Exception e) {
            log().error("updateRRD: Error updating RRD file " + rrdFile + " with values '" + updateVal + "': " + e, e);
            throw new org.opennms.netmgt.rrd.RrdException("Error updating RRD file " + rrdFile + " with values '" + updateVal + "': " + e, e);
        } finally {
            try {
                if (rrd != null) {
                    getStrategy().closeFile(rrd);
                }
            } catch (Exception e) {
                log().error("updateRRD: Exception closing RRD file " + rrdFile + ": " + e, e);
                throw new org.opennms.netmgt.rrd.RrdException("Exception closing RRD file " + rrdFile + ": " + e, e);
            }
        }

        if (log().isDebugEnabled()) {
            log().debug("updateRRD: RRD update command completed.");
        }
    }

    /**
     * This method issues an round robin fetch command to retrieve the last
     * value of the datasource stored in the specified RRD file. The retrieved
     * value returned to the caller.
     * 
     * NOTE: This method assumes that each RRD file contains a single
     * datasource.
     * 
     * @param rrdFile
     *            RRD file from which to fetch the data.
     * @param interval
     *            Thresholding interval (should equal RRD step size)
     * @param ds
     *            Name of the Data Source to be used
     * 
     * @return Retrived datasource value as a java.lang.Double
     * 
     * @throws NumberFormatException
     *             if the retrieved value fails to convert to a double
     */
    public static Double fetchLastValue(String rrdFile, String ds, int interval) throws NumberFormatException, RrdException {
        return getStrategy().fetchLastValue(rrdFile, ds, interval);
    }

    /**
     * This method issues an round robing fetch command to retrieve the last 
     * value of the datasource stored in the specified RRD file within given
     * tolerance (which should be a multiple of the RRD interval). This is useful
     * If you are not entirely sure when an RRD might have been updated, but you 
     * want to retrieve the last value which is not NaN
     * NOTE: This method assumes that each RRD file contains a single
     * datasource.
     * 
     * @param rrdFile
     *            RRD file from which to fetch the data.
     * @param interval
     *            Thresholding interval (should equal RRD step size)
     * @param ds
     *            Name of the Data Source to be used
     * 
     * @return Retrived datasource value as a java.lang.Double
     * 
     * @throws NumberFormatException
     *             if the retrieved value fails to convert to a double
     */
    public static Double fetchLastValueInRange(String rrdFile, String ds, int interval, int range) throws NumberFormatException, RrdException {
        return getStrategy().fetchLastValueInRange(rrdFile, ds, interval, range);
    }

    /**
     * Creates an InputStream representing the bytes of a graph created from
     * round robin data. It accepts an rrdtool graph command. The underlying
     * implementation converts this command to a format appropriate for it .
     * 
     * @param command
     *            the command needed to create the graph
     * @param workDir
     *            the directory that all referenced files are relative to
     * @return an input stream representing the bytes of a graph image as a PNG
     *         file
     * @throws IOException
     *             if an IOError occurs
     * @throws RrdException
     *             if an RRD error occurs
     */
    public static InputStream createGraph(String command, File workDir) throws IOException, RrdException {
        return getStrategy().createGraph(command, workDir);
    }

    public static String getExtension() {
        String rrdExtension = (String)m_context.getBean("rrdFileExtension");
        if (rrdExtension == null || "".equals(rrdExtension)) {
            return getStrategy().getDefaultFileExtension();
        }
        return rrdExtension;
    }

    public static void promoteEnqueuedFiles(Collection<String> files) {
        getStrategy().promoteEnqueuedFiles(files);
    }
}
