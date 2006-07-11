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

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsAssetRecord;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

public class AssetRecordSaveOrUpdate extends SqlUpdate {

    public AssetRecordSaveOrUpdate(DataSource ds, String updateStmt) {
        setDataSource(ds);
        setSql(updateStmt);
        
        declareParameter(new SqlParameter("category", Types.VARCHAR));             // category        varchar(64) not null,
        declareParameter(new SqlParameter("manufacturer", Types.VARCHAR));         // manufacturer    varchar(64),
        declareParameter(new SqlParameter("vendor", Types.VARCHAR));               // vendor          varchar(64),
        declareParameter(new SqlParameter("modelNumber", Types.VARCHAR));          // modelNumber     varchar(64),
        declareParameter(new SqlParameter("serialNumber", Types.VARCHAR));         // serialNumber    varchar(64),
        declareParameter(new SqlParameter("description", Types.VARCHAR));          // description     varchar(128),
        declareParameter(new SqlParameter("circuitId", Types.VARCHAR));            // circuitId       varchar(64),
        declareParameter(new SqlParameter("assetNumber", Types.VARCHAR));          // assetNumber     varchar(64),
        declareParameter(new SqlParameter("operatingSystem", Types.VARCHAR));      // operatingSystem varchar(64),
        declareParameter(new SqlParameter("rack", Types.VARCHAR));                 // rack            varchar(64),
        declareParameter(new SqlParameter("slot", Types.VARCHAR));                 // slot            varchar(64),
        declareParameter(new SqlParameter("port", Types.VARCHAR));                 // port            varchar(64),
        declareParameter(new SqlParameter("region", Types.VARCHAR));               // region          varchar(64),
        declareParameter(new SqlParameter("division", Types.VARCHAR));             // division        varchar(64),
        declareParameter(new SqlParameter("department", Types.VARCHAR));           // department      varchar(64),
        declareParameter(new SqlParameter("address1", Types.VARCHAR));             // address1        varchar(256),
        declareParameter(new SqlParameter("address2", Types.VARCHAR));             // address2        varchar(256),
        declareParameter(new SqlParameter("city", Types.VARCHAR));                 // city            varchar(64),
        declareParameter(new SqlParameter("state", Types.VARCHAR));                // state           varchar(64),
        declareParameter(new SqlParameter("zip", Types.VARCHAR));                  // zip             varchar(64),
        declareParameter(new SqlParameter("building", Types.VARCHAR));             // building        varchar(64),
        declareParameter(new SqlParameter("floor", Types.VARCHAR));                // floor           varchar(64),
        declareParameter(new SqlParameter("room", Types.VARCHAR));                 // room            varchar(64),
        declareParameter(new SqlParameter("vendorPhone", Types.VARCHAR));          // vendorPhone     varchar(64),
        declareParameter(new SqlParameter("vendorFax", Types.VARCHAR));            // vendorFax       varchar(64),
        declareParameter(new SqlParameter("vendorAssetNumber", Types.VARCHAR));    // vendorAssetNumber varchar(64),
        declareParameter(new SqlParameter("userLastModified", Types.VARCHAR));     // userLastModified char(20) not null,
        declareParameter(new SqlParameter("lastModifiedDate", Types.TIMESTAMP));   // lastModifiedDate timestamp without time zone not null,
        declareParameter(new SqlParameter("dateInstalled", Types.VARCHAR));        // dateInstalled   varchar(64),
        declareParameter(new SqlParameter("lease", Types.VARCHAR));                // lease           varchar(64),
        declareParameter(new SqlParameter("leaseExpires", Types.VARCHAR));         // leaseExpires    varchar(64),
        declareParameter(new SqlParameter("supportPhone", Types.VARCHAR));         // supportPhone    varchar(64),
        declareParameter(new SqlParameter("maintContract", Types.VARCHAR));        // maintContract   varchar(64),
        declareParameter(new SqlParameter("maintContractExpires", Types.VARCHAR)); // maintContractExpires varchar(64),
        declareParameter(new SqlParameter("displayCategory", Types.VARCHAR));      // displayCategory   varchar(64),
        declareParameter(new SqlParameter("notifyCategory", Types.VARCHAR));       // notifyCategory   varchar(64),
        declareParameter(new SqlParameter("pollerCategory", Types.VARCHAR));       // pollerCategory   varchar(64),
        declareParameter(new SqlParameter("thresholdCategory", Types.VARCHAR));    // thresholdCategory   varchar(64),
        declareParameter(new SqlParameter("comment", Types.VARCHAR));              // comment         varchar(1024),
        declareParameter(new SqlParameter("nodeID", Types.INTEGER));               // nodeID          integer,

        compile();
    }
    
    public int persist(OnmsAssetRecord asset) {
        Object[] parms = new Object[] {
                asset.getCategory(),                // category        varchar(64) not null,
                asset.getManufacturer(),            // manufacturer    varchar(64),
                asset.getVendor(),                  // vendor          varchar(64),
                asset.getModelNumber(),             // modelNumber     varchar(64),
                asset.getSerialNumber(),            // serialNumber    varchar(64),
                asset.getDescription(),             // description     varchar(128),
                asset.getCircuitId(),               // circuitId       varchar(64),
                asset.getAssetNumber(),             // assetNumber     varchar(64),
                asset.getOperatingSystem(),         // operatingSystem varchar(64),
                asset.getRack(),                    // rack            varchar(64),
                asset.getSlot(),                    // slot            varchar(64),
                asset.getPort(),                    // port            varchar(64),
                asset.getRegion(),                  // region          varchar(64),
                asset.getDivision(),                // division        varchar(64),
                asset.getDepartment(),              // department      varchar(64),
                asset.getAddress1(),                // address1        varchar(256),
                asset.getAddress2(),                // address2        varchar(256),
                asset.getCity(),                    // city            varchar(64),
                asset.getState(),                   // state           varchar(64),
                asset.getZip(),                     // zip             varchar(64),
                asset.getBuilding(),                // building        varchar(64),
                asset.getFloor(),                   // floor           varchar(64),
                asset.getRoom(),                    // room            varchar(64),
                asset.getVendorPhone(),             // vendorPhone     varchar(64),
                asset.getVendorFax(),               // vendorFax       varchar(64),
                asset.getVendorAssetNumber(),       // vendorAssetNumber varchar(64),
                asset.getLastModifiedBy(),          // userLastModified char(20) not null,
                asset.getLastModifiedDate(),        // lastModifiedDate timestamp without time zone not null,
                asset.getDateInstalled(),           // dateInstalled   varchar(64),
                asset.getLease(),                   // lease           varchar(64),
                asset.getLeaseExpires(),            // leaseExpires    varchar(64),
                asset.getSupportPhone(),            // supportPhone    varchar(64),
                asset.getMaintContractNumber(),     // maintContract   varchar(64),
                asset.getMaintContractExpiration(), // maintContractExpires varchar(64),
                asset.getDisplayCategory(),         // displayCategory   varchar(64),
                asset.getNotifyCategory(),          // notifyCategory   varchar(64),
                asset.getPollerCategory(),          // pollerCategory   varchar(64),
                asset.getThresholdCategory(),       // thresholdCategory   varchar(64),
                asset.getComment(),                 // comment         varchar(1024),
                asset.getNode().getId()             // nodeID          integer,

        };
        return update(parms);
    }
    

}