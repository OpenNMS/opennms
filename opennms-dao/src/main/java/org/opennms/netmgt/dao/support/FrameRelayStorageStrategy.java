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
package org.opennms.netmgt.dao.support;

import java.util.StringTokenizer;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;

/**
 * This class use the new implementation of SnmpStorageStrategy extending the new
 * IndexStorageStrategy from opennms-services
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class FrameRelayStorageStrategy extends IndexStorageStrategy {

    /** {@inheritDoc} */
    @Override
    public String getResourceNameFromIndex(CollectionResource resource) {
        StringTokenizer indexes = new StringTokenizer(resource.getInstance(), ".");
        String ifIndex = indexes.nextToken();
        String ifName = getInterfaceName(resource.getParent().getName(), ifIndex);
        String dlci = indexes.nextToken();
        return ifName + "." + dlci;
    }
       
    /**
     * <p>getInterfaceName</p>
     *
     * @param nodeId a {@link java.lang.String} object.
     * @param ifIndex a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getInterfaceName(String nodeId, String ifIndex) {
       String label = m_storageStrategyService.getSnmpInterfaceLabel(Integer.valueOf(ifIndex));
       return label != null ? label : ifIndex;
    }

}
