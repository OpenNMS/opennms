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
// 2007 Jul 24: Organize imports. - dj@opennms.org
// 2004 Oct 07: Added code to support RTC rescan on asset update
// 2004 Jan 06: Added support for Display, Notify, Poller and Threshold Categories
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
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.MissingParameterException;
import org.opennms.web.Util;
import org.opennms.web.WebSecurityUtils;

public class ModifyAssetServlet extends HttpServlet {
    private static final long serialVersionUID = 9203659232262966182L;
    
    protected AssetModel model;

    public void init() throws ServletException {
        this.model = new AssetModel();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nodeIdString = request.getParameter("node");
        String isNewString = request.getParameter("isnew");

        if (nodeIdString == null) {
            throw new MissingParameterException("node", new String[] { "node", "isnew" });
        }

        if (isNewString == null) {
            throw new MissingParameterException("isnew", new String[] { "node", "isnew" });
        }

        int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
        boolean isNew = Boolean.valueOf(isNewString).booleanValue();

        Asset asset = this.parms2Asset(request, nodeId);

        Event evnt = EventUtils.createAssetInfoChangedEvent("OpenNMS.WebUI", nodeId, -1L);
        sendEvent(evnt);

        try {
            if (isNew) {
                this.model.createAsset(asset);
            } else {
                this.model.modifyAsset(asset);
            }

            response.sendRedirect("modify.jsp?node=" + nodeId);
        } catch (SQLException e) {
            throw new ServletException("database error", e);
        }
    }

    protected Asset parms2Asset(HttpServletRequest request, int nodeId) {
        Asset asset = new Asset();

        asset.setNodeId(nodeId);
        asset.setCategory(request.getParameter("category"));
        asset.setManufacturer(request.getParameter("manufacturer"));
        asset.setVendor(request.getParameter("vendor"));
        asset.setModelNumber(request.getParameter("modelnumber"));
        asset.setSerialNumber(request.getParameter("serialnumber"));
        asset.setDescription(request.getParameter("description"));
        asset.setCircuitId(request.getParameter("circuitid"));
        asset.setAssetNumber(request.getParameter("assetnumber"));
        asset.setOperatingSystem(request.getParameter("operatingsystem"));
        asset.setRack(request.getParameter("rack"));
        asset.setSlot(request.getParameter("slot"));
        asset.setPort(request.getParameter("port"));
        asset.setRegion(request.getParameter("region"));
        asset.setDivision(request.getParameter("division"));
        asset.setDepartment(request.getParameter("department"));
        asset.setAddress1(request.getParameter("address1"));
        asset.setAddress2(request.getParameter("address2"));
        asset.setCity(request.getParameter("city"));
        asset.setState(request.getParameter("state"));
        asset.setZip(request.getParameter("zip"));
        asset.setBuilding(request.getParameter("building"));
        asset.setFloor(request.getParameter("floor"));
        asset.setRoom(request.getParameter("room"));
        asset.setVendorPhone(request.getParameter("vendorphone"));
        asset.setVendorFax(request.getParameter("vendorfax"));
        asset.setDateInstalled(request.getParameter("dateinstalled"));
        asset.setLease(request.getParameter("lease"));
        asset.setLeaseExpires(request.getParameter("leaseexpires"));
        asset.setSupportPhone(request.getParameter("supportphone"));
        asset.setMaintContract(request.getParameter("maintcontract"));
        asset.setVendorAssetNumber(request.getParameter("vendorassetnumber"));
        asset.setMaintContractExpires(request.getParameter("maintcontractexpires"));
        asset.setDisplayCategory(request.getParameter("displaycategory"));
        asset.setNotifyCategory(request.getParameter("notifycategory"));
        asset.setPollerCategory(request.getParameter("pollercategory"));
        asset.setThresholdCategory(request.getParameter("thresholdcategory"));
        asset.setComments(request.getParameter("comments"));

        asset.setUserLastModified(request.getRemoteUser());
        asset.setLastModifiedDate(new Date());

        return (asset);
    }

    private void sendEvent(Event event) throws ServletException {
        try {
            Util.createEventProxy().send(event);
        } catch (Exception e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
        }
    }

}
