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
package org.opennms.netmgt.collectd;

import java.util.Set;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.StorageStrategyService;

/**
 * <p>CollectionAgent interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface SnmpCollectionAgent extends CollectionAgent, StorageStrategyService {

    /**
     * <p>setSavedIfCount</p>
     *
     * @param ifCount a int.
     */
    void setSavedIfCount(int ifCount);

    /**
     * <p>getSavedIfCount</p>
     *
     * @return a int.
     */
    int getSavedIfCount();

    /**
     * <p>getSysObjectId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getSysObjectId();

    /**
     * <p>validateAgent</p>
     * @throws CollectionInitializationException 
     */
    void validateAgent() throws CollectionInitializationException;

    /**
     * <p>getSnmpInterfaceInfo</p>
     *
     * @param type a {@link org.opennms.netmgt.collectd.IfResourceType} object.
     * @return a {@link java.util.Set} object.
     */
    Set<IfInfo> getSnmpInterfaceInfo(IfResourceType type);

}
