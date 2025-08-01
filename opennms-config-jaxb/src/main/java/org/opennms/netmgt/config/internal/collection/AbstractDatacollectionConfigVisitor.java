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

public class AbstractDatacollectionConfigVisitor implements DatacollectionConfigVisitor {

    @Override
    public void visitDatacollectionConfig(DatacollectionConfig config) {
    }

    @Override
    public void visitDatacollectionConfigComplete() {
    }

    @Override
    public void visitSnmpCollection(SnmpCollection collection) {
    }

    @Override
    public void visitSnmpCollectionComplete() {
    }

    @Override
    public void visitIncludeCollection(IncludeCollection includeCollection) {
    }

    @Override
    public void visitIncludeCollectionComplete() {
    }

    @Override
    public void visitGroup(Group group) {
    }

    @Override
    public void visitGroupComplete() {
    }

    @Override
    public void visitMibObj(MibObj mibObj) {
    }

    @Override
    public void visitMibObjComplete() {
    }

    @Override
    public void visitSystemDef(SystemDef systemDef) {
    }

    @Override
    public void visitSystemDefComplete() {
    }

    @Override
    public void visitIpList(IpList ipList) {
    }

    @Override
    public void visitIpListComplete() {
    }

    @Override
    public void visitCollect(Collect collect) {
    }

    @Override
    public void visitCollectComplete() {
    }

    @Override
    public void visitResourceType(ResourceType resourceType) {
    }

    @Override
    public void visitResourceTypeComplete() {
    }

}
