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
        super(ds, "SELECT nodeID, category, manufacturer, vendor, " +
                "modelNumber, serialNumber, description, " +
                "circuitId, assetNumber, operatingSystem, " +
                "rack, slot, port, " +
                "region, division, department, " +
                "address1, address2, city, state, zip, " +
                "building, floor, room, " +
                "vendorPhone, vendorFax, vendorAssetNumber, " +
                "userLastModified, lastModifiedDate, dateInstalled, " +
                "lease, leaseExpires, supportPhone, maintContract, maintContractExpires, " +
                "displayCategory, notifyCategory, pollerCategory, thresholdCategory, " +
                "comment "+clause);
        
    }
    
    protected DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    protected Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        Integer nodeId = (Integer)rs.getObject("nodeId");
        LazyAssetRecord asset = (LazyAssetRecord)Cache.obtain(OnmsAssetRecord.class, nodeId);
        asset.setLoaded(true);
        asset.setCategory(rs.getString("category"));
        asset.setManufacturer(rs.getString("manufacturer"));
        asset.setVendor(rs.getString("vendor"));
        asset.setModelNumber(rs.getString("modelnumber"));
        asset.setSerialNumber(rs.getString("serialnumber"));
        asset.setDescription(rs.getString("description"));
        asset.setCircuitId(rs.getString("circuitId"));
        asset.setAssetNumber(rs.getString("assetNumber"));
        asset.setOperatingSystem(rs.getString("operatingSystem"));
        asset.setRack(rs.getString("rack"));
        asset.setSlot(rs.getString("slot"));
        asset.setPort(rs.getString("port"));
        asset.setRegion(rs.getString("region"));
        asset.setDivision(rs.getString("division"));
        asset.setDepartment(rs.getString("department"));
        asset.setAddress1(rs.getString("address1"));
        asset.setAddress2(rs.getString("address2"));
        asset.setCity(rs.getString("city"));
        asset.setState(rs.getString("state"));
        asset.setZip(rs.getString("zip"));
        asset.setBuilding(rs.getString("building"));
        asset.setFloor(rs.getString("floor"));
        asset.setRoom(rs.getString("room"));
        asset.setVendorPhone(rs.getString("vendorPhone"));
        asset.setVendorFax(rs.getString("vendorFax"));
        asset.setVendorAssetNumber(rs.getString("vendorAssetNumber"));
        asset.setLastModifiedBy(rs.getString("userLastModified"));
        asset.setLastModifiedDate(rs.getDate("lastModifiedDate"));
        asset.setDateInstalled(rs.getString("dateInstalled"));
        asset.setLease(rs.getString("lease"));
        asset.setLeaseExpires(rs.getString("leaseExpires"));
        asset.setSupportPhone(rs.getString("supportPhone"));
        asset.setMaintContractNumber(rs.getString("maintContract"));
        asset.setMaintContractExpiration(rs.getString("maintContractExpires"));
        asset.setDisplayCategory(rs.getString("displayCategory"));
        asset.setNotifyCategory(rs.getString("notifyCategory"));
        asset.setPollerCategory(rs.getString("pollerCategory"));
        asset.setThresholdCategory(rs.getString("thresholdCategory"));
        asset.setComment(rs.getString("comment"));
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