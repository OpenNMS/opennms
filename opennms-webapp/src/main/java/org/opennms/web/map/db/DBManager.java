/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 10, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.map.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;

import org.opennms.core.resource.Vault;
import org.opennms.core.resource.db.DbConnectionFactory;
import org.opennms.core.resource.db.SimpleDbConnectionFactory;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.InitializationObj;
import org.opennms.web.map.config.MapPropertiesFactory;

import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VMapInfo;

/**
 * @author maumig
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * 
 * The class manages maps stored on DB. It uses the constructor parameters
 * for the connection. If default constructor is called, it uses default OpenNMS db connector (Vault)
 */
public class DBManager extends Manager {

	/**
	 * the map table to use.
	 */
	String mapTable = "map";

	/**
	 * the element table to use.
	 */
	String elementTable = "element";

	Category log = null;

	private DbConnectionFactory m_factory = null;
	
	public DBManager() throws MapsException {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		if (log.isDebugEnabled())
			log.debug("Instantiating DBManager (using Vault)");
	}
	
	public DBManager(java.util.Map<String,String> params)
	throws MapsException {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		if (log.isDebugEnabled())
			log.debug("Instantiating DBManager with params: "+params);		
		String url = params.get("url");
		String driver = params.get("driver");
		String user =params.get("user");
		String password = params.get("password");
		m_factory=new SimpleDbConnectionFactory();
		try {
			m_factory.init(url, driver, user, password);
		} catch (Exception e) {
			throw new MapsException("Error while initializing dbconnection factory",e);
		}
		
	}

	/**
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */

	 Connection createConnection() throws MapsException {

		log.debug("creating connection");
		if(m_factory!=null){
			try {
                return m_factory.getConnection();
            } catch (SQLException e) {
                log.error("Exception while creating connection");
                throw new MapsException(e);
            }
		}else{
			try {
                return Vault.getDbConnection();
            } catch (SQLException e) {
                log.error("Exception while creating connection");
                throw new MapsException(e);
            }
		}
	}

	 void releaseConnection(Connection conn) throws MapsException {
		log.debug("releasing connection");
		try {
			if (conn != null && !conn.isClosed()){
				if(m_factory!=null){
					conn.close();
				}else{
					Vault.releaseDbConnection(conn);
				}
			}
		} catch (Exception e) {
			log.error("Exception while releasing connection");
			throw new MapsException(e);
		}
	}

	public void finalize(Connection conn) throws MapsException {
		log.debug("finalizing...");
		try {
			releaseConnection(conn);
		} catch (Exception e) {
			log.error("Exception while finalizing", e);
			throw new MapsException(e);
		}
	}

	private Connection startSession() throws MapsException {
			try {
				Connection conn = createConnection();
				log.debug("setting AutoCommit false db connection...");
				conn.setAutoCommit(false);
				conn
						.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				return conn;
			} catch (Exception e) {
				log.error("error while starting session");
				throw new MapsException(e);
			}
	}

	synchronized private void endSession(Connection conn) throws MapsException {
		try {
			log.debug("ending session");
			conn.commit();
			conn.setAutoCommit(true);
			releaseConnection(conn);
		} catch (Exception e) {
			log.error("error while ending session");
			throw new MapsException(e);
		}
	}

	private void rollback(Connection conn) throws MapsException {
		try {
			conn.rollback();
		} catch (SQLException ex) {
			log.error("Error while rollback");
			throw new MapsException(ex);
		} finally {
			releaseConnection(conn);
		}
	}

	public synchronized void saveMaps(Map[] m) throws MapsException {
	    Connection conn = startSession();

	    try {
			log.debug("saving maps");

			for (int i = 0, n = m.length; i < n; i++) {
				saveMapInSession(m[i], conn);
			}
		} catch (Exception e) {
			log.error("Error while saving maps ");
			rollback(conn);
		} finally {
			endSession(conn);
		}
	}

	private synchronized void saveMapInSession(Map m, Connection conn) throws MapsException {
		log.debug("saving map...");
		final String sqlGetCurrentTimestamp = "SELECT CURRENT_TIMESTAMP";
		final String sqlGetMapNxtId = "SELECT nextval('mapnxtid')";
		final String sqlInsertQuery = "INSERT INTO "
				+ mapTable
				+ " (mapid, mapname, mapbackground, mapowner, mapcreatetime, mapaccess, userlastmodifies, lastmodifiedtime, mapscale, mapxoffset, mapyoffset, maptype, mapwidth, mapheight) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		final String sqlUpdateQuery = "UPDATE "
				+ mapTable
				+ " SET mapname = ?, mapbackground = ?, mapowner = ?, mapaccess = ?, userlastmodifies = ?, lastmodifiedtime = ?, mapscale = ?, mapxoffset = ?, mapyoffset = ?, maptype = ? , mapwidth = ?, mapheight = ? WHERE mapid = ?";
		Timestamp currentTimestamp = null;
		int nxtid = 0;

		int count = -1;

		try {
			Statement stmtCT = conn.createStatement();
			ResultSet rs = stmtCT.executeQuery(sqlGetCurrentTimestamp);
			if (rs.next()) {
				currentTimestamp = rs.getTimestamp(1);
				PreparedStatement statement;
				if (m.isNew()) {
					Statement stmtID = conn.createStatement();
					ResultSet rsStmt = stmtID.executeQuery(sqlGetMapNxtId);
					if (rsStmt.next()) {
						nxtid = rsStmt.getInt(1);
					}
					rsStmt.close();
					stmtID.close();

					statement = conn.prepareStatement(sqlInsertQuery);
					statement.setInt(1, nxtid);
					statement.setString(2, m.getName());
					statement.setString(3, m.getBackground());
					statement.setString(4, m.getOwner());
					statement.setTimestamp(5, currentTimestamp);
					statement.setString(6, m.getAccessMode());
					statement.setString(7, m.getUserLastModifies());
					statement.setTimestamp(8, currentTimestamp);
					statement.setDouble(9, m.getScale());
					statement.setInt(10, m.getOffsetX());
					statement.setInt(11, m.getOffsetY());
					statement.setString(12, m.getType());
					statement.setInt(13, m.getWidth());
					statement.setInt(14, m.getHeight());
				} else {
					statement = conn.prepareStatement(sqlUpdateQuery);
					statement.setString(1, m.getName());
					statement.setString(2, m.getBackground());
					statement.setString(3, m.getOwner());
					statement.setString(4, m.getAccessMode());
					statement.setString(5, m.getUserLastModifies());
					statement.setTimestamp(6, currentTimestamp);
					statement.setDouble(7, m.getScale());
					statement.setInt(8, m.getOffsetX());
					statement.setInt(9, m.getOffsetY());
					statement.setString(10, m.getType());
					statement.setInt(11, m.getWidth());
					statement.setInt(12, m.getHeight());
					statement.setInt(13, m.getId());
				}
				count = statement.executeUpdate();

				statement.close();
			}
			rs.close();
			stmtCT.close();
		} catch (SQLException ex) {
			log.error("Error while saving map");
			throw new MapsException(ex);

		}

		if (count == 0) {
			log.warn("Called saveMap() on deleted map");
			throw new MapsException("Called saveMap() on deleted map");
		}
		if (m.isNew()) {
			m.setId(nxtid);
			m.setCreateTime(currentTimestamp);
			m.setAsNew(false);
		}
		m.setLastModifiedTime(currentTimestamp);
	}

