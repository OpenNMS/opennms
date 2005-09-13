//
//Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
//Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact:
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.sortova.com/
//
package org.opennms.web.map.db;

import java.util.*;
import java.sql.*;
import org.opennms.core.resource.Vault;

/**
 * @author micmas
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Factory {
    private Factory() {
        // Blank
    }

    public static int countMaps(int mapId) throws SQLException {
        final String sqlQuery = "SELECT COUNT(*) FROM map WHERE mapid = ?";

        Connection dbConn = Vault.getDbConnection();
        PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
        statement.setInt(1, mapId);
        ResultSet rs = statement.executeQuery();
        int count =0;
        if(rs.next()){
        	count = rs.getInt(1);
        }
        rs.close();
        statement.close();
        Vault.releaseDbConnection(dbConn);
        return count;
    }

    
    public static Element getElement(int id, int mapId) throws SQLException {
        final String sqlQuery = "SELECT * FROM element WHERE elementid = ? AND mapid = ?";

        Connection dbConn = Vault.getDbConnection();
        PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
        statement.setInt(1, id);
        statement.setInt(2, mapId);
        ResultSet rs = statement.executeQuery();
        Element el = rs2Element(rs);
        rs.close();
        statement.close();
        Vault.releaseDbConnection(dbConn);

        return el;
    }

    public static Map getMap(int id) throws SQLException {
        final String sqlQuery = "SELECT * FROM map WHERE mapId = ?";

        Connection dbConn = Vault.getDbConnection();
        PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
        statement.setInt(1, id);
        ResultSet rs = statement.executeQuery();
        Map map = rs2Map(rs);
        rs.close();
        statement.close();
        Vault.releaseDbConnection(dbConn);

        return map;
    }

    public static Element[] getAllElements() throws SQLException {
        final String sqlQuery = "SELECT * FROM element";

        Connection dbConn = Vault.getDbConnection();
        Statement statement = dbConn.createStatement();
        ResultSet rs = statement.executeQuery(sqlQuery);
        Vector elements = rs2ElementVector(rs);
        Element[] el = new Element[elements.size()];
        el=(Element[])elements.toArray(el);
        rs.close();
        statement.close();
        Vault.releaseDbConnection(dbConn);
        
        return el;
    }

    public static Element[] getElementsOfMap(int mapid) throws SQLException {
        final String sqlQuery = "SELECT * FROM element WHERE mapid = ?";

        Connection dbConn = Vault.getDbConnection();
        PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
        statement.setInt(1, mapid);
        ResultSet rs = statement.executeQuery();
        Vector elements = rs2ElementVector(rs);
        Element[] el =null;
        if(elements!=null){
	        el = new Element[elements.size()];
	        el=(Element[])elements.toArray(el);
        }
        rs.close();
        statement.close();
        Vault.releaseDbConnection(dbConn);
        return el;
    }

    public static Map[] getAllMaps() throws SQLException {
        final String sqlQuery = "SELECT * FROM map";

        Connection dbConn = Vault.getDbConnection();
        Statement statement = dbConn.createStatement();
        ResultSet rs = statement.executeQuery(sqlQuery);
        Vector maps = rs2MapVector(rs);
        //System.out.println(maps);
        
        Map[] el=null;
        if(maps!=null){
	        el = new Map[maps.size()];
	        el=(Map[])maps.toArray(el);
        }
        rs.close();
        statement.close();
        Vault.releaseDbConnection(dbConn);
        
        return el;
    }

    public static Element[] getElementsLike(String elementLabel)
            throws SQLException {
        final String sqlQuery = "SELECT * FROM element WHERE elementlabel LIKE ?";

        Connection dbConn = Vault.getDbConnection();
        PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
        elementLabel = "%" + elementLabel + "%";
        statement.setString(1, elementLabel);
        ResultSet rs = statement.executeQuery();
        Vector elements = rs2ElementVector(rs);
        Element[] el = new Element[elements.size()];
        el=(Element[])elements.toArray(el);
        rs.close();
        statement.close();
        Vault.releaseDbConnection(dbConn);

        return el;
    }

    public static Map[] getMapsLike(String mapLabel) throws SQLException {
        final String sqlQuery = "SELECT * FROM map WHERE mapname LIKE ?";

        Connection dbConn = Vault.getDbConnection();
        PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
        mapLabel = "%" + mapLabel + "%";
        statement.setString(1, mapLabel);
        ResultSet rs = statement.executeQuery();
        Vector mapVector = rs2MapVector(rs);
        Map[] maps =null;
        if(mapVector!=null){
        	maps = new Map[mapVector.size()];
        	maps=(Map[])mapVector.toArray(maps);
        }
        rs.close();
        statement.close();
        Vault.releaseDbConnection(dbConn);

        return maps;
    }

    public static Map[] getContainerMaps(int id) throws SQLException {
        final String sqlQuery = "SELECT map.* FROM map INNER JOIN element ON map.mapid = element.mapid WHERE elementid = ?";

        Connection dbConn = Vault.getDbConnection();
        dbConn = Vault.getDbConnection();
        PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
        statement.setInt(1, id);
        ResultSet rs = statement.executeQuery();
        Vector el = rs2MapVector(rs);
        Map[] maps = new Map[el.size()];
        maps=(Map[])el.toArray(maps);
        rs.close();
        statement.close();
        Vault.releaseDbConnection(dbConn);

        return maps;
    }

    public static boolean isElementInMap(int elementId, int mapId) throws SQLException {
        Element element = null;
        element = getElement(elementId, mapId);
        return (element != null);
    }
        
    private static Vector rs2MapVector(ResultSet rs) throws SQLException {
        Vector mapVec = null;
        boolean firstTime = true;
        while (rs.next()) {
            if (firstTime) {
                mapVec = new Vector();
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
            currMap.setUserLastModifies(rs.getString("userLastModifies"));
            currMap.setCreateTime(rs.getTimestamp("mapCreateTime"));
            currMap.setLastModifiedTime(rs.getTimestamp("lastmodifiedtime"));
            mapVec.add(currMap);
        }
        return mapVec;
    }

    private static Map rs2Map(ResultSet rs) throws SQLException {
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
            map.setUserLastModifies(rs.getString("userLastModifies"));
            map.setCreateTime(rs.getTimestamp("mapCreateTime"));
            map.setLastModifiedTime(rs.getTimestamp("lastmodifiedtime"));
        }
        return map;
    }

    private static Element rs2Element(ResultSet rs) throws SQLException {
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

    private static Vector rs2ElementVector(ResultSet rs) throws SQLException {
        Vector vecElem = null;
        boolean firstTime = true;
        while (rs.next()) {
            if (firstTime) {
                vecElem = new Vector();
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
}
