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
package org.opennms.netmgt.config.internal.collection;

import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;

public interface DatacollectionConfigVisitor {

    void visitDatacollectionConfig(DatacollectionConfig config);
    void visitDatacollectionConfigComplete();

    void visitSnmpCollection(SnmpCollection collection);
    void visitSnmpCollectionComplete();

    void visitIncludeCollection(IncludeCollection includeCollection);
    void visitIncludeCollectionComplete();
    
    void visitGroup(Group group);
    void visitGroupComplete();
    
    void visitMibObj(MibObj mibObj);
    void visitMibObjComplete();
    
    void visitSystemDef(SystemDef systemDef);
    void visitSystemDefComplete();
    
    void visitIpList(IpList ipList);
    void visitIpListComplete();
    
    void visitCollect(Collect collect);
    void visitCollectComplete();
    
    void visitResourceType(ResourceType resourceType);
    void visitResourceTypeComplete();
}
