//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// Modifications:
//
// 2007 Jul 24: Add serialVersionUID. - dj@opennms.org
// 2004 Jan 06: Added support for Display, Notify, Poller and Threshold categories
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.asset;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.element.NetworkElementFactory;

import au.com.bytecode.opencsv.CSVWriter;

/**
 *
 * Exports the assets database to a comma-separated values text file.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="mailto:ranger@opennms.org">Benjamin Reed</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="mailto:ranger@opennms.org">Benjamin Reed</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="mailto:ranger@opennms.org">Benjamin Reed</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @version $Id: $
 * @since 1.8.1
 */
public class ExportAssetsServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    
    protected AssetModel model;

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    public void init() throws ServletException {
        this.model = new AssetModel();
    }

    /** {@inheritDoc} */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Asset[] assets = null;

        try {
            assets = this.model.getAllAssets();
        } catch (SQLException e) {
            throw new ServletException("Database exception", e);
        }

        response.setContentType("text/plain");

        CSVWriter out = new CSVWriter(response.getWriter());

        String[] header = {
                "Node Label",
                "Node ID",
                "Category",
                "Manufacturer",
                "Vendor",
                "Model Number",
                "Serial Number",
                "Description",
                "Circuit ID",
                "Asset Number",
                "Operating System",
                "Rack",
                "Slot",
                "Port",
                "Region",
                "Division",
                "Department",
                "Address 1",
                "Address 2",
                "City",
                "State",
                "Zip",
                "Building",
                "Floor",
                "Room",
                "Vendor Phone",
                "Vendor Fax",
                "Date Installed",
                "Lease",
                "Lease Expires",
                "Support Phone",
                "Maint Contract",
                "Vendor Asset Number",
                "Maint Contract Expires",
                "Display Category",
                "Notification Category",
                "Poller Category",
                "Threshold Category",
                "Username",
                "Password",
                "Enable",
                "Connection",
                "Auto Enable",
                "Comments"
        };
        
        out.writeNext(header);

        // print a single line for each asset
        for (int i = 0; i < assets.length; i++) {
            Asset asset = assets[i];
            ArrayList<String> entries = new ArrayList<String>();

            try {
                entries.add(NetworkElementFactory.getNodeLabel(asset.getNodeId()));
            } catch (SQLException e) {
                // just log the error, the node label is only a human aid,
                // anyway,
                // so don't hold up the export
                this.log("Database error while looking up node label for node " + asset.getNodeId(), e);
            }

            entries.add(Integer.toString(asset.getNodeId()));
            entries.add(asset.getCategory());
            entries.add(asset.getManufacturer());
            entries.add(asset.getVendor());
            entries.add(asset.getModelNumber());
            entries.add(asset.getSerialNumber());
            entries.add(asset.getDescription());
            entries.add(asset.getCircuitId());
            entries.add(asset.getAssetNumber());
            entries.add(asset.getOperatingSystem());
            entries.add(asset.getRack());
            entries.add(asset.getSlot());
            entries.add(asset.getPort());
            entries.add(asset.getRegion());
            entries.add(asset.getDivision());
            entries.add(asset.getDepartment());
            entries.add(asset.getAddress1());
            entries.add(asset.getAddress2());
            entries.add(asset.getCity());
            entries.add(asset.getState());
            entries.add(asset.getZip());
            entries.add(asset.getBuilding());
            entries.add(asset.getFloor());
            entries.add(asset.getRoom());
            entries.add(asset.getVendorPhone());
            entries.add(asset.getVendorFax());
            entries.add(asset.getDateInstalled());
            entries.add(asset.getLease());
            entries.add(asset.getLeaseExpires());
            entries.add(asset.getSupportPhone());
            entries.add(asset.getMaintContract());
            entries.add(asset.getVendorAssetNumber());
            entries.add(asset.getMaintContractExpires());
            entries.add(asset.getDisplayCategory());
            entries.add(asset.getNotifyCategory());
            entries.add(asset.getPollerCategory());
            entries.add(asset.getThresholdCategory());
            entries.add(asset.getUsername());
            entries.add(asset.getPassword());
            entries.add(asset.getEnable());
            entries.add(asset.getConnection());
            entries.add(asset.getAutoenable());
            entries.add(asset.getComments());
            
            out.writeNext(entries.toArray(new String[0]));
        }

        out.close();
    }
}
