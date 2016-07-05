/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.rrd.RrdRepository;

public class MockDataCollectionConfigDao implements DataCollectionConfigDao {

    @Override
    public String getSnmpStorageFlag(final String collectionName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<MibObject> getMibObjectList(final String cName, final String aSysoid, final String anAddress, final int ifType) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<MibObjProperty> getMibObjProperties(final String cName, final String aSysoid, final String anAddress) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Map<String, ResourceType> getConfiguredResourceTypes() {
        return Collections.emptyMap();
    }

    @Override
    public RrdRepository getRrdRepository(final String collectionName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int getStep(final String collectionName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<String> getRRAList(final String collectionName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public String getRrdPath() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public DatacollectionConfig getRootDataCollection() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<String> getAvailableDataCollectionGroups() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<String> getAvailableSystemDefs() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<String> getAvailableMibGroups() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void reload() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Date getLastUpdate() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
