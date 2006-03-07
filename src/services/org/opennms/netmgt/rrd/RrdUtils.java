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
// Jun 24, 2004: Created this file.
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
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

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
public class RrdUtils {

    private static final String DEFAULT_RRD_STRATEGY_CLASSNAME = "org.opennms.netmgt.rrd.rrdtool.JniRrdStrategy";

    private static final boolean USE_QUEUE = RrdConfig.getProperty("org.opennms.rrd.usequeue", true);

    private static final String RRD_STRATEGY_CLASSNAME = RrdConfig.getProperty("org.opennms.rrd.strategyClass", DEFAULT_RRD_STRATEGY_CLASSNAME);

    private static RrdStrategy m_rrdStrategy = null;
    
    private static String m_rrdExtension = RrdConfig.getProperty("org.opennms.rrd.fileExtension",".rrd");

    private static RrdStrategy getStrategy() throws RrdException {
        if (m_rrdStrategy == null)
            throw new IllegalStateException("RrdUtils not initiailzed");
        return m_rrdStrategy;
    }
    
    public static void setStrategy(RrdStrategy strategy) {
        m_rrdStrategy = strategy;
    }

    /**
     * Initializes the underlying round robin system and sets up the appropriate
     * strategy. The strategies are currently selected using System properties.
     * This creates the appropriate RrdStrategy and calls its initialize method
     */
    public static void initialize() throws RrdException {
        try {
            createStrategy();
            m_rrdStrategy.initialize();
        } catch (Exception e) {
            throw new org.opennms.netmgt.rrd.RrdException("An error occured initializing the Rrd subsytem", e);

        }
    }

    /**
     * 
     * 
     */
    public static void graphicsInitialize() throws RrdException {
        try {
            createStrategy();
            m_rrdStrategy.graphicsInitialize();
        } catch (Exception e) {
            throw new org.opennms.netmgt.rrd.RrdException("An error occured initializing the Rrd subsytem", e);

        }
    }

    /**
     * Create the appropriate RrdStrategy object based on the configuration
     * @throws RrdException 
     */
    private static void createStrategy() throws RrdException {
        if (m_rrdStrategy == null) {
            RrdStrategy rrdStategy = constructStrategyInstance();
            if (USE_QUEUE) {
                rrdStategy = new QueuingRrdStrategy(rrdStategy);
            }

            // would like to have this be queued as well, but queueing seems too
            //   implementation-specific at the moment
/*            if (USE_K5) {
                rrdStategy = new K5RrdStrategy(rrdStategy);
            }
*/            m_rrdStrategy = rrdStategy;
        }
    }

    private static RrdStrategy constructStrategyInstance() throws RrdException {
        try {
            return (RrdStrategy) Class.forName(RRD_STRATEGY_CLASSNAME).newInstance();
        } catch (Exception e) {
            log().error("Unable to load RrdStrategyClass "+RRD_STRATEGY_CLASSNAME, e);
            throw new RrdException("Unable to construct RrdStrategy: "+RRD_STRATEGY_CLASSNAME, e);
        }
        
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
    public static boolean createRRD(String creator, String directory, String dsName, int step, String dsType, int dsHeartbeat, String dsMin, String dsMax, List rraList) throws RrdException {
        String fileName = dsName + get_extension();

        if (log().isDebugEnabled())
            log().debug("createRRD: rrd path and file name to create: " + directory + File.separator + fileName);

        String completePath = directory + File.separator + fileName;

        // Create directories if necessary
        //

        File f = new File(completePath);
        if (f.exists()) {
            return false;
        }

        File dir = new File(directory);
        if (!dir.isDirectory()) {
            if (!dir.mkdirs()) {
                throw new org.opennms.netmgt.rrd.RrdException("Unable to create RRD repository directory: " + directory);
            }
        }

        try {
            Object def = getStrategy().createDefinition(creator, directory, dsName, step, dsType, dsHeartbeat, dsMin, dsMax, rraList);
            getStrategy().createFile(def);
            return true;
        } catch (Exception e) {
            log().debug("An error occured creating rrdfile " + completePath, e);
            throw new org.opennms.netmgt.rrd.RrdException("An error occured creating rrdfile " + completePath, e);
        }
    }

    private static Category log() {
        return ThreadCategory.getInstance(RrdUtils.class);
    }

    /**
     * Add a datapoint to a round robin database.
     * 
     * @param owner
     *            the owner of the file. This is used in log messages
     * @param repositoryDir
     *            the directory the file resides in
     * @param dsName
     *            the datasource name for file. (Also becames the basename of
     *            the file)
     * @param val
     *            the value to be stored. This should be a string representation
     *            of a number
     * @throws RrdException
     */
    public static void updateRRD(String owner, String repositoryDir, String dsName, String val) throws RrdException {
        // Issue the RRD update
        String rrdFile = repositoryDir + File.separator + dsName + get_extension();
        long time = (System.currentTimeMillis() + 500L) / 1000L;

        String updateVal = Long.toString(time) + ":" + val;

        if (log().isDebugEnabled())
            log().debug("updateRRD:updating RRD file: " + rrdFile + " with value: " + updateVal);

        Object rrd = null;
        try {
            rrd = getStrategy().openFile(rrdFile);
            getStrategy().updateFile(rrd, owner, updateVal);
        } catch (Exception e) {
            log().error("Error updating rrdFile " + rrdFile + " with value: " + updateVal, e);
            throw new org.opennms.netmgt.rrd.RrdException("Error updating rrdFile " + rrdFile + " with value: " + updateVal, e);
        } finally {
            try {
                if (rrd != null)
                    getStrategy().closeFile(rrd);
            } catch (Exception e) {
                throw new org.opennms.netmgt.rrd.RrdException("Exception closing rrdDb", e);
            }
        }

        if (log().isDebugEnabled())
            log().debug("updateRRD: RRD update command completed.");
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
     * 
     * @return Retrived datasource value as a java.lang.Double
     * 
     * @throws NumberFormatException
     *             if the retrieved value fails to convert to a double
     */
    public static Double fetchLastValue(String rrdFile, int interval) throws NumberFormatException, RrdException {
        return getStrategy().fetchLastValue(rrdFile, interval);
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
     * 
     * @return Retrived datasource value as a java.lang.Double
     * 
     * @throws NumberFormatException
     *             if the retrieved value fails to convert to a double
     */
    public static Double fetchLastValueInRange(String rrdFile, int interval, int range) throws NumberFormatException, RrdException {
        return getStrategy().fetchLastValueInRange(rrdFile, interval, range);
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

	public static String get_extension() {
		return m_rrdExtension;
	}

}