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

import org.opennms.web.map.view.VElementInfo;
import org.opennms.web.map.view.VMapInfo;

/**
 * <p>DBManager class.</p>
 *
 * @author maumig
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 *
 * The class manages maps stored on DB. It uses the constructor parameters
 * for the connection. If default constructor is called, it uses default OpenNMS db connector (Vault)
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 *
 * The class manages maps stored on DB. It uses the constructor parameters
 * for the connection. If default constructor is called, it uses default OpenNMS db connector (Vault)
 * @version $Id: $
 * @since 1.6.12
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

	Connection m_dbConnection = null;

	Category log = null;

	private DbConnectionFactory m_factory = null;
	
	/**
	 * <p>Constructor for DBManager.</p>
	 *
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public DBManager() throws MapsException {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		if (log.isDebugEnabled())
			log.debug("Instantiating DBManager (using Vault)");
	}
	
	/**
	 * <p>Constructor for DBManager.</p>
	 *
	 * @param params a java$util$Map object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public DBManager(java.util.Map params)
	throws MapsException {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		if (log.isDebugEnabled())
			log.debug("Instantiating DBManager with params: "+params);		
		String url = (String) params.get("url");
		String driver = (String)params.get("driver");
		String user =(String) params.get("user");
		String password = (String)params.get("password");
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

	 void createConnection() throws Exception {

		// create the dbconnection only if not yet created and active
		if (isConnectionCreated())
			return;

		log.debug("creating connection");
		if(m_factory!=null){
			m_dbConnection = m_factory.getConnection();
		}else{
			m_dbConnection = Vault.getDbConnection();
		}
	}

	 void releaseConnection() throws MapsException {
		log.debug("releasing connection");
		try {
			if (m_dbConnection != null && !m_dbConnection.isClosed()){
				if(m_factory!=null){
					m_dbConnection.close();
				}else{
					Vault.releaseDbConnection(m_dbConnection);
				}
			}
		} catch (Exception e) {
			log.error("Exception while releasing connection");
			throw new MapsException(e);
		}
	}

	/**
	 * <p>finalize</p>
	 *
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public void finalize() throws MapsException {
		log.debug("finalizing...");
		try {
			releaseConnection();
		} catch (Exception e) {
			log.error("Exception while finalizing", e);
			throw new MapsException(e);
		}
	}

	private void startSession() throws MapsException {
		if (!isStartedSession()) {
			try {
				createConnection();
				log.debug("setting AutoCommit false db connection...");
				m_dbConnection.setAutoCommit(false);
				m_dbConnection
						.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			} catch (Exception e) {
				log.error("error while starting session");
				throw new MapsException(e);
			}
		}
	}

	synchronized private void endSession() throws MapsException {
		try {
			log.debug("ending session");
			if (!isStartedSession()) {
				log.debug("session not started");
				return;
			}
			m_dbConnection.commit();
			m_dbConnection.setAutoCommit(true);
			releaseConnection();
		} catch (Exception e) {
			log.error("error while ending session");
			throw new MapsException(e);
		}
	}

	private boolean isConnectionCreated() throws MapsException {
		try {
			return (m_dbConnection != null && !m_dbConnection.isClosed());
		} catch (SQLException s) {
			throw new MapsException(s);
		}
	}

	private void rollback() throws MapsException {
		try {
			m_dbConnection.rollback();
		} catch (SQLException ex) {
			log.error("Error while rollback");
			throw new MapsException(ex);
		} finally {
			releaseConnection();
		}
	}

	private boolean isStartedSession() throws MapsException {
		try {
			return (isConnectionCreated() && m_dbConnection.getAutoCommit() == false);
		} catch (SQLException s) {
			throw new MapsException(s);
		}
	}

	/**
	 * <p>saveMaps</p>
	 *
	 * @param m an array of {@link org.opennms.web.map.db.Map} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public synchronized void saveMaps(Map[] m) throws MapsException {
		try {
			log.debug("saving maps");
			startSession();

			for (int i = 0, n = m.length; i < n; i++) {
				saveMapInSession(m[i]);
			}
		} catch (Exception e) {
			log.error("Error while saving maps ");
			rollback();
		} finally {
			endSession();
		}
	}

	private synchronized void saveMapInSession(Map m) throws MapsException {
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
			Statement stmtCT = m_dbConnection.createStatement();
			ResultSet rs = stmtCT.executeQuery(sqlGetCurrentTimestamp);
			if (rs.next()) {
				currentTimestamp = rs.getTimestamp(1);
				PreparedStatement statement;
				if (m.isNew()) {
					Statement stmtID = m_dbConnection.createStatement();
					ResultSet rsStmt = stmtID.executeQuery(sqlGetMapNxtId);
					if (rsStmt.next()) {
						nxtid = rsStmt.getInt(1);
					}
					rsStmt.close();
					stmtID.close();

					statement = m_dbConnection.prepareStatement(sqlInsertQuery);
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
					statement = m_dbConnection.prepareStatement(sqlUpdateQuery);
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

	/** {@inheritDoc} */
	public synchronized void saveMap(Map m) throws MapsException {
		log.debug("saving map...");
		startSession();
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
			Statement stmtCT = m_dbConnection.createStatement();
			ResultSet rs = stmtCT.executeQuery(sqlGetCurrentTimestamp);
			if (rs.next()) {
				currentTimestamp = rs.getTimestamp(1);
				PreparedStatement statement;
				if (m.isNew()) {
					Statement stmtID = m_dbConnection.createStatement();
					ResultSet rsStmt = stmtID.executeQuery(sqlGetMapNxtId);
					if (rsStmt.next()) {
						nxtid = rsStmt.getInt(1);
					}
					rsStmt.close();
					stmtID.close();

					statement = m_dbConnection.prepareStatement(sqlInsertQuery);
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
					statement = m_dbConnection.prepareStatement(sqlUpdateQuery);
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
			rollback();
			throw new MapsException("Error while saving map " + m.getId(), ex);
		} finally {
			endSession();
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

	/**
	 * <p>saveElements</p>
	 *
	 * @param e an array of {@link org.opennms.web.map.db.Element} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public synchronized void saveElements(Element[] e) throws MapsException {
		try {
			log.debug("saving elements");
			startSession();
			if (e != null) {
				for (int i = 0, n = e.length; i < n; i++) {
					saveElementInSession(e[i]);
				}
			}
		} catch (Exception ex) {
			log.error("error while saving elements");
			rollback();
			throw new MapsException(ex);
		} finally {
			endSession();
		}
	}

	private synchronized void saveElementInSession(Element e)
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
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlSelectQuery);
			statement.setInt(1, e.getId());
			statement.setInt(2, e.getMapId());
			statement.setString(3, e.getType());
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				int count = rs.getInt(1);
				statement.close();
				if (count == 0) {
					statement = m_dbConnection.prepareStatement(sqlInsertQuery);
					statement.setInt(1, e.getMapId());
					statement.setInt(2, e.getId());
					statement.setString(3, e.getType());
					statement.setString(4, e.getLabel());
					statement.setString(5, e.getIcon());
					statement.setInt(6, e.getX());
					statement.setInt(7, e.getY());
				} else {
					statement = m_dbConnection.prepareStatement(sqlUpdateQuery);
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

	/** {@inheritDoc} */
	public synchronized void saveElement(Element e) throws MapsException {
		log.debug("saving element");
		startSession();

		final String sqlSelectQuery = "SELECT COUNT(*) FROM " + elementTable
				+ " WHERE elementid = ? AND MAPID = ? AND elementtype = ?";
		final String sqlInsertQuery = "INSERT INTO "
				+ elementTable
				+ " (mapid, elementid, elementtype, elementlabel, elementicon, elementx, elementy) VALUES (?, ?, ?, ?, ?, ?, ?)";
		final String sqlUpdateQuery = "UPDATE "
				+ elementTable
				+ " SET mapid = ?, elementid = ?, elementtype = ?, elementlabel = ?, elementicon = ?, elementx = ?, elementy = ? WHERE elementid = ? AND mapid = ? AND elementtype = ?";
		try {
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlSelectQuery);
			statement.setInt(1, e.getId());
			statement.setInt(2, e.getMapId());
			statement.setString(3, e.getType());
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				int count = rs.getInt(1);
				statement.close();
				if (count == 0) {
					statement = m_dbConnection.prepareStatement(sqlInsertQuery);
					statement.setInt(1, e.getMapId());
					statement.setInt(2, e.getId());
					statement.setString(3, e.getType());
					statement.setString(4, e.getLabel());
					statement.setString(5, e.getIcon());
					statement.setInt(6, e.getX());
					statement.setInt(7, e.getY());
				} else {
					statement = m_dbConnection.prepareStatement(sqlUpdateQuery);
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
			rollback();
			throw new MapsException(ex);

		} finally {
			endSession();
		}
	}

	/**
	 * <p>deleteElements</p>
	 *
	 * @param elems an array of {@link org.opennms.web.map.db.Element} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public synchronized void deleteElements(Element[] elems)
			throws MapsException {
		log.debug("deleting elements...");
		try {
			startSession();
			if (elems != null) {
				for (int i = 0; i < elems.length; i++) {
					deleteElementInSession(elems[i].getId(), elems[i]
							.getMapId(), elems[i].getType());
				}
			}
		} catch (MapsException e) {
			log.error("Error while deleting elements");
			rollback();
			throw e;
		} finally {
			endSession();
		}
	}

	/** {@inheritDoc} */
	public synchronized void deleteElement(Element e) throws MapsException {
		log.debug("deleting element...");
		if (e != null) {
			deleteElement(e.getId(), e.getMapId(), e.getType());
		}
	}

	private synchronized void deleteElementInSession(int id, int mapid,
			String type) throws MapsException {
		log.debug("deleting element...");

		final String sqlDelete = "DELETE FROM " + elementTable
				+ " WHERE elementid = ? AND mapid = ? AND elementtype = ?";

		try {
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlDelete);
			statement.setInt(1, id);
			statement.setInt(2, mapid);
			statement.setString(3, type);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			log.error("error while deleting element...");
			throw new MapsException(e);
		}
	}

	/** {@inheritDoc} */
	public synchronized void deleteElement(int id, int mapid, String type)
			throws MapsException {
		log.debug("deleting element...");
		startSession();
		final String sqlDelete = "DELETE FROM " + elementTable
				+ " WHERE elementid = ? AND mapid = ? AND elementtype = ?";

		try {
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlDelete);
			statement.setInt(1, id);
			statement.setInt(2, mapid);
			statement.setString(3, type);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			log.error("error while deleting element...", e);
			rollback();
			throw new MapsException(e);

		} finally {
			endSession();
		}
	}

	/** {@inheritDoc} */
	public synchronized void deleteElementsOfMap(int id) throws MapsException {
		log.debug("deleting elements of map...");
		startSession();
		final String sqlDelete = "DELETE FROM " + elementTable
				+ " WHERE mapid = ?";

		try {
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlDelete);
			statement.setInt(1, id);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			log.error("Error while deleting elements of map " + id);
			rollback();
			throw new MapsException(e);
		} finally {
			endSession();
		}
	}

	/** {@inheritDoc} */
	public synchronized int deleteMap(Map m) throws MapsException {
		log.debug("deleting map...");
		return deleteMap(m.getId());
	}

	/**
	 * <p>deleteMap</p>
	 *
	 * @param id a int.
	 * @return a int.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public synchronized int deleteMap(int id) throws MapsException {
		log.debug("deleting map...");
		startSession();
		final String sqlDeleteMap = "DELETE FROM " + mapTable
				+ " WHERE mapid = ?";
		final String sqlDeleteElemMap = "DELETE FROM " + elementTable
				+ " WHERE elementid = ? AND elementtype = ?";
		int countDelete = 0;
		try {
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlDeleteMap);
			statement.setInt(1, id);
			countDelete = statement.executeUpdate();
			statement.close();
			statement = m_dbConnection.prepareStatement(sqlDeleteElemMap);
			statement.setInt(1, id);
			statement.setString(2, Element.MAP_TYPE);
			statement.executeUpdate();
			statement.close();
			return countDelete;
		} catch (SQLException e) {
			log.error("error while deleting map " + id);
			rollback();
			throw new MapsException(e);
		} finally {
			endSession();
		}
	}

	/**
	 * <p>deleteNodeTypeElementsFromAllMaps</p>
	 *
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public synchronized void deleteNodeTypeElementsFromAllMaps()
			throws MapsException {
		log.debug("deleting all node elements...");
		startSession();
		final String sqlDelete = "DELETE FROM " + elementTable
				+ " WHERE elementtype = ?";

		try {
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlDelete);
			statement.setString(1, Element.NODE_TYPE);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			log.error("error while deleting all node elements");
			rollback();
			throw new MapsException(e);

		} finally {
			endSession();
		}
	}

	/**
	 * <p>deleteMapTypeElementsFromAllMaps</p>
	 *
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public synchronized void deleteMapTypeElementsFromAllMaps()
			throws MapsException {
		log.debug("deleting all map elements...");
		startSession();
		final String sqlDelete = "DELETE FROM " + elementTable
				+ " WHERE elementtype = ?";

		try {
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlDelete);
			statement.setString(1, Element.MAP_TYPE);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			log.error("error while deleting all map elements");
			rollback();
			throw new MapsException(e);
		} finally {
			endSession();
		}
	}

	/** {@inheritDoc} */
	public Element getElement(int id, int mapId, String type)
			throws MapsException {
		try {
			final String sqlQuery = "SELECT * FROM " + elementTable
					+ " WHERE elementid = ? AND mapid = ? and elementtype = ?";

			createConnection();
			PreparedStatement statement = m_dbConnection
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
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
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

		String sqlQuery = null;
		try {
			createConnection();
			if (e.getType().equals(Element.MAP_TYPE)) {
				e.setIcon(Element.defaultMapIcon);
				sqlQuery = "SELECT mapname FROM " + mapTable
						+ " WHERE mapId = ?";
			} else {
				sqlQuery = "SELECT nodelabel,displaycategory FROM assets,node WHERE assets.nodeid=node.nodeid AND node.nodeid = ?";
			}
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlQuery);
			statement.setInt(1, e.getId());
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				e.setLabel(rs.getString(1));
				if (e.getType().equals(Element.NODE_TYPE)) {
					String iconName = rs.getString(2);
					if (iconName == null || iconName.trim().equals("")) {
						iconName = Element.defaultNodeIcon;
					}
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
			releaseConnection();
		}

		return e;
	}

	/**
	 * <p>getAllElements</p>
	 *
	 * @return an array of {@link org.opennms.web.map.db.Element} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public Element[] getAllElements() throws MapsException {
		try {
			final String sqlQuery = "SELECT * FROM " + elementTable;

			createConnection();
			Statement statement = m_dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(sqlQuery);
			Vector<Element> elements = rs2ElementVector(rs);
			Element[] el = new Element[elements.size()];
			el = elements.toArray(el);
			rs.close();
			statement.close();
			// m_dbConnection.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting all elements");
			throw new MapsException(e);
		} finally {
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
	public Element[] getElementsOfMap(int mapid) throws MapsException {
		try {
			final String sqlQuery = "SELECT * FROM " + elementTable
					+ " WHERE mapid = ?";

			createConnection();
			PreparedStatement statement = m_dbConnection
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
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
	public Element[] getNodeElementsOfMap(int mapid) throws MapsException {
		try {
			final String sqlQuery = "SELECT * FROM " + elementTable
					+ " WHERE mapid = ? AND elementtype = 'N' ";
			createConnection();
			PreparedStatement statement = m_dbConnection
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
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
	public Element[] getMapElementsOfMap(int mapid) throws MapsException {
		try {
			final String sqlQuery = "SELECT * FROM " + elementTable
					+ " WHERE mapid = ? AND elementtype = 'M' ";

			createConnection();
			PreparedStatement statement = m_dbConnection
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
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
	public Element[] getElementsLike(String elementLabel) throws MapsException {
		try {
			final String sqlQuery = "SELECT * FROM " + elementTable
					+ " WHERE elementlabel LIKE ?";

			createConnection();
			PreparedStatement statement = m_dbConnection
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
			releaseConnection();
		}
	}

	/**
	 * <p>getMapsStructure</p>
	 *
	 * @return a java$util$Map object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public java.util.Map<Integer, Set<Integer>> getMapsStructure()
			throws MapsException {
		try {
			java.util.Map<Integer, Set<Integer>> maps = new HashMap<Integer, Set<Integer>>();
			String sqlQuery = "select elementid,mapid from " + elementTable
					+ " where elementtype=?";
			createConnection();
			PreparedStatement ps = m_dbConnection.prepareStatement(sqlQuery);
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
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
	public int countMaps(int mapId) throws MapsException {
		try {
			final String sqlQuery = "SELECT COUNT(*) FROM " + mapTable
					+ " WHERE mapid = ?";

			createConnection();
			PreparedStatement statement = m_dbConnection
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
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
	public Map getMap(int id) throws MapsException {
		try {
			final String sqlQuery = "SELECT * FROM " + mapTable
					+ " WHERE mapId = ?";
			createConnection();
			PreparedStatement statement = m_dbConnection
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
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
	public Map[] getMaps(String mapname, String maptype) throws MapsException {
		try {
			final String sqlQuery = "SELECT * FROM " + mapTable
					+ " WHERE mapName= ? AND maptype = ? ";
			createConnection();
			PreparedStatement statement = m_dbConnection
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
			releaseConnection();
		}
	}

	/**
	 * <p>getAllMaps</p>
	 *
	 * @return an array of {@link org.opennms.web.map.db.Map} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public Map[] getAllMaps() throws MapsException {
		try {
			final String sqlQuery = "SELECT * FROM " + mapTable;
			createConnection();
			Statement statement = m_dbConnection.createStatement();
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
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
	public Map[] getMapsLike(String mapLabel) throws MapsException {
		try {
			final String sqlQuery = "SELECT * FROM " + mapTable
					+ " WHERE mapname LIKE ?";

			createConnection();
			PreparedStatement statement = m_dbConnection
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
			// m_dbConnection.close();
			return maps;
		} catch (Exception e) {
			log.error("Exception while getting maps by label like " + mapLabel);
			throw new MapsException(e);
		} finally {
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
	public Map[] getMapsByName(String mapLabel) throws MapsException {
		try {
			final String sqlQuery = "SELECT * FROM " + mapTable
					+ " WHERE mapname = ?";

			createConnection();
			PreparedStatement statement = m_dbConnection
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
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
	public Map[] getContainerMaps(int id, String type) throws MapsException {
		try {
			final String sqlQuery = "SELECT " + mapTable + ".* FROM "
					+ mapTable + " INNER JOIN " + elementTable + " ON "
					+ mapTable + ".mapid = " + elementTable
					+ ".mapid WHERE elementid = ? AND elementtype = ?";

			createConnection();
			PreparedStatement statement = m_dbConnection
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
			releaseConnection();
		}
	}

	/**
	 * <p>getAllMapMenus</p>
	 *
	 * @return an array of {@link org.opennms.web.map.view.VMapInfo} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public VMapInfo[] getAllMapMenus() throws MapsException {
		try {
			final String sqlQuery = "SELECT mapid,mapname,mapowner FROM "
					+ mapTable + " order by mapname";

			createConnection();
			Statement statement = m_dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(sqlQuery);
			Vector<VMapInfo> maps = rs2MapMenuVector(rs);

			VMapInfo[] el = null;
			if (maps != null) {
				el = new VMapInfo[maps.size()];
				el = maps.toArray(el);
			}
			rs.close();
			statement.close();
			// m_dbConnection.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting all map-menu");
			throw new MapsException(e);
		} finally {
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
	public VMapInfo getMapMenu(int mapId) throws MapsException {
		try {
			final String sqlQuery = "SELECT mapid,mapname,mapowner FROM "
					+ mapTable + " where mapId= ?";

			createConnection();
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlQuery);
			statement.setInt(1, mapId);
			ResultSet rs = statement.executeQuery();
			VMapInfo mm = rs2MapMenu(rs);

			rs.close();
			statement.close();
			// m_dbConnection.close();

			return mm;
		} catch (Exception e) {
			log.error("Exception while getting map-menu for mapid " + mapId);
			throw new MapsException(e);
		} finally {
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
	public VMapInfo[] getMapsMenuByName(String mapLabel) throws MapsException {
		try {
			final String sqlQuery = "SELECT mapid,mapname,mapowner FROM "
					+ mapTable + " WHERE upper( mapname ) = upper( ? )";

			createConnection();
			PreparedStatement statement = m_dbConnection
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
			// m_dbConnection.close();
			return maps;
		} catch (Exception e) {
			log.error("Exception while getting all map-menu for map named "
					+ mapLabel);
			throw new MapsException(e);
		} finally {
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
	public VMapInfo[] getMapsMenuByOwner(String owner) throws MapsException {
		try {
			final String sqlQuery = "SELECT mapid,mapname,mapowner FROM "
					+ mapTable + " WHERE upper( mapowner ) = upper( ? )";

			createConnection();
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlQuery);
			statement.setString(1, owner);
			ResultSet rs = statement.executeQuery();
			Vector<VMapInfo> mapVector = rs2MapMenuVector(rs);
			VMapInfo[] maps = null;
			if (mapVector != null) {
				maps = new VMapInfo[mapVector.size()];
				maps = mapVector.toArray(maps);
			}
			rs.close();
			statement.close();
			// m_dbConnection.close();
			return maps;
		} catch (Exception e) {
			log
					.error("Exception while getting all map-menu for owner "
							+ owner);
			throw new MapsException(e);
		} finally {
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
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

	/**
	 * <p>getAllElementInfo</p>
	 *
	 * @return an array of {@link org.opennms.web.map.view.VElementInfo} objects.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public VElementInfo[] getAllElementInfo() throws MapsException {
		try {
			final String sqlQuery = " SELECT  nodeid,nodelabel FROM node WHERE nodetype!='D'";
			createConnection();
			PreparedStatement statement = m_dbConnection
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
			releaseConnection();
		}
	}

	/** {@inheritDoc} */
	public VElementInfo[] getElementInfoLike(String like) throws MapsException {
		try {
			final String sqlQuery = " SELECT  nodeid,nodelabel FROM node WHERE nodelabel like '"
					+ like + "%' AND  nodetype!='D'";
			createConnection();
			PreparedStatement statement = m_dbConnection
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
			releaseConnection();
		}
	}

	/**
	 * <p>getOutagedElements</p>
	 *
	 * @return a {@link java.util.List} object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public List<VElementInfo> getOutagedElements() throws MapsException {
		try {
			final String sqlQuery = "select distinct outages.nodeid, eventuei,eventseverity from outages left join events on events.eventid = outages.svclosteventid where ifregainedservice is null order by nodeid";

			createConnection();
			PreparedStatement statement = m_dbConnection
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
			// m_dbConnection.close();
			return elems;
		} catch (Exception e) {
			log.error("Exception while getting outaged elements");
			throw new MapsException(e);
		} finally {
			releaseConnection();
		}

	}

	/**
	 * <p>getAvails</p>
	 *
	 * @param mapElements an array of {@link org.opennms.web.map.db.Element} objects.
	 * @return a java$util$Map object.
	 * @throws org.opennms.web.map.MapsException if any.
	 */
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
	private java.util.Map<Integer, Double> getNodeAvailability(Set nodeIds)
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
			try {
				createConnection();
				StringBuffer sb = new StringBuffer(
						"select nodeid, getManagePercentAvailNodeWindow(nodeid, ?, ?)  from node where nodeid in (");
				Iterator it = nodeIds.iterator();
				while (it.hasNext()) {
					sb.append(it.next());
					if (it.hasNext()) {
						sb.append(", ");
					}
				}
				sb.append(")");
				PreparedStatement stmt = m_dbConnection.prepareStatement(sb
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
				releaseConnection();
			}
		}

		return retMap;
	}

	String getMapName(int id) throws MapsException {
		try {
			final String sqlQuery = "SELECT mapname FROM " + mapTable
					+ " WHERE mapId = ?";
			createConnection();
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlQuery);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			String label = null;
			if (rs.next()) {
				label = rs.getString(1);
			}
			rs.close();
			statement.close();
			// m_dbConnection.close();
			return label;
		} catch (Exception e) {
			log.error("Exception while getting name of map with mapid " + id);
			throw new MapsException(e);
		} finally {
			releaseConnection();
		}
	}

	/**
	 * gets a Vector containing the nodeids of all deleted nodes
	 *
	 * @return Vector of Integer containing all deleted nodes' ids
	 * @throws org.opennms.web.map.MapsException if any.
	 */
	public Vector<Integer> getDeletedNodes() throws MapsException {
		try {
			final String sqlQuery = "SELECT nodeid  FROM node where nodetype='D'";

			createConnection();
			Statement statement = m_dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(sqlQuery);
			Vector<Integer> elements = new Vector<Integer>();
			while (rs.next()) {
				int nId = rs.getInt(1);
				elements.add(new Integer(nId));
			}
			rs.close();
			statement.close();
			// m_dbConnection.close();
			return elements;
		} catch (Exception e) {
			log.error("Exception while getting deleted nodes");
			throw new MapsException(e);
		} finally {
			releaseConnection();
		}
	}

	/**
	 * {@inheritDoc}
	 *
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

	/** {@inheritDoc} */
	public Set<LinkInfo> getLinksOnElements(Set<Integer> allnodes)
			throws MapsException {
		log.debug("getLinksOnElements "+allnodes);
		Set<LinkInfo> nodes = null;
		try {
			nodes = new HashSet<LinkInfo>();
			if (allnodes == null || allnodes.size()==0)
				return nodes;
			createConnection();
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
			Statement stmt = m_dbConnection.createStatement();
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
			releaseConnection();
		}
		return nodes;

	}

	/** {@inheritDoc} */
	public VMapInfo[] getVisibleMapsMenu(String user) throws MapsException {
		VMapInfo[] retMaps = null;
		/*
		 * for the moment, returns all maps.
		 * if(userRole.equals(Authentication.ADMIN_ROLE)){
		 * retMaps=getAllMapMenus(); }else{ retMaps=getMapsMenuByOwner(user); }
		 */
		retMaps = getAllMapMenus();
		return retMaps;
	}

	/** {@inheritDoc} */
	public Set<Integer> getNodeIdsBySource(String query) throws MapsException {
		if (query == null) {
			return getAllNodes();
		}
		Statement stmt = null;
		ResultSet rs = null;
		Set<Integer> nodes = new HashSet<Integer>();
		try {
			createConnection();
			String sqlQuery = query;
			log.debug("Applying filters for source " + " '" + sqlQuery + "'");

			stmt = m_dbConnection.createStatement();
			rs = stmt.executeQuery(sqlQuery);
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
			releaseConnection();
		}
		return nodes;
	}

	private Set<Integer> getAllNodes() throws MapsException {
		Statement stmt = null;
		ResultSet rs = null;
		Set<Integer> nodes = new HashSet<Integer>();
		try {
			createConnection();
			String sqlQuery = "select distinct nodeid from ipinterface";
			stmt = m_dbConnection.createStatement();
			rs = stmt.executeQuery(sqlQuery);
			// add all matching nodes (id) with the source to the Set.
			while (rs.next()) {
				nodes.add(new Integer(rs.getInt(1)));
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new MapsException("Exception while getting all nodes " + e);
		} finally {
			releaseConnection();
		}
		return nodes;
	}

}
