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

package org.opennms.netmgt.collectd;


import java.io.File;
import java.util.*;

import org.opennms.core.utils.DefaultTimeKeeper;
import org.opennms.core.utils.StringUtils;
import org.opennms.core.utils.TimeKeeper;
import org.opennms.netmgt.config.collector.AttributeDefinition;
import org.opennms.netmgt.config.collector.ByNameComparator;
import org.opennms.netmgt.config.collector.ResourceIdentifier;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;

/**
 * <p>PersistOperationBuilder class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class PersistOperationBuilder {
    
    private RrdRepository m_repository;
    private String m_rrdName;
    private ResourceIdentifier m_resource;
    private Map<AttributeDefinition, String> m_declarations = new TreeMap<AttributeDefinition, String>(new ByNameComparator());
    private Map<String, String> m_metaData = new LinkedHashMap<String, String>();
    private TimeKeeper m_timeKeeper = new DefaultTimeKeeper();
    
    /**
     * RRDTool defined Data Source Types NOTE: "DERIVE" and "ABSOLUTE" not
     * currently supported.
     */
    static final String DST_GAUGE = "GAUGE";
    static final String DST_COUNTER = "COUNTER";
    /** Constant <code>MAX_DS_NAME_LENGTH=19</code> */
    public static final int MAX_DS_NAME_LENGTH = 19;

    /**
     * <p>Constructor for PersistOperationBuilder.</p>
     *
     * @param repository a {@link org.opennms.netmgt.model.RrdRepository} object.
     * @param resource a {@link org.opennms.netmgt.config.collector.ResourceIdentifier} object.
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
     * @return a {@link org.opennms.netmgt.model.RrdRepository} object.
     */
    public RrdRepository getRepository() {
        return m_repository;
    }

    private File getResourceDir(ResourceIdentifier resource) {
        return resource.getResourceDir(getRepository());
    }

    /**
     * <p>declareAttribute</p>
     *
     * @param attrType a {@link org.opennms.netmgt.config.collector.AttributeDefinition} object.
     */
    public void declareAttribute(AttributeDefinition attrType) {
        m_declarations.put(attrType, "U");
    }

    /**
     * <p>setAttributeValue</p>
     *
     * @param attrType a {@link org.opennms.netmgt.config.collector.AttributeDefinition} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setAttributeValue(AttributeDefinition attrType, String value) {
        m_declarations.put(attrType, value);
    }
    
    public void setAttributeMetadata(String metricIdentifier, String name) {
        m_metaData.put(metricIdentifier, name);
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
        }
        
        return PersistOperationBuilder.DST_GAUGE;
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

        final String ownerName = m_resource.getOwnerName();
        final String absolutePath = getResourceDir(m_resource).getAbsolutePath();
        RrdUtils.createRRD(ownerName, absolutePath, m_rrdName, getRepository().getStep(), getDataSources(), getRepository().getRraList(), getAttributeMappings());
        RrdUtils.updateRRD(ownerName, absolutePath, m_rrdName, m_timeKeeper.getCurrentTime(), getValues());
        RrdUtils.createMetaDataFile(absolutePath, m_rrdName, m_metaData);
    }

    private String getValues() {
        boolean first = true;
        StringBuffer values = new StringBuffer();
        for (Iterator<AttributeDefinition> iter = m_declarations.keySet().iterator(); iter.hasNext();) {
            AttributeDefinition attrDef = iter.next();
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
        for (AttributeDefinition attrDef : m_declarations.keySet()) {

            String minval = "U";
            String maxval = "U";
            if(attrDef instanceof NumericAttributeType) {
                minval = ((NumericAttributeType) attrDef).getMinval() != null ? ((NumericAttributeType) attrDef).getMinval() : "U";
                maxval = ((NumericAttributeType) attrDef).getMaxval() != null ? ((NumericAttributeType) attrDef).getMaxval() : "U";
            }
            RrdDataSource rrdDataSource = new RrdDataSource(StringUtils.truncate(attrDef.getName(), PersistOperationBuilder.MAX_DS_NAME_LENGTH), PersistOperationBuilder.mapType(attrDef.getType()), getRepository().getHeartBeat(), minval, maxval);

            dataSources.add(rrdDataSource);
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
     * @return a {@link org.opennms.core.utils.TimeKeeper} object.
     */
    public TimeKeeper getTimeKeeper() {
        return m_timeKeeper;
    }

    /**
     * <p>setTimeKeeper</p>
     *
     * @param timeKeeper a {@link org.opennms.core.utils.TimeKeeper} object.
     */
    public void setTimeKeeper(TimeKeeper timeKeeper) {
        m_timeKeeper = timeKeeper;
    }

}
