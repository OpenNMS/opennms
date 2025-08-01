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
package org.opennms.netmgt.enlinkd.service.api;


import java.util.Date;
import java.util.List;

import org.opennms.netmgt.enlinkd.model.OspfArea;
import org.opennms.netmgt.enlinkd.model.OspfAreaTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfElement;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.model.OspfLinkTopologyEntity;

public interface OspfTopologyService extends TopologyService {
        
    void delete(int nodeid);
    void reconcile(int nodeId, Date now);
    void store(int nodeId, OspfElement cdp);
    void store(int nodeId, OspfLink link);
    void store(int nodeId, OspfArea area);

    List<OspfElement> findAllOspfElements();
    List<OspfAreaTopologyEntity> findAllOspfAreas();
    List<TopologyConnection<OspfLinkTopologyEntity, OspfLinkTopologyEntity>> match();

    void deletePersistedData();

}
