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

package org.opennms.netmgt.rrd;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Provides static methods for interacting with round robin files. Supports JNI
 * and JRobin based files and provides queuing for managing differences in
 * collection speed and disk write speed. This behavior is implemented using the
 * Strategy pattern with a different RrdStrategy for JRobin and JNI as well as a
 * Strategy that provides Queueing on top of either one.
 *
 * The following System properties select which strategy is in use.
 *
 * <pre>
 *
 *  org.opennms.rrd.usejni: (defaults to true)
 *   true - use the existing RRDTool code via the JNI interface
 *
 * @see JniRrdStrategy false - use the pure java JRobin interface
 * @see JRobinRrdStrategy
 *
 * org.opennms.rrd.usequeue: (defaults to true) use the queueing that allows
 * collection to occur even though the disks are keeping up.
 * @see QueuingRrdStrategy
 *
 *
 * </pre>
 */
public abstract class RrdUtils {
    private static final Logger LOG = LoggerFactory.getLogger(RrdUtils.class);

    private static RrdStrategy<?, ?> m_rrdStrategy = null;

    private static BeanFactory m_context = new ClassPathXmlApplicationContext(new String[]{
                // Default RRD configuration context
                "org/opennms/netmgt/rrd/rrd-configuration.xml"
            });

    /**
     * Writes a file with the attribute to rrd track mapping next to the rrd file.
     *
     * attributMappings = Key(attributeId, for example SNMP OID or JMX bean)
     *                  = value(Name of data source, for example ifInOctets)
     *
     * @param directory
     * @param rrdName
     * @param attributeMappings a {@link Map<String, String>} that represents
     * the mapping of attributeId to rrd track names
     */
    public static void createMetaDataFile(String directory, String rrdName, Map<String, String> attributeMappings) {
        if (attributeMappings != null) {
            Writer fileWriter = null;
            String mapping = "";
            StringBuilder sb = new StringBuilder(mapping);
            for (Entry<String, String> mappingEntry : attributeMappings.entrySet()) {
                sb.append(mappingEntry.getKey());
                sb.append("=");
                sb.append(mappingEntry.getValue());
                sb.append("\n");
            }
            String rrdMetaFileName = directory + File.separator + rrdName + ".meta";
            try {
                fileWriter = new FileWriter(rrdMetaFileName);
                fileWriter.write(sb.toString());
                LOG.info("createRRD: creating META file {}", rrdMetaFileName);
            } catch (IOException e) {
                LOG.error("createMetaDataFile: An error occured creating metadatafile: {}", rrdMetaFileName, e);
            } finally {
                if (fileWriter != null) {
                    try {
                        fileWriter.close();
                    } catch (IOException e) {
                        LOG.error("createMetaDataFile: An error occured closing fileWriter", e);
                    }
                }
            }
        }
    }

    public static enum StrategyName {

        basicRrdStrategy,
        queuingRrdStrategy,
        tcpAndBasicRrdStrategy,
        tcpAndQueuingRrdStrategy

    }

    /**
     * <p>getStrategy</p>
     *
     * @return a {@link org.opennms.netmgt.rrd.RrdStrategy} object.
     */
    @SuppressWarnings("unchecked")
    public static <D, F> RrdStrategy<D, F> getStrategy() {
        RrdStrategy<D, F> retval = null;
        if (m_rrdStrategy == null) {
            if ((Boolean) m_context.getBean("useQueue")) {
                if ((Boolean) m_context.getBean("useTcp")) {
                    retval = (RrdStrategy<D, F>) m_context.getBean(StrategyName.tcpAndQueuingRrdStrategy.toString());
                } else {
                    retval = (RrdStrategy<D, F>) m_context.getBean(StrategyName.queuingRrdStrategy.toString());
                }
            } else {
                if ((Boolean) m_context.getBean("useTcp")) {
                    retval = (RrdStrategy<D, F>) m_context.getBean(StrategyName.tcpAndBasicRrdStrategy.toString());
                } else {
                    retval = (RrdStrategy<D, F>) m_context.getBean(StrategyName.basicRrdStrategy.toString());
                }
            }
        } else {
            retval = (RrdStrategy<D, F>) m_rrdStrategy;
        }

        if (retval == null) {
            throw new IllegalStateException("RrdUtils not initialized");
        }
        return retval;
    }

    /**
     * <p>getSpecificStrategy</p>
     *
     * @param strategy a {@link org.opennms.netmgt.rrd.RrdUtils.StrategyName}
     * object.
     * @return a {@link org.opennms.netmgt.rrd.RrdStrategy} object.
     */
    @SuppressWarnings("unchecked")
    public static <D, F> RrdStrategy<D, F> getSpecificStrategy(StrategyName strategy) {
        RrdStrategy<D, F> retval = null;
        retval = (RrdStrategy<D, F>) m_context.getBean(strategy.toString());
        if (retval == null) {
            throw new IllegalStateException("RrdUtils not initialized");
        }
        return retval;
    }

