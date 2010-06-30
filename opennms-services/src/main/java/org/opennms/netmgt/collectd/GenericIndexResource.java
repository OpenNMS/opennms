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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.collectd;

import java.io.File;

import org.opennms.netmgt.config.StorageStrategy;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.snmp.SnmpInstId;

/**
 * <p>GenericIndexResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GenericIndexResource extends SnmpCollectionResource {

    private SnmpInstId m_inst;
    private String m_name;
    private String m_resourceLabel;

    /**
     * <p>Constructor for GenericIndexResource.</p>
     *
     * @param def a {@link org.opennms.netmgt.collectd.ResourceType} object.
     * @param name a {@link java.lang.String} object.
     * @param inst a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     */
    public GenericIndexResource(ResourceType def, String name, SnmpInstId inst) {
        super(def);
        m_name = name;
        m_inst = inst;
    }

    /** {@inheritDoc} */
    @Override
    public File getResourceDir(RrdRepository repository) {
        String resourcePath = getStrategy().getRelativePathForAttribute(getParent(), getLabel(), null);
        File resourceDir = new File(repository.getRrdBaseDir(), resourcePath);
        log().debug("getResourceDir: " + resourceDir);
        return resourceDir;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return "node["+getCollectionAgent().getNodeId() + "]." + getResourceTypeName() + "[" + getLabel() + "]";
    }


    /** {@inheritDoc} */
    @Override
    public int getType() {
        return -1;	// XXX is this right?
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;// XXX should be based on the persistanceSelectorStrategy
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceTypeName() {
        return m_name;
    }
    
    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInstance() {
        return m_inst.toString();
    }

    private StorageStrategy getStrategy() {
        return ((GenericIndexResourceType)getResourceType()).getStorageStrategy();
    }

    private String getParent() {
        return String.valueOf(getCollectionAgent().getNodeId());
    }

    /*
     * Because call getResourceNameFromIndex could be expensive.
     * This class save the returned value from Strategy on a local variable.
     */
    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        if (m_resourceLabel == null) {
            m_resourceLabel = getStrategy().getResourceNameFromIndex(getParent(), getInstance());
        }
        return m_resourceLabel;
    }
}
