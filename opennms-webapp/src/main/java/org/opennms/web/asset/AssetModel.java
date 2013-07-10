/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.asset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <p>AssetModel class.</p>
 */
public class AssetModel {
	
	private static final Logger LOG = LoggerFactory.getLogger(AssetModel.class);


    /**
     * <p>getAsset</p>
     *
     * @param nodeId a int.
     * @return a {@link org.opennms.web.asset.Asset} object.
     * @throws java.sql.SQLException if any.
     */
    public Asset getAsset(int nodeId) throws SQLException {
        Asset asset = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ASSETS WHERE NODEID=?");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();

            Asset[] assets = rs2Assets(rs);

            // XXX what if this returns more than one?
            if (assets.length > 0) {
                asset = assets[0];
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return asset;
    }

    /**
     * <p>getAllAssets</p>
     *
     * @return an array of {@link org.opennms.web.asset.Asset} objects.
     * @throws java.sql.SQLException if any.
     */
    public Asset[] getAllAssets() throws SQLException {
        Asset[] assets = new Asset[0];

        final DBUtils d = new DBUtils(getClass());
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ASSETS");
            d.watch(stmt);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            assets = rs2Assets(rs);
        } finally {
            d.cleanUp();
        }

        return assets;
    }

    /**
     * <p>createAsset</p>
     *
     * @param asset a {@link org.opennms.web.asset.Asset} object.
     * @throws java.sql.SQLException if any.
     */
    public void createAsset(Asset asset) throws SQLException {
        Assert.notNull(asset, "argument asset cannot be null");

        final DBUtils d = new DBUtils(getClass());
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);

