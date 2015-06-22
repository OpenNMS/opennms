/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.rrd;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.utils.PropertiesCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Provides static methods for interacting with round robin files.
 */
public abstract class RrdUtils {
    private static final Logger LOG = LoggerFactory.getLogger(RrdUtils.class);
    private static PropertiesCache s_cache = new PropertiesCache();

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
    public static void createMetaDataFile(final String directory, final String rrdName, final Map<String, String> attributeMappings) {
        final File metaFile = new File(directory + File.separator + rrdName + ".meta");

        try {
            if (metaFile.exists()) {
                s_cache.updateProperties(metaFile, attributeMappings);
            } else {
                s_cache.saveProperties(metaFile, attributeMappings);
            }
        } catch (final IOException e) {
            LOG.error("Failed to save metadata file {}", metaFile, e);
        }
    }

    public static Map<String,String> readMetaDataFile(final String directory, final String rrdName) {
        final File metaFile = new File(directory + File.separator + rrdName + ".meta");

        try {
            final Properties props = s_cache.getProperties(metaFile);
            final Map<String,String> ret = new HashMap<String,String>();
            for (final Map.Entry<Object,Object> entry : props.entrySet()) {
                final Object value = entry.getValue();
                ret.put(entry.getKey().toString(), value == null? null : value.toString());
            }
            return ret;
        } catch (final IOException e) {
            LOG.warn("Failed to retrieve metadata from {}", metaFile, e);
        }

        return Collections.emptyMap();
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
    public static boolean createRRD(RrdStrategy<?, ?> rrdStrategy, String creator, String directory, String dsName, int step, String dsType, int dsHeartbeat, String dsMin, String dsMax, List<String> rraList, Map<String, String> attributeMappings) throws RrdException {
        return createRRD(rrdStrategy, creator, directory, dsName, step, Collections.singletonList(new RrdDataSource(dsName, dsType, dsHeartbeat, dsMin, dsMax)), rraList, attributeMappings);
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
    public static boolean createRRD(RrdStrategy<?, ?> rrdStrategy, String creator, String directory, String dsName, int step, String dsType, int dsHeartbeat, String dsMin, String dsMax, List<String> rraList) throws RrdException {
        return createRRD(rrdStrategy, creator, directory, dsName, step, Collections.singletonList(new RrdDataSource(dsName, dsType, dsHeartbeat, dsMin, dsMax)), rraList, null);
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
    public static boolean createRRD(RrdStrategy<?, ?> rrdStrategy, String creator, String directory, String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList) throws RrdException {
        return createRRD(rrdStrategy, creator, directory, rrdName, step, dataSources, rraList, null);
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
    public static boolean createRRD(RrdStrategy<?, ?> rrdStrategy, String creator, String directory, String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList, Map<String, String> attributeMappings) throws RrdException {
        Object def = null;

        try {
            RrdStrategy<Object, Object> strategy = toGenericType(rrdStrategy);

            def = strategy.createDefinition(creator, directory, rrdName, step, dataSources, rraList);
            // def can be null if the rrd-db exists already, but doesn't have to be (see MultiOutput/QueuingRrdStrategy
            strategy.createFile(def, attributeMappings);

            return true;
        } catch (Throwable e) {
            String path = directory + File.separator + rrdName + rrdStrategy.getDefaultFileExtension();
            LOG.error("createRRD: An error occurred creating rrdfile {}", path, e);
            throw new org.opennms.netmgt.rrd.RrdException("An error occurred creating rrdfile " + path + ": " + e, e);
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
    public static void updateRRD(RrdStrategy<?, ?> rrdStrategy, String owner, String repositoryDir, String rrdName, String val) throws RrdException {
        updateRRD(rrdStrategy, owner, repositoryDir, rrdName, System.currentTimeMillis(), val);
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
    public static void updateRRD(RrdStrategy<?, ?> rrdStrategy, String owner, String repositoryDir, String rrdName, long timestamp, String val) throws RrdException {
        // Issue the RRD update
        String rrdFile = repositoryDir + File.separator + rrdName + rrdStrategy.getDefaultFileExtension();
        long time = (timestamp + 500L) / 1000L;

        String updateVal = Long.toString(time) + ":" + val;

        LOG.info("updateRRD: updating RRD file {} with values '{}'", rrdFile, updateVal);

        RrdStrategy<Object, Object> strategy = toGenericType(rrdStrategy);
        Object rrd = null;
        try {
            rrd = strategy.openFile(rrdFile);
            strategy.updateFile(rrd, owner, updateVal);
        } catch (Throwable e) {
            LOG.error("updateRRD: Error updating RRD file {} with values '{}'", rrdFile, updateVal, e);
            throw new org.opennms.netmgt.rrd.RrdException("Error updating RRD file " + rrdFile + " with values '" + updateVal + "': " + e, e);
        } finally {
            try {
                if (rrd != null) {
                    strategy.closeFile(rrd);
                }
            } catch (Throwable e) {
                LOG.error("updateRRD: Exception closing RRD file {}", rrdFile, e);
                throw new org.opennms.netmgt.rrd.RrdException("Exception closing RRD file " + rrdFile + ": " + e, e);
            }
        }

        LOG.debug("updateRRD: RRD update command completed.");
    }

    @SuppressWarnings("unchecked")
    private static RrdStrategy<Object, Object> toGenericType(RrdStrategy<?, ?> rrdStrategy) {
        Assert.notNull(rrdStrategy);
        return (RrdStrategy<Object, Object>) rrdStrategy;
    }

}
