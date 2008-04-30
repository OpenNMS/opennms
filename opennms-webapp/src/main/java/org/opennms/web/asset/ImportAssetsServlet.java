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
// 2007 Jul 24: Add serialVersionUID and Java 5 generics. - dj@opennms.org
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.mail.Session;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.resource.Vault;
import org.opennms.web.MissingParameterException;
import org.opennms.web.Util;
import org.opennms.web.WebSecurityUtils;

/**
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class ImportAssetsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private class AssetException extends Exception {
		private static final long serialVersionUID = 1L;

		public AssetException(String message) {
    		super(message);
    	}
    }
    
    /** The URL to redirect the client to in case of success. */
    protected String redirectSuccess;

    protected AssetModel model;

    /**
     * Looks up the <code>redirect.success</code> parameter in the servlet's
     * configuration. If not present, this servlet will throw an exception so it will
     * be marked unavailable.
     */
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();

        this.redirectSuccess = config.getInitParameter("redirect.success");

        if (this.redirectSuccess == null) {
            throw new UnavailableException("Require a redirect.success init parameter.");
        }

        this.model = new AssetModel();
    }

    /**
     * Acknowledge the events specified in the POST and then redirect the client
     * to an appropriate URL for display.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String assetsText = request.getParameter("assetsText");

        if (assetsText == null) {
            throw new MissingParameterException("assetsText");
        }

        try {
            List<Asset> assets = this.decodeAssetsText(assetsText);
            List<Integer> nodesWithAssets = this.getCurrentAssetNodesList();

            int assetCount = assets.size();

            for (int i = 0; i < assetCount; i++) {
                Asset asset = (Asset) assets.get(i);

                // update with the current information
                asset.setUserLastModified(request.getRemoteUser());
                asset.setLastModifiedDate(new Date());

                if (nodesWithAssets.contains(new Integer(asset.getNodeId()))) {
                    this.model.modifyAsset(asset);
                } else {
                    this.model.createAsset(asset);
                }
            }

            request.getSession().setAttribute("message", "Successfully imported " + assets.size() + " asset" + (assets.size() == 1? "" : "s") + ".");
            response.sendRedirect(response.encodeRedirectURL(this.redirectSuccess + "&showMessage=true"));
//            response.sendRedirect(response.encodeRedirectURL(this.redirectSuccess + "&message=Import%20complete."));
        } catch (AssetException e) {
        	String message = "Error importing assets: " + e.getMessage();
        	redirectWithErrorMessage(request, response, e, message);
        } catch (SQLException e) {
        	String message ="Database exception importing assets: " + e.getMessage();
        	redirectWithErrorMessage(request, response, e, message);
        }
    }

	private void redirectWithErrorMessage(HttpServletRequest request, HttpServletResponse response,
			Exception e, String message) throws IOException, UnsupportedEncodingException {
		this.log(message, e);
		request.getSession().setAttribute("message", message);
//		response.sendRedirect(response.encodeRedirectURL("import.jsp?errorMessage=" + URLEncoder.encode(message, "ISO-8859-1")));
		response.sendRedirect(response.encodeRedirectURL("import.jsp?showMessage=true"));
	}

    public List<Asset> decodeAssetsText(String text) throws AssetException {
        List<Asset> list = new ArrayList<Asset>();

        List<String> lines = this.splitfields(text, "\r\n", -1);
        int lineCount = lines.size();

        if (lineCount == 0) {
        	throw new AssetException("No asset information was found.");
        }

        for (int i = 0; i < lineCount; i++) {
            String line = lines.get(i);
            List<String> tokens = this.splitfields(line, ",", -1);

            try {
                if (tokens.size() != 39) {
                    throw new NoSuchElementException();
                }

                Asset asset = new Asset();

                // ignore tokens.get(0) = node label (for display only)
                asset.setNodeId(WebSecurityUtils.safeParseInt((String) tokens.get(1)));
                asset.setCategory(Util.decode((String) tokens.get(2)));
                asset.setManufacturer(Util.decode((String) tokens.get(3)));
                asset.setVendor(Util.decode((String) tokens.get(4)));
                asset.setModelNumber(Util.decode((String) tokens.get(5)));
                asset.setSerialNumber(Util.decode((String) tokens.get(6)));
                asset.setDescription(Util.decode((String) tokens.get(7)));
                asset.setCircuitId(Util.decode((String) tokens.get(8)));
                asset.setAssetNumber(Util.decode((String) tokens.get(9)));
                asset.setOperatingSystem(Util.decode((String) tokens.get(10)));
                asset.setRack(Util.decode((String) tokens.get(11)));
                asset.setSlot(Util.decode((String) tokens.get(12)));
                asset.setPort(Util.decode((String) tokens.get(13)));
                asset.setRegion(Util.decode((String) tokens.get(14)));
                asset.setDivision(Util.decode((String) tokens.get(15)));
                asset.setDepartment(Util.decode((String) tokens.get(16)));
                asset.setAddress1(Util.decode((String) tokens.get(17)));
                asset.setAddress2(Util.decode((String) tokens.get(18)));
                asset.setCity(Util.decode((String) tokens.get(19)));
                asset.setState(Util.decode((String) tokens.get(20)));
                asset.setZip(Util.decode((String) tokens.get(21)));
                asset.setBuilding(Util.decode((String) tokens.get(22)));
                asset.setFloor(Util.decode((String) tokens.get(23)));
                asset.setRoom(Util.decode((String) tokens.get(24)));
                asset.setVendorPhone(Util.decode((String) tokens.get(25)));
                asset.setVendorFax(Util.decode((String) tokens.get(26)));
                asset.setDateInstalled(Util.decode((String) tokens.get(27)));
                asset.setLease(Util.decode((String) tokens.get(28)));
                asset.setLeaseExpires(Util.decode((String) tokens.get(29)));
                asset.setSupportPhone(Util.decode((String) tokens.get(30)));
                asset.setMaintContract(Util.decode((String) tokens.get(31)));
                asset.setVendorAssetNumber(Util.decode((String) tokens.get(32)));
                asset.setMaintContractExpires(Util.decode((String) tokens.get(33)));
                asset.setDisplayCategory(Util.decode((String) tokens.get(34)));
                asset.setNotifyCategory(Util.decode((String) tokens.get(35)));
                asset.setPollerCategory(Util.decode((String) tokens.get(36)));
                asset.setThresholdCategory(Util.decode((String) tokens.get(37)));
                asset.setComments(Util.decode((String) tokens.get(38)));

                list.add(asset);
            } catch (NoSuchElementException e) {
            	throw new AssetException("Ignoring malformed import on line " + i + ", not enough values.");
            } catch (NumberFormatException e) {
                throw new AssetException("Ignoring malformed import on line " + i + ", node id not a number.");
            }
        }

        return list;
    }

    public List<Integer> getCurrentAssetNodesList() throws SQLException {
        Connection conn = Vault.getDbConnection();
        List<Integer> list = new ArrayList<Integer>();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT NODEID FROM ASSETS");

            while (rs.next()) {
                list.add(new Integer(rs.getInt("NODEID")));
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return list;
    }

    private List<String> splitfields(String string, String sep, int maxsplit) {
        List<String> list = new ArrayList<String>();

        int length = string.length();
        if (maxsplit < 0) {
            maxsplit = length;
        }

        int lastbreak = 0;
        int splits = 0;
        int sepLength = sep.length();
        while (splits < maxsplit) {
            int index = string.indexOf(sep, lastbreak);
            if (index == -1) {
                break;
            }
            splits += 1;
            list.add(string.substring(lastbreak, index));
            lastbreak = index + sepLength;
        }
        if (lastbreak <= length) {
            list.add(string.substring(lastbreak, length));
        }
        return list;
    }

}
