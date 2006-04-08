//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.jdbc.asset.AssetRecordSaveOrUpdate;
import org.opennms.netmgt.dao.jdbc.asset.FindAll;
import org.opennms.netmgt.dao.jdbc.asset.FindByAssetId;
import org.opennms.netmgt.dao.jdbc.asset.LazyAssetRecord;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.springframework.jdbc.core.RowCallbackHandler;

public class AssetRecordDaoJdbc extends AbstractDaoJdbc implements AssetRecordDao {
    
    public static class Save extends AssetRecordSaveOrUpdate {
        
        // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE UPDATE STATEMENT AND THE
        // PARAMETERS IN AssetRecordSaveOrUpdate
        private static final String insertStmt =
            "insert into assets (" +
            "category, " +
            "manufacturer, " +
            "vendor, " +
            "modelNumber, " +
            "serialNumber, " +
            "description, " +
            "circuitId, " +
            "assetNumber, " +
            "operatingSystem, " +
            "rack, " +
            "slot, " +
            "port, " +
            "region, " +
            "division, " +
            "department, " +
            "address1, " +
            "address2, " +
            "city, " +
            "state, " +
            "zip, " +
            "building, " +
            "floor, " +
            "room, " +
            "vendorPhone, " +
            "vendorFax, " +
            "vendorAssetNumber, " +
            "userLastModified, " +
            "lastModifiedDate, " +
            "dateInstalled, " +
            "lease, " +
            "leaseExpires, " +
            "supportPhone," +
            " maintContract, " +
            "maintContractExpires, " +
            "displayCategory, " +
            "notifyCategory, " +
            "pollerCategory, " +
            "thresholdCategory, " +
            "comment, " +
            "nodeID" +
            ") values " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        
        public Save(DataSource ds) {
            super(ds, insertStmt);
        }
        
        public int doInsert(OnmsAssetRecord asset) {
            return persist(asset);
        }

        
    }
    
    public static class Update extends AssetRecordSaveOrUpdate {

        // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE INSERT STATEMENT AND THE
        // PARAMETERS IN AssetRecordSaveOrUpdate
        private static final String updateStmt =
            "update assets set " +
            "category = ?, " +
            "manufacturer = ?, " +
            "vendor = ?, " +
            "modelNumber = ?, " +
            "serialNumber = ?, " +
            "description = ?, " +
            "circuitId = ?, " +
            "assetNumber = ?, " +
            "operatingSystem = ?, " +
            "rack = ?, " +
            "slot = ?, " +
            "port = ?, " +
            "region = ?, " +
            "division = ?, " +
            "department = ?, " +
            "address1 = ?, " +
            "address2 = ?, " +
            "city = ?, " +
            "state = ?, " +
            "zip = ?, " +
            "building = ?, " +
            "floor = ?, " +
            "room = ?, " +
            "vendorPhone = ?, " +
            "vendorFax = ?, " +
            "vendorAssetNumber = ?, " +
            "userLastModified = ?, " +
            "lastModifiedDate = ?, " +
            "dateInstalled = ?, " +
            "lease = ?, " +
            "leaseExpires = ?, " +
            "supportPhone = ?," +
            " maintContract = ?, " +
            "maintContractExpires = ?, " +
            "displayCategory = ?, " +
            "notifyCategory = ?, " +
            "pollerCategory = ?, " +
            "thresholdCategory = ?, " +
            "comment = ? " +
            "where nodeID = ?";

        public Update(DataSource ds) {
            super(ds, updateStmt);
        }
        
        public int doUpdate(OnmsAssetRecord asset) {
            return persist(asset);
        }
        
    }
    

    
    public AssetRecordDaoJdbc() {
        super();
    }
    

    public AssetRecordDaoJdbc(DataSource ds) {
        super(ds);
    }

    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from assets");
    }
    
    public Collection findAll() {
        return new FindAll(getDataSource()).execute();
    }
    
    
    public Map findImportedAssetNumbersToNodeIds() {
        
        final Map assetNumbersToNodeIds = new HashMap();
        getJdbcTemplate().query("select assetNumber, nodeId from assets where assetNumber like '"+AssetRecordDao.IMPORTED_ID+"%'", new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                assetNumbersToNodeIds.put(rs.getString("assetNumber"), rs.getObject("nodeId"));
            }
        });
        return assetNumbersToNodeIds;
    }

    public OnmsAssetRecord findByNodeId(Integer id) {
        return get(id);
    }

    public void flush() {
    }

    public OnmsAssetRecord get(Integer id) {
        return new FindByAssetId(getDataSource()).findUnique(id);
    }

    public OnmsAssetRecord load(Integer id) {
        OnmsAssetRecord asset = get(id);
        if (asset == null)
            throw new IllegalArgumentException("unable to load asset with id: "+id);
        return asset;
    }

    public void save(OnmsAssetRecord asset) {
        new Save(getDataSource()).doInsert(asset);
    }

    public void update(OnmsAssetRecord asset) {
    	if (!isDirty(asset)) return;
        new Update(getDataSource()).doUpdate(asset);
    }


	private boolean isDirty(OnmsAssetRecord asset) {
		if (asset instanceof LazyAssetRecord) {
			LazyAssetRecord lazyAsset = (LazyAssetRecord) asset;
			return lazyAsset.isDirty();
		}
		return true;
	}
    
   

}
