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
        super(ds, "SELECT assets.nodeID as assets_nodeID, assets.category as assets_category, " +
        		"assets.manufacturer as assets_manufacturer, assets.vendor as assets_vendor, " +
                "assets.modelNumber as assets_modelNumber, assets.serialNumber as assets_serialNumber, " +
                "assets.description as assets_description, " +
                "assets.circuitId as assets_circuitId, assets.assetNumber as assets_assetNumber, " +
                "assets.operatingSystem as assets_operatingSystem, " +
                "assets.rack as assets_rack, assets.slot as assets_slot, " +
                "assets.port as assets_port, " +
                "assets.region as assets_region, assets.division as assets_division, " +
                "assets.department as assets_department, " +
                "assets.address1 as assets_address1, assets.address2 as assets_address2, " +
                "assets.city as assets_city, assets.state as assets_state, " +
                "assets.zip as assets_zip, " +
                "assets.building as assets_building, assets.floor as assets_floor, " +
                "assets.room as assets_room, " +
                "assets.vendorPhone as assets_vendorPhone, assets.vendorFax as assets_vendorFax, " +
                "assets.vendorAssetNumber as assets_vendorAssetNumber, " +
                "assets.userLastModified as assets_userLastModified, " +
                "assets.lastModifiedDate as assets_lastModifiedDate, " +
                "assets.dateInstalled as assets_dateInstalled, " +
                "assets.lease as assets_lease, assets.leaseExpires as assets_leaseExpires, " +
                "assets.supportPhone as assets_supportPhone, " +
                "assets.maintContract as assets_maintContract, assets.maintContractExpires as assets_maintContractExpires, " +
                "assets.displayCategory as assets_displayCategory, " +
                "assets.notifyCategory as assets_notifyCategory, " +
                "assets.pollerCategory as assets_pollerCategory, " +
                "assets.thresholdCategory as assets_thresholdCategory, " +
                "assets.comment as assets_comment, " +
                "assets.managedObjectInstance as managedObjectInstance , " +
                "assets.managedObjectType as managedObjectType "+clause);
        
    }
    
    protected DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    protected Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        Integer nodeId = (Integer)rs.getObject("assets_nodeId");
        LazyAssetRecord asset = (LazyAssetRecord)Cache.obtain(OnmsAssetRecord.class, nodeId);
        asset.setLoaded(true);
        asset.setCategory(rs.getString("assets_category"));
        asset.setManufacturer(rs.getString("assets_manufacturer"));
        asset.setVendor(rs.getString("assets_vendor"));
        asset.setModelNumber(rs.getString("assets_modelnumber"));
        asset.setSerialNumber(rs.getString("assets_serialnumber"));
        asset.setDescription(rs.getString("assets_description"));
        asset.setCircuitId(rs.getString("assets_circuitId"));
        asset.setAssetNumber(rs.getString("assets_assetNumber"));
        asset.setOperatingSystem(rs.getString("assets_operatingSystem"));
        asset.setRack(rs.getString("assets_rack"));
        asset.setSlot(rs.getString("assets_slot"));
        asset.setPort(rs.getString("assets_port"));
        asset.setRegion(rs.getString("assets_region"));
        asset.setDivision(rs.getString("assets_division"));
        asset.setDepartment(rs.getString("assets_department"));
        asset.setAddress1(rs.getString("assets_address1"));
        asset.setAddress2(rs.getString("assets_address2"));
        asset.setCity(rs.getString("assets_city"));
        asset.setState(rs.getString("assets_state"));
        asset.setZip(rs.getString("assets_zip"));
        asset.setBuilding(rs.getString("assets_building"));
        asset.setFloor(rs.getString("assets_floor"));
        asset.setRoom(rs.getString("assets_room"));
        asset.setVendorPhone(rs.getString("assets_vendorPhone"));
        asset.setVendorFax(rs.getString("assets_vendorFax"));
        asset.setVendorAssetNumber(rs.getString("assets_vendorAssetNumber"));
        asset.setLastModifiedBy(rs.getString("assets_userLastModified"));
        asset.setLastModifiedDate(rs.getDate("assets_lastModifiedDate"));
        asset.setDateInstalled(rs.getString("assets_dateInstalled"));
        asset.setLease(rs.getString("assets_lease"));
        asset.setLeaseExpires(rs.getString("assets_leaseExpires"));
        asset.setSupportPhone(rs.getString("assets_supportPhone"));
        asset.setMaintContractNumber(rs.getString("assets_maintContract"));
        asset.setMaintContractExpiration(rs.getString("assets_maintContractExpires"));
        asset.setDisplayCategory(rs.getString("assets_displayCategory"));
        asset.setNotifyCategory(rs.getString("assets_notifyCategory"));
        asset.setPollerCategory(rs.getString("assets_pollerCategory"));
        asset.setThresholdCategory(rs.getString("assets_thresholdCategory"));
        asset.setComment(rs.getString("assets_comment"));
        asset.setManagedObjectInstance(rs.getString("managedObjectInstance")); // changed
        asset.setManagedObjectType(rs.getString("managedObjectType")); // changed
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