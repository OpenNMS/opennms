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

package org.opennms.netmgt.config.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.rrd.RrdRepository;

/**
 * <p>DataCollectionConfig interface.</p>
 */
public interface DataCollectionConfigDao {

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
     * <p>getMibObjProperties</p>
     *
     * @param cName a {@link java.lang.String} object.
     * @param aSysoid a {@link java.lang.String} object.
     * @param anAddress a {@link java.lang.String} object.
     * @param ifType a int.
     * @return a {@link java.util.List} object.
     */
    List<MibObjProperty> getMibObjProperties(String cName, String aSysoid, String anAddress);

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
     * @return a {@link org.opennms.netmgt.rrd.RrdRepository} object.
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

    /**
     * <p>getRootDataCollection</p>
     * 
     * @return a {@link org.opennms.netmgt.config.datacollection.DatacollectionConfig} object.
     */
    DatacollectionConfig getRootDataCollection();

    List<String> getAvailableDataCollectionGroups();

    List<String> getAvailableSystemDefs();

    List<String> getAvailableMibGroups();

    void reload();

    Date getLastUpdate();
}
