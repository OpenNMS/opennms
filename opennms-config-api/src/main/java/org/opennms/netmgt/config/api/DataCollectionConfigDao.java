/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
