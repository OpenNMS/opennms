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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.collection.api.ByNameComparator;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.NumericCollectionAttributeType;
import org.opennms.netmgt.collection.api.ResourceIdentifier;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.collection.support.DefaultTimeKeeper;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>PersistOperationBuilder class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class PersistOperationBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(PersistOperationBuilder.class);
    
    private final RrdRepository m_repository;
    private final String m_rrdName;
    private final ResourceIdentifier m_resource;
    private final Map<CollectionAttributeType, String> m_declarations = new TreeMap<CollectionAttributeType, String>(new ByNameComparator());
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
     * @param repository a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     * @param resource a {@link org.opennms.netmgt.collection.api.ResourceIdentifier} object.
     * @param rrdName a {@link java.lang.String} object.
     */
    public PersistOperationBuilder(RrdRepository repository, ResourceIdentifier resource, String rrdName) {
        m_repository = repository;
        m_resource = resource;
        m_rrdName = rrdName;
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
        return resource.getResourceDir(getRepository());
    }

    /**
     * <p>declareAttribute</p>
     *
     * @param attrType a {@link org.opennms.netmgt.collection.api.CollectionAttributeType} object.
     */
    public void declareAttribute(CollectionAttributeType attrType) {
        m_declarations.put(attrType, "U");
    }

    /**
     * <p>setAttributeValue</p>
     *
     * @param attrType a {@link org.opennms.netmgt.collection.api.CollectionAttributeType} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setAttributeValue(CollectionAttributeType attrType, String value) {
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
            return PersistOperationBuilder.DST_COUNTER;
        } else if ("string".equalsIgnoreCase(objectType)) {
            return null;
        } else if ("octetstring".equalsIgnoreCase(objectType)) {
            return null;
        } else {
            return PersistOperationBuilder.DST_GAUGE;
        }
    }

    /**
     * <p>commit</p>
     *
     * @throws org.opennms.netmgt.rrd.RrdException if any.
     */
    public void commit() throws RrdException {
        if (m_declarations.size() == 0) {
            // Nothing to do.  In fact, we'll get an error if we try to create an RRD file with no data sources            
            return;
        }

        try {
            final String ownerName = m_resource.getOwnerName();
            final String absolutePath = getResourceDir(m_resource).getAbsolutePath();
            List<RrdDataSource> dataSources = getDataSources();
            if (dataSources != null && dataSources.size() > 0) {
                RrdUtils.createRRD(ownerName, absolutePath, m_rrdName, getRepository().getStep(), dataSources, getRepository().getRraList(), getAttributeMappings());
                RrdUtils.updateRRD(ownerName, absolutePath, m_rrdName, m_timeKeeper.getCurrentTime(), getValues());
                RrdUtils.createMetaDataFile(absolutePath, m_rrdName, m_metaData);
            }
        } catch (FileNotFoundException e) {
            LoggerFactory.getLogger(getClass()).warn("Could not get resource directory: " + e.getMessage(), e);
            return;
        }
    }

    private String getValues() {
        boolean first = true;
        StringBuffer values = new StringBuffer();
        for (Iterator<CollectionAttributeType> iter = m_declarations.keySet().iterator(); iter.hasNext();) {
        	CollectionAttributeType attrDef = iter.next();
            String value = m_declarations.get(attrDef);
            if (!first) {
                values.append(':');
            } else {
                first = false;
            }
            values.append(value);
        }
        return values.toString();
    }

    private Map<String, String> getAttributeMappings() {
        return null;
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
            String type = PersistOperationBuilder.mapType(attrDef.getType());
            // If the type is supported by RRD...
            if (type != null) {
                RrdDataSource rrdDataSource = new RrdDataSource(StringUtils.truncate(attrDef.getName(), PersistOperationBuilder.MAX_DS_NAME_LENGTH), type, getRepository().getHeartBeat(), minval, maxval);
                dataSources.add(rrdDataSource);
            }
        }
        return dataSources;
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