	public synchronized void saveMap(Map m) throws MapsException {
		log.debug("saving map...");
		Connection conn = startSession();
		final String sqlGetCurrentTimestamp = "SELECT CURRENT_TIMESTAMP";
		final String sqlGetMapNxtId = "SELECT nextval('mapnxtid')";
		final String sqlInsertQuery = "INSERT INTO "
				+ mapTable
				+ " (mapid, mapname, mapbackground, mapowner, mapcreatetime, mapaccess, userlastmodifies, lastmodifiedtime, mapscale, mapxoffset, mapyoffset, maptype, mapwidth, mapheight) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		final String sqlUpdateQuery = "UPDATE "
				+ mapTable
				+ " SET mapname = ?, mapbackground = ?, mapowner = ?, mapaccess = ?, userlastmodifies = ?, lastmodifiedtime = ?, mapscale = ?, mapxoffset = ?, mapyoffset = ?, maptype = ? , mapwidth = ?, mapheight = ? WHERE mapid = ?";
		Timestamp currentTimestamp = null;
		int nxtid = 0;

		int count = -1;

		try {
			Statement stmtCT = conn.createStatement();
			ResultSet rs = stmtCT.executeQuery(sqlGetCurrentTimestamp);
			if (rs.next()) {
				currentTimestamp = rs.getTimestamp(1);
				PreparedStatement statement;
				if (m.isNew()) {
					Statement stmtID = conn.createStatement();
					ResultSet rsStmt = stmtID.executeQuery(sqlGetMapNxtId);
					if (rsStmt.next()) {
						nxtid = rsStmt.getInt(1);
					}
					rsStmt.close();
					stmtID.close();

					statement = conn.prepareStatement(sqlInsertQuery);
					statement.setInt(1, nxtid);
					statement.setString(2, m.getName());
					statement.setString(3, m.getBackground());
					statement.setString(4, m.getOwner());
					statement.setTimestamp(5, currentTimestamp);
					statement.setString(6, m.getAccessMode());
					statement.setString(7, m.getUserLastModifies());
					statement.setTimestamp(8, currentTimestamp);
					statement.setDouble(9, m.getScale());
					statement.setInt(10, m.getOffsetX());
					statement.setInt(11, m.getOffsetY());
					statement.setString(12, m.getType());
					statement.setInt(13, m.getWidth());
					statement.setInt(14, m.getHeight());
				} else {
					statement = conn.prepareStatement(sqlUpdateQuery);
					statement.setString(1, m.getName());
					statement.setString(2, m.getBackground());
					statement.setString(3, m.getOwner());
					statement.setString(4, m.getAccessMode());
					statement.setString(5, m.getUserLastModifies());
					statement.setTimestamp(6, currentTimestamp);
					statement.setDouble(7, m.getScale());
					statement.setInt(8, m.getOffsetX());
					statement.setInt(9, m.getOffsetY());
					statement.setString(10, m.getType());
					statement.setInt(11, m.getWidth());
					statement.setInt(12, m.getHeight());
					statement.setInt(13, m.getId());
				}
				count = statement.executeUpdate();

				statement.close();
			}
			rs.close();
			stmtCT.close();
		} catch (SQLException ex) {
			log.error("Error while saving map");
			rollback(conn);
			throw new MapsException("Error while saving map " + m.getId(), ex);
		} finally {
			endSession(conn);
		}

		if (count == 0) {
			log.warn("Called saveMap() on deleted map");
			throw new MapsException("Called saveMap() on deleted map");
		}
		if (m.isNew()) {
			m.setId(nxtid);
			m.setCreateTime(currentTimestamp);
			m.setAsNew(false);
		}
		m.setLastModifiedTime(currentTimestamp);
	}

	public synchronized void saveElements(Element[] e) throws MapsException {
        Connection conn = startSession();
		try {
			log.debug("saving elements");
			if (e != null) {
				for (int i = 0, n = e.length; i < n; i++) {
					saveElementInSession(e[i], conn);
				}
			}
		} catch (Exception ex) {
			log.error("error while saving elements");
			rollback(conn);
			throw new MapsException(ex);
		} finally {
			endSession(conn);
		}
	}

