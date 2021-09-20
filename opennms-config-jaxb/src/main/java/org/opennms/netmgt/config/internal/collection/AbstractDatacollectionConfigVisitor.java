/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
