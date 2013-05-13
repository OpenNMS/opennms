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

import org.apache.commons.io.IOUtils;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.web.api.Util;
import org.opennms.web.servlet.MissingParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
 * <p>ImportAssetsServlet class.</p>
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="mailto:ranger@opennms.org">Benjamin Reed</A>
 */
public class ImportAssetsServlet extends HttpServlet {
    private static final long serialVersionUID = 8282814214167099107L;
    private Logger logger = LoggerFactory.getLogger(ImportAssetsServlet.class.getName());
    private List<String> errors = new ArrayList<String>();

    private class AssetException extends Exception {
        private static final long serialVersionUID = 2498335935646001342L;

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
    @Override
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
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String assetsText = request.getParameter("assetsText");

        if (assetsText == null) {
            logger.error("assetsText was null");
            throw new MissingParameterException("assetsText");
        }

        try {
            List<Asset> assets = this.decodeAssetsText(assetsText);
            List<Integer> nodesWithAssets = this.getCurrentAssetNodesList();

            for (Asset asset : assets) {
                // update with the current information
                asset.setUserLastModified(request.getRemoteUser());
                asset.setLastModifiedDate(new Date());

                if (nodesWithAssets.contains(Integer.valueOf(asset.getNodeId()))) {
                    logger.debug("modifyAsset call for asset:'{}'", asset);
                    this.model.modifyAsset(asset);
                } else {
                    logger.debug("createAsset:'{}'", asset);
                    this.model.createAsset(asset);
                }
            }

            StringBuffer messageText = new StringBuffer();
            messageText.append("Successfully imported ").append(assets.size()).append(" asset");
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
        CSVReader csvReader = null;
        StringReader stringReader = null;
        String[] line;
        List<Asset> list = new ArrayList<Asset>();
        text = text.trim();
        
        int count = 0;

        try {
            stringReader = new StringReader(text);
            csvReader = new CSVReader(stringReader);

            while ((line = csvReader.readNext()) != null) {
                count++;
                try {
                    logger.debug("asset line is:'{}'", (Object)line);
                    if (line.length <= 37) {
                        logger.error("csv test row length was not at least 37 line length: '{}' line was:'{}', line length", line.length, line);
                        throw new NoSuchElementException();
                    }

                    // skip the first line if it's the headers
                    if (line[0].equals("Node Label")) {
                        logger.debug("line was header. line:'{}'", (Object)line);
                        continue;
                    }
                    
                    final Asset asset = new Asset();

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
                    
                    if (line.length > 37) {
                        asset.setThresholdCategory(Util.decode(line[37]));
                        asset.setUsername(Util.decode(line[38]));
                        asset.setPassword(Util.decode(line[39]));
                        asset.setEnable(Util.decode(line[40]));
                        asset.setConnection(Util.decode(line[41]));
                        asset.setAutoenable(Util.decode(line[42]));
                        asset.setComments(Util.decode(line[43]));
                    }

                    if (line.length > 44) {
                        asset.setCpu(Util.decode(line[44]));
                        asset.setRam(Util.decode(line[45]));
                        asset.setStoragectrl(Util.decode(line[46]));
                        asset.setHdd1(Util.decode(line[47]));
                        asset.setHdd2(Util.decode(line[48]));
                        asset.setHdd3(Util.decode(line[49]));
                        asset.setHdd4(Util.decode(line[50]));
                        asset.setHdd5(Util.decode(line[51]));
                        asset.setHdd6(Util.decode(line[52]));
    
                        asset.setNumpowersupplies(Util.decode(line[53]));
                        asset.setInputpower(Util.decode(line[54]));
    
                        asset.setAdditionalhardware(Util.decode(line[55]));
                        asset.setAdmin(Util.decode(line[56]));
                        asset.setSnmpcommunity(Util.decode(line[57]));
                        asset.setRackunitheight(Util.decode(line[58]));
                    }

                    if (line.length > 59) {
                        asset.setCountry(Util.decode(line[59]));
                        asset.setLongitude(Util.decode(line[60]));
                        asset.setLatitude(Util.decode(line[61]));
                    }

                    list.add(asset);
                    logger.debug("decoded asset:'{}'", (Object)asset);

                } catch (NoSuchElementException e) {
                    errors.add("Ignoring malformed import for entry " + count + ", not enough values.");
                } catch (NumberFormatException e) {
                    logger.error("NodeId parsing to int faild, ignoreing malformed import for entry number '{}' exception message:'{}'", count, e.getMessage());
                    errors.add("Ignoring malformed import for entry " + count + ", node id not a number.");
                }
            }
        } catch (IOException e) {
            logger.error("An error occurred reading the CSV input. Message:'{}'", e.getMessage());
            throw new AssetException("An error occurred reading the CSV input.", e);
        } finally {
            IOUtils.closeQuietly(stringReader);
            IOUtils.closeQuietly(csvReader);
        }

        if (list.size() == 0) {
            logger.error("No asset information was found, list size was 0");
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
                list.add(Integer.valueOf(rs.getInt("NODEID")));
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return list;
    }

}