	private synchronized void saveElementInSession(Element e, Connection conn)
			throws MapsException {
		log.debug("saving element");

		final String sqlSelectQuery = "SELECT COUNT(*) FROM " + elementTable
				+ " WHERE elementid = ? AND MAPID = ? AND elementtype = ?";
		final String sqlInsertQuery = "INSERT INTO "
				+ elementTable
				+ " (mapid, elementid, elementtype, elementlabel, elementicon, elementx, elementy) VALUES (?, ?, ?, ?, ?, ?, ?)";
		final String sqlUpdateQuery = "UPDATE "
				+ elementTable
				+ " SET mapid = ?, elementid = ?, elementtype = ?, elementlabel = ?, elementicon = ?, elementx = ?, elementy = ? WHERE elementid = ? AND mapid = ? AND elementtype = ?";
		try {
			PreparedStatement statement = conn
					.prepareStatement(sqlSelectQuery);
			statement.setInt(1, e.getId());
			statement.setInt(2, e.getMapId());
			statement.setString(3, e.getType());
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				int count = rs.getInt(1);
				statement.close();
				if (count == 0) {
					statement = conn.prepareStatement(sqlInsertQuery);
					statement.setInt(1, e.getMapId());
					statement.setInt(2, e.getId());
					statement.setString(3, e.getType());
					statement.setString(4, e.getLabel());
					statement.setString(5, e.getIcon());
					statement.setInt(6, e.getX());
					statement.setInt(7, e.getY());
				} else {
					statement = conn.prepareStatement(sqlUpdateQuery);
					statement.setInt(1, e.getMapId());
					statement.setInt(2, e.getId());
					statement.setString(3, e.getType());
					statement.setString(4, e.getLabel());
					statement.setString(5, e.getIcon());
					statement.setInt(6, e.getX());
					statement.setInt(7, e.getY());
					statement.setInt(8, e.getId());
					statement.setInt(9, e.getMapId());
					statement.setString(10, e.getType());
				}
				// now count counts number of modified record
				count = statement.executeUpdate();
				rs.close();
				statement.close();
			}
		} catch (SQLException ex) {
			log.error("error while saving element");
			throw new MapsException(ex);
		}
	}

	public synchronized void saveElement(Element e) throws MapsException {
		log.debug("saving element");
		Connection conn = startSession();

		final String sqlSelectQuery = "SELECT COUNT(*) FROM " + elementTable
				+ " WHERE elementid = ? AND MAPID = ? AND elementtype = ?";
		final String sqlInsertQuery = "INSERT INTO "
				+ elementTable
				+ " (mapid, elementid, elementtype, elementlabel, elementicon, elementx, elementy) VALUES (?, ?, ?, ?, ?, ?, ?)";
		final String sqlUpdateQuery = "UPDATE "
				+ elementTable
				+ " SET mapid = ?, elementid = ?, elementtype = ?, elementlabel = ?, elementicon = ?, elementx = ?, elementy = ? WHERE elementid = ? AND mapid = ? AND elementtype = ?";
		try {
			PreparedStatement statement = conn
					.prepareStatement(sqlSelectQuery);
			statement.setInt(1, e.getId());
			statement.setInt(2, e.getMapId());
			statement.setString(3, e.getType());
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				int count = rs.getInt(1);
				statement.close();
				if (count == 0) {
					statement = conn.prepareStatement(sqlInsertQuery);
					statement.setInt(1, e.getMapId());
					statement.setInt(2, e.getId());
					statement.setString(3, e.getType());
					statement.setString(4, e.getLabel());
					statement.setString(5, e.getIcon());
					statement.setInt(6, e.getX());
					statement.setInt(7, e.getY());
				} else {
					statement = conn.prepareStatement(sqlUpdateQuery);
					statement.setInt(1, e.getMapId());
					statement.setInt(2, e.getId());
					statement.setString(3, e.getType());
					statement.setString(4, e.getLabel());
					statement.setString(5, e.getIcon());
					statement.setInt(6, e.getX());
					statement.setInt(7, e.getY());
					statement.setInt(8, e.getId());
					statement.setInt(9, e.getMapId());
					statement.setString(10, e.getType());
				}
				// now count counts number of modified record
				count = statement.executeUpdate();
				rs.close();
				statement.close();
			}
		} catch (SQLException ex) {
			log.error("error while saving element");
			rollback(conn);
			throw new MapsException(ex);

		} finally {
			endSession(conn);
		}
	}

	public synchronized void deleteElements(Element[] elems)
			throws MapsException {
		log.debug("deleting elements...");
        Connection conn = startSession();
		try {
			if (elems != null) {
				for (int i = 0; i < elems.length; i++) {
					deleteElementInSession(elems[i].getId(), elems[i]
							.getMapId(), elems[i].getType());
				}
			}
		} catch (MapsException e) {
			log.error("Error while deleting elements");
			rollback(conn);
			throw e;
		} finally {
			endSession(conn);
		}
	}

	public synchronized void deleteElement(Element e) throws MapsException {
		log.debug("deleting element...");
		if (e != null) {
			deleteElement(e.getId(), e.getMapId(), e.getType());
		}
	}

	private synchronized void deleteElementInSession(int id, int mapid,
			String type) throws MapsException {
		log.debug("deleting element...");
        Connection conn = startSession();

		final String sqlDelete = "DELETE FROM " + elementTable
				+ " WHERE elementid = ? AND mapid = ? AND elementtype = ?";

		try {
			PreparedStatement statement = conn
					.prepareStatement(sqlDelete);
			statement.setInt(1, id);
			statement.setInt(2, mapid);
			statement.setString(3, type);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			log.error("error while deleting element...");
            rollback(conn);
			throw new MapsException(e);
		} finally {
		    endSession(conn);
		}
	}

	public synchronized void deleteElement(int id, int mapid, String type)
			throws MapsException {
		log.debug("deleting element...");
		Connection conn = startSession();
		final String sqlDelete = "DELETE FROM " + elementTable
				+ " WHERE elementid = ? AND mapid = ? AND elementtype = ?";

		try {
			PreparedStatement statement = conn
					.prepareStatement(sqlDelete);
			statement.setInt(1, id);
			statement.setInt(2, mapid);
			statement.setString(3, type);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			log.error("error while deleting element...", e);
			rollback(conn);
			throw new MapsException(e);

		} finally {
			endSession(conn);
		}
	}

	public synchronized void deleteElementsOfMap(int id) throws MapsException {
		log.debug("deleting elements of map...");
		Connection conn = startSession();
		final String sqlDelete = "DELETE FROM " + elementTable
				+ " WHERE mapid = ?";

		try {
			PreparedStatement statement = conn
					.prepareStatement(sqlDelete);
			statement.setInt(1, id);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			log.error("Error while deleting elements of map " + id);
			rollback(conn);
			throw new MapsException(e);
		} finally {
			endSession(conn);
		}
	}

	public synchronized int deleteMap(Map m) throws MapsException {
		log.debug("deleting map...");
		return deleteMap(m.getId());
	}

	public synchronized int deleteMap(int id) throws MapsException {
		log.debug("deleting map...");
		Connection conn = startSession();
		final String sqlDeleteMap = "DELETE FROM " + mapTable
				+ " WHERE mapid = ?";
		final String sqlDeleteElemMap = "DELETE FROM " + elementTable
				+ " WHERE elementid = ? AND elementtype = ?";
		int countDelete = 0;
		try {
			PreparedStatement statement = conn
					.prepareStatement(sqlDeleteMap);
			statement.setInt(1, id);
			countDelete = statement.executeUpdate();
			statement.close();
			statement = conn.prepareStatement(sqlDeleteElemMap);
			statement.setInt(1, id);
			statement.setString(2, Element.MAP_TYPE);
			statement.executeUpdate();
			statement.close();
			return countDelete;
		} catch (SQLException e) {
			log.error("error while deleting map " + id);
			rollback(conn);
			throw new MapsException(e);
		} finally {
			endSession(conn);
		}
	}

	public synchronized void deleteNodeTypeElementsFromAllMaps()
			throws MapsException {
		log.debug("deleting all node elements...");
		Connection conn = startSession();
		final String sqlDelete = "DELETE FROM " + elementTable
				+ " WHERE elementtype = ?";

		try {
			PreparedStatement statement = conn
					.prepareStatement(sqlDelete);
			statement.setString(1, Element.NODE_TYPE);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			log.error("error while deleting all node elements");
			rollback(conn);
			throw new MapsException(e);

		} finally {
			endSession(conn);
		}
	}

	public synchronized void deleteMapTypeElementsFromAllMaps()
			throws MapsException {
		log.debug("deleting all map elements...");
		Connection conn = startSession();
		final String sqlDelete = "DELETE FROM " + elementTable
				+ " WHERE elementtype = ?";

		try {
			PreparedStatement statement = conn
					.prepareStatement(sqlDelete);
			statement.setString(1, Element.MAP_TYPE);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			log.error("error while deleting all map elements");
			rollback(conn);
			throw new MapsException(e);
		} finally {
			endSession(conn);
		}
	}

	public Element getElement(int id, int mapId, String type)
			throws MapsException {
        Connection conn = createConnection();

	    try {
			final String sqlQuery = "SELECT * FROM " + elementTable
					+ " WHERE elementid = ? AND mapid = ? and elementtype = ?";

			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			statement.setInt(1, id);
			statement.setInt(2, mapId);
			statement.setString(3, type);
			ResultSet rs = statement.executeQuery();
			Element el = rs2Element(rs);
			rs.close();
			statement.close();

			return el;
		} catch (Exception e) {
			log.error("Exception while getting element with elementid=" + id
					+ " and mapid=" + mapId);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public Element newElement(int id, int mapId, String type)
			throws MapsException {
		Element e = new Element(mapId, id, type, null, null, 0, 0);
		e = completeElement(e);
		log.debug("Creating new VElement mapId:" + mapId + " id:" + id
				+ " type:" + type + " label:" + e.getLabel() + " iconname:"
				+ e.getIcon() + " x:" + 0 + " y:" + 0);
		return e;
	}

	/**
	 * Completes the element in input (with id and type already valorized) with
	 * its label (or name if is a map) and iconname
	 * 
	 * @param e
	 * @return the element completed of label and icon name
	 */
	private Element completeElement(Element e) throws MapsException {

        Connection conn = createConnection();
		String sqlQuery = null;
		try {
			if (e.getType().equals(Element.MAP_TYPE)) {
				e.setIcon(Element.defaultMapIcon);
				sqlQuery = "SELECT mapname FROM " + mapTable
						+ " WHERE mapId = ?";
			} else {
				sqlQuery = "SELECT nodelabel,nodesysoid FROM node WHERE nodeid = ?";
			}
			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			statement.setInt(1, e.getId());
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				e.setLabel(rs.getString(1));
				if (e.getType().equals(Element.NODE_TYPE)) {
					String iconName = null;
					if (rs.getString(2) != null && getIconBySysoid(rs.getString(2)) !=null) {
					    iconName = getIconBySysoid(rs.getString(2));
					} else {
					    iconName = Element.defaultNodeIcon;
					}
					log.debug("DBManager: iconName = " + iconName + ", sysoid = " + rs.getString(2));
					e.setIcon(iconName);
				}
			}
			rs.close();
			statement.close();

		} catch (Exception e1) {
			log.error("Error while completing element (" + e.getId()
					+ ") with label and icon ", e1);
			throw new MapsException(e1);
		} finally {
			releaseConnection(conn);
		}

		return e;
	}

	private String getIconBySysoid(String sysoid) throws MapsException {
	    try {
	        MapPropertiesFactory mpf =new MapPropertiesFactory();
	        java.util.Map<String, String> iconsBySysoid = mpf.getIconsBySysoid();
	        if (iconsBySysoid != null) {
	            log.debug("getIconBySysoid: sysoid = " + sysoid);
	            for (String key : iconsBySysoid.keySet()) {
	                log.debug("getIconBySysoid: key = " + key);
	                if(key.equals(sysoid)) {
	                    log.debug("getIconBySysoid: iconBySysoid = " + iconsBySysoid.get(key));
	                    return iconsBySysoid.get(key);
	                }
	            }
	        }
	    } catch (Exception e) {
            log.error("Exception while getting icons by sysoid");
            throw new MapsException(e);
        }
	    return Element.defaultNodeIcon;
    }

    public Element[] getAllElements() throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT * FROM " + elementTable;

			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sqlQuery);
			Vector<Element> elements = rs2ElementVector(rs);
			Element[] el = new Element[elements.size()];
			el = elements.toArray(el);
			rs.close();
			statement.close();
			// conn.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting all elements");
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public Element[] getElementsOfMap(int mapid) throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT * FROM " + elementTable
					+ " WHERE mapid = ?";

			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			statement.setInt(1, mapid);
			ResultSet rs = statement.executeQuery();
			Vector<Element> elements = rs2ElementVector(rs);
			Element[] el = null;
			if (elements != null) {
				el = new Element[elements.size()];
				el = elements.toArray(el);
			}
			rs.close();
			statement.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting elements of map with mapid="
					+ mapid);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public Element[] getNodeElementsOfMap(int mapid) throws MapsException {
        Connection conn = createConnection();
        try {
			final String sqlQuery = "SELECT * FROM " + elementTable
					+ " WHERE mapid = ? AND elementtype = 'N' ";
			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			statement.setInt(1, mapid);
			ResultSet rs = statement.executeQuery();
			Vector<Element> elements = rs2ElementVector(rs);
			Element[] el = null;
			if (elements != null) {
				el = new Element[elements.size()];
				el = elements.toArray(el);
			}
			rs.close();
			statement.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting element node of map with mapid "
					+ mapid);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public Element[] getMapElementsOfMap(int mapid) throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT * FROM " + elementTable
					+ " WHERE mapid = ? AND elementtype = 'M' ";

			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			statement.setInt(1, mapid);
			ResultSet rs = statement.executeQuery();
			Vector<Element> elements = rs2ElementVector(rs);
			Element[] el = null;
			if (elements != null) {
				el = new Element[elements.size()];
				el = elements.toArray(el);
			}
			rs.close();
			statement.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting map element of map with mapid "
					+ mapid);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public Element[] getElementsLike(String elementLabel) throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT * FROM " + elementTable
					+ " WHERE elementlabel LIKE ?";

			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			elementLabel = "%" + elementLabel + "%";
			statement.setString(1, elementLabel);
			ResultSet rs = statement.executeQuery();
			Vector<Element> elements = rs2ElementVector(rs);
			Element[] el = new Element[elements.size()];
			el = elements.toArray(el);
			rs.close();
			statement.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting elements by label like "
					+ elementLabel);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public java.util.Map<Integer, Set<Integer>> getMapsStructure()
			throws MapsException {
        Connection conn = createConnection();
		try {
			java.util.Map<Integer, Set<Integer>> maps = new HashMap<Integer, Set<Integer>>();
			String sqlQuery = "select elementid,mapid from " + elementTable
					+ " where elementtype=?";
			PreparedStatement ps = conn.prepareStatement(sqlQuery);
			ps.setString(1, Element.MAP_TYPE);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Integer parentId = new Integer(rs.getInt("mapid"));
				Integer childId = new Integer(rs.getInt("elementid"));

				Set<Integer> childs = maps.get(parentId);

				if (childs == null) {
					childs = new HashSet<Integer>();
				}

				if (!childs.contains(childId)) {
					childs.add(childId);
				}
				maps.put(parentId, childs);
			}
			return maps;
		} catch (Exception e) {
			log.error("Exception while getting maps parent-child structure");
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public int countMaps(int mapId) throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT COUNT(*) FROM " + mapTable
					+ " WHERE mapid = ?";

			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			statement.setInt(1, mapId);
			ResultSet rs = statement.executeQuery();
			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			statement.close();
			return count;
		} catch (Exception e) {
			log.error("Exception while counting maps");
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public Map getMap(int id) throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT * FROM " + mapTable
					+ " WHERE mapId = ?";
			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			Map map = rs2Map(rs);
			rs.close();
			statement.close();

			return map;
		} catch (Exception e) {
			log.error("Exception while getting map with mapid=" + id);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public Map[] getMaps(String mapname, String maptype) throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT * FROM " + mapTable
					+ " WHERE mapName= ? AND maptype = ? ";
			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			statement.setString(1, mapname);
			statement.setString(2, maptype);
			ResultSet rs = statement.executeQuery();
			Vector<Map> maps = rs2MapVector(rs);
			Map[] el = null;
			if (maps != null) {
				el = new Map[maps.size()];
				el = (Map[]) maps.toArray(el);
			}
			rs.close();
			statement.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting maps with name=" + mapname
					+ " and type=" + maptype);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public Map[] getAllMaps() throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT * FROM " + mapTable;
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sqlQuery);
			Vector<Map> maps = rs2MapVector(rs);

			Map[] el = null;
			if (maps != null) {
				el = new Map[maps.size()];
				el = maps.toArray(el);
			}
			rs.close();
			statement.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting all Maps");
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public Map[] getMapsLike(String mapLabel) throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT * FROM " + mapTable
					+ " WHERE mapname LIKE ?";

			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			mapLabel = "%" + mapLabel + "%";
			statement.setString(1, mapLabel);
			ResultSet rs = statement.executeQuery();
			Vector<Map> mapVector = rs2MapVector(rs);
			Map[] maps = null;
			if (mapVector != null) {
				maps = new Map[mapVector.size()];
				maps = mapVector.toArray(maps);
			}
			rs.close();
			statement.close();
			// conn.close();
			return maps;
		} catch (Exception e) {
			log.error("Exception while getting maps by label like " + mapLabel);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public Map[] getMapsByName(String mapLabel) throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT * FROM " + mapTable
					+ " WHERE mapname = ?";

			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			statement.setString(1, mapLabel);
			ResultSet rs = statement.executeQuery();
			Vector<Map> mapVector = rs2MapVector(rs);
			Map[] maps = null;
			if (mapVector != null) {
				maps = new Map[mapVector.size()];
				maps = mapVector.toArray(maps);
			}
			rs.close();
			statement.close();

			return maps;
		} catch (Exception e) {
			log
					.error("Exception while getting elements with label "
							+ mapLabel);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public Map[] getContainerMaps(int id, String type) throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT " + mapTable + ".* FROM "
					+ mapTable + " INNER JOIN " + elementTable + " ON "
					+ mapTable + ".mapid = " + elementTable
					+ ".mapid WHERE elementid = ? AND elementtype = ?";

			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			statement.setInt(1, id);
			statement.setString(2, type);
			ResultSet rs = statement.executeQuery();
			Vector<Map> el = rs2MapVector(rs);
			Map[] maps = new Map[el.size()];
			maps = el.toArray(maps);
			rs.close();
			statement.close();

			return maps;
		} catch (Exception e) {
			log
					.error("Exception while getting container maps of element with id/type "
							+ id + "/" + type);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public VMapInfo[] getAllMapMenus() throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT mapid,mapname,mapowner FROM "
					+ mapTable + " order by mapname";

			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sqlQuery);
			Vector<VMapInfo> maps = rs2MapMenuVector(rs);

			VMapInfo[] el = null;
			if (maps != null) {
				el = new VMapInfo[maps.size()];
				el = maps.toArray(el);
			}
			rs.close();
			statement.close();
			// conn.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting all map-menu");
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public VMapInfo getMapMenu(int mapId) throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT mapid,mapname,mapowner FROM "
					+ mapTable + " where mapId= ?";

			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			statement.setInt(1, mapId);
			ResultSet rs = statement.executeQuery();
			VMapInfo mm = rs2MapMenu(rs);

			rs.close();
			statement.close();
			// conn.close();

			return mm;
		} catch (Exception e) {
			log.error("Exception while getting map-menu for mapid " + mapId);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public VMapInfo[] getMapsMenuByName(String mapLabel) throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT mapid,mapname,mapowner FROM "
					+ mapTable + " WHERE upper( mapname ) = upper( ? )";

			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			statement.setString(1, mapLabel);
			ResultSet rs = statement.executeQuery();
			Vector<VMapInfo> mapVector = rs2MapMenuVector(rs);
			VMapInfo[] maps = null;
			if (mapVector != null) {
				maps = new VMapInfo[mapVector.size()];
				maps = mapVector.toArray(maps);
			}
			rs.close();
			statement.close();
			// conn.close();
			return maps;
		} catch (Exception e) {
			log.error("Exception while getting all map-menu for map named "
					+ mapLabel);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public VMapInfo[] getMapsMenuByOwner(String owner) throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT mapid,mapname,mapowner FROM "
					+ mapTable + " WHERE upper( mapowner ) = upper( ? ) and " +
					"upper( mapaccess ) = upper( ? )" ;

			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			statement.setString(1, owner);
            statement.setString(2, Map.ACCESS_MODE_GROUP);
			ResultSet rs = statement.executeQuery();
			Vector<VMapInfo> mapVector = rs2MapMenuVector(rs);
			VMapInfo[] maps = null;
			if (mapVector != null) {
				maps = new VMapInfo[mapVector.size()];
				maps = mapVector.toArray(maps);
			}
			rs.close();
			statement.close();
			return maps;
		} catch (Exception e) {
			log
					.error("Exception while getting all map-menu for owner "
							+ owner);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

   public VMapInfo[] getMapsMenuByGroup(String group) throws MapsException {
        Connection conn = createConnection();
        try {
            final String sqlQuery = "SELECT mapid,mapname,mapowner FROM "
                    + mapTable + " WHERE upper( mapgroup ) = upper( ? ) and " +
                    		"upper( mapaccess ) = upper( ? )";

            PreparedStatement statement = conn
                    .prepareStatement(sqlQuery);
            statement.setString(1, group);
            statement.setString(2, Map.ACCESS_MODE_GROUP);
            ResultSet rs = statement.executeQuery();
            Vector<VMapInfo> mapVector = rs2MapMenuVector(rs);
            VMapInfo[] maps = null;
            if (mapVector != null) {
                maps = new VMapInfo[mapVector.size()];
                maps = mapVector.toArray(maps);
            }
            rs.close();
            statement.close();
            return maps;
        } catch (Exception e) {
            log
                    .error("Exception while getting all map-menu for group "
                            + group);
            throw new MapsException(e);
        } finally {
            releaseConnection(conn);
        }
    }

   public VMapInfo[] getMapsMenuByOther() throws MapsException {
       Connection conn = createConnection();
       try {
           final String sqlQuery = "SELECT mapid,mapname,mapowner FROM "
                   + mapTable + " WHERE upper( mapaccess ) = upper( ? ) or " +
                   		"upper( mapaccess ) = upper( ? )";

           PreparedStatement statement = conn
                   .prepareStatement(sqlQuery);
           statement.setString(1, Map.ACCESS_MODE_ADMIN);
           statement.setString(2, Map.ACCESS_MODE_USER);
           ResultSet rs = statement.executeQuery();
           Vector<VMapInfo> mapVector = rs2MapMenuVector(rs);
           VMapInfo[] maps = null;
           if (mapVector != null) {
               maps = new VMapInfo[mapVector.size()];
               maps = mapVector.toArray(maps);
           }
           rs.close();
           statement.close();
           return maps;
       } catch (Exception e) {
           log
                   .error("Exception while getting other map for access ");
           throw new MapsException(e);
       } finally {
           releaseConnection(conn);
       }
   }

	public boolean isElementInMap(int elementId, int mapId, String type)
			throws MapsException {
		try {
			Element element = null;
			element = getElement(elementId, mapId, type);
			return (element != null);
		} catch (Exception e) {
			throw new MapsException(e);
		}
	}

	public VElementInfo[] getAllElementInfo() throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = " SELECT  nodeid,nodelabel FROM node WHERE nodetype!='D'";
			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			ResultSet rs = statement.executeQuery();
			Vector<VElementInfo> elements = new Vector<VElementInfo>();
			while (rs.next()) {
				VElementInfo ei = new VElementInfo(rs.getInt("nodeid"), "", 0,
						(rs.getString("nodelabel") != null) ? rs
								.getString("nodelabel") : "("
								+ rs.getInt("nodeid") + ")");
				elements.add(ei);
			}
			VElementInfo[] el = null;
			if (elements != null) {
				el = new VElementInfo[elements.size()];
				el = elements.toArray(el);
			}
			rs.close();
			statement.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting all element infos", e);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public VElementInfo[] getElementInfoLike(String like) throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = " SELECT  nodeid,nodelabel FROM node WHERE nodelabel like '"
					+ like + "%' AND  nodetype!='D'";
			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			ResultSet rs = statement.executeQuery();
			Vector<VElementInfo> elements = new Vector<VElementInfo>();
			while (rs.next()) {
				VElementInfo ei = new VElementInfo(rs.getInt("nodeid"), "", 0,
						(rs.getString("nodelabel") != null) ? rs
								.getString("nodelabel") : "("
								+ rs.getInt("nodeid") + ")");
				elements.add(ei);
			}
			VElementInfo[] el = null;
			if (elements != null) {
				el = new VElementInfo[elements.size()];
				el = elements.toArray(el);
			}
			rs.close();
			statement.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting element infos like " + like, e);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	public List<VElementInfo> getAlarmedElements() throws MapsException {
        Connection conn = createConnection();
		try {
//			final String sqlQuery = "select distinct outages.nodeid, eventuei,eventseverity from outages left join events on events.eventid = outages.svclosteventid where ifregainedservice is null order by nodeid";
		    final String sqlQuery = "select nodeid, eventuei,severity from alarms where nodeid is not null and severity > 3 order by nodeid, lasteventtime desc";
			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			ResultSet rs = statement.executeQuery();
			List<VElementInfo> elems = new ArrayList<VElementInfo>();
			while (rs.next()) {
				VElementInfo einfo = new VElementInfo(rs.getInt(1), rs
						.getString(2), rs.getInt(3));
				elems.add(einfo);
			}
			rs.close();
			statement.close();
			// conn.close();
			return elems;
		} catch (Exception e) {
			log.error("Exception while getting outaged elements");
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}

	}

	public java.util.Map<Integer, Double> getAvails(Element[] mapElements)
			throws MapsException {
		// get avails for all nodes in map and its submaps
		java.util.Map<Integer, Double> availsMap = null;
		log.debug("avail Enabled");
		log.debug("getting all nodeids of map (and submaps)");
		Set<Integer> nodeIds = new HashSet<Integer>();
		if (mapElements != null) {
			for (int i = 0; i < mapElements.length; i++) {
				if (mapElements[i].isNode()) {
					nodeIds.add(new Integer(mapElements[i].getId()));
				} else {
					nodeIds.addAll(getNodeidsOnElement(mapElements[i]));
				}
			}
		}
		log.debug("all nodeids obtained");
		log.debug("Getting avails for nodes of map (" + nodeIds.size()
				+ " nodes)");

		availsMap = getNodeAvailability(nodeIds);
		log.debug("Avails obtained");
		return availsMap;
	}

	/**
	 * Return the availability percentage for all managed services on the given
	 * nodes from the given start time until the given end time. If there are no
	 * managed services on these nodes, then a value of -1 is returned.
	 */
	private java.util.Map<Integer, Double> getNodeAvailability(Set<Integer> nodeIds)
			throws MapsException {

		Calendar cal = new GregorianCalendar();
		Date end = cal.getTime();
		cal.add(Calendar.DATE, -1);
		Date start = cal.getTime();

		if (nodeIds == null) {
			throw new IllegalArgumentException("Cannot take nodeIds null.");
		}
		if (start == null || end == null) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}

		if (end.before(start)) {
			throw new IllegalArgumentException(
					"Cannot have an end time before the start time.");
		}

		if (end.equals(start)) {
			throw new IllegalArgumentException(
					"Cannot have an end time equal to the start time.");
		}

		double avail = -1;
		int nodeid = 0;
		java.util.Map<Integer, Double> retMap = new TreeMap<Integer, Double>();
		if (nodeIds.size() > 0) {
            Connection conn = createConnection();
			try {
				StringBuffer sb = new StringBuffer(
						"select nodeid, getManagePercentAvailNodeWindow(nodeid, ?, ?)  from node where nodeid in (");
				Iterator<Integer> it = nodeIds.iterator();
				while (it.hasNext()) {
					sb.append(it.next());
					if (it.hasNext()) {
						sb.append(", ");
					}
				}
				sb.append(")");
				PreparedStatement stmt = conn.prepareStatement(sb
						.toString());

				// yes, these are supposed to be backwards, the end time first
				stmt.setTimestamp(1, new Timestamp(end.getTime()));
				stmt.setTimestamp(2, new Timestamp(start.getTime()));

				ResultSet rs = stmt.executeQuery();

				while (rs.next()) {
					nodeid = rs.getInt(1);
					avail = rs.getDouble(2);
					retMap.put(new Integer(nodeid), new Double(avail));
				}
			} catch (Exception e) {
				throw new MapsException(e);
			} finally {
				releaseConnection(conn);
			}
		}

		return retMap;
	}

	String getMapName(int id) throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT mapname FROM " + mapTable
					+ " WHERE mapId = ?";
			PreparedStatement statement = conn
					.prepareStatement(sqlQuery);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			String label = null;
			if (rs.next()) {
				label = rs.getString(1);
			}
			rs.close();
			statement.close();
			// conn.close();
			return label;
		} catch (Exception e) {
			log.error("Exception while getting name of map with mapid " + id);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	/**
	 * gets a Vector containing the nodeids of all deleted nodes
	 * 
	 * @return Vector of Integer containing all deleted nodes' ids
	 */
	public Vector<Integer> getDeletedNodes() throws MapsException {
        Connection conn = createConnection();
		try {
			final String sqlQuery = "SELECT nodeid  FROM node where nodetype='D'";

			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sqlQuery);
			Vector<Integer> elements = new Vector<Integer>();
			while (rs.next()) {
				int nId = rs.getInt(1);
				elements.add(new Integer(nId));
			}
			rs.close();
			statement.close();
			// conn.close();
			return elements;
		} catch (Exception e) {
			log.error("Exception while getting deleted nodes");
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
	}

	/**
	 * recursively gets all nodes contained by elem and its submaps (if elem is
	 * a map)
	 */
	public Set<Integer> getNodeidsOnElement(Element elem) throws MapsException {
		Set<Integer> elementNodeIds = new HashSet<Integer>();
		if (elem.isNode()) {
			elementNodeIds.add(new Integer(elem.getId()));
			// This is not OK now
			// elementNodeIds.addAll(getNodesFromParentNode(elem.getId()));
		} else if (elem.isMap()) {
			int curMapId = elem.getId();
			Element[] elemNodeElems = getNodeElementsOfMap(curMapId);
			if (elemNodeElems != null && elemNodeElems.length > 0) {
				for (int i = 0; i < elemNodeElems.length; i++) {
					elementNodeIds.add(new Integer(elemNodeElems[i].getId()));
				}
			}

			Element[] elemMapElems = getMapElementsOfMap(curMapId);
			if (elemMapElems != null && elemMapElems.length > 0) {
				for (int i = 0; i < elemMapElems.length; i++) {
					elementNodeIds.addAll(getNodeidsOnElement(elemMapElems[i]));
				}
			}
		}
		return elementNodeIds;

	}

	private Vector<Map> rs2MapVector(ResultSet rs) throws SQLException {
		Vector<Map> mapVec = null;
		boolean firstTime = true;
		while (rs.next()) {
			if (firstTime) {
				mapVec = new Vector<Map>();
				firstTime = false;
			}
			Map currMap = new Map();
			currMap.setAccessMode(rs.getString("mapAccess"));
			currMap.setBackground(rs.getString("mapBackGround"));
			currMap.setId(rs.getInt("mapId"));
			currMap.setName(rs.getString("mapName"));
			currMap.setOffsetX(rs.getInt("mapXOffset"));
			currMap.setOffsetY(rs.getInt("mapYOffset"));
			currMap.setOwner(rs.getString("mapOwner"));
			currMap.setScale(rs.getFloat("mapScale"));
			currMap.setType(rs.getString("mapType"));
			currMap.setWidth(rs.getInt("mapwidth"));
			currMap.setHeight(rs.getInt("mapheight"));
			currMap.setUserLastModifies(rs.getString("userLastModifies"));
			currMap.setCreateTime(rs.getTimestamp("mapCreateTime"));
			currMap.setLastModifiedTime(rs.getTimestamp("lastmodifiedtime"));
			currMap.setAsNew(false);
			mapVec.add(currMap);
		}
		return mapVec;
	}

	private Vector<VMapInfo> rs2MapMenuVector(ResultSet rs) throws SQLException {
		Vector<VMapInfo> mapVec = null;
		boolean firstTime = true;
		while (rs.next()) {
			if (firstTime) {
				mapVec = new Vector<VMapInfo>();
				firstTime = false;
			}

			VMapInfo currMap = new VMapInfo(rs.getInt("mapId"), rs
					.getString("mapName"), rs.getString("mapOwner"));
			mapVec.add(currMap);
		}
		return mapVec;
	}

	private VMapInfo rs2MapMenu(ResultSet rs) throws SQLException {
		VMapInfo map = null;
		if (rs.next()) {
			map = new VMapInfo(rs.getInt("mapId"), rs.getString("mapName"), rs
					.getString("mapOwner"));
		}
		return map;
	}

	private Map rs2Map(ResultSet rs) throws SQLException {
		Map map = null;
		if (rs.next()) {
			map = new Map();
			map.setAccessMode(rs.getString("mapAccess"));
			map.setBackground(rs.getString("mapBackGround"));
			map.setId(rs.getInt("mapId"));
			map.setName(rs.getString("mapName"));
			map.setOffsetX(rs.getInt("mapXOffset"));
			map.setOffsetY(rs.getInt("mapYOffset"));
			map.setOwner(rs.getString("mapOwner"));
            map.setGroup(rs.getString("mapGroup"));
			map.setScale(rs.getFloat("mapScale"));
			map.setType(rs.getString("mapType"));
			map.setWidth(rs.getInt("mapwidth"));
			map.setHeight(rs.getInt("mapheight"));
			map.setUserLastModifies(rs.getString("userLastModifies"));
			map.setCreateTime(rs.getTimestamp("mapCreateTime"));
			map.setLastModifiedTime(rs.getTimestamp("lastmodifiedtime"));
			map.setAsNew(false);
		}
		return map;
	}

	private Element rs2Element(ResultSet rs) throws SQLException, MapsException {
		Element element = null;
		if (rs.next()) {
			element = new Element();
			element.setMapId(rs.getInt("mapId"));
			element.setId(rs.getInt("elementId"));
			element.setType(rs.getString("elementType"));
			element.setLabel(rs.getString("elementLabel"));
			element.setIcon(rs.getString("elementIcon"));
			element.setX(rs.getInt("elementX"));
			element.setY(rs.getInt("elementY"));
		}
		return element;
	}

	private Vector<Element> rs2ElementVector(ResultSet rs) throws SQLException,
			MapsException {
		Vector<Element> vecElem = null;
		boolean firstTime = true;
		while (rs.next()) {
			if (firstTime) {
				vecElem = new Vector<Element>();
				firstTime = false;
			}
			Element currElem = new Element();
			currElem.setMapId(rs.getInt("mapId"));
			currElem.setId(rs.getInt("elementId"));
			currElem.setType(rs.getString("elementType"));
			currElem.setLabel(rs.getString("elementLabel"));
			currElem.setIcon(rs.getString("elementIcon"));
			currElem.setX(rs.getInt("elementX"));
			currElem.setY(rs.getInt("elementY"));
			vecElem.add(currElem);
		}
		return vecElem;
	}

	public Set<LinkInfo> getLinksOnElements(Set<Integer> allnodes)
			throws MapsException {
		log.debug("getLinksOnElements "+allnodes);
		Set<LinkInfo> nodes = null;
        Connection conn = createConnection();
		try {
			nodes = new HashSet<LinkInfo>();
			if (allnodes == null || allnodes.size()==0)
				return nodes;
			String nodelist = "";
			Iterator<Integer> ite = allnodes.iterator();
			while (ite.hasNext()) {
				nodelist += ite.next();
				if (ite.hasNext())
					nodelist += ",";
			}

			String sql = "SELECT "
					+ "datalinkinterface.nodeid, ifindex,nodeparentid, parentifindex, snmpiftype,snmpifspeed,snmpifoperstatus "
					+ "FROM datalinkinterface "
					+ "left join snmpinterface on nodeparentid = snmpinterface.nodeid "
					+ "WHERE"
					+ " (datalinkinterface.nodeid IN ("
					+ nodelist
					+ ")"
					+ " OR nodeparentid in ("
					+ nodelist
					+ ")) "
					+ "AND status != 'D' and datalinkinterface.parentifindex = snmpinterface.snmpifindex";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Object element = new Integer(rs.getInt("nodeid"));
				int nodeid = -1;
				if (element != null) {
					nodeid = ((Integer) element);
				}

				element = new Integer(rs.getInt("ifindex"));
				int ifindex = -1;
				if (element != null) {
					ifindex = ((Integer) element);
				}

				element = new Integer(rs.getInt("nodeparentid"));
				int nodeparentid = -1;
				if (element != null) {
					nodeparentid = ((Integer) element);
				}

				element = new Integer(rs.getInt("parentifindex"));
				int parentifindex = -1;
				if (element != null) {
					parentifindex = ((Integer) element);
				}

				element = new Integer(rs.getInt("snmpiftype"));
				int snmpiftype = -1;
				if (element != null) {
					snmpiftype = ((Integer) element);
				}

				element = new Long(rs.getLong("snmpifspeed"));
				long snmpifspeed = -1;
				if (element != null) {
					snmpifspeed = ((Long) element);
				}

				element = new Integer(rs.getInt("snmpifoperstatus"));
				int snmpifoperstatus = -1;
				if (element != null) {
					snmpifoperstatus = ((Integer) element);
				}

				LinkInfo link = new LinkInfo(nodeid, ifindex, nodeparentid,
						parentifindex, snmpiftype, snmpifspeed,
						snmpifoperstatus);

				nodes.add(link);
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			log.error("Exception while getting links on elements " + allnodes,
					e);
			throw new MapsException(e);
		} finally {
			releaseConnection(conn);
		}
		return nodes;

	}

	public Set<Integer> getNodeIdsBySource(String query) throws MapsException {
		if (query == null) {
			return getAllNodes();
		}
		Set<Integer> nodes = new HashSet<Integer>();
        Connection conn = createConnection();
		try {
			String sqlQuery = query;
			log.debug("Applying filters for source " + " '" + sqlQuery + "'");

			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlQuery);
			// add all matching nodes (id) with the source to the Set.
			while (rs.next()) {
				nodes.add(new Integer(rs.getInt(1)));
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new MapsException(
					"Exception while getting nodes by source label " + e);
		} finally {
			releaseConnection(conn);
		}
		return nodes;
	}

	private Set<Integer> getAllNodes() throws MapsException {
        Connection conn = createConnection();
        Set<Integer> nodes = new HashSet<Integer>();
		try {
			String sqlQuery = "select distinct nodeid from ipinterface";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlQuery);
			// add all matching nodes (id) with the source to the Set.
			while (rs.next()) {
				nodes.add(new Integer(rs.getInt(1)));
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new MapsException("Exception while getting all nodes " + e);
		} finally {
			releaseConnection(conn);
		}
		return nodes;
	}

    @Override
    public boolean isElementNotDeleted(int elementId, String type) throws MapsException {
        log.debug("isElementNotDeleted: elementId=" + elementId + " type= " + type);
        if (type.equals(Element.MAP_TYPE)) {
            log.debug("isElementNotDeleted: elementId=" + elementId + " type= " + type);
            return isMapInRow(elementId);            
        } else if (type.equals(Element.NODE_TYPE)) {
            log.debug("isElementNotDeleted: elementId=" + elementId + " type= " + type);
            return isNodeInRow(elementId);
        }
        return false;
    }

    private boolean isMapInRow(int mapId) throws MapsException {
        Connection conn = createConnection();
        boolean isThere = false;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT mapid FROM map WHERE MAPID = ?");
            stmt.setInt(1, mapId);
            ResultSet rs = stmt.executeQuery();
            if (rs == null) {
                throw new IllegalArgumentException("rs parameter cannot be null");
            }
            isThere = !rs.next();
            rs.close();
            stmt.close();
        } catch (Exception e) {
            throw new MapsException("Exception while getting mapid " + e);
        } finally {
            releaseConnection(conn);
        }
        log.debug("isMapInRow: elementId=" + mapId + "is There: " + isThere);
        return isThere;
    }

    private boolean isNodeInRow(int nodeId) throws MapsException {
        Connection conn = createConnection();
        boolean isThere = false;        
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT nodeid FROM NODE WHERE NODEID = ?");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
            if (rs == null) {
                throw new IllegalArgumentException("rs parameter cannot be null");
            }
            isThere = !rs.next();
            rs.close();
            stmt.close();
        } catch (Exception e) {
            throw new MapsException("Exception while getting nodeid " + e);
        } finally {
            releaseConnection(conn);
        }
        log.debug("isNodeInRow: elementId=" + nodeId + "is There: " + isThere);
        return isThere;
    }

}
