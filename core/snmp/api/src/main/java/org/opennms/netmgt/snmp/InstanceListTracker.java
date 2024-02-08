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
package org.opennms.netmgt.snmp;

public class InstanceListTracker extends AggregateTracker {
    
    public InstanceListTracker(SnmpObjId base, String instances) {
        this(base, SnmpInstId.convertToSnmpInstIds(instances), null);
    }
    
    public InstanceListTracker(SnmpObjId base, String instances, CollectionTracker parent) {
        this(base, SnmpInstId.convertToSnmpInstIds(instances), parent);
    }
    
    public InstanceListTracker(SnmpObjId base, SnmpInstId[] instances) {
        this(base, instances, null);
    }
    
    public InstanceListTracker(SnmpObjId base, SnmpInstId[] instances, CollectionTracker parent) {
        super(getSingleInstanceTrackers(base, instances), parent);
    }
    
    private static SingleInstanceTracker[] getSingleInstanceTrackers(SnmpObjId base, SnmpInstId[] instances) {
        SingleInstanceTracker[] trackers = new SingleInstanceTracker[instances.length];
        for (int i = 0; i < instances.length; i++) {
            trackers[i] = new SingleInstanceTracker(base, instances[i]);
        }
        return trackers;
        
    }

}