    /**
     * <p>setStrategy</p>
     *
     * @param strategy a {@link org.opennms.netmgt.rrd.RrdStrategy} object.
     */
    public static void setStrategy(RrdStrategy<?, ?> strategy) {
        m_rrdStrategy = strategy;
    }

    /**
     * Create a round robin database file. See the man page for rrdtool create
     * for definitions of each of these.
     *
     * @param creator - A string representing who is creating this file for use
     * in log msgs
     * @param directory - The directory to create the file in
     * @param dsName - The datasource name for use in the round robin database
     * @param step - the step for the database
     * @param dsType - the type for the datasource
     * @param dsHeartbeat - the heartbeat for the datasouce
     * @param dsMin - the minimum allowable value for the datasource
     * @param dsMax - the maximum allowable value for the datasouce
     * @param rraList - a List of the round robin archives to create in the
     * database
     * @param attributeMappings a {@link Map<String, String>} that represents the mapping of attributeId to rrd track names
     * @return true if the file was actually created, false otherwise
     * @throws org.opennms.netmgt.rrd.RrdException if any.
     */
    public static boolean createRRD(String creator, String directory, String dsName, int step, String dsType, int dsHeartbeat, String dsMin, String dsMax, List<String> rraList, Map<String, String> attributeMappings) throws RrdException {
        return createRRD(creator, directory, dsName, step, Collections.singletonList(new RrdDataSource(dsName, dsType, dsHeartbeat, dsMin, dsMax)), rraList, attributeMappings);
    }

    /**
     * Create a round robin database file. See the man page for rrdtool create
     * for definitions of each of these.
     *
     * @param creator - A string representing who is creating this file for use
     * in log msgs
     * @param directory - The directory to create the file in
     * @param dsName - The datasource name for use in the round robin database
     * @param step - the step for the database
     * @param dsType - the type for the datasource
     * @param dsHeartbeat - the heartbeat for the datasouce
     * @param dsMin - the minimum allowable value for the datasource
     * @param dsMax - the maximum allowable value for the datasouce
     * @param rraList - a List of the round robin archives to create in the
     * database
     * @return true if the file was actually created, false otherwise
     * @throws org.opennms.netmgt.rrd.RrdException if any.
     */
    public static boolean createRRD(String creator, String directory, String dsName, int step, String dsType, int dsHeartbeat, String dsMin, String dsMax, List<String> rraList) throws RrdException {
        return createRRD(creator, directory, dsName, step, Collections.singletonList(new RrdDataSource(dsName, dsType, dsHeartbeat, dsMin, dsMax)), rraList, null);
    }

/**
     * <p>createRRD</p>
     *
     * @param creator a {@link java.lang.String} object.
     * @param directory a {@link java.lang.String} object.
     * @param rrdName a {@link java.lang.String} object.
     * @param step a int.
     * @param dataSources a {@link java.util.List} object.
     * @param rraList a {@link java.util.List} object.
     * @return a boolean.
     * @throws org.opennms.netmgt.rrd.RrdException if any.
     */
    public static boolean createRRD(String creator, String directory, String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList) throws RrdException {
        return createRRD(creator, directory, rrdName, step, dataSources, rraList, null);
    }
    
    /**
     * <p>createRRD</p>
     *
     * @param creator a {@link java.lang.String} object.
     * @param directory a {@link java.lang.String} object.
     * @param rrdName a {@link java.lang.String} object.
     * @param step a int.
     * @param dataSources a {@link java.util.List} object.
     * @param rraList a {@link java.util.List} object.
     * @param attributeMappings a {@link Map<String, String>} that represents the mapping of attributeId to rrd track names
     * @return a boolean.
     * @throws org.opennms.netmgt.rrd.RrdException if any.
     */
    public static boolean createRRD(String creator, String directory, String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList, Map<String, String> attributeMappings) throws RrdException {
    	Object def = null;
    	
        try {
            def = getStrategy().createDefinition(creator, directory, rrdName, step, dataSources, rraList);
            // def can be null if the rrd-db exists already, but doesn't have to be (see MultiOutput/QueuingRrdStrategy
            getStrategy().createFile(def, attributeMappings);

            return true;
        } catch (Throwable e) {
            String path = directory + File.separator + rrdName + getStrategy().getDefaultFileExtension();
            LOG.error("createRRD: An error occured creating rrdfile {}", path, e);
            throw new org.opennms.netmgt.rrd.RrdException("An error occured creating rrdfile " + path + ": " + e, e);
        }
    }

    /**
     * Add datapoints to a round robin database using the current system time as
     * the timestamp for the values
     *
     * @param owner the owner of the file. This is used in log messages
     * @param repositoryDir the directory the file resides in
     * @param rrdName the name for the rrd file.
     * @param val a colon separated list of values representing the updates for
     * datasources for this rrd
     * @throws org.opennms.netmgt.rrd.RrdException if any.
     */
    public static void updateRRD(String owner, String repositoryDir, String rrdName, String val) throws RrdException {
        updateRRD(owner, repositoryDir, rrdName, System.currentTimeMillis(), val);
    }

