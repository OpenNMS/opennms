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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.model.OnmsAssetRecord;


public class MockAssetRecordDao extends AbstractMockDao<OnmsAssetRecord, Integer> implements AssetRecordDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsAssetRecord asset) {
        asset.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsAssetRecord asset) {
        return asset.getId();
    }

    @Override
    public OnmsAssetRecord findByNodeId(final Integer id) {
        for (final OnmsAssetRecord asset : findAll()) {
            if (asset.getNode().getId() == id) {
                return asset;
            }
        }
        return null;
    }

    @Override
    public Map<String, Integer> findImportedAssetNumbersToNodeIds(final String foreignSource) {
        final Map<String,Integer> ret = new HashMap<String,Integer>();
        for (final OnmsAssetRecord asset : findAll()) {
            ret.put(asset.getAssetNumber(), asset.getNode().getId());
        }
        return ret;
    }

    @Override
    public List<OnmsAssetRecord> getDistinctProperties() {
        return findAll();
    }

}
