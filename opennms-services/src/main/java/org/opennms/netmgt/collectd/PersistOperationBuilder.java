//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Mar 04: Use TimeKeeper to get the current time and let it be dependency
//              injected for tests.  Java 5 generics. - dj@opennms.org
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.collectd;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.core.utils.DefaultTimeKeeper;
import org.opennms.core.utils.StringUtils;
import org.opennms.core.utils.TimeKeeper;
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
     * @param resource a {@link org.opennms.netmgt.collectd.ResourceIdentifier} object.
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
     * @param attrType a {@link org.opennms.netmgt.collectd.AttributeDefinition} object.
     */
    public void declareAttribute(AttributeDefinition attrType) {
        m_declarations.put(attrType, "U");
    }

    /**
     * <p>setAttributeValue</p>
     *
     * @param attrType a {@link org.opennms.netmgt.collectd.AttributeDefinition} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setAttributeValue(AttributeDefinition attrType, String value) {
        m_declarations.put(attrType, value);
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
        
        RrdUtils.createRRD(m_resource.getOwnerName(), getResourceDir(m_resource).getAbsolutePath(), m_rrdName, getRepository().getStep(), getDataSources(), getRepository().getRraList());
        RrdUtils.updateRRD(m_resource.getOwnerName(), getResourceDir(m_resource).getAbsolutePath(), m_rrdName, m_timeKeeper.getCurrentTime(), getValues());
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
