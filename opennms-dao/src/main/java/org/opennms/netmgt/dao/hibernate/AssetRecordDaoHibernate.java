/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.netmgt.dao.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.model.OnmsAssetRecord;

public class AssetRecordDaoHibernate extends AbstractDaoHibernate<OnmsAssetRecord, Integer> implements AssetRecordDao {

    /**
     * <p>Constructor for AssetRecordDaoHibernate.</p>
     */
    public AssetRecordDaoHibernate() {
		super(OnmsAssetRecord.class);
	}

    /**
     * <p>findByNodeId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAssetRecord} object.
     */
    public OnmsAssetRecord findByNodeId(Integer id) {
        return (OnmsAssetRecord)findUnique("from OnmsAssetRecord rec where rec.nodeId = ?", id);
    }

    /**
     * <p>findImportedAssetNumbersToNodeIds</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Integer> findImportedAssetNumbersToNodeIds(String foreignSource) {

        @SuppressWarnings("unchecked")
        List<Object[]> assetNumbers = getHibernateTemplate().find("select a.node.id, a.assetNumber from OnmsAssetRecord a where a.assetNumber like '"+foreignSource+"%'");

        Map<String, Integer> assetNumberMap = new HashMap<String, Integer>();
        for (Object[] an : assetNumbers) {
            assetNumberMap.put((String)an[1], (Integer)an[0]);
        }
        return assetNumberMap;
    }


}
