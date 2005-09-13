/*
 * Created on 4-gen-2005
 *
 */
package org.opennms.web.map.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.opennms.core.resource.Vault;

/**
 * @author micmas
 * 
 */
public class Manager {
    private Connection connection = null;

    private boolean isStartedSession() {
        return (connection != null);
    }

    public void startSession() throws SQLException {
        connection = Vault.getDbConnection();
        connection.setAutoCommit(false);
        connection
                .setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }

    synchronized public void endSession() throws SQLException {
        if (!isStartedSession())
            throw new IllegalStateException("Call startSession() first.");
    	connection.commit();
        Vault.releaseDbConnection(connection);
        connection = null;
    }

    public synchronized void saveMaps(Map[] m) throws SQLException {
        if (!isStartedSession())
            throw new IllegalStateException("Call startSession() first.");

        for (int i = 0, n = m.length; i < n; i++) {
            saveMap(m[i]);
        }
    }

    public synchronized void saveElements(Element[] e) throws SQLException {
        if (!isStartedSession())
            throw new IllegalStateException("Call startSession() first.");
        if(e!=null){
        	for (int i = 0, n = e.length; i < n; i++) {
        		saveElement(e[i]);
        	}
        }
    }

    public synchronized void saveElement(Element e) throws SQLException {
        if (!isStartedSession())
            throw new IllegalStateException("Call startSession() first.");

        final String sqlSelectQuery = "SELECT COUNT(*) FROM element WHERE elementid = ? AND MAPID = ?";
        final String sqlInsertQuery = "INSERT INTO element (mapid, elementid, elementtype, elementlabel, elementicon, elementx, elementy) VALUES (?, ?, ?, ?, ?, ?, ?)";
        final String sqlUpdateQuery = "UPDATE element SET mapid = ?, elementid = ?, elementtype = ?, elementlabel = ?, elementicon = ?, elementx = ?, elementy = ? WHERE elementid = ? AND mapid = ?";
        try {
            PreparedStatement statement = connection
                    .prepareStatement(sqlSelectQuery);
            statement.setInt(1, e.getId());
            statement.setInt(2, e.getMapId());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                statement.close();
                if (count == 0) {
                    statement = connection.prepareStatement(sqlInsertQuery);
                    statement.setInt(1, e.getMapId());
                    statement.setInt(2, e.getId());
                    statement.setString(3, e.getType());
                    statement.setString(4, e.getLabel());
                    statement.setString(5, e.getIcon());
                    statement.setInt(6, e.getX());
                    statement.setInt(7, e.getY());
                } else {
                    statement = connection.prepareStatement(sqlUpdateQuery);
                    statement.setInt(1, e.getMapId());
                    statement.setInt(2, e.getId());
                    statement.setString(3, e.getType());
                    statement.setString(4, e.getLabel());
                    statement.setString(5, e.getIcon());
                    statement.setInt(6, e.getX());
                    statement.setInt(7, e.getY());
                    statement.setInt(8, e.getId());
                    statement.setInt(9, e.getMapId());
                }
                // now count counts number of modified record
                count = statement.executeUpdate();
                rs.close();
                statement.close();
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
                Vault.releaseDbConnection(connection);
                throw ex;
            } catch (SQLException eex) {
                throw eex;
            }
        }
    }

    public synchronized void deleteElement(Element e) throws SQLException {
        if(e!=null){
        	deleteElement(e.getId(), e.getMapId());
        }
    }
    
    public synchronized void deleteElements(Element[] elems) throws SQLException {
    	if(elems!=null){
    		for(int i=0;i<elems.length;i++){
    			deleteElement(elems[i]);
    		}
    	}
    }

    public synchronized void deleteElementsOfMap(int id) throws SQLException {
        if (!isStartedSession())
            throw new IllegalStateException("Call startSession() first.");
        final String sqlDelete = "DELETE FROM element WHERE mapid = ?";

        try {
            PreparedStatement statement = connection
                    .prepareStatement(sqlDelete);
            statement.setInt(1, id);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            try {
                connection.rollback();
                Vault.releaseDbConnection(connection);
                throw e;
            } catch (SQLException ex) {
                throw ex;
            }
        }
    }    
    
    public synchronized void deleteElement(int id, int mapid)
            throws SQLException {
        if (!isStartedSession())
            throw new IllegalStateException("Call startSession() first.");
        final String sqlDelete = "DELETE FROM element WHERE elementid = ? AND mapid = ?";

        try {
            PreparedStatement statement = connection
                    .prepareStatement(sqlDelete);
            statement.setInt(1, id);
            statement.setInt(2, mapid);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            try {
                connection.rollback();
                Vault.releaseDbConnection(connection);
                throw e;
            } catch (SQLException ex) {
                throw ex;
            }
        }
    }

    public synchronized void deleteMap(Map m) throws SQLException {
        deleteMap(m.getId());
    }

    public synchronized int deleteMap(int id) throws SQLException {
        if (!isStartedSession())
            throw new IllegalStateException("Call startSession() first.");
        final String sqlDeleteMap = "DELETE FROM map WHERE mapid = ?";
        final String sqlDeleteElemMap = "DELETE FROM element WHERE elementid = ?";
        int countDelete = 0;
        try {
            PreparedStatement statement = connection
                    .prepareStatement(sqlDeleteMap);
            statement.setInt(1, id);
            countDelete = statement.executeUpdate();
            statement.close();
            statement = connection.prepareStatement(sqlDeleteElemMap);
            statement.setInt(1, id);
            statement.executeUpdate();
            statement.close();
            return countDelete;
        } catch (SQLException e) {
            try {
                connection.rollback();
                Vault.releaseDbConnection(connection);
                throw e;
            } catch (SQLException ex) {
                throw ex;
            }
        } 
    }

    public synchronized void saveMap(Map m) throws SQLException {
        if (!isStartedSession())
            throw new IllegalStateException("Call startSession() first.");
        
        final String sqlGetCurrentTimestamp = "SELECT CURRENT_TIMESTAMP";
        final String sqlGetMapNxtId = "SELECT nextval('mapnxtid')";
        final String sqlInsertQuery = "INSERT INTO map (mapid, mapname, mapbackground, mapowner, mapcreatetime, mapaccess, userlastmodifies, lastmodifiedtime, mapscale, mapxoffset, mapyoffset, maptype) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        final String sqlUpdateQuery = "UPDATE map SET mapname = ?, mapbackground = ?, mapowner = ?, mapaccess = ?, userlastmodifies = ?, lastmodifiedtime = ?, mapscale = ?, mapxoffset = ?, mapyoffset = ?, maptype = ? WHERE mapid = ?";
        Timestamp currentTimestamp = null;
        int nxtid = 0;
        
        int count = -1;
        
        try {
            Statement stmtCT = connection.createStatement();
            ResultSet rs = stmtCT.executeQuery(sqlGetCurrentTimestamp);
            if (rs.next()) {
                currentTimestamp = rs.getTimestamp(1);
                PreparedStatement statement;
                if (m.isNew()) {
                    Statement stmtID = connection.createStatement();
                    ResultSet rsStmt = stmtID.executeQuery(sqlGetMapNxtId);
                    if (rsStmt.next()) {
                        nxtid = rsStmt.getInt(1);
                    }
                    rsStmt.close();
                    stmtID.close();
                    
                    statement = connection.prepareStatement(sqlInsertQuery);
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
                } else {
                    statement = connection.prepareStatement(sqlUpdateQuery);
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
                    statement.setInt(11, m.getId());
                }
                count = statement.executeUpdate();

                statement.close();
            }
            rs.close();
            stmtCT.close();
        } catch (SQLException ex) {
            try {
                connection.rollback();
                Vault.releaseDbConnection(connection);
            } catch (SQLException eex) {
                throw eex;
            }
            throw ex;
        }
        
        if (count == 0)
            throw new SQLException("Called saveMap() on deleted map;");        
        
        if (m.isNew()) {
            m.setId(nxtid);
            m.setCreateTime(currentTimestamp);
            m.setAsNew(false);
        }
        m.setLastModifiedTime(currentTimestamp);
    }
}