    /**
     * Add datapoints to a round robin database.
     *
     * @param owner the owner of the file. This is used in log messages
     * @param repositoryDir the directory the file resides in
     * @param rrdName the name for the rrd file.
     * @param timestamp the timestamp in millis to use for the rrd update (this
     * gets rounded to the nearest second)
     * @param val a colon separated list of values representing the updates for
     * datasources for this rrd
     * @throws org.opennms.netmgt.rrd.RrdException if any.
     */
    public static void updateRRD(String owner, String repositoryDir, String rrdName, long timestamp, String val) throws RrdException {
        // Issue the RRD update
        String rrdFile = repositoryDir + File.separator + rrdName + getExtension();
        long time = (timestamp + 500L) / 1000L;

        String updateVal = Long.toString(time) + ":" + val;

        LOG.info("updateRRD: updating RRD file {} with values '{}'", rrdFile, updateVal);

        Object rrd = null;
        try {
            rrd = getStrategy().openFile(rrdFile);
            getStrategy().updateFile(rrd, owner, updateVal);
        } catch (Throwable e) {
            LOG.error("updateRRD: Error updating RRD file {} with values '{}'", rrdFile, updateVal, e);
            throw new org.opennms.netmgt.rrd.RrdException("Error updating RRD file " + rrdFile + " with values '" + updateVal + "': " + e, e);
        } finally {
            try {
                if (rrd != null) {
                    getStrategy().closeFile(rrd);
                }
            } catch (Throwable e) {
                LOG.error("updateRRD: Exception closing RRD file {}", rrdFile, e);
                throw new org.opennms.netmgt.rrd.RrdException("Exception closing RRD file " + rrdFile + ": " + e, e);
            }
        }

        LOG.debug("updateRRD: RRD update command completed.");
    }

    /**
     * This method issues an round robin fetch command to retrieve the last
     * value of the datasource stored in the specified RRD file. The retrieved
     * value returned to the caller.
     *
     * NOTE: This method assumes that each RRD file contains a single
     * datasource.
     *
     * @param rrdFile RRD file from which to fetch the data.
     * @param interval Thresholding interval (should equal RRD step size)
     * @param ds Name of the Data Source to be used
     * @return Retrived datasource value as a java.lang.Double
     * @throws java.lang.NumberFormatException if the retrieved value fails to
     * convert to a double
     * @throws org.opennms.netmgt.rrd.RrdException if any.
     */
    public static Double fetchLastValue(String rrdFile, String ds, int interval) throws NumberFormatException, RrdException {
        return getStrategy().fetchLastValue(rrdFile, ds, interval);
    }

    /**
     * This method issues an round robing fetch command to retrieve the last
     * value of the datasource stored in the specified RRD file within given
     * tolerance (which should be a multiple of the RRD interval). This is
     * useful If you are not entirely sure when an RRD might have been updated,
     * but you want to retrieve the last value which is not NaN NOTE: This
     * method assumes that each RRD file contains a single datasource.
     *
     * @param rrdFile RRD file from which to fetch the data.
     * @param interval Thresholding interval (should equal RRD step size)
     * @param ds Name of the Data Source to be used
     * @return Retrived datasource value as a java.lang.Double
     * @throws java.lang.NumberFormatException if the retrieved value fails to
     * convert to a double
     * @param range a int.
     * @throws org.opennms.netmgt.rrd.RrdException if any.
     */
    public static Double fetchLastValueInRange(String rrdFile, String ds, int interval, int range) throws NumberFormatException, RrdException {
        return getStrategy().fetchLastValueInRange(rrdFile, ds, interval, range);
    }

    /**
     * Creates an InputStream representing the bytes of a graph created from
     * round robin data. It accepts an rrdtool graph command. The underlying
     * implementation converts this command to a format appropriate for it .
     *
     * @param command the command needed to create the graph
     * @param workDir the directory that all referenced files are relative to
     * @return an input stream representing the bytes of a graph image as a PNG
     * file
     * @throws java.io.IOException if an IOError occurs
     * @throws org.opennms.netmgt.rrd.RrdException if an RRD error occurs
     */
    public static InputStream createGraph(String command, File workDir) throws IOException, RrdException {
        return getStrategy().createGraph(command, workDir);
    }

    /**
     * <p>getExtension</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getExtension() {
        String rrdExtension = (String) m_context.getBean("rrdFileExtension");
        if (rrdExtension == null || "".equals(rrdExtension)) {
            return getStrategy().getDefaultFileExtension();
        }
        return rrdExtension;
    }

    /**
     * <p>promoteEnqueuedFiles</p>
     *
     * @param files a {@link java.util.Collection} object.
     */
    public static void promoteEnqueuedFiles(Collection<String> files) {
        getStrategy().promoteEnqueuedFiles(files);
    }
}
