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
 */
public class ExportAssetsServlet extends HttpServlet {
    private static final long serialVersionUID = -4854445395857220978L;
    protected AssetModel model;

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    @Override
    public void init() throws ServletException {
        this.model = new AssetModel();
    }

    /** {@inheritDoc} */
    @Override
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
                "Comments",
		"Cpu",
		"Ram",
		"Storage Controller",
		"HDD 1",
		"HDD 2",
		"HDD 3",
		"HDD 4",
		"HDD 5",
		"HDD 6",
		"Number of power supplies",
		"Inputpower",
		"Additional hardware",
		"Admin",
		"SNMP Community",
		"Rack unit height",
                "Country",
                "Longitude",
                "Latitude"
        };
        
        out.writeNext(header);

        // print a single line for each asset
        for (int i = 0; i < assets.length; i++) {
            Asset asset = assets[i];
            ArrayList<String> entries = new ArrayList<String>();

            entries.add(NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(asset.getNodeId()));
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
            entries.add(asset.getCpu());
            entries.add(asset.getRam());
            entries.add(asset.getStoragectrl());
            entries.add(asset.getHdd1());
            entries.add(asset.getHdd2());
            entries.add(asset.getHdd3());
            entries.add(asset.getHdd4());
            entries.add(asset.getHdd5());
            entries.add(asset.getHdd6());
            entries.add(asset.getNumpowersupplies());
            entries.add(asset.getInputpower());
            entries.add(asset.getAdditionalhardware());
            entries.add(asset.getAdmin());
            entries.add(asset.getSnmpcommunity());
            entries.add(asset.getRackunitheight());
            entries.add(asset.getCountry());
            entries.add(asset.getLongitude());
            entries.add(asset.getLatitude());
            
            out.writeNext(entries.toArray(new String[0]));
        }

        out.close();
    }
}
