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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.Util;
import org.opennms.web.servlet.MissingParameterException;

/**
 * <p>ModifyAssetServlet class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ModifyAssetServlet extends HttpServlet {
    private static final long serialVersionUID = 1476437673416953289L;
    private static Set<String> s_allowHtmlFields;
    
    protected AssetModel model;

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    @Override
    public void init() throws ServletException {
        this.model = new AssetModel();
        initAllowHtmlFields();
    }
    
    private void initAllowHtmlFields() {
        s_allowHtmlFields = new HashSet<String>();
        String allowHtmlFieldNames = System.getProperty("opennms.assets.allowHtmlFields");
        if (allowHtmlFieldNames == null)
            return;
        for (String fieldName : allowHtmlFieldNames.split("\\s*,\\s*")) {
            s_allowHtmlFields.add(fieldName.toLowerCase());
        }
        
    }

    /** {@inheritDoc} */
    @Override
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

    /**
     * <p>getRequestParameter</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected String getRequestParameter(final HttpServletRequest request, final String name) {
        boolean allowHTML = false;
        if (s_allowHtmlFields.contains(name.toLowerCase())) {
            allowHTML = true;
        }
        return WebSecurityUtils.sanitizeString(request.getParameter(name), allowHTML);
    }
    
    /**
     * <p>parms2Asset</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param nodeId a int.
     * @return a {@link org.opennms.web.asset.Asset} object.
     */
    protected Asset parms2Asset(HttpServletRequest request, int nodeId) {
        Asset asset = new Asset();

        asset.setNodeId(nodeId);
        asset.setCategory(getRequestParameter(request, "category"));
        asset.setManufacturer(getRequestParameter(request, "manufacturer"));
        asset.setVendor(getRequestParameter(request, "vendor"));
        asset.setModelNumber(getRequestParameter(request, "modelnumber"));
        asset.setSerialNumber(getRequestParameter(request, "serialnumber"));
        asset.setDescription(getRequestParameter(request, "description"));
        asset.setCircuitId(getRequestParameter(request, "circuitid"));
        asset.setAssetNumber(getRequestParameter(request, "assetnumber"));
        asset.setOperatingSystem(getRequestParameter(request, "operatingsystem"));
        asset.setRack(getRequestParameter(request, "rack"));
        asset.setSlot(getRequestParameter(request, "slot"));
        asset.setPort(getRequestParameter(request, "port"));
        asset.setRegion(getRequestParameter(request, "region"));
        asset.setDivision(getRequestParameter(request, "division"));
        asset.setDepartment(getRequestParameter(request, "department"));
        asset.setAddress1(getRequestParameter(request, "address1"));
        asset.setAddress2(getRequestParameter(request, "address2"));
        asset.setCity(getRequestParameter(request, "city"));
        asset.setState(getRequestParameter(request, "state"));
        asset.setZip(getRequestParameter(request, "zip"));
        asset.setCountry(getRequestParameter(request, "country"));
        asset.setLongitude(getRequestParameter(request, "longitude"));
        asset.setLatitude(getRequestParameter(request, "latitude"));
        asset.setBuilding(getRequestParameter(request, "building"));
        asset.setFloor(getRequestParameter(request, "floor"));
        asset.setRoom(getRequestParameter(request, "room"));
        asset.setVendorPhone(getRequestParameter(request, "vendorphone"));
        asset.setVendorFax(getRequestParameter(request, "vendorfax"));
        asset.setDateInstalled(getRequestParameter(request, "dateinstalled"));
        asset.setLease(getRequestParameter(request, "lease"));
        asset.setLeaseExpires(getRequestParameter(request, "leaseexpires"));
        asset.setSupportPhone(getRequestParameter(request, "supportphone"));
        asset.setMaintContract(getRequestParameter(request, "maintcontract"));
        asset.setVendorAssetNumber(getRequestParameter(request, "vendorassetnumber"));
        asset.setMaintContractExpires(getRequestParameter(request, "maintcontractexpires"));
        asset.setDisplayCategory(getRequestParameter(request, "displaycategory"));
        asset.setNotifyCategory(getRequestParameter(request, "notifycategory"));
        asset.setPollerCategory(getRequestParameter(request, "pollercategory"));
        asset.setThresholdCategory(getRequestParameter(request, "thresholdcategory"));
        asset.setComments(getRequestParameter(request, "comments"));
        asset.setUsername(getRequestParameter(request, "username"));
        asset.setPassword(getRequestParameter(request, "password"));
        asset.setEnable(getRequestParameter(request, "enable"));
        asset.setConnection(getRequestParameter(request, "connection"));
        asset.setAutoenable(getRequestParameter(request, "autoenable"));
        asset.setCpu(getRequestParameter(request, "cpu"));
        asset.setRam(getRequestParameter(request, "ram"));
        asset.setStoragectrl(getRequestParameter(request, "storagectrl"));
        asset.setHdd1(getRequestParameter(request, "hdd1"));
        asset.setHdd2(getRequestParameter(request, "hdd2"));
        asset.setHdd3(getRequestParameter(request, "hdd3"));
        asset.setHdd4(getRequestParameter(request, "hdd4"));
        asset.setHdd5(getRequestParameter(request, "hdd5"));
        asset.setHdd6(getRequestParameter(request, "hdd6"));
        asset.setNumpowersupplies(getRequestParameter(request, "numpowersupplies"));
        asset.setInputpower(getRequestParameter(request, "inputpower"));
        asset.setAdditionalhardware(getRequestParameter(request, "additionalhardware"));
        asset.setAdmin(getRequestParameter(request, "admin"));
        asset.setSnmpcommunity(getRequestParameter(request, "snmpcommunity"));
        asset.setRackunitheight(getRequestParameter(request, "rackunitheight"));

        asset.setUserLastModified(request.getRemoteUser());
        asset.setLastModifiedDate(new Date());

        return (asset);
    }

    private void sendEvent(Event event) throws ServletException {
        try {
            Util.createEventProxy().send(event);
        } catch (Throwable e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
        }
    }

}
