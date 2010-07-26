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
package org.opennms.netmgt.config;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.model.RrdRepository;

/**
 * <p>DataCollectionConfig interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface DataCollectionConfig {
    
    /** Constant <code>NODE_ATTRIBUTES=-1</code> */
    static final int NODE_ATTRIBUTES = -1;
    /** Constant <code>ALL_IF_ATTRIBUTES=-2</code> */
    static final int ALL_IF_ATTRIBUTES = -2;

    /**
     * <p>getSnmpStorageFlag</p>
     *
     * @param collectionName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getSnmpStorageFlag(String collectionName);

    /**
     * <p>getMibObjectList</p>
     *
     * @param cName a {@link java.lang.String} object.
     * @param aSysoid a {@link java.lang.String} object.
     * @param anAddress a {@link java.lang.String} object.
     * @param ifType a int.
     * @return a {@link java.util.List} object.
     */
    List<MibObject> getMibObjectList(String cName, String aSysoid, String anAddress, int ifType);

    /**
     * <p>getConfiguredResourceTypes</p>
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String,ResourceType> getConfiguredResourceTypes();
    
    /**
     * <p>getRrdRepository</p>
     *
     * @param collectionName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.RrdRepository} object.
     */
    RrdRepository getRrdRepository(String collectionName);
    
    /**
     * <p>getStep</p>
     *
     * @param collectionName a {@link java.lang.String} object.
     * @return a int.
     */
    int getStep(String collectionName);

    /**
     * <p>getRRAList</p>
     *
     * @param collectionName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<String> getRRAList(String collectionName);

    /**
     * <p>getRrdPath</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getRrdPath();

}
