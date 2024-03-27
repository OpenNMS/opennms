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
