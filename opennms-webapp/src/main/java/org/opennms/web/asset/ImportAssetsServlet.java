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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.resource.Vault;
import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.api.Util;

import au.com.bytecode.opencsv.CSVReader;

/**
 * <p>ImportAssetsServlet class.</p>
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
public class ImportAssetsServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private List<String> errors = new ArrayList<String>();

    private class AssetException extends Exception {
		private static final long serialVersionUID = 1L;

		public AssetException(String message) {
    		super(message);
    	}

		public AssetException(String message, Throwable t) {
		    super(message, t);
	    }

    }
    
    /** The URL to redirect the client to in case of success. */
    protected String redirectSuccess;

    protected AssetModel model;

    /**
     * Looks up the <code>redirect.success</code> parameter in the servlet's
     * configuration. If not present, this servlet will throw an exception so it will
     * be marked unavailable.
     *
     * @throws javax.servlet.ServletException if any.
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
     * {@inheritDoc}
     *
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

            StringBuffer messageText = new StringBuffer();
            messageText.append("Successfully imported").append(assets.size()).append(" asset");
            if (assets.size() > 1) {
                messageText.append("s");
            }
            messageText.append(".");
            
            if (errors.size() > 0) {
                messageText.append("  ").append(errors.size()).append(" non-fatal errors occurred:");
                for (String error : errors) {
                    messageText.append("<br />").append(error);
                }
            }
            
            request.getSession().setAttribute("message", messageText.toString());
            response.sendRedirect(response.encodeRedirectURL(this.redirectSuccess + "&showMessage=true"));
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
		response.sendRedirect(response.encodeRedirectURL("import.jsp?showMessage=true"));
	}

    /**
     * <p>decodeAssetsText</p>
     *
     * @param text a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     * @throws org.opennms.web.asset.ImportAssetsServlet$AssetException if any.
     */
    public List<Asset> decodeAssetsText(String text) throws AssetException {
        CSVReader reader = new CSVReader(new StringReader(text));
        String[] line;
        List<Asset> list = new ArrayList<Asset>();
        text = text.trim();
        
        int count = 0;

        try {
            while ((line = reader.readNext()) != null) {
                count++;
                try {

                    if (line.length != 44) {
                        throw new NoSuchElementException();
                    }

                    // skip the first line if it's the headers
                    if (line[0].equals("Node Label")) {
                        continue;
                    }
                    
                    Asset asset = new Asset();

                    asset.setNodeId(WebSecurityUtils.safeParseInt(line[1]));
                    asset.setCategory(Util.decode(line[2]));
                    asset.setManufacturer(Util.decode(line[3]));
                    asset.setVendor(Util.decode(line[4]));
                    asset.setModelNumber(Util.decode(line[5]));
                    asset.setSerialNumber(Util.decode(line[6]));
                    asset.setDescription(Util.decode(line[7]));
                    asset.setCircuitId(Util.decode(line[8]));
                    asset.setAssetNumber(Util.decode(line[9]));
                    asset.setOperatingSystem(Util.decode(line[10]));
                    asset.setRack(Util.decode(line[11]));
                    asset.setSlot(Util.decode(line[12]));
                    asset.setPort(Util.decode(line[13]));
                    asset.setRegion(Util.decode(line[14]));
                    asset.setDivision(Util.decode(line[15]));
                    asset.setDepartment(Util.decode(line[16]));
                    asset.setAddress1(Util.decode(line[17]));
                    asset.setAddress2(Util.decode(line[18]));
                    asset.setCity(Util.decode(line[19]));
                    asset.setState(Util.decode(line[20]));
                    asset.setZip(Util.decode(line[21]));
                    asset.setBuilding(Util.decode(line[22]));
                    asset.setFloor(Util.decode(line[23]));
                    asset.setRoom(Util.decode(line[24]));
                    asset.setVendorPhone(Util.decode(line[25]));
                    asset.setVendorFax(Util.decode(line[26]));
                    asset.setDateInstalled(Util.decode(line[27]));
                    asset.setLease(Util.decode(line[28]));
                    asset.setLeaseExpires(Util.decode(line[29]));
                    asset.setSupportPhone(Util.decode(line[30]));
                    asset.setMaintContract(Util.decode(line[31]));
                    asset.setVendorAssetNumber(Util.decode(line[32]));
                    asset.setMaintContractExpires(Util.decode(line[33]));
                    asset.setDisplayCategory(Util.decode(line[34]));
                    asset.setNotifyCategory(Util.decode(line[35]));
                    asset.setPollerCategory(Util.decode(line[36]));
                    asset.setThresholdCategory(Util.decode(line[37]));
                    asset.setUsername(Util.decode(line[38]));
                    asset.setPassword(Util.decode(line[39]));
                    asset.setEnable(Util.decode(line[40]));
                    asset.setConnection(Util.decode(line[41]));
                    asset.setAutoenable(Util.decode(line[42]));
                    asset.setComments(Util.decode(line[43]));
                    
                    list.add(asset);

                } catch (NoSuchElementException e) {
                    errors.add("Ignoring malformed import for entry " + count + ", not enough values.");
                } catch (NumberFormatException e) {
                    errors.add("Ignoring malformed import for entry " + count + ", node id not a number.");
                }
            }
        } catch (IOException e) {
            throw new AssetException("An error occurred reading the CSV input.", e);
        }

        if (list.size() == 0) {
        	throw new AssetException("No asset information was found.");
        }

        return list;
    }

    /**
     * <p>getCurrentAssetNodesList</p>
     *
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     */
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

}
