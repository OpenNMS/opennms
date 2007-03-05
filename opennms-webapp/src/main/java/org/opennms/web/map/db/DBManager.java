/*
 * Created on 4-gen-2005
 *
 */
package org.opennms.web.map.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.Set;
import java.util.TreeMap;
//import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Category;
import org.opennms.core.resource.db.SimpleDbConnectionFactory;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.acegisecurity.Authentication;
import org.opennms.web.event.EventUtil;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.config.Avail;
import org.opennms.web.map.config.DataSource;
import org.opennms.web.map.config.Link;
import org.opennms.web.map.config.MapPropertiesFactory;
import org.opennms.web.map.datasources.DataSourceInterface;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;

/**
 * @author maumig
 * 
 * 
 */
public class DBManager extends Manager {

	private class LinkInfo {
		int nodeid;
		int ifindex;
		int nodeparentid;
		int parentifindex;
		int snmpiftype;
		long snmpifspeed;
		int snmpifoperstatus;
		
		LinkInfo(int nodeid, int ifindex, int nodeparentid, int parentifindex, int snmpiftype, long snmpifspeed, int snmpifoperstatus) {
			super();
			this.nodeid = nodeid;
			this.ifindex = ifindex;
			this.nodeparentid = nodeparentid;
			this.parentifindex = parentifindex;
			this.snmpiftype = snmpiftype;
			this.snmpifspeed = snmpifspeed;
			this.snmpifoperstatus = snmpifoperstatus;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof LinkInfo ) {
				LinkInfo ol = (LinkInfo) obj;
				return 
				(ol.nodeid == this.nodeid 
						&& ol.ifindex == this.ifindex 
						&& ol.nodeparentid== this.nodeparentid 
						&& ol.parentifindex == this.parentifindex
						&& ol.snmpiftype == this.snmpiftype
						&& ol.snmpifspeed == this.snmpifspeed
						&& ol.snmpifoperstatus==this.snmpifoperstatus);
				
			} 
			return false;
		}
		
