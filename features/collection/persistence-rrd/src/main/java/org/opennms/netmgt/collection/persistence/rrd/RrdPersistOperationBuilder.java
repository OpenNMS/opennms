/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.persistence.rrd;


import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.collection.api.ByNameComparator;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.NumericCollectionAttributeType;
import org.opennms.netmgt.collection.api.PersistException;
import org.opennms.netmgt.collection.api.PersistOperationBuilder;
import org.opennms.netmgt.collection.api.ResourceIdentifier;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.collection.support.DefaultTimeKeeper;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdMetaDataUtils;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * <p>PersistOperationBuilder class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class RrdPersistOperationBuilder implements PersistOperationBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(RrdPersistOperationBuilder.class);

    private final RrdStrategy<?, ?> m_rrdStrategy;
    private final RrdRepository m_repository;
    private final String m_rrdName;
    private final ResourceIdentifier m_resource;
    private final Map<CollectionAttributeType, Number> m_declarations;
    private final Map<String, String> m_metaData = new LinkedHashMap<String, String>();
    private TimeKeeper m_timeKeeper = new DefaultTimeKeeper();

    /**
     * RRDTool defined Data Source Types NOTE: "DERIVE" and "ABSOLUTE" not
     * currently supported.
     */
    private static final String DST_GAUGE = "GAUGE";
    private static final String DST_COUNTER = "COUNTER";
    /** Constant <code>MAX_DS_NAME_LENGTH=19</code> */
    public static final int MAX_DS_NAME_LENGTH = 19;

    /**
     * <p>Constructor for PersistOperationBuilder.</p>
     *
     * @param rrdStrategy a {@link org.opennms.netmgt.rrd.RrdStrategy} object.
     * @param repository a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     * @param resource a {@link org.opennms.netmgt.collection.api.ResourceIdentifier} object.
     * @param rrdName a {@link java.lang.String} object.
     */
    public RrdPersistOperationBuilder(RrdStrategy<?, ?> rrdStrategy, RrdRepository repository, ResourceIdentifier resource, String rrdName, boolean dontReorderAttributes) {
        m_rrdStrategy = rrdStrategy;
        m_repository = repository;
        m_resource = resource;
        m_rrdName = rrdName;
        if (dontReorderAttributes) {
            m_declarations = new LinkedHashMap<>();
        } else {
            m_declarations = new TreeMap<>(new ByNameComparator());
        }
    }

    public RrdStrategy<?, ?> getRrdStrategy() {
        return m_rrdStrategy;
    }

    /**
     * <p>getRepository</p>
     *
     * @return a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     */
    public RrdRepository getRepository() {
        return m_repository;
    }

    private File getResourceDir(ResourceIdentifier resource) throws FileNotFoundException {
        return getRepository().getRrdBaseDir().toPath()
                .resolve(resource.getPath())
                .toFile();
    }

    /**
     * <p>declareAttribute</p>
     *
     * @param attrType a {@link org.opennms.netmgt.collection.api.CollectionAttributeType} object.
     */
    public void declareAttribute(CollectionAttributeType attrType) {
        m_declarations.put(attrType, Double.NaN);
    }

    /**
     * <p>setAttributeValue</p>
     *
     * @param attrType a {@link org.opennms.netmgt.collection.api.CollectionAttributeType} object.
     * @param value a {@link java.lang.Number} object.
     */
    public void setAttributeValue(CollectionAttributeType attrType, Number value) {
        m_declarations.put(attrType, value);
    }
    
    public void setAttributeMetadata(String metricIdentifier, String name) {
        if (metricIdentifier == null) {
            if (name == null) {
                LOG.warn("Cannot set attribute metadata with null key and null value");
            } else {
                LOG.warn("Cannot set attribute metadata with null key and value of: {}", name);
            }
        } else {
            m_metaData.put(metricIdentifier, name);
        }
    }

    /**
     * Static method which takes a MIB object type (counter, counter32,
     * octetstring, etc...) and returns the appropriate RRD data type. If the
     * object type cannot be mapped to an RRD type, null is returned. RRD only
     * supports integer data so MIB objects of type 'octetstring' are not
     * supported.
     *
     * @param objectType -
     *            MIB object type to be mapped.
     * @return RRD type string or NULL object type is not supported.
     */
    public static String mapType(String objectType) {
        if (objectType.toLowerCase().startsWith("counter")) {
            return RrdPersistOperationBuilder.DST_COUNTER;
        } else if ("string".equalsIgnoreCase(objectType)) {
            return null;
        } else if ("octetstring".equalsIgnoreCase(objectType)) {
            return null;
        } else {
            return RrdPersistOperationBuilder.DST_GAUGE;
        }
    }

    public static String mapValue(Number num) {
        if (num == null) {
            return "U";
        }

        if (!Double.isFinite(num.doubleValue())) {
            return "U";
        }

        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setGroupingUsed(false);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(Integer.MAX_VALUE);
        nf.setMinimumIntegerDigits(0);
        nf.setMaximumIntegerDigits(Integer.MAX_VALUE);
        return nf.format(num);
    }

    /**
     * <p>commit</p>
     *
     * @throws org.opennms.netmgt.collection.api.PersistException if any.
     */
    public void commit() throws PersistException {
        if (m_declarations.size() == 0) {
            // Nothing to do.  In fact, we'll get an error if we try to create an RRD file with no data sources            
            return;
        }

        try {
            final String ownerName = m_resource.getOwnerName();
            final String absolutePath = getResourceDir(m_resource).getAbsolutePath();

            RrdMetaDataUtils.createMetaDataFile(absolutePath, m_rrdName, m_metaData);

            List<RrdDataSource> dataSources = getDataSources();
            if (dataSources != null && dataSources.size() > 0) {
                createRRD(m_rrdStrategy, ownerName, absolutePath, m_rrdName, getRepository().getStep(), dataSources, getRepository().getRraList());
                updateRRD(m_rrdStrategy, ownerName, absolutePath, m_rrdName, m_timeKeeper.getCurrentTime(), getValues());
            }
        } catch (FileNotFoundException e) {
            LoggerFactory.getLogger(getClass()).warn("Could not get resource directory: " + e.getMessage(), e);
            return;
        } catch (RrdException e) {
            throw new PersistException(e);
        }
    }

    private String getValues() {
        boolean first = true;
        StringBuffer values = new StringBuffer();
        for (Iterator<CollectionAttributeType> iter = m_declarations.keySet().iterator(); iter.hasNext();) {
        	CollectionAttributeType attrDef = iter.next();
            Number value = m_declarations.get(attrDef);
            if (!first) {
                values.append(':');
            } else {
                first = false;
            }
            values.append(mapValue(value));
        }
        return values.toString();
    }

    private List<RrdDataSource> getDataSources() {
        List<RrdDataSource> dataSources = new ArrayList<RrdDataSource>(m_declarations.size());
        for (CollectionAttributeType attrDef : m_declarations.keySet()) {

            String minval = "U";
            String maxval = "U";
            if(attrDef instanceof NumericCollectionAttributeType) {
                minval = ((NumericCollectionAttributeType) attrDef).getMinval() != null ? ((NumericCollectionAttributeType) attrDef).getMinval() : "U";
                maxval = ((NumericCollectionAttributeType) attrDef).getMaxval() != null ? ((NumericCollectionAttributeType) attrDef).getMaxval() : "U";
            }
            String type = RrdPersistOperationBuilder.mapType(attrDef.getType());
            // If the type is supported by RRD...
            if (type != null) {
                if (attrDef.getName().length() > MAX_DS_NAME_LENGTH) {
                    LOG.warn("Mib object name/alias '{}' exceeds 19 char maximum for RRD data source names, truncating.", attrDef.getName());
                }
                RrdDataSource rrdDataSource = new RrdDataSource(StringUtils.truncate(attrDef.getName(), RrdPersistOperationBuilder.MAX_DS_NAME_LENGTH), type, getRepository().getHeartBeat(), minval, maxval);
                dataSources.add(rrdDataSource);
            }
        }
        return dataSources;
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
    private static boolean createRRD(RrdStrategy<?, ?> rrdStrategy, String creator, String directory, String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList) throws RrdException {
        Object def = null;

        try {
            RrdStrategy<Object, Object> strategy = toGenericType(rrdStrategy);

            def = strategy.createDefinition(creator, directory, rrdName, step, dataSources, rraList);
            // def can be null if the rrd-db exists already, but doesn't have to be (see MultiOutput/QueuingRrdStrategy
            strategy.createFile(def);

            return true;
        } catch (Throwable e) {
            String path = directory + File.separator + rrdName + rrdStrategy.getDefaultFileExtension();
            LOG.error("createRRD: An error occurred creating rrdfile {}", path, e);
            throw new org.opennms.netmgt.rrd.RrdException("An error occurred creating rrdfile " + path + ": " + e, e);
        }
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
    private static void updateRRD(RrdStrategy<?, ?> rrdStrategy, String owner, String repositoryDir, String rrdName, long timestamp, String val) throws RrdException {
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

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_rrdName;
    }

    /**
     * <p>getTimeKeeper</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.TimeKeeper} object.
     */
    public TimeKeeper getTimeKeeper() {
        return m_timeKeeper;
    }

    /**
     * <p>setTimeKeeper</p>
     *
     * @param timeKeeper a {@link org.opennms.netmgt.collection.api.TimeKeeper} object.
     */
    public void setTimeKeeper(TimeKeeper timeKeeper) {
        m_timeKeeper = timeKeeper;
    }

}