            PreparedStatement stmt = conn.prepareStatement("INSERT INTO ASSETS (nodeID,category,manufacturer,vendor,modelNumber,serialNumber,description,circuitId,assetNumber,operatingSystem,rack,slot,port,region,division,department,address1,address2,city,state,zip,building,floor,room,vendorPhone,vendorFax,userLastModified,lastModifiedDate,dateInstalled,lease,leaseExpires,supportPhone,maintContract,vendorAssetNumber,maintContractExpires,displayCategory,notifyCategory,pollerCategory,thresholdCategory,comment,username,password,enable,connection,autoenable,cpu,ram,storagectrl,hdd1,hdd2,hdd3,hdd4,hdd5,hdd6,numpowersupplies,inputpower,additionalhardware,admin,snmpcommunity,rackunitheight,longitude,latitude,country) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            d.watch(stmt);
            stmt.setInt(1, asset.nodeId);
            stmt.setString(2, asset.category);
            stmt.setString(3, asset.manufacturer);
            stmt.setString(4, asset.vendor);
            stmt.setString(5, asset.modelNumber);
            stmt.setString(6, asset.serialNumber);
            stmt.setString(7, asset.description);
            stmt.setString(8, asset.circuitId);
            stmt.setString(9, asset.assetNumber);
            stmt.setString(10, asset.operatingSystem);
            stmt.setString(11, asset.rack);
            stmt.setString(12, asset.slot);
            stmt.setString(13, asset.port);
            stmt.setString(14, asset.region);
            stmt.setString(15, asset.division);
            stmt.setString(16, asset.department);
            stmt.setString(17, asset.address1);
            stmt.setString(18, asset.address2);
            stmt.setString(19, asset.city);
            stmt.setString(20, asset.state);
            stmt.setString(21, asset.zip);
            stmt.setString(22, asset.building);
            stmt.setString(23, asset.floor);
            stmt.setString(24, asset.room);
            stmt.setString(25, asset.vendorPhone);
            stmt.setString(26, asset.vendorFax);
            stmt.setString(27, asset.userLastModified);
            stmt.setTimestamp(28, new Timestamp(asset.lastModifiedDate.getTime()));
            stmt.setString(29, asset.dateInstalled);
            stmt.setString(30, asset.lease);
            stmt.setString(31, asset.leaseExpires);
            stmt.setString(32, asset.supportPhone);
            stmt.setString(33, asset.maintContract);
            stmt.setString(34, asset.vendorAssetNumber);
            stmt.setString(35, asset.maintContractExpires);
            stmt.setString(36, asset.displayCategory);
            stmt.setString(37, asset.notifyCategory);
            stmt.setString(38, asset.pollerCategory);
            stmt.setString(39, asset.thresholdCategory);
            stmt.setString(40, asset.comments);
            stmt.setString(41, asset.username);
            stmt.setString(42, asset.password);
            stmt.setString(43, asset.enable);
            stmt.setString(44, asset.connection);
            stmt.setString(45, asset.autoenable);
            stmt.setString(46, asset.cpu);
            stmt.setString(47, asset.ram);
            stmt.setString(48, asset.storagectrl);
            stmt.setString(49, asset.hdd1);
            stmt.setString(50, asset.hdd2);
            stmt.setString(51, asset.hdd3);
            stmt.setString(52, asset.hdd4);
            stmt.setString(53, asset.hdd5);
            stmt.setString(54, asset.hdd6);
            stmt.setString(55, asset.numpowersupplies);
            stmt.setString(56, asset.inputpower);
            stmt.setString(57, asset.additionalhardware);
            stmt.setString(58, asset.admin);
            stmt.setString(59, asset.snmpcommunity);
            stmt.setString(60, asset.rackunitheight);
            stmt.setFloat(61, safeFloat(asset.longitude));
            stmt.setFloat(62, safeFloat(asset.latitude));
            stmt.setString(63, asset.country);

            stmt.execute();
        } finally {
            d.cleanUp();
        }
    }
    
    /**
     * <p>modifyAsset</p>
     *
     * @param asset a {@link org.opennms.web.asset.Asset} object.
     * @throws java.sql.SQLException if any.
     */
    public void modifyAsset(Asset asset) throws SQLException {
        Assert.notNull(asset, "argument asset cannot be null");

        final DBUtils d = new DBUtils(getClass());
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);

            PreparedStatement stmt = conn.prepareStatement("UPDATE ASSETS SET category=?,manufacturer=?,vendor=?,modelNumber=?,serialNumber=?,description=?,circuitId=?,assetNumber=?,operatingSystem=?,rack=?,slot=?,port=?,region=?,division=?,department=?,address1=?,address2=?,city=?,state=?,zip=?,building=?,floor=?,room=?,vendorPhone=?,vendorFax=?,userLastModified=?,lastModifiedDate=?,dateInstalled=?,lease=?,leaseExpires=?,supportPhone=?,maintContract=?,vendorAssetNumber=?,maintContractExpires=?,displayCategory=?,notifyCategory=?,pollerCategory=?,thresholdCategory=?,comment=?, username=?, password=?,enable=?,connection=?,autoenable=?,cpu=?,ram=?,storagectrl=?,hdd1=?,hdd2=?,hdd3=?,hdd4=?,hdd5=?,hdd6=?,numpowersupplies=?,inputpower=?,additionalhardware=?,admin=?,snmpcommunity=?,rackunitheight=?,longitude=?,latitude=?,country=? WHERE nodeid=?");
            d.watch(stmt);
            stmt.setString(1, asset.category);
            stmt.setString(2, asset.manufacturer);
            stmt.setString(3, asset.vendor);
            stmt.setString(4, asset.modelNumber);
            stmt.setString(5, asset.serialNumber);
            stmt.setString(6, asset.description);
            stmt.setString(7, asset.circuitId);
            stmt.setString(8, asset.assetNumber);
            stmt.setString(9, asset.operatingSystem);
            stmt.setString(10, asset.rack);
            stmt.setString(11, asset.slot);
            stmt.setString(12, asset.port);
            stmt.setString(13, asset.region);
            stmt.setString(14, asset.division);
            stmt.setString(15, asset.department);
            stmt.setString(16, asset.address1);
            stmt.setString(17, asset.address2);
            stmt.setString(18, asset.city);
            stmt.setString(19, asset.state);
            stmt.setString(20, asset.zip);
            stmt.setString(21, asset.building);
            stmt.setString(22, asset.floor);
            stmt.setString(23, asset.room);
            stmt.setString(24, asset.vendorPhone);
            stmt.setString(25, asset.vendorFax);
            stmt.setString(26, asset.userLastModified);
            stmt.setTimestamp(27, new Timestamp(asset.lastModifiedDate.getTime()));
            stmt.setString(28, asset.dateInstalled);
            stmt.setString(29, asset.lease);
            stmt.setString(30, asset.leaseExpires);
            stmt.setString(31, asset.supportPhone);
            stmt.setString(32, asset.maintContract);
            stmt.setString(33, asset.vendorAssetNumber);
            stmt.setString(34, asset.maintContractExpires);
            stmt.setString(35, asset.displayCategory);
            stmt.setString(36, asset.notifyCategory);
            stmt.setString(37, asset.pollerCategory);
            stmt.setString(38, asset.thresholdCategory);
            stmt.setString(39, asset.comments);
            stmt.setString(40, asset.username);
            stmt.setString(41, asset.password);
            stmt.setString(42, asset.enable);
            stmt.setString(43, asset.connection);
            stmt.setString(44, asset.autoenable);
            stmt.setString(45, asset.cpu);
            stmt.setString(46, asset.ram);
            stmt.setString(47, asset.storagectrl);
            stmt.setString(48, asset.hdd1);
            stmt.setString(49, asset.hdd2);
            stmt.setString(50, asset.hdd3);
            stmt.setString(51, asset.hdd4);
            stmt.setString(52, asset.hdd5);
            stmt.setString(53, asset.hdd6);
            stmt.setString(54, asset.numpowersupplies);
            stmt.setString(55, asset.inputpower);
            stmt.setString(56, asset.additionalhardware);
            stmt.setString(57, asset.admin);
            stmt.setString(58, asset.snmpcommunity);
            stmt.setString(59, asset.rackunitheight);
            final Float longitude = safeFloat(asset.longitude);
            if (longitude == null) {
                stmt.setNull(60, Types.FLOAT);
            } else {
                stmt.setFloat(60, longitude);
            }
            final Float latitude = safeFloat(asset.latitude);
            if (latitude == null) {
                stmt.setNull(61, Types.FLOAT);
            } else {
                stmt.setFloat(61, latitude);
            }
            stmt.setString(62, asset.country);
            stmt.setInt(63, asset.nodeId);

            stmt.execute();
        } finally {
            d.cleanUp();
        }
    }

    public static class MatchingAsset extends Object {
        public int nodeId;

        public String nodeLabel;

        public String matchingValue;

        public String columnSearched;
    }

    /**
     * <p>searchAssets</p>
     *
     * @param columnName a {@link java.lang.String} object.
     * @param searchText a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.asset.AssetModel.MatchingAsset} objects.
     * @throws java.sql.SQLException if any.
     */
    public static MatchingAsset[] searchAssets(String columnName, String searchText) throws SQLException {
        Assert.notNull(columnName, "argument columnName cannot be null");
        Assert.notNull(searchText, "argument searchText cannot be null");

        /* 
         * TODO: delete this test soon.
         * The category column is used in the search and but is not in the
         * s_columns static var.  This breaks the WebUI.
         */
        // Assert.isTrue(isColumnValid(columnName), "Column \"" + columnName + "\" is not a valid column name");
        
        List<MatchingAsset> list = new ArrayList<MatchingAsset>();

        columnName = WebSecurityUtils.sanitizeDbColumnName(columnName);

        final DBUtils d = new DBUtils(AssetModel.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT ASSETS.NODEID, NODE.NODELABEL, ASSETS." + columnName + " FROM ASSETS, NODE WHERE LOWER(ASSETS." + columnName + ") LIKE ? AND ASSETS.NODEID=NODE.NODEID ORDER BY NODE.NODELABEL");
            d.watch(stmt);
            stmt.setString(1, "%" + searchText.toLowerCase() + "%");

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            while (rs.next()) {
                MatchingAsset asset = new MatchingAsset();

                asset.nodeId = rs.getInt("nodeID");
                asset.nodeLabel = rs.getString("nodelabel");
                asset.matchingValue = rs.getString(columnName);
                asset.columnSearched = columnName;

                list.add(asset);
            }
        } finally {
            d.cleanUp();
        }
        
        return list.toArray(new MatchingAsset[list.size()]);
    }

    public static MatchingAsset[] searchNodesWithAssets() throws SQLException {
        List<MatchingAsset> list = new ArrayList<MatchingAsset>();

        final DBUtils d = new DBUtils(AssetModel.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("select nodeid, nodelabel from node where nodeid in (select nodeid from assets where coalesce(manufacturer,'') != '' or coalesce(vendor,'') != '' or coalesce(modelNumber,'') != '' or coalesce(serialNumber,'') != '' or coalesce(description,'') != '' or coalesce(circuitId,'') != '' or coalesce(assetNumber,'') != '' or coalesce(operatingSystem,'') != '' or coalesce(rack,'') != '' or coalesce(slot,'') != '' or coalesce(port,'') != '' or coalesce(region,'') != '' or coalesce(division,'') != '' or coalesce(department,'') != '' or coalesce(address1,'') != '' or coalesce(address2,'') != '' or coalesce(city,'') != '' or coalesce(state,'') != '' or coalesce(zip,'') != '' or coalesce(building,'') != '' or coalesce(floor,'') != '' or coalesce(room,'') != '' or coalesce(vendorPhone,'') != '' or coalesce(vendorFax,'') != '' or coalesce(dateInstalled,'') != '' or coalesce(lease,'') != '' or coalesce(leaseExpires,'') != '' or coalesce(supportPhone,'') != '' or coalesce(maintContract,'') != '' or coalesce(vendorAssetNumber,'') != '' or coalesce(maintContractExpires,'') != '' or coalesce(displayCategory,'') != '' or coalesce(notifyCategory,'') != '' or coalesce(pollerCategory,'') != '' or coalesce(thresholdCategory,'') != '' or coalesce(comment,'') != '' or coalesce(username,'') != '' or coalesce(password,'') != '' or coalesce(enable,'') != '' or coalesce(connection,'') != '' or coalesce(autoenable,'') != '' or coalesce(cpu,'') != '' or coalesce(ram,'') != '' or coalesce(storagectrl,'') != '' or coalesce(hdd1,'') != '' or coalesce(hdd2,'') != '' or coalesce(hdd3,'') != '' or coalesce(hdd4,'') != '' or coalesce(hdd5,'') != '' or coalesce(hdd6,'') != '' or coalesce(numpowersupplies,'') != '' or coalesce(inputpower,'') != '' or coalesce(additionalhardware,'') != '' or coalesce(admin,'') != '' or coalesce(snmpcommunity,'') != '' or coalesce(rackunitheight,'') != '')");
            d.watch(stmt);

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            while (rs.next()) {
                MatchingAsset asset = new MatchingAsset();

                asset.nodeId = rs.getInt("nodeID");
                asset.nodeLabel = rs.getString("nodelabel");
                asset.matchingValue = "";
                asset.columnSearched = "";

                list.add(asset);
            }
        } finally {
            d.cleanUp();
        }
        
        return list.toArray(new MatchingAsset[list.size()]);
    }

    /**
     * <p>rs2Assets</p>
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.asset.Asset} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static Asset[] rs2Assets(ResultSet rs) throws SQLException {
        List<Asset> list = new ArrayList<Asset>();

        while (rs.next()) {
            Asset asset = new Asset();

            asset.nodeId = rs.getInt("nodeID");

            asset.setCategory(rs.getString("category"));
            asset.setManufacturer(rs.getString("manufacturer"));
            asset.setVendor(rs.getString("vendor"));
            asset.setModelNumber(rs.getString("modelNumber"));
            asset.setSerialNumber(rs.getString("serialNumber"));
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
            asset.setUserLastModified(rs.getString("userLastModified"));
            asset.setLease(rs.getString("lease"));
            asset.setSupportPhone(rs.getString("supportPhone"));
            asset.setMaintContract(rs.getString("maintContract"));
            asset.setDateInstalled(rs.getString("dateInstalled"));
            asset.setLeaseExpires(rs.getString("leaseExpires"));
            asset.setMaintContractExpires(rs.getString("maintContractExpires"));
            asset.setVendorAssetNumber(rs.getString("vendorAssetNumber"));
            asset.setDisplayCategory(rs.getString("displayCategory"));
            asset.setNotifyCategory(rs.getString("notifyCategory"));
            asset.setPollerCategory(rs.getString("pollerCategory"));
            asset.setThresholdCategory(rs.getString("thresholdCategory"));
            asset.setComments(rs.getString("comment"));
            asset.setUsername(rs.getString("username"));
            asset.setPassword(rs.getString("password"));
            asset.setEnable(rs.getString("enable"));
            asset.setConnection(rs.getString("connection"));
            asset.setAutoenable(rs.getString("autoenable"));
            asset.setCpu(rs.getString("cpu"));
            asset.setRam(rs.getString("ram"));
            asset.setStoragectrl(rs.getString("storagectrl"));
            asset.setHdd1(rs.getString("hdd1"));
            asset.setHdd2(rs.getString("hdd2"));
            asset.setHdd3(rs.getString("hdd3"));
            asset.setHdd4(rs.getString("hdd4"));
            asset.setHdd5(rs.getString("hdd5"));
            asset.setHdd6(rs.getString("hdd6"));
            asset.setNumpowersupplies(rs.getString("numpowersupplies"));
            asset.setInputpower(rs.getString("inputpower"));
            asset.setAdditionalhardware(rs.getString("additionalhardware"));
            asset.setAdmin(rs.getString("admin"));
            asset.setSnmpcommunity(rs.getString("snmpcommunity"));
            asset.setRackunitheight(rs.getString("rackunitheight"));
            final Object longitude = rs.getObject("longitude");
            if (longitude != null) {
                asset.setLongitude(Float.valueOf(rs.getFloat("longitude")).toString());
            }
            final Object latitude = rs.getObject("latitude");
            if (latitude != null) {
                asset.setLatitude(Float.valueOf(rs.getFloat("latitude")).toString());
            }
            asset.setCountry(rs.getString("country"));

            // Convert from java.sql.Timestamp to java.util.Date, since it looks more pretty or something
            asset.lastModifiedDate = new Date(rs.getTimestamp("lastModifiedDate").getTime());

            list.add(asset);
        }

        return list.toArray(new Asset[list.size()]);
    }

    /**
     * <p>getColumns</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public static String[][] getColumns() {
        return s_columns;
    }

    //TODO: no one is calling this now... delete soon.
    @SuppressWarnings("unused")
    private static boolean isColumnValid(String column) {
        Assert.notNull(column, "argument column cannot be null");

        for (String[] assetColumn : s_columns) {
            if (column.equals(assetColumn[1])) {
                return true;
            }
        }

        return false;
    }

    /**
     * Hard-coded (for now) list of human-readable asset columns and the
     * corresponding database column.
     */
    private static final String[][] s_columns = new String[][] {
        new String[] { "Address 1", "address1" },
        new String[] { "Address 2", "address2" }, 
        new String[] { "Asset Number", "assetNumber" }, 
        new String[] { "Building", "building" }, 
        new String[] { "Circuit ID", "circuitId" }, 
        new String[] { "City", "city" }, 
        new String[] { "Comments", "comment" }, 
        new String[] { "Date Installed", "dateInstalled" }, 
        new String[] { "Department", "department" }, 
        new String[] { "Description", "description" }, 
        new String[] { "Display Category", "displayCategory" }, 
        new String[] { "Division", "division" }, 
        new String[] { "Floor", "floor" }, 
        new String[] { "Lease", "lease" }, 
        new String[] { "Lease Expires", "leaseExpires" }, 
        new String[] { "Maint Contract", "maintContract" },
        new String[] { "Maint Contract Expires", "maintContractExpires" }, 
        new String[] { "Maint Phone", "supportPhone" }, 
        new String[] { "Manufacturer", "manufacturer" }, 
        new String[] { "Model Number", "modelNumber" }, 
        new String[] { "Notification Category", "notifyCategory" }, 
        new String[] { "Operating System", "operatingSystem" }, 
        new String[] { "Port", "port" }, 
        new String[] { "Poller Category", "pollerCategory" }, 
        new String[] { "Rack", "rack" }, 
        new String[] { "Region", "region" }, 
        new String[] { "Room", "room" }, 
        new String[] { "Serial Number", "serialNumber" }, 
        new String[] { "Slot", "slot" }, 
        new String[] { "State", "state" }, 
        new String[] { "Threshold Category", "thresholdCategory" }, 
        new String[] { "User Last Modified", "userLastModified" },
        new String[] { "Vendor", "vendor" }, 
        new String[] { "Vendor Asset Number", "vendorAssetNumber" }, 
        new String[] { "Vendor Fax", "vendorFax" }, 
        new String[] { "Vendor Phone", "vendorPhone" }, 
        new String[] { "ZIP Code", "zip" },
        new String[] { "Username", "username" },
        new String[] { "Password", "password" },
        new String[] { "Enable Password", "enable" },
        new String[] { "Connection type", "connection" },
        new String[] { "Auto Enable", "autoenable" },
        new String[] { "Cpu", "cpu" },        
        new String[] { "Ram", "ram" },
        new String[] { "Storage Controller", "storagectrl" },
        new String[] { "HDD 1", "hdd1" },
        new String[] { "HDD 2", "hdd2" },
        new String[] { "HDD 3", "hdd3" },
        new String[] { "HDD 4", "hdd4" },
        new String[] { "HDD 5", "hdd5" },
        new String[] { "HDD 6", "hdd6" },
        new String[] { "Number of power supplies", "numpowersupplies" },
        new String[] { "Inputpower", "inputpower" },
        new String[] { "Additional hardware", "additionalhardware" },
        new String[] { "Admin", "admin" },
        new String[] { "SNMP community", "snmpcommunity" },
	new String[] { "Rack unit height", "rackunitheight" },
        new String[] { "GeoLocation Longitude", "longitude" },
        new String[] { "GeoLocation Latitude", "latitude" },
        new String[] { "Country", "country" }
    };

    private Float safeFloat(final String value) {
        if (StringUtils.hasLength(value)) {
            try {
                return Float.valueOf(value);
            } catch (final NumberFormatException e) {
                LOG.trace("Failed to parse float value from {}", value, e);
            }
        }
        return null;
    }
}