		public int hashCode() {
			int speed = (int)(snmpifspeed/1000);
			return (((nodeid*(ifindex+1)*(parentifindex+1))/(nodeparentid))*(snmpiftype+1)/snmpifoperstatus)*speed;
		}

	}
    
	public class OutageInfo {
    	int nodeid;
    	int status;
    	float severity;
    	
    	protected OutageInfo(int nodeid,int status,float severity) {
    		this.nodeid = nodeid;
    		this.severity = severity;
    		this.status = status;
    	}
    	
    	
    	public float getSeverity() {
    		return severity;
    	}

    	public void setSeverity(float severity) {
    		this.severity = severity;
    	}
    	
    	public int getStatus() {
    		return status;
    	}
    	
    	public void setStatus(int status) {
    		this.status = status;
    	}
    }
	/**
	 * the map table to use.
	 */
	String mapTable = null;

	/**
	 * the element table to use.
	 */
	String elementTable = null;

	Connection m_dbConnection = null;

	MapPropertiesFactory mpf=null;
	String LOG4J_CATEGORY = "OpenNMS.Map";
    int defaultStatusId;
    int defaultSeverityId;
    int indeterminateSeverityId;
    Avail undefinedAvail;
    int unknownStatusId;
    boolean availEnabled = true;
    Avail defaultEnableFalseAvail;
    String calculateSeverityAs;

	Category log = null;

	public DBManager(DataSource dataSource) throws MapsException{
		super(dataSource);
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
	}
	
	public DBManager(DataSource dataSource, java.util.Map params)throws MapsException {
		super(dataSource,params);
		init();
	}

	void createConnection() throws SQLException, ClassNotFoundException {
		
		// create the dbconnection only if not yet created
		if (m_dbConnection != null && !m_dbConnection.isClosed())
			return;
		
		log.debug("creating connection");
		
		if (m_params == null) {
			throw new IllegalStateException(
					"parameters not found while creating db connection.");
		}
		String driver = (String) m_params.get("driver");
		if (driver == null) {
			throw new IllegalStateException("parameter 'driver' not found.");
		}
		String url = (String) m_params.get("url");
		if (url == null) {
			throw new IllegalStateException("parameter 'url' not found.");
		}
		String user = (String) m_params.get("user");
		if (user == null) {
			throw new IllegalStateException("parameter 'user' not found.");
		}
		String password = (String) m_params.get("password");
		if (password == null) {
			throw new IllegalStateException("parameter 'password' not found.");
		}
		log.debug("using parameters: driver="+driver+", url="+url+", user="+user+", password="+password);
		SimpleDbConnectionFactory m_dbConnectionFactory = new SimpleDbConnectionFactory();
		m_dbConnectionFactory.init(url, driver, user, password);
		m_dbConnection = m_dbConnectionFactory.getConnection();
	}

	public void init() throws MapsException {

		if(!initialized){
			ThreadCategory.setPrefix(LOG4J_CATEGORY);
			log = ThreadCategory.getInstance(this.getClass());
			log.debug("Init...");

			try {
				mapTable = (String) m_params.get("maptable");
				if (mapTable == null) {
					throw new IllegalStateException(
							"parameter 'maptable' not found.");
				}
				elementTable = (String) m_params.get("elementtable");
				if (elementTable == null) {
					throw new IllegalStateException(
							"parameter 'elementtable' not found.");
				}
			} catch (Exception e) {
				throw new MapsException(e);
			}
			
			try {
				createConnection();
			} catch (Exception e) {
				log.error("Exception while creating db connection");
				throw new MapsException(e);
			}
			
			try{
				MapPropertiesFactory.reload(true);
				mpf=MapPropertiesFactory.getInstance();
		    	defaultStatusId = mpf.getDefaultStatus().getId();
		    	defaultSeverityId = mpf.getDefaultSeverity().getId();
		    	indeterminateSeverityId = mpf.getIndeterminateSeverity().getId();
		    	undefinedAvail = mpf.getUndefinedAvail();
		    	unknownStatusId = mpf.getUnknownUeiStatus().getId();
		    	availEnabled = mpf.enableAvail();
		    	defaultEnableFalseAvail = mpf.getDisabledAvail();
		    	calculateSeverityAs = mpf.getSeverityMapAs();
			}catch(Exception e){
				log.error("Error while reloading MapPropertiesFactory " +e);
				throw new MapsException(e);
			}

			initialized=true;
		}
	}

	public void finalize() throws MapsException {
		log.debug("finalizing...");
		try {
			if (m_dbConnection != null && !m_dbConnection.isClosed())
				m_dbConnection.close();
			m_dbConnection = null;
		} catch (Exception e) {
			log.error("Exception while finalizing");
			throw new MapsException(e);
		}
	}
	
	public void startSession() throws MapsException {
		if(!isStartedSession()){
			if(m_params==null){
				throw new MapsException("Call first Manager(java.util.Map params) constructor");
			}
			try {
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

	synchronized public void endSession() throws MapsException {
		try {
			log.debug("ending session");
			if (!isStartedSession()){
				log.debug("session not started");
				return;
			}
			m_dbConnection.commit();
			m_dbConnection.setAutoCommit(true);
		} catch (Exception e) {
			log.error("error while ending session");
			throw new MapsException(e);
		}
	}

	public boolean isStartedSession() throws MapsException{
		try{
			return (m_dbConnection != null && !m_dbConnection.isClosed());
		}catch(SQLException s){
			throw new MapsException(s);
		}
	}

	public synchronized void saveMaps(Map[] m) throws MapsException {
		try {
			log.debug("saving maps");
			if (!isStartedSession())
				throw new IllegalStateException("Call startSession() first.");

			for (int i = 0, n = m.length; i < n; i++) {
				saveMap(m[i]);
			}
		}  catch (Exception e) {
			log.error("error while saving maps");
			throw new MapsException(e);
		}
	}

	public synchronized void saveMap(Map m) throws MapsException {
		log.debug("saving map...");
		if (!isStartedSession()){
			log.error("Call startSession() first.");
			throw new IllegalStateException("Call startSession() first.");
		}
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
			try {
				m_dbConnection.rollback();
				m_dbConnection.close();
				throw new MapsException(ex);
			} catch (SQLException e) {
				log.error("Error while rollback");
				throw new MapsException(e);
			}
		}

		if (count == 0){
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
		try {
			log.debug("saving elements");
			if (!isStartedSession())
				throw new IllegalStateException("Call startSession() first.");
			if (e != null) {
				for (int i = 0, n = e.length; i < n; i++) {
					saveElement(e[i]);
				}
			}
		} catch (Exception ex) {
			log.error("error while saving elements");
			throw new MapsException(ex);
		}
	}

	public synchronized void saveElement(Element e) throws MapsException {
		log.debug("saving element");
		if (!isStartedSession())
			throw new IllegalStateException("Call startSession() first.");

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
			try {
				m_dbConnection.rollback();
				m_dbConnection.close();
				throw new MapsException(ex);
			} catch (SQLException eex) {
				log.error("error while rollback");
				throw new MapsException(eex);
			}
		}
	}

	public synchronized void deleteElements(Element[] elems)
			throws MapsException {
		log.debug("deleting elements...");
		if (elems != null) {
			for (int i = 0; i < elems.length; i++) {
				deleteElement(elems[i]);
			}
		}
	}
	
	public synchronized void deleteElement(Element e) throws MapsException {
		log.debug("deleting element...");
		if (e != null) {
			deleteElement(e.getId(), e.getMapId(), e.getType());
		}
	}

	public synchronized void deleteElement(int id, int mapid, String type)
			throws MapsException {
		log.debug("deleting element...");
		if (!isStartedSession())
			throw new IllegalStateException("Call startSession() first.");
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
			try {
				m_dbConnection.rollback();
				m_dbConnection.close();
				throw new MapsException(e);
			} catch (SQLException ex) {
				log.error("error while rollback");
				throw new MapsException(ex);
			}
		}
	}

	public synchronized void deleteElementsOfMap(int id) throws MapsException {
		log.debug("deleting elements of map...");
		if (!isStartedSession())
			throw new IllegalStateException("Call startSession() first.");
		final String sqlDelete = "DELETE FROM " + elementTable
				+ " WHERE mapid = ?";

		try {
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlDelete);
			statement.setInt(1, id);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			try {
				m_dbConnection.rollback();
				m_dbConnection.close();
				throw new MapsException(e);
			} catch (SQLException ex) {
				throw new MapsException(ex);
			}
		}
	}

	public synchronized int deleteMap(Map m) throws MapsException {
		log.debug("deleting map...");	
		return deleteMap(m.getId());
	}

	public synchronized int deleteMap(int id) throws MapsException {
		log.debug("deleting map...");
		if (!isStartedSession())
			throw new IllegalStateException("Call startSession() first.");
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
			log.error("error while deleting map");
			try {
				m_dbConnection.rollback();
				m_dbConnection.close();
				throw new MapsException(e);
			} catch (SQLException ex) {
				log.error("error while rollback");
				throw new MapsException(ex);
			}
		}
	}

	public synchronized void deleteNodeTypeElementsFromAllMaps()
	throws MapsException {
		log.debug("deleting all node elements...");
		if (!isStartedSession())
			throw new IllegalStateException("Call startSession() first.");
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
			try {
				m_dbConnection.rollback();
				m_dbConnection.close();
				throw new MapsException(e);
			} catch (SQLException ex) {
				log.error("error while rollback");
				throw new MapsException(ex);
			}
		}
	}
	
	public synchronized void deleteMapTypeElementsFromAllMaps()
	throws MapsException {
		log.debug("deleting all map elements...");
		if (!isStartedSession())
			throw new IllegalStateException("Call startSession() first.");
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
			try {
				m_dbConnection.rollback();
				m_dbConnection.close();
				throw new MapsException(e);
			} catch (SQLException ex) {
				log.error("error while rollback");
				throw new MapsException(ex);
			}
		}
	}

	public Element getElement(int id, int mapId, String type) throws MapsException {
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
			//m_dbConnection.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting element with elementid="+id+" and mapid="+mapId);
			throw new MapsException(e);
		}
	}
	
	public Element newElement(int id, int mapId, String type) throws MapsException {
		
		String label = null;
		if (type.equals(Element.NODE_TYPE)) label = getNodeLabel(id);
		if (type.equals(Element.MAP_TYPE)) label = getMapName(id);
		String iconname = getIconName(id,type);
		log.debug("Creating new VElement mapId:"+mapId+" id:"+id+" type:"+type+" label:"+label+" iconname:"+iconname+" x:"+0+" y:"+0);
		return new Element(mapId,id,type,label,iconname,0,0);
	}

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
			//m_dbConnection.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting all elements");
			throw new MapsException(e);
		}
	}

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
			log.error("Exception while getting elements of map with mapid="+mapid);
			throw new MapsException(e);
		}
	}

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
			log.error("Exception while getting element node of map with mapid "+mapid);
			throw new MapsException(e);
		}
	}

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
			log.error("Exception while getting map element of map with mapid "+mapid);
			throw new MapsException(e);
		}
	}

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
			log.error("Exception while getting elements by label like "+elementLabel);
			throw new MapsException(e);
		}
	}

	public java.util.Map getMapsStructure() throws MapsException {
		try {
			java.util.Map<Integer,Set<Integer>> maps = new HashMap<Integer,Set<Integer>>();
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
		}
	}

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
		}
	}

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
			log.error("Exception while getting map with mapid="+id);
			throw new MapsException(e);
		}
	}

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
			log.error("Exception while getting maps with name="+mapname+" and type="+maptype);
			throw new MapsException(e);
		}
	}

	public Map[] getAllMaps() throws MapsException {
		try {
			final String sqlQuery = "SELECT * FROM " + mapTable;
			createConnection();
			Statement statement = m_dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(sqlQuery);
			Vector<Map> maps = rs2MapVector(rs);
			// System.out.println(maps);

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
		}
	}

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
			//m_dbConnection.close();
			return maps;
		} catch (Exception e) {
			log.error("Exception while getting maps by label like "+mapLabel);
			throw new MapsException(e);
		}
	}

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
			log.error("Exception while getting elements with label "+mapLabel);
			throw new MapsException(e);
		}
	}

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
			log.error("Exception while getting container maps of element with id/type "+id+"/"+type);
			throw new MapsException(e);
		}
	}

	public MapMenu[] getAllMapMenus() throws MapsException {
		try {
			final String sqlQuery = "SELECT mapid,mapname,mapowner FROM "
					+ mapTable +" order by mapname";

			createConnection();
			Statement statement = m_dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(sqlQuery);
			Vector<MapMenu> maps = rs2MapMenuVector(rs);
			// System.out.println(maps);

			MapMenu[] el = null;
			if (maps != null) {
				el = new MapMenu[maps.size()];
				el = maps.toArray(el);
			}
			rs.close();
			statement.close();
			//m_dbConnection.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting all map-menu");
			throw new MapsException(e);
		}
	}

	public MapMenu getMapMenu(int mapId) throws MapsException {
		try {
			final String sqlQuery = "SELECT mapid,mapname,mapowner FROM "
					+ mapTable + " where mapId= ?";

			createConnection();
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlQuery);
			statement.setInt(1, mapId);
			ResultSet rs = statement.executeQuery();
			MapMenu mm = rs2MapMenu(rs);
			// System.out.println(maps);

			rs.close();
			statement.close();
			//m_dbConnection.close();

			return mm;
		} catch (Exception e) {
			log.error("Exception while getting map-menu for mapid "+mapId);
			throw new MapsException(e);
		}
	}

	public MapMenu[] getMapsMenuByName(String mapLabel) throws MapsException {
		try {
			final String sqlQuery = "SELECT mapid,mapname,mapowner FROM "
					+ mapTable + " WHERE upper( mapname ) = upper( ? )";

			createConnection();
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlQuery);
			statement.setString(1, mapLabel);
			ResultSet rs = statement.executeQuery();
			Vector<MapMenu> mapVector = rs2MapMenuVector(rs);
			MapMenu[] maps = null;
			if (mapVector != null) {
				maps = new MapMenu[mapVector.size()];
				maps = mapVector.toArray(maps);
			}
			rs.close();
			statement.close();
			//m_dbConnection.close();
			return maps;
		} catch (Exception e) {
			log.error("Exception while getting all map-menu for map named "+mapLabel);
			throw new MapsException(e);
		}
	}
	
	public MapMenu[] getMapsMenuByOwner(String owner) throws MapsException {
		try {
			final String sqlQuery = "SELECT mapid,mapname,mapowner FROM "
					+ mapTable + " WHERE upper( mapowner ) = upper( ? )";

			createConnection();
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlQuery);
			statement.setString(1, owner);
			ResultSet rs = statement.executeQuery();
			Vector<MapMenu> mapVector = rs2MapMenuVector(rs);
			MapMenu[] maps = null;
			if (mapVector != null) {
				maps = new MapMenu[mapVector.size()];
				maps = mapVector.toArray(maps);
			}
			rs.close();
			statement.close();
			//m_dbConnection.close();
			return maps;
		} catch (Exception e) {
			log.error("Exception while getting all map-menu for owner "+owner);
			throw new MapsException(e);
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

	public ElementInfo[] getAllElementInfo() throws MapsException {
		try {
			final String sqlQuery = " SELECT  nodeid,nodelabel FROM node WHERE nodetype!='D'";
			createConnection();
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlQuery);
			ResultSet rs = statement.executeQuery();
			Vector<ElementInfo> elements = new Vector<ElementInfo>();
			while (rs.next()) {
				ElementInfo ei = new ElementInfo(rs.getInt("nodeid"), "", 0,
						(rs.getString("nodelabel") != null) ? rs
								.getString("nodelabel") : "("
								+ rs.getInt("nodeid") + ")");
				elements.add(ei);
			}
			ElementInfo[] el = null;
			if (elements != null) {
				el = new ElementInfo[elements.size()];
				el = elements.toArray(el);
			}
			rs.close();
			statement.close();
			return el;
		} catch (Exception e) {
			log.error("Exception while getting all ipaddrs");
			throw new MapsException(e);
		}
	}

    public VLink[] getLinks(VElement[] elems) throws MapsException {
    	String multilinkStatus = mpf.getMultilinkStatus();
    	List<VLink> links = new ArrayList<VLink>();
        
    	// this is the list of nodes set related to Element
    	//java.util.List<Set<Integer>> elemNodes = new java.util.ArrayList<Set<Integer>>();
    	java.util.Map<Integer,Set<Integer>> node2Element = new HashMap<Integer,Set<Integer>>();
    	
    	HashSet<Integer> allNodes = new HashSet<Integer>();
		try {
	    	if (elems != null) {
	        	for (int i = 0; i < elems.length; i++) {
	        			        		
	                Set<Integer> nodeids = getNodeidsOnElement(elems[i]);
	                allNodes.addAll(nodeids);
	                //elemNodes.add(nodeids);
	                Iterator<Integer> ite = nodeids.iterator();
	    	    	while(ite.hasNext()) {
	    	    		Integer nodeid = ite.next();
	    	    		Set<Integer> elements = node2Element.get(nodeid);
	    	    		if (elements == null) elements = new java.util.HashSet<Integer>();
	    	    		elements.add(new Integer(i));
	    	    		node2Element.put(nodeid,elements);
	    	    	}
	    	    		                	         	}
	        }else{
	        	return null;
	        }
    		
	    	log.debug("----------Node2Element----------");
	    	Iterator<Integer> it = node2Element.keySet().iterator();
	    	while(it.hasNext()){
	    		Integer nodeid=it.next();
	    		log.debug("Node "+nodeid+" contained in Elements "+node2Element.get(nodeid).toString());
	    	}
	    	log.debug("----------End of Node2Element----------");

	    	log.debug("----------Link on Elements ----------");
	    	Set<LinkInfo> linkinfo = getLinksOnElements(allNodes);
	    	Iterator<LinkInfo> ite = linkinfo.iterator();
	    	while(ite.hasNext()) {
	    		LinkInfo linfo = ite.next();
	    		log.debug(""+linfo.nodeid+"-"+linfo.nodeparentid);
	    	}
	    	log.debug("----------End of Link on Elements ----------");
	    	ite = linkinfo.iterator();
	    	while(ite.hasNext()) {
	    		LinkInfo linfo = ite.next();
	    		log.debug("Getting linkinfo for nodeid "+linfo.nodeid);
	    		Set<Integer> fE = node2Element.get(linfo.nodeid);
	    		log.debug("Got "+fE);
	    		if(fE!=null){
		    		Iterator<Integer> firstElements = fE.iterator();
		    		while (firstElements.hasNext()) {
			    		log.debug("Getting linkinfo for nodeid "+linfo.nodeparentid);
		    			Set<Integer> sE=node2Element.get(linfo.nodeparentid);
		    			log.debug("Got "+sE);
		    			Integer firstNext = firstElements.next();
		    			if(sE!=null){
				    		Iterator<Integer> secondElements = sE.iterator();
				    		VElement first = elems[firstNext.intValue()]; 
				    		while (secondElements.hasNext()) {
				    			VElement second = elems[secondElements.next().intValue()];
				    			if (first.hasSameIdentifier(second)) continue;
				    			VLink vlink = new VLink(first,second);
				    			vlink.setLinkOperStatus(linfo.snmpifoperstatus);
				    			vlink.setLinkTypeId(getLinkTypeId(linfo));
				    			int index = links.indexOf(vlink);
				    			if(index!=-1){
				    				VLink alreadyIn = links.get(index);
				    				if(alreadyIn.equals(vlink)){
				    					if(multilinkStatus.equals(mpf.MULTILINK_BEST_STATUS)){
				    						if(vlink.getLinkOperStatus()<alreadyIn.getLinkOperStatus()){
				    							log.debug("removing to the array link "+alreadyIn.toString()+ " with status "+alreadyIn.getLinkOperStatus());
				    							links.remove(index);
				    							links.add(vlink);
				    							log.debug("adding to the array link "+vlink.toString()+ " with status "+vlink.getLinkOperStatus());
				    						}
				    					}else if(vlink.getLinkOperStatus()>alreadyIn.getLinkOperStatus()){
				    						log.debug("removing to the array link "+alreadyIn.toString()+ " with status "+alreadyIn.getLinkOperStatus());
			    							links.remove(index);
			    							links.add(vlink);
			    							log.debug("adding to the array link "+vlink.toString()+ " with status "+vlink.getLinkOperStatus());
			    						}
				    				}
				    			}else{
					    			log.debug("adding link ("+vlink.hashCode()+") "+vlink.getFirst().getId()+"-"+vlink.getSecond().getId());
					    			links.add(vlink);
				    			}
				    		}
		    			}
		    			
		    		}
	    		}else{ 
	    			log.debug("Getting linkinfo for nodeid "+linfo.nodeparentid);
		    		Set<Integer> ffE = node2Element.get(linfo.nodeparentid);
		    		log.debug("Got "+ffE);
	    			if(ffE!=null){
			    		Iterator<Integer> firstElements = ffE.iterator();
			    		while (firstElements.hasNext()) {
				    		log.debug("Getting linkinfo for nodeid "+linfo.nodeparentid);
			    			Set<Integer> sE=node2Element.get(linfo.nodeparentid);
			    			log.debug("Got "+sE);
			    			Integer firstNext = firstElements.next();
			    			if(sE!=null){
					    		Iterator<Integer> secondElements = sE.iterator();
					    		VElement first = elems[firstNext.intValue()]; 
					    		while (secondElements.hasNext()) {
					    			VElement second = elems[secondElements.next().intValue()];
					    			if (first.hasSameIdentifier(second)) continue;
					    			VLink vlink = new VLink(first,second);
					    			vlink.setLinkOperStatus(linfo.snmpifoperstatus);
					    			vlink.setLinkTypeId(getLinkTypeId(linfo));
					    			int index = links.indexOf(vlink);
					    			if(index!=-1){
					    				VLink alreadyIn = links.get(index);
					    				if(alreadyIn.equals(vlink)){
					    					if(multilinkStatus.equals(mpf.MULTILINK_BEST_STATUS)){
					    						if(vlink.getLinkOperStatus()<alreadyIn.getLinkOperStatus()){
					    							log.debug("removing to the array link "+alreadyIn.toString()+ " with status "+alreadyIn.getLinkOperStatus());
					    							links.remove(index);
					    							links.add(vlink);
					    							log.debug("adding to the array link "+vlink.toString()+ " with status "+vlink.getLinkOperStatus());
					    						}
					    					}else if(vlink.getLinkOperStatus()>alreadyIn.getLinkOperStatus()){
					    						log.debug("removing to the array link "+alreadyIn.toString()+ " with status "+alreadyIn.getLinkOperStatus());
				    							links.remove(index);
				    							links.add(vlink);
				    							log.debug("adding to the array link "+vlink.toString()+ " with status "+vlink.getLinkOperStatus());
				    						}
					    				}
					    			}else{
						    			log.debug("adding link ("+vlink.hashCode()+") "+vlink.getFirst().getId()+"-"+vlink.getSecond().getId());
						    			links.add(vlink);
					    			}
					    		}
			    			}
			    			
			    		}
	    			}
	    		}
	    		
	    	}
	    	log.debug("Exit...");
	    	/* old method to restore if new is slower
	        Iterator ite = elemNodes.iterator();
	        int firstelemcount = 0;
	        while (ite.hasNext()) {
	        	Set firstelemnodes = (TreeSet) ite.next();
	        	Set<LinkInfo> firstlinkednodes = getLinkedNodeidInfosOnNodes(firstelemnodes);
	            int secondelemcount = firstelemcount +1;
	            Iterator sub_ite = elemNodes.subList(secondelemcount,elemNodes.size()).iterator(); 
	        	while (sub_ite.hasNext()) {
	        		Iterator node_ite = ((TreeSet) sub_ite.next()).iterator();
	        		while (node_ite.hasNext()) {
	        			Integer curNodeId = (Integer) node_ite.next();
	        			if (firstlinkednodes.contains(curNodeId)) {
	        				VLink vlink = new VLink(elems[firstelemcount],elems[secondelemcount]);
	        				vlink.setLinkOperStatus(getLinkOperStatus(vlink));
	        				vlink.setLinkTypeId(getLinkTypeId(vlink));
	        				if(!links.contains(vlink)){
	        					log.debug("adding link "+vlink.getFirst().getId()+vlink.getFirst().getType()+"-"+vlink.getSecond().getId()+vlink.getSecond().getType());
	        					links.add(vlink);
	        				}
	        			}
	        		}
	        		secondelemcount++;
				}
				firstelemcount++;
			}
			*/
	    	
    	}catch(Exception e){
    		log.error(e);
    		StackTraceElement[] ste = e.getStackTrace();
    		for(int k=0;k<ste.length;k++){
    			log.error(ste[k].getLineNumber()+":"+ste[k].toString());
    		}
    		throw new MapsException(e);
    	}
        return links.toArray(new VLink[0]);
    }
    
    
 	public VLink[] getLinksOnElement(VElement[] elems,VElement elem) throws MapsException {
 		if(elems==null || elem==null) return null;
 		ArrayList<VElement> listOfElems = new ArrayList<VElement>();
 		for(int i=0;i<elems.length;i++){
 			if(elems[i]!=null)
 				listOfElems.add(elems[i]);
 		}
 		listOfElems.add(elem);
 		return getLinks((VElement[])listOfElems.toArray(new VElement[0]));
 		/*
 		
    	HashSet<VLink> links = new HashSet<VLink>();
        
    	// this is the list of nodes set related to Element
    	Set<LinkInfo> linkinfo = null;
		if (elem != null) {
            linkinfo = getLinkedNodeidInfosOnNodes(getNodeidsOnElement(elem));
        } else {
        	return null;
        }
        
        if (elems != null && linkinfo != null) {
    		
        	for (int i = 0; i < elems.length; i++) {
	    		Iterator node_ite = getNodeidsOnElement(elems[i]).iterator();
	    		while (node_ite.hasNext()) {
	    			Integer elemNodeId = (Integer) node_ite.next();
	    	    	Iterator<LinkInfo> ite = linkinfo.iterator();
	    	    	while(ite.hasNext()) {
	    	    		LinkInfo linfo = ite.next();
	    	    		if (linfo.nodeid != elemNodeId && linfo.nodeparentid != elemNodeId) continue;
	    				VLink vlink = new VLink(elems[i],elem);
		    			vlink.setLinkOperStatus(linfo.snmpifoperstatus);
		    			vlink.setLinkTypeId(getLinkTypeId(linfo));
		    			log.debug("adding "+vlink.hashCode());
		    			log.debug(links.toString()+" "+links.add(vlink));
		       		}
	    		}
	    	}
        }
        return links.toArray(new VLink[0]);*/
    }    

    public VElement refreshElement(VElement mapElement) throws MapsException {
    	Vector<Integer> deletedNodeids= getDeletedNodes();
    	java.util.Map<Integer,OutageInfo> outagedNodes=getOutagedNodes();
    	VElement[] velems = {mapElement};
    	java.util.Map<Integer,Double> avails=getAvails(velems);
    	Set nodesBySource = new HashSet();
    	log.debug("m_datasource is "+m_dataSource);
    	if (m_dataSource != null) nodesBySource = mpf.getNodeIdsBySource(m_dataSource.getLabel());
    	VElement ve = refresh(mapElement,nodesBySource,deletedNodeids,outagedNodes,avails);
    	if (ve.equalsIgnorePosition(mapElement)) return null;
    	return ve;
    }
    
	public VElement[] refreshElements(VElement[] mapElements) throws MapsException {
		List<VElement> elems = new ArrayList<VElement>();
    	Vector<Integer> deletedNodeids= getDeletedNodes();
    	java.util.Map<Integer,OutageInfo> outagedNodes=getOutagedNodes();
    	java.util.Map<Integer,Double> avails=getAvails(mapElements);
    	Set nodesBySource = new HashSet();
    	log.debug("m_datasource is "+m_dataSource);
    	if (m_dataSource != null) nodesBySource = mpf.getNodeIdsBySource(m_dataSource.getLabel());
		VElement ve = null;
		if (mapElements != null) 
    	for(int i=0;i<mapElements.length;i++){
    		ve = refresh(mapElements[i],nodesBySource,deletedNodeids,outagedNodes,avails);
    		if (!(ve.equalsIgnorePosition(mapElements[i]))) {
				elems.add(ve);
			}
    	}
    	return elems.toArray(new VElement[0]);

	}
	
	public VMap reloadMap(VMap map) throws MapsException {
		VElement[] velems = refreshElements((map.getAllElements()));
		map.addElements(velems);
		map.removeAllLinks();
		return map;
	}
	
	private List<ElementInfo> getOutagedVElems() throws MapsException {
		try {
			final String sqlQuery = "select distinct  outages.nodeid, eventuei,eventseverity from outages left join events on events.eventid = outages.svclosteventid where ifregainedservice is null order by nodeid";

			createConnection();
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlQuery);
			ResultSet rs = statement.executeQuery();
			List<ElementInfo> elems = new ArrayList<ElementInfo>();
			while (rs.next()) {
				ElementInfo einfo = new ElementInfo(rs.getInt(1), rs
						.getString(2), rs.getInt(3));
				elems.add(einfo);
			}
			rs.close();
			statement.close();
			//m_dbConnection.close();
			return elems;
		} catch (Exception e) {
			log.error("Exception while getting outaged elements");
			throw new MapsException(e);
		}

	}

	java.util.Map<Integer,Double> getAvails(Element[] mapElements)throws MapsException{
        // get avails for all nodes in map and its submaps
		java.util.Map<Integer,Double> availsMap = null;
		log.debug("avail Enabled");
		log.debug("getting all nodeids of map (and submaps)"); 
		Set<Integer> nodeIds = new HashSet<Integer>();
		if (mapElements != null) {
			for(int i=0;i<mapElements.length;i++){
				if(mapElements[i].isNode()){
					nodeIds.add(new Integer(mapElements[i].getId()));
				}else{
					nodeIds.addAll(getNodeidsOnElement(mapElements[i]));
				}
			}			
		}
		log.debug("all nodeids obtained");
		log.debug("Getting avails for nodes of map ("+nodeIds.size()+" nodes)");
		
		availsMap = getNodeAvailability(nodeIds);
		log.debug("Avails obtained");
		return availsMap;
    }
	
    /**
     * Return the availability percentage for all managed services on the given
     * nodes from the given start time until the given end time. If there are no
     * managed services on these nodes, then a value of -1 is returned.
     */
    private java.util.Map<Integer,Double> getNodeAvailability(Set nodeIds) throws MapsException {
    	
    	
        Calendar cal = new GregorianCalendar();
        Date end = cal.getTime();
        cal.add(Calendar.DATE, -1);
        Date start = cal.getTime();

    	if(nodeIds==null){
    		throw new IllegalArgumentException("Cannot take nodeIds null.");
    	}
        if (start == null || end == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (end.before(start)) {
            throw new IllegalArgumentException("Cannot have an end time before the start time.");
        }

        if (end.equals(start)) {
            throw new IllegalArgumentException("Cannot have an end time equal to the start time.");
        }

        double avail = -1;
        int nodeid = 0;
        java.util.Map<Integer,Double> retMap = new TreeMap<Integer,Double>();
        if(nodeIds.size()>0){
	        try {
		        createConnection();
	        	StringBuffer sb = new StringBuffer("select nodeid, getManagePercentAvailNodeWindow(nodeid, ?, ?)  from node where nodeid in (");
	        	Iterator it = nodeIds.iterator();
	        	while(it.hasNext()){
	        		sb.append(it.next());
	        		if(it.hasNext()){
	        			sb.append(", ");
	        		}
	        	}
	        	sb.append(")");
	            PreparedStatement stmt = m_dbConnection.prepareStatement(sb.toString());
	            
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
			}
        }

        return retMap;
    }
    
    private double getNodeAvailability(Set nodes,java.util.Map availsMap){
    	Iterator ite = nodes.iterator();
    	double avail = 0.0;
    	while (ite.hasNext()) {
			avail+=((Double)availsMap.get((Integer)ite.next())).doubleValue();
		}
		avail = avail/nodes.size();
		return avail;
    }

	String getIconName(int elementId, String type) throws MapsException {
		log.debug("getting icon name...");
		try {
			if (type.equals(VElement.MAP_TYPE))
				return "map";
			final String sqlQuery = "SELECT displaycategory FROM assets WHERE nodeid = ?";
			log.debug(sqlQuery+ " nodeid="+elementId);
			createConnection();

			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlQuery);
			statement.setInt(1, elementId);
			ResultSet rs = statement.executeQuery();
			String iconName = "unspecified";
			if (rs.next()) {
				iconName = rs.getString(1);
			}
			rs.close();
			statement.close();

			//m_dbConnection.close();

			if (iconName == null || iconName.trim().equals("")) {
				return "unspecified";
			}

			return iconName;
		} catch (Exception e) {
			log.error("Exception while getting icon name");
			throw new MapsException(e);
		}
	}
	
    private VElement refresh(VElement mapElement, Set nodesBySource, Vector deletedNodeids, java.util.Map outagedNodes,java.util.Map avails) throws MapsException {
		VElement ve = (VElement) mapElement.clone();
		if (log.isDebugEnabled())
			log.debug("refreshElements: parsing VElement ID " + ve.getId()
					+ ve.getType() + ", label:"+ve.getLabel()+" with node by sources: " +nodesBySource.toString() + " deletedNodeids: " + deletedNodeids.toString()
					+ " outagedNode: " +outagedNodes.keySet().toString());

		double elementAvail = defaultEnableFalseAvail.getMin();
		int elementStatus = defaultStatusId;
		float elementSeverity = defaultSeverityId;

		
		// get status, severity and availability: for each node, look for alternative data
		// sources; if no source is found or if the data is not retrieved, use opennms. 
		if (ve.isNode()) {
			ve.setLabel(getNodeLabel(ve.getId()));
			//FIRST: get data from OpenNMS
			if(deletedNodeids.contains(new Integer(ve.getId()))){
				elementAvail = undefinedAvail.getMin();
				elementStatus=unknownStatusId;
				elementSeverity = indeterminateSeverityId;
			} else{ //if the node isn't deleted
				
				if (nodesBySource.contains(new Integer(ve.getId()))) {
					org.opennms.web.map.datasources.DataSourceInterface dataSourceToUse = getDataSourceImplementation();
					Object id = new Integer(ve.getId());
					log.debug("getting status from alternative source " + dataSourceToUse.getClass().getName());
					int status = dataSourceToUse.getStatus(id);
					if (status >= 0) {
						elementStatus = status;
						log.debug("got status from alternative source. Value is "+elementStatus);
					}
					
					int sev = dataSourceToUse.getSeverity(id);
					if (sev >= 0) {
						elementSeverity = sev;
						log.debug("got severity from alternative source. Value is "+sev);
					} 
					if (availEnabled) {
						double avail = dataSourceToUse.getAvailability(id);
						if (avail >= 0) {
							elementAvail = avail;
							log.debug("got availability from alternative source. Value is "+avail);
						} 
					}
				} else {
					OutageInfo oi = (OutageInfo) outagedNodes.get(new Integer(ve.getId()));
					if (oi != null) {
						elementStatus = oi.getStatus();
						elementSeverity= oi.getSeverity();
					}
	  				if (availEnabled) {
	   					elementAvail =((Double) avails.get(new Integer(ve.getId()))).doubleValue();
	   				}				
					
				}
			}				
		} else { // the element is a Map
			log.debug("Calculating severity for submap Element " + ve.getId()
					+ " using '" + calculateSeverityAs + "' mode.");
			Set nodesonve = getNodeidsOnElement(ve);
			if (nodesonve != null && nodesonve.size() > 0) {
				log.debug("found nodes on Map element :" + nodesonve.toString());
				elementAvail = defaultEnableFalseAvail.getMin();
				float sev = 0;
				if (calculateSeverityAs.equalsIgnoreCase("worst")
						|| calculateSeverityAs.equalsIgnoreCase("best")) {
					sev = defaultSeverityId;
				}
				Iterator ite = nodesonve.iterator();
				while (ite.hasNext()) {
					Integer nextNodeId = (Integer) ite.next();
					if(deletedNodeids.contains(nextNodeId)){
						elementAvail = undefinedAvail.getMin();
						elementStatus=unknownStatusId;
						elementSeverity = indeterminateSeverityId;
					}else{ //if the node isn't deleted
						if (nodesBySource.contains(nextNodeId)) {
							org.opennms.web.map.datasources.DataSourceInterface dataSourceToUse = getDataSourceImplementation();
							int st = dataSourceToUse.getStatus(nextNodeId);
							if (st >= 0) {
								if (st < elementStatus) {
									elementStatus = st;
								}
								log.debug("got status from alternative source. Value is "+st);
							}

							int tempSeverity = dataSourceToUse.getSeverity(nextNodeId);
							if (tempSeverity >= 0) {
								log.debug("got severity from alternative source. Value is "+tempSeverity);
								if (calculateSeverityAs.equalsIgnoreCase("avg")) {
									sev += tempSeverity;
								} else if (calculateSeverityAs
										.equalsIgnoreCase("worst")) {
									if (sev > tempSeverity) {
										sev = tempSeverity;
									}
								} else if (calculateSeverityAs
										.equalsIgnoreCase("best")) {
									if (sev < tempSeverity) {
										sev = tempSeverity;
									}
								}
							} 	
							if (availEnabled) {
								double avail = dataSourceToUse.getAvailability(nextNodeId);
								if (avail >= 0) {
									elementAvail = avail;
									log.debug("got availability from alternative source. Value is "+avail);
								} 
							}
							
						} else {
							OutageInfo oi = (OutageInfo) outagedNodes.get(nextNodeId);
							if (oi != null) {
								elementStatus = oi.getStatus();
								float tempSeverity= oi.getSeverity();
								if (tempSeverity >= 0) {
									if (calculateSeverityAs.equalsIgnoreCase("avg")) {
										sev += tempSeverity;
									} else if (calculateSeverityAs
											.equalsIgnoreCase("worst")) {
										if (sev > tempSeverity) {
											sev = tempSeverity;
										}
									} else if (calculateSeverityAs
											.equalsIgnoreCase("best")) {
										if (sev < tempSeverity) {
											sev = tempSeverity;
										}
									}
								} 	
							}
			  				if (availEnabled) {
			   					elementAvail =((Double) avails.get(nextNodeId)).doubleValue();
			   				}	
							
						}
					}
				}
				if (calculateSeverityAs.equalsIgnoreCase("avg")) {
					elementSeverity = sev / nodesonve.size();
				} else {
					elementSeverity = sev;
				}
			} else {
				log.debug("no nodes on Map element found");
			}
		}
		

		if (log.isDebugEnabled())
			log.debug("refreshElement: element avail/status/severity "
					+ elementAvail + "/" + elementStatus + "/"
					+ elementSeverity);

		ve.setRtc(elementAvail);
		ve.setStatus(elementStatus);
		ve.setSeverity(new BigDecimal(elementSeverity + 1 / 2).intValue());
		return ve;
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
			//m_dbConnection.close();
			return label;
		} catch (Exception e) {
			log.error("Exception while getting name of map with mapid "+id);
			throw new MapsException(e);
		}
	}

	private String getMapElemName(int mapId, int elemId) throws MapsException {
		try {
			final String sqlQuery = "SELECT elementlabel FROM " + elementTable
					+ " WHERE mapId = ? and elementid= ?";

			createConnection();
			PreparedStatement statement = m_dbConnection
					.prepareStatement(sqlQuery);
			statement.setInt(1, mapId);
			statement.setInt(2, elemId);
			ResultSet rs = statement.executeQuery();
			String label = null;
			if (rs.next()) {
				label = rs.getString(1);
			}
			rs.close();
			statement.close();
			//m_dbConnection.close();
			return label;
		} catch (Exception e) {
			log.error("Exception while getting map elem name");
			throw new MapsException(e);
		}
	}

	private String getNodeLabel(int id) throws MapsException {
		try {
			final String sqlQuery = "SELECT NODELABEL FROM NODE WHERE NODEID = ?";

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
			return label;
		} catch (Exception e) {
			log.error("Exception while getting node label");
			throw new MapsException(e);
		}
	}

	private String getSeverityLabel(int severity) throws MapsException {
		
		return EventUtil.getSeverityLabel(severity);
	}	
	
	/**
	 * gets a Vector containing the nodeids of all child nodes
	 * 
	 * @return Vector of Integer containing all child nodes' ids
	 */
	private Vector<Integer> getNodesFromParentNode(int nodeparentid) throws MapsException {
		try {
			final String sqlQuery = "SELECT nodeid  FROM node where nodetype='D' AND nodeparentid = ? ";

			createConnection();
			PreparedStatement statement = m_dbConnection.prepareStatement(sqlQuery);
			statement.setInt(1, nodeparentid);
			ResultSet rs = statement.executeQuery();
			Vector<Integer> elements = new Vector<Integer>();
			while (rs.next()) {
				int nId = rs.getInt(1);
				elements.add(new Integer(nId));
			}
			rs.close();
			statement.close();
			//m_dbConnection.close();
			return elements;
		} catch (Exception e) {
			log.error("Exception while getting deleted nodes");
			throw new MapsException(e);
		}
	}

	/**
	 * gets a Vector containing the nodeids of all deleted nodes
	 * 
	 * @return Vector of Integer containing all deleted nodes' ids
	 */
	Vector<Integer> getDeletedNodes() throws MapsException {
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
			//m_dbConnection.close();
			return elements;
		} catch (Exception e) {
			log.error("Exception while getting deleted nodes");
			throw new MapsException(e);
		}
	}
	
    java.util.Map<Integer,OutageInfo> getOutagedNodes()throws MapsException{
        java.util.Map<Integer,OutageInfo> outagedNodes = new HashMap<Integer,OutageInfo>();
        log.debug("Getting outaged elems.");
        Iterator ite = getOutagedVElems().iterator();
        log.debug("Outaged elems obtained.");
		while (ite.hasNext()) {
			ElementInfo outagelem = (ElementInfo) ite.next();
			int outageStatus = mpf.getStatus(outagelem.getUei());
			int outageSeverity = mpf.getSeverity(getSeverityLabel(outagelem.getSeverity()));


			if (log.isInfoEnabled())
				log.info("parsing outaged node with nodeid: " + outagelem.getId() + " severity: " + outagelem.getSeverity() + " severity label: " +getSeverityLabel(outagelem.getSeverity()));

			if (log.isInfoEnabled())
				log.info("parsing outaged node with nodeid: " + outagelem.getId() + " status: " + outagelem.getUei() + " severity label: " +getSeverityLabel(outagelem.getSeverity()));

			if (log.isDebugEnabled()) 
    			log.debug("local outaged node status/severity " + outageStatus + "/" + outageSeverity);

			OutageInfo oi = (OutageInfo)outagedNodes.get(new Integer(outagelem.getId())); 

			if (oi != null) {
				if (oi.getStatus() > outageStatus) {
					oi.setStatus(outageStatus);
				}
				oi.setSeverity((oi.getSeverity()+outageSeverity)/2);
			} else {
				int curStatus = outageStatus;
				float curSeverity = outageSeverity;
				oi = new OutageInfo(outagelem.getId(),curStatus,curSeverity);
			}
			outagedNodes.put(new Integer(outagelem.getId()),oi);
    		if (log.isDebugEnabled()) 
    			log.debug("global outaged node status/severity " + outageStatus + "/" + outageSeverity);
		}
        return outagedNodes;
    }    
    
    /**
     * recursively gets all nodes contained by elem and its submaps (if elem is a map)
     */
    Set<Integer> getNodeidsOnElement(Element elem) throws MapsException {
   		Set<Integer> elementNodeIds = new HashSet<Integer>();
		if (elem.isNode()) {
			elementNodeIds.add(new Integer(elem.getId()));
			// This is not OK now
			//elementNodeIds.addAll(getNodesFromParentNode(elem.getId()));
		} else if (elem.isMap()) {
			int curMapId = elem.getId();
			Element[] elemNodeElems = getNodeElementsOfMap(curMapId);
			if (elemNodeElems != null && elemNodeElems.length >0 ) {
				for (int i=0; i<elemNodeElems.length;i++ ) {
					elementNodeIds.add(new Integer(elemNodeElems[i].getId()));
				}
			}
			
			Element[] elemMapElems = getMapElementsOfMap(curMapId);
			if (elemMapElems != null && elemMapElems.length >0 ) {
				for (int i=0; i<elemMapElems.length;i++ ) {
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

	private Vector<MapMenu> rs2MapMenuVector(ResultSet rs) throws SQLException {
		Vector<MapMenu> mapVec = null;
		boolean firstTime = true;
		while (rs.next()) {
			if (firstTime) {
				mapVec = new Vector<MapMenu>();
				firstTime = false;
			}

			MapMenu currMap = new MapMenu(rs.getInt("mapId"), rs
					.getString("mapName"), rs.getString("mapOwner"));
			mapVec.add(currMap);
		}
		return mapVec;
	}

	private MapMenu rs2MapMenu(ResultSet rs) throws SQLException {
		MapMenu map = null;
		if (rs.next()) {
			map = new MapMenu(rs.getInt("mapId"), rs.getString("mapName"), rs
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

    DataSourceInterface getDataSourceImplementation() throws MapsException {
		if(m_dataSource==null) return null;
    	try {
			java.util.Map params = m_dataSource.getParam();
	
			log.debug("dataSource " + m_dataSource.getImplClass()
					+ " obtained.");
			Class implClass = Class.forName(m_dataSource.getImplClass());
			 DataSourceInterface dsi = (org.opennms.web.map.datasources.DataSourceInterface)implClass.newInstance();
			 
			 dsi.init(params);
			 return dsi;
		} catch (Exception e) {
			log.error("Error while getting instance of data source " + e);
			throw new MapsException(e);
		}
    }
    
    private Set<LinkInfo> getLinkedNodeidInfosOnNodes(Set nodes) throws MapsException {
   		Set<LinkInfo> linkedNodeIds = new HashSet<LinkInfo>();
        if (nodes != null) {
        	Iterator ite = nodes.iterator();
        	while (ite.hasNext()) {
        		Integer curnodeid = (Integer) ite.next();
        		try {
                    linkedNodeIds.addAll(getLinkedNodeIdOnNode(curnodeid.intValue()));
        		} catch (SQLException e) {
					throw new MapsException(e);
				} catch (ClassNotFoundException c) {
					throw new MapsException(c);
				}
        		
        	}
        }
        return linkedNodeIds;
    }
	private Set<LinkInfo> getLinksOnElements(Set<Integer> allnodes) throws SQLException, ClassNotFoundException {
	       Set<LinkInfo> nodes = new HashSet<LinkInfo>();
	        createConnection();
	        String nodelist="";
	        Iterator<Integer> ite = allnodes.iterator();
	        while (ite.hasNext()) {
	        	nodelist+=ite.next();
	        	if (ite.hasNext()) nodelist+=",";
	        }

	        String sql = "SELECT " +
		"datalinkinterface.nodeid, ifindex,nodeparentid, parentifindex, snmpiftype,snmpifspeed,snmpifoperstatus " +
		"FROM datalinkinterface " +
		"left join snmpinterface on nodeparentid = snmpinterface.nodeid " +
		"WHERE" +
		" (datalinkinterface.nodeid IN ("+nodelist+")" +
		" OR nodeparentid in ("+nodelist+")) " +
		"AND status != 'D' and datalinkinterface.parentifindex = snmpinterface.snmpifindex";
	        Statement stmt = m_dbConnection
            .createStatement();
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
	            
	            LinkInfo link = new LinkInfo(nodeid,ifindex,nodeparentid,parentifindex,snmpiftype,snmpifspeed,snmpifoperstatus);

	            nodes.add(link);
	        }
	        rs.close();
	        stmt.close();
	        
	        
	        return nodes;
	         		
	}
    
    private Set<LinkInfo> getLinkedNodeIdOnNode(int nodeID) throws SQLException, ClassNotFoundException {
        Set<LinkInfo> nodes = new HashSet<LinkInfo>();
        createConnection();
        
        PreparedStatement stmt = m_dbConnection
                .prepareStatement("select datalinkinterface.nodeid, ifindex,nodeparentid, parentifindex, snmpiftype,snmpifspeed,snmpifoperstatus from datalinkinterface left join snmpinterface on nodeparentid = snmpinterface.nodeid where datalinkinterface.nodeid = ? and status != 'D' and datalinkinterface.parentifindex = snmpinterface.snmpifindex");
        stmt.setInt(1, nodeID);
        ResultSet rs = stmt.executeQuery();
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
            
            LinkInfo node = new LinkInfo(nodeid,ifindex,nodeparentid,parentifindex,snmpiftype,snmpifspeed,snmpifoperstatus);

            nodes.add(node);
        }
        rs.close();
        stmt.close();
        stmt = m_dbConnection.prepareStatement("SELECT datalinkinterface.nodeid, ifindex,nodeparentid, parentifindex, snmpiftype,snmpifspeed,snmpifoperstatus from datalinkinterface left join snmpinterface on nodeparentid = snmpinterface.nodeid where NODEPARENTID = ? and status != 'D' and datalinkinterface.parentifindex = snmpinterface.snmpifindex");
	    stmt.setInt(1, nodeID);
	    rs = stmt.executeQuery();
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
            
            LinkInfo node = new LinkInfo(nodeid,ifindex,nodeparentid,parentifindex,snmpiftype,snmpifspeed,snmpifoperstatus);

            nodes.add(node);
        }
	    rs.close();
	    stmt.close();
        return nodes;
        
    }
    
    public java.util.Map getElementInfo(int elementId, int mapId, String type) throws MapsException {
    	//TODO
    	return null;
    }
    
    public MapMenu[] getVisibleMapsMenu(String user, String userRole) throws MapsException {
    	MapMenu[] retMaps = null;
    	if(userRole.equals(Authentication.ADMIN_ROLE)){
    		retMaps=getAllMapMenus();
    	}else{
    		retMaps=getMapsMenuByOwner(user);
    	}
    	return retMaps;
    }
    
    /**
     * gets the id corresponding to the link defined in configuration file. The match is performed first by snmptype, 
     * then by speed (if more are defined). If there is no match, the default link id is returned. 
     * @param linkinfo
     * @return the id corresponding to the link defined in configuration file. If there is no match, the default link id is returned.
     */
    private int getLinkTypeId(LinkInfo linkinfo) {
    	return mpf.getLinkTypeId(linkinfo.snmpiftype, linkinfo.snmpifspeed);
    }
    
}
