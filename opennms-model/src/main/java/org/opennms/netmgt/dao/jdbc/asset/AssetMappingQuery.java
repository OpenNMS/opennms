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
package org.opennms.netmgt.dao.jdbc.asset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.springframework.jdbc.object.MappingSqlQuery;

public class AssetMappingQuery extends MappingSqlQuery {
    
    public AssetMappingQuery(DataSource ds, String clause) {
        super(ds, "SELECT assets.nodeID, assets.category, assets.manufacturer, assets.vendor, " +
                "assets.modelNumber, assets.serialNumber, assets.description, " +
                "assets.circuitId, assets.assetNumber, assets.operatingSystem, " +
                "assets.rack, assets.slot, assets.port, " +
                "assets.region, assets.division, assets.department, " +
                "assets.address1, assets.address2, assets.city, assets.state, assets.zip, " +
                "assets.building, assets.floor, assets.room, " +
                "assets.vendorPhone, assets.vendorFax, assets.vendorAssetNumber, " +
                "assets.userLastModified, assets.lastModifiedDate, assets.dateInstalled, " +
                "assets.lease, assets.leaseExpires, assets.supportPhone, assets.maintContract, assets.maintContractExpires, " +
                "assets.displayCategory, assets.notifyCategory, assets.pollerCategory, assets.thresholdCategory, " +
                "assets.comment "+clause);
        
    }
    
    protected DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    protected Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        Integer nodeId = (Integer)rs.getObject("assets.nodeId");
        LazyAssetRecord asset = (LazyAssetRecord)Cache.obtain(OnmsAssetRecord.class, nodeId);
        asset.setLoaded(true);
        asset.setCategory(rs.getString("assets.category"));
        asset.setManufacturer(rs.getString("assets.manufacturer"));
        asset.setVendor(rs.getString("assets.vendor"));
        asset.setModelNumber(rs.getString("assets.modelnumber"));
        asset.setSerialNumber(rs.getString("assets.serialnumber"));
        asset.setDescription(rs.getString("assets.description"));
        asset.setCircuitId(rs.getString("assets.circuitId"));
        asset.setAssetNumber(rs.getString("assets.assetNumber"));
        asset.setOperatingSystem(rs.getString("assets.operatingSystem"));
        asset.setRack(rs.getString("assets.rack"));
        asset.setSlot(rs.getString("assets.slot"));
        asset.setPort(rs.getString("assets.port"));
        asset.setRegion(rs.getString("assets.region"));
        asset.setDivision(rs.getString("assets.division"));
        asset.setDepartment(rs.getString("assets.department"));
        asset.setAddress1(rs.getString("assets.address1"));
        asset.setAddress2(rs.getString("assets.address2"));
        asset.setCity(rs.getString("assets.city"));
        asset.setState(rs.getString("assets.state"));
        asset.setZip(rs.getString("assets.zip"));
        asset.setBuilding(rs.getString("assets.building"));
        asset.setFloor(rs.getString("assets.floor"));
        asset.setRoom(rs.getString("assets.room"));
        asset.setVendorPhone(rs.getString("assets.vendorPhone"));
        asset.setVendorFax(rs.getString("assets.vendorFax"));
        asset.setVendorAssetNumber(rs.getString("assets.vendorAssetNumber"));
        asset.setLastModifiedBy(rs.getString("assets.userLastModified"));
        asset.setLastModifiedDate(rs.getDate("assets.lastModifiedDate"));
        asset.setDateInstalled(rs.getString("assets.dateInstalled"));
        asset.setLease(rs.getString("assets.lease"));
        asset.setLeaseExpires(rs.getString("assets.leaseExpires"));
        asset.setSupportPhone(rs.getString("assets.supportPhone"));
        asset.setMaintContractNumber(rs.getString("assets.maintContract"));
        asset.setMaintContractExpiration(rs.getString("assets.maintContractExpires"));
        asset.setDisplayCategory(rs.getString("assets.displayCategory"));
        asset.setNotifyCategory(rs.getString("assets.notifyCategory"));
        asset.setPollerCategory(rs.getString("assets.pollerCategory"));
        asset.setThresholdCategory(rs.getString("assets.thresholdCategory"));
        asset.setComment(rs.getString("assets.comment"));
        asset.setDirty(false);
        return asset;
    }
    
    public OnmsAssetRecord findUnique() {
        return findUnique((Object[])null);
    }
    
    public OnmsAssetRecord findUnique(Object obj) {
        return findUnique(new Object[] {obj});
    }
    
    public OnmsAssetRecord findUnique(Object[] objs) {
        List assets = execute(objs);
        if (assets.size() > 0)
            return (OnmsAssetRecord) assets.get(0);
        else
            return null;
    }
}