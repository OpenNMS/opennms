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
