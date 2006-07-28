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
// OpenNMS Licensing       <license@opennms.org>
// http://www.opennms.org/
// http://www.sortova.com/
//
package org.opennms.web.map.db;

import java.util.*;
import java.sql.*;

import org.opennms.core.resource.Vault;
import org.opennms.web.asset.AssetModel;
import org.opennms.web.map.MapsException;
import org.opennms.web.map.config.MapPropertiesFactory;
import org.opennms.web.map.view.VElement;

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

  public static String getIconName(int elementId, String type, Connection conn)throws SQLException{
/*
  	if (type.equals(VElement.MAP_TYPE )) return "map";
    final String sqlQuery = "SELECT displaycategory FROM assets WHERE nodeid = ?";

    PreparedStatement statement = conn.prepareStatement(sqlQuery);
    statement.setInt(1,elementId);
    ResultSet rs = statement.executeQuery();
    String iconName="unspecified";
    if(rs.next()){
    	iconName = rs.getString(1);
    }
    rs.close();
    statement.close();
	
    if(iconName==null || iconName.equals("")){
		return "unspecified";
	}
    return iconName;
*/
	return "unspecified";
  }
  
  public static String getIconName(int elementId, String type)throws SQLException{
  	/*if (type.equals(VElement.MAP_TYPE )) return "map";
    final String sqlQuery = "SELECT displaycategory FROM assets WHERE nodeid = ?";
    Connection dbConn = Vault.getDbConnection();
    
    PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
    statement.setInt(1,elementId);
    ResultSet rs = statement.executeQuery();
    String iconName="unspecified";
    if(rs.next()){
    	iconName = rs.getString(1);
    }
    rs.close();
    statement.close();
    Vault.releaseDbConnection(dbConn);
//FIXME workaround to test why it is not working
    return "unspecified";
    //if(iconName==null || iconName.equals("")){
	//	return "unspecified";
	//}
    //return iconName;*/
	return "unspecified";

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

  
  public static Element getElement(int id, int mapId) throws SQLException, MapsException {
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

  public static Map[] getMaps(String mapname, String maptype) throws SQLException {
      final String sqlQuery = "SELECT * FROM map WHERE mapName= ? AND maptype = ? ";

      Connection dbConn = Vault.getDbConnection();
      PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
      statement.setString(1, mapname);
      statement.setString(2, maptype);

      ResultSet rs = statement.executeQuery();

      Vector maps = rs2MapVector(rs);
      
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

  public static String getMapName(int id) throws SQLException {
      final String sqlQuery = "SELECT mapname FROM map WHERE mapId = ?";

      Connection dbConn = Vault.getDbConnection();
      PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();
      String label = null;
      if (rs.next()) {
      	label = rs.getString(1);
      }
      rs.close();
      statement.close();
      Vault.releaseDbConnection(dbConn);

      return label;
  }
  
  public static String getMapName(int id, Connection dbConn) throws SQLException {
    final String sqlQuery = "SELECT mapname FROM map WHERE mapId = ?";

    PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
    statement.setInt(1, id);
    ResultSet rs = statement.executeQuery();
    String label = null;
    if (rs.next()) {
    	label = rs.getString(1);
    }
    rs.close();
    statement.close();
   
    return label;
}  

  public static Element[] getAllElements() throws SQLException, MapsException {
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

  public static Element[] getElementsOfMap(int mapid) throws SQLException, MapsException {
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

  public static Element[] getNodeElementsOfMap(int mapid) throws SQLException, MapsException {
      final String sqlQuery = "SELECT * FROM element WHERE mapid = ? AND elementtype = 'N' ";

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

  public static Element[] getMapElementsOfMap(int mapid) throws SQLException, MapsException {
      final String sqlQuery = "SELECT * FROM element WHERE mapid = ? AND elementtype = 'M' ";

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

  public static Element[] getAllMapMapElements() throws SQLException, MapsException {
      final String sqlQuery = "SELECT * FROM element WHERE elementtype = 'M' ";

      Connection dbConn = Vault.getDbConnection();
      PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
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

	public static java.util.Map getMapsStructure() throws SQLException {
		java.util.Map maps = new HashMap();
	    String sqlQuery = "select elementid,mapid from element where elementtype=?";
		Connection connection = Vault.getDbConnection();
		PreparedStatement ps = connection.prepareStatement(sqlQuery);
		ps.setString(1,Element.MAP_TYPE);
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			Integer parentId = new Integer(rs.getInt("mapid"));
			Integer childId = new Integer(rs.getInt("elementid"));
			Set childs  = (Set)maps.get(parentId);
			if(childs==null){
				childs=new TreeSet();
			}
			if(!childs.contains(childId)){
				childs.add(childId);
			}
			maps.put(parentId,childs);
		}

		return maps;
	
	}

  public static Element[] getAllNodeMapElements() throws SQLException, MapsException {
      final String sqlQuery = "SELECT * FROM element WHERE elementtype = 'M' ";

      Connection dbConn = Vault.getDbConnection();
      PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
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

  public static MapMenu[] getAllMapsMenu() throws SQLException {
      final String sqlQuery = "SELECT mapid,mapname,mapowner FROM map";

      Connection dbConn = Vault.getDbConnection();
      Statement statement = dbConn.createStatement();
      ResultSet rs = statement.executeQuery(sqlQuery);
      Vector maps = rs2MapMenuVector(rs);
      //System.out.println(maps);
      
      MapMenu[] el=null;
      if(maps!=null){
	        el = new MapMenu[maps.size()];
	        el=(MapMenu[])maps.toArray(el);
      }
      rs.close();
      statement.close();
      Vault.releaseDbConnection(dbConn);
      
      return el;
  }
  
  public static MapMenu getMapMenu(int mapId) throws SQLException {
      final String sqlQuery = "SELECT mapid,mapname,mapowner FROM map where mapId= ?" ;

      Connection dbConn = Vault.getDbConnection();
      PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
      statement.setInt(1,mapId);
      ResultSet rs = statement.executeQuery();
      MapMenu mm = rs2MapMenu(rs);
      //System.out.println(maps);
      
      rs.close();
      statement.close();
      Vault.releaseDbConnection(dbConn);
      
      return mm;
  }    
  
  public static MapMenu[] getMapsMenuByName(String mapLabel) throws SQLException {
      final String sqlQuery = "SELECT mapid,mapname,mapowner FROM map WHERE upper( mapname ) = upper( ? )";

      Connection dbConn = Vault.getDbConnection();
      PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
      statement.setString(1, mapLabel);
      ResultSet rs = statement.executeQuery();
      Vector mapVector = rs2MapMenuVector(rs);
      MapMenu[] maps =null;
      if(mapVector!=null){
      	maps = new MapMenu[mapVector.size()];
      	maps=(MapMenu[])mapVector.toArray(maps);
      }
      rs.close();
      statement.close();
      Vault.releaseDbConnection(dbConn);

      return maps;
  }

  public static Element[] getElementsLike(String elementLabel)
          throws SQLException, MapsException {
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
  
  public static Map[] getMapsByName(String mapLabel) throws SQLException {
      final String sqlQuery = "SELECT * FROM map WHERE mapname = ?";

      Connection dbConn = Vault.getDbConnection();
      PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
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

  public static Map[] getContainerMaps(int id, String type) throws SQLException {
      final String sqlQuery = "SELECT map.* FROM map INNER JOIN element ON map.mapid = element.mapid WHERE elementid = ? AND elementtype = ?";

      Connection dbConn = Vault.getDbConnection();
      dbConn = Vault.getDbConnection();
      PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
      statement.setInt(1, id);
      statement.setString(2, type);
      ResultSet rs = statement.executeQuery();
      Vector el = rs2MapVector(rs);
      Map[] maps = new Map[el.size()];
      maps=(Map[])el.toArray(maps);
      rs.close();
      statement.close();
      Vault.releaseDbConnection(dbConn);

      return maps;
  }

  public static boolean isElementInMap(int elementId, int mapId) throws SQLException, MapsException {
      Element element = null;
      element = getElement(elementId, mapId);
      return (element != null);
  }

  public static List getOutagedVElems() throws SQLException {
      final String sqlQuery = "select distinct nodeid,eventuei,eventseverity from events where exists (select svclosteventid from outages where ifregainedservice is null and events.eventid = svclosteventid) order by nodeid";

      Connection dbConn = Vault.getDbConnection();
      PreparedStatement statement = dbConn.prepareStatement(sqlQuery);
      ResultSet rs = statement.executeQuery();
      List elems = new ArrayList();
      while (rs.next()) {
      	ElementInfo einfo = new ElementInfo(rs.getInt(1),rs.getString(2),rs.getInt(3));
      	elems.add(einfo);
      }
      rs.close();
      statement.close();
      Vault.releaseDbConnection(dbConn);
      return elems;

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
  
  private static Vector rs2MapMenuVector(ResultSet rs) throws SQLException {
      Vector mapVec = null;
      boolean firstTime = true;
      while (rs.next()) {
          if (firstTime) {
              mapVec = new Vector();
              firstTime = false;
          }
          
          MapMenu currMap = new MapMenu(rs.getInt("mapId"),rs.getString("mapName"),rs.getString("mapOwner"));
          mapVec.add(currMap);
      }
      return mapVec;
  }
  
  private static MapMenu rs2MapMenu(ResultSet rs) throws SQLException {
      MapMenu map = null;
      if (rs.next()) {
      	map = new MapMenu(rs.getInt("mapId"),rs.getString("mapName"),rs.getString("mapOwner"));
      }
      return map;
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
          map.setWidth(rs.getInt("mapwidth"));
          map.setHeight(rs.getInt("mapheight"));
          map.setUserLastModifies(rs.getString("userLastModifies"));
          map.setCreateTime(rs.getTimestamp("mapCreateTime"));
          map.setLastModifiedTime(rs.getTimestamp("lastmodifiedtime"));
          map.setAsNew(false);
      }
      return map;
  }

  private static Element rs2Element(ResultSet rs) throws SQLException,MapsException {
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

  private static Vector rs2ElementVector(ResultSet rs) throws SQLException,MapsException {
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
