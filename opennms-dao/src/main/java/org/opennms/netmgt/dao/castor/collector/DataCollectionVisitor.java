/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.castor.collector;

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
        public abstract void visitDataCollectionConfig(DatacollectionConfig dataCollectionConfig);

        /**
         * <p>completeDataCollectionConfig</p>
         *
         * @param dataCollectionConfig a {@link org.opennms.netmgt.config.datacollection.DatacollectionConfig} object.
         */
        public abstract void completeDataCollectionConfig(DatacollectionConfig dataCollectionConfig);

        /**
         * <p>visitSnmpCollection</p>
         *
         * @param snmpCollection a {@link org.opennms.netmgt.config.datacollection.SnmpCollection} object.
         */
        public abstract void visitSnmpCollection(SnmpCollection snmpCollection);

        /**
         * <p>completeSnmpCollection</p>
         *
         * @param snmpCollection a {@link org.opennms.netmgt.config.datacollection.SnmpCollection} object.
         */
        public abstract void completeSnmpCollection(SnmpCollection snmpCollection);

        /**
         * <p>visitRrd</p>
         *
         * @param rrd a {@link org.opennms.netmgt.config.common.Rrd} object.
         */
        public abstract void visitRrd(Rrd rrd);

        /**
         * <p>completeRrd</p>
         *
         * @param rrd a {@link org.opennms.netmgt.config.common.Rrd} object.
         */
        public abstract void completeRrd(Rrd rrd);

        /**
         * <p>visitRra</p>
         *
         * @param rra a {@link java.lang.String} object.
         */
        public abstract void visitRra(String rra);

        /**
         * <p>completeRra</p>
         *
         * @param rra a {@link java.lang.String} object.
         */
        public abstract void completeRra(String rra);

        /**
         * <p>visitSystemDef</p>
         *
         * @param systemDef a {@link org.opennms.netmgt.config.datacollection.SystemDef} object.
         */
        public abstract void visitSystemDef(SystemDef systemDef);

        /**
         * <p>completeSystemDef</p>
         *
         * @param systemDef a {@link org.opennms.netmgt.config.datacollection.SystemDef} object.
         */
        public abstract void completeSystemDef(SystemDef systemDef);

        /**
         * <p>visitSysOid</p>
         *
         * @param sysoid a {@link java.lang.String} object.
         */
        public abstract void visitSysOid(String sysoid);

        /**
         * <p>completeSysOid</p>
         *
         * @param sysoid a {@link java.lang.String} object.
         */
        public abstract void completeSysOid(String sysoid);

        /**
         * <p>visitSysOidMask</p>
         *
         * @param sysoidMask a {@link java.lang.String} object.
         */
        public abstract void visitSysOidMask(String sysoidMask);

        /**
         * <p>completeSysOidMask</p>
         *
         * @param sysoidMask a {@link java.lang.String} object.
         */
        public abstract void completeSysOidMask(String sysoidMask);

        /**
         * <p>visitIpList</p>
         *
         * @param ipList a {@link org.opennms.netmgt.config.datacollection.IpList} object.
         */
        public abstract void visitIpList(IpList ipList);

        /**
         * <p>completeIpList</p>
         *
         * @param ipList a {@link org.opennms.netmgt.config.datacollection.IpList} object.
         */
        public abstract void completeIpList(IpList ipList);

        /**
         * <p>visitCollect</p>
         *
         * @param collect a {@link org.opennms.netmgt.config.datacollection.Collect} object.
         */
        public abstract void visitCollect(Collect collect);

        /**
         * <p>completeCollect</p>
         *
         * @param collect a {@link org.opennms.netmgt.config.datacollection.Collect} object.
         */
        public abstract void completeCollect(Collect collect);

        /**
         * <p>visitIncludeGroup</p>
         *
         * @param includeGroup a {@link java.lang.String} object.
         */
        public abstract void visitIncludeGroup(String includeGroup);

        /**
         * <p>completeIncludeGroup</p>
         *
         * @param includeGroup a {@link java.lang.String} object.
         */
        public abstract void completeIncludeGroup(String includeGroup);

        /**
         * <p>visitGroup</p>
         *
         * @param group a {@link org.opennms.netmgt.config.datacollection.Group} object.
         */
        public abstract void visitGroup(Group group);

        /**
         * <p>completeGroup</p>
         *
         * @param group a {@link org.opennms.netmgt.config.datacollection.Group} object.
         */
        public abstract void completeGroup(Group group);

        /**
         * <p>visitSubGroup</p>
         *
         * @param subGroup a {@link java.lang.String} object.
         */
        public abstract void visitSubGroup(String subGroup);

        /**
         * <p>completeSubGroup</p>
         *
         * @param subGroup a {@link java.lang.String} object.
         */
        public abstract void completeSubGroup(String subGroup);

        /**
         * <p>visitMibObj</p>
         *
         * @param mibObj a {@link org.opennms.netmgt.config.datacollection.MibObj} object.
         */
        public abstract void visitMibObj(MibObj mibObj);

        /**
         * <p>completeMibObj</p>
         *
         * @param mibObj a {@link org.opennms.netmgt.config.datacollection.MibObj} object.
         */
        public abstract void completeMibObj(MibObj mibObj);

        
}
