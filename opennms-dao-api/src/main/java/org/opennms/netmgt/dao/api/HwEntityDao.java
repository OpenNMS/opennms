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
package org.opennms.netmgt.dao.api;

import org.opennms.netmgt.model.OnmsHwEntity;

/**
 * The Interface HwEntityDao.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public interface HwEntityDao extends OnmsDao<OnmsHwEntity, Integer> {

    /**
     * Find root by node id.
     *
     * @param nodeId the node id
     * @return the OpenNMS hardware entity
     */
    public OnmsHwEntity findRootByNodeId(Integer nodeId);

    /**
     * Better performant than #findRootByNodeId.
     * Useful when dealing with large trees. See NMS-13256
     * Find root by node id
     *
     * @param nodeId the node id
     * @return the OpenNMS hardware entity
     */

    public OnmsHwEntity findRootEntityByNodeId(Integer nodeId);

    /**
     * Find entity by index.
     *
     * @param nodeId the node id
     * @param entPhysicalIndex the entity physical index
     * @return the OpenNMS hardware entity
     */
    public OnmsHwEntity findEntityByIndex(Integer nodeId, Integer entPhysicalIndex);

    /**
     * Find entity by name.
     *
     * @param nodeId the node id
     * @param entPhysicalName the entity physical name
     * @return the OpenNMS hardware entity
     */
    public OnmsHwEntity findEntityByName(Integer nodeId, String entPhysicalName);

    /**
     * Gets the attribute value.
     *
     * @param nodeId the node id
     * @param entPhysicalIndex the entity physical index
     * @param attributeName the name of the desired attribute
     * @return the attribute value
     */
    public String getAttributeValue(Integer nodeId, Integer entPhysicalIndex, String attributeName);

    /**
     * Gets the attribute value.
     *
     * @param nodeId the node id
     * @param nameSource either the value of entPhysicalName or a regular expression to be applied over the entPhysicalName (should start with '~')
     * @param attributeName the name of the desired attribute
     * @return the attribute value
     */
    public String getAttributeValue(Integer nodeId, String nameSource, String attributeName);

}
