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
import java.util.TreeMap;

import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;

public class PersistOperationBuilder {
    
    private RrdRepository m_repository;
    private String m_rrdName;
    private CollectionResource m_resource;
    private TreeMap m_declarations = new TreeMap(new ByNameComparator());
    
    /**
     * RRDTool defined Data Source Types NOTE: "DERIVE" and "ABSOLUTE" not
     * currently supported.
     */
    static final String DST_GAUGE = "GAUGE";
    public static final int MAX_DS_NAME_LENGTH = 19;

    public PersistOperationBuilder(RrdRepository repository, CollectionResource resource, String rrdName) {
        m_repository = repository;
        m_resource = resource;
        m_rrdName = rrdName;
    }

    public RrdRepository getRepository() {
        return m_repository;
    }

    File getResourceDir(CollectionResource resource) {
        return resource.getResourceDir(getRepository());
    }

    void declareAttribute(AttributeType attrType) {
        m_declarations.put(attrType, "U");
    }

    void setAttributeValue(AttributeType attrType, String value) {
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
     * 
     * @return RRD type string or NULL object type is not supported.
     */
    public static String mapType(String objectType) {
        if (objectType.toLowerCase().startsWith("counter"))
            return NumericAttributeType.DST_COUNTER;
        
        return PersistOperationBuilder.DST_GAUGE;
    }

    public void commit() throws RrdException {
        RrdUtils.createRRD(m_resource.getCollectionAgent().getHostAddress(), getResourceDir(m_resource).getAbsolutePath(), m_rrdName, getRepository().getStep(), getDataSources(), getRepository().getRraList());
        RrdUtils.updateRRD(m_resource.getCollectionAgent().getHostAddress(), getResourceDir(m_resource).getAbsolutePath(), m_rrdName, System.currentTimeMillis(), getValues());
        
    }

    private String getValues() {
        boolean first = true;
        StringBuffer values = new StringBuffer();
        for (Iterator iter = m_declarations.keySet().iterator(); iter.hasNext();) {
            AttributeType attrType = (AttributeType) iter.next();
            String value = (String)m_declarations.get(attrType);
            if (!first) {
                values.append(':');
            } else {
                first = false;
            }
            values.append(value);
        }
        return values.toString();

    }

    private List getDataSources() {
        List dataSources = new ArrayList(m_declarations.size());
        for (Iterator it = m_declarations.keySet().iterator(); it.hasNext();) {
            AttributeType attrType = (AttributeType)it.next();
            RrdDataSource rrdDataSource = new RrdDataSource(StringUtils.truncate(attrType.getName(), PersistOperationBuilder.MAX_DS_NAME_LENGTH), PersistOperationBuilder.mapType(attrType.getType()), getRepository().getHeartBeat(), "U", "U");
            dataSources.add(rrdDataSource);
        }
        return dataSources;
    }

    public String getName() {
        return m_rrdName;
    }

}
