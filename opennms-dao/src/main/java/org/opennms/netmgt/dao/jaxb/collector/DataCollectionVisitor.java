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
package org.opennms.netmgt.dao.jaxb.collector;

import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.Rrd;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;

public interface DataCollectionVisitor {

        /**
         * <p>visitDataCollectionConfig</p>
         *
         * @param dataCollectionConfig a {@link org.opennms.netmgt.config.datacollection.DatacollectionConfig} object.
         */
        void visitDataCollectionConfig(DatacollectionConfig dataCollectionConfig);

        /**
         * <p>completeDataCollectionConfig</p>
         *
         * @param dataCollectionConfig a {@link org.opennms.netmgt.config.datacollection.DatacollectionConfig} object.
         */
        void completeDataCollectionConfig(DatacollectionConfig dataCollectionConfig);

        /**
         * <p>visitSnmpCollection</p>
         *
         * @param snmpCollection a {@link org.opennms.netmgt.config.datacollection.SnmpCollection} object.
         */
        void visitSnmpCollection(SnmpCollection snmpCollection);

        /**
         * <p>completeSnmpCollection</p>
         *
         * @param snmpCollection a {@link org.opennms.netmgt.config.datacollection.SnmpCollection} object.
         */
        void completeSnmpCollection(SnmpCollection snmpCollection);

        /**
         * <p>visitRrd</p>
         *
         * @param rrd a {@link org.opennms.netmgt.config.common.Rrd} object.
         */
        void visitRrd(Rrd rrd);

        /**
         * <p>completeRrd</p>
         *
         * @param rrd a {@link org.opennms.netmgt.config.common.Rrd} object.
         */
        void completeRrd(Rrd rrd);

        /**
         * <p>visitRra</p>
         *
         * @param rra a {@link java.lang.String} object.
         */
        void visitRra(String rra);

        /**
         * <p>completeRra</p>
         *
         * @param rra a {@link java.lang.String} object.
         */
        void completeRra(String rra);

        /**
         * <p>visitSystemDef</p>
         *
         * @param systemDef a {@link org.opennms.netmgt.config.datacollection.SystemDef} object.
         */
        void visitSystemDef(SystemDef systemDef);

        /**
         * <p>completeSystemDef</p>
         *
         * @param systemDef a {@link org.opennms.netmgt.config.datacollection.SystemDef} object.
         */
        void completeSystemDef(SystemDef systemDef);

        /**
         * <p>visitSysOid</p>
         *
         * @param sysoid a {@link java.lang.String} object.
         */
        void visitSysOid(String sysoid);

        /**
         * <p>completeSysOid</p>
         *
         * @param sysoid a {@link java.lang.String} object.
         */
        void completeSysOid(String sysoid);

        /**
         * <p>visitSysOidMask</p>
         *
         * @param sysoidMask a {@link java.lang.String} object.
         */
        void visitSysOidMask(String sysoidMask);

        /**
         * <p>completeSysOidMask</p>
         *
         * @param sysoidMask a {@link java.lang.String} object.
         */
        void completeSysOidMask(String sysoidMask);

        /**
         * <p>visitIpList</p>
         *
         * @param ipList a {@link org.opennms.netmgt.config.datacollection.IpList} object.
         */
        void visitIpList(IpList ipList);

        /**
         * <p>completeIpList</p>
         *
         * @param ipList a {@link org.opennms.netmgt.config.datacollection.IpList} object.
         */
        void completeIpList(IpList ipList);

        /**
         * <p>visitCollect</p>
         *
         * @param collect a {@link org.opennms.netmgt.config.datacollection.Collect} object.
         */
        void visitCollect(Collect collect);

        /**
         * <p>completeCollect</p>
         *
         * @param collect a {@link org.opennms.netmgt.config.datacollection.Collect} object.
         */
        void completeCollect(Collect collect);

        /**
         * <p>visitIncludeGroup</p>
         *
         * @param includeGroup a {@link java.lang.String} object.
         */
        void visitIncludeGroup(String includeGroup);

        /**
         * <p>completeIncludeGroup</p>
         *
         * @param includeGroup a {@link java.lang.String} object.
         */
        void completeIncludeGroup(String includeGroup);

        /**
         * <p>visitGroup</p>
         *
         * @param group a {@link org.opennms.netmgt.config.datacollection.Group} object.
         */
        void visitGroup(Group group);

        /**
         * <p>completeGroup</p>
         *
         * @param group a {@link org.opennms.netmgt.config.datacollection.Group} object.
         */
        void completeGroup(Group group);

        /**
         * <p>visitSubGroup</p>
         *
         * @param subGroup a {@link java.lang.String} object.
         */
        void visitSubGroup(String subGroup);

        /**
         * <p>completeSubGroup</p>
         *
         * @param subGroup a {@link java.lang.String} object.
         */
        void completeSubGroup(String subGroup);

        /**
         * <p>visitMibObj</p>
         *
         * @param mibObj a {@link org.opennms.netmgt.config.datacollection.MibObj} object.
         */
        void visitMibObj(MibObj mibObj);

        /**
         * <p>completeMibObj</p>
         *
         * @param mibObj a {@link org.opennms.netmgt.config.datacollection.MibObj} object.
         */
        void completeMibObj(MibObj mibObj);
}
