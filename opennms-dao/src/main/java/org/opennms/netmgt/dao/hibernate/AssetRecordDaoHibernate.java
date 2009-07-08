/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.dao.hibernate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.model.OnmsAssetRecord;

public class AssetRecordDaoHibernate extends AbstractDaoHibernate<OnmsAssetRecord, Integer> implements AssetRecordDao {

    public AssetRecordDaoHibernate() {
		super(OnmsAssetRecord.class);
	}

    public OnmsAssetRecord findByNodeId(Integer id) {
        return (OnmsAssetRecord)findUnique("from OnmsAssetRecord rec where rec.nodeId = ?", id);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Integer> findImportedAssetNumbersToNodeIds(String foreignSource) {
        List assetNumbers = getHibernateTemplate().find("select a.node.id, a.assetNumber from OnmsAssetRecord a where a.assetNumber like '"+foreignSource+"%'");
        Map<String, Integer> assetNumberMap = new HashMap<String, Integer>();
        for (Iterator it = assetNumbers.iterator(); it.hasNext();) {
            Object[] an = (Object[]) it.next();
            assetNumberMap.put((String)an[1], (Integer)an[0]);
        }
        return assetNumberMap;
    }


}
