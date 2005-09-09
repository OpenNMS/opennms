/*
 * Created on 11-ott-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.opennms.web.inventory;
import java.sql.*;
import java.util.*;

import org.opennms.core.resource.Vault;
import org.opennms.web.element.NetworkElementFactory;
/**
 * @author maurizio
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class InventoryFactory {

	public List getNames() throws SQLException {
		
		Connection CONN = null;
		Statement  ST	= null;
		ResultSet  RS	= null;
		
		List retList = new ArrayList();
		CONN=Vault.getDbConnection();
		ST = CONN.createStatement();    	
		final String SQL_QUERY = "select distinct(name) from inventory";	
		RS = ST.executeQuery(SQL_QUERY);
		while(RS.next()){
			String name = RS.getString("name");
			retList.add(name);
		}
		CONN.close();
		Vault.releaseDbConnection(CONN);  
	    return retList;

		}
		
/*	 public Inventory[] getAllInventories(){
	 
	 }
*/
	 public Inventory[] getLastInventories(int nodeId)throws SQLException{
		Connection dbConn = null;
		PreparedStatement pStat = null;
		ResultSet resSet = null;
		final String SQL_QUERY = "select * from inventory where nodeid=? and status in (select min(status) from inventory where  status<>'D') ";
		dbConn = Vault.getDbConnection();
		pStat = dbConn.prepareStatement(SQL_QUERY);
		pStat.setInt(1,nodeId);
		resSet = pStat.executeQuery();
		Inventory[] inventories = rsToInventory(resSet);
		resSet.close();
		pStat.close();
		return inventories;
	 }
	 
	public Inventory getLastInventory(int nodeId, String inventCategory)throws SQLException{
	   Connection dbConn = null;
	   PreparedStatement pStat = null;
	   ResultSet resSet = null;
	   final String SQL_QUERY = "select * from inventory where nodeid=? and lower(name)=? and status ='A'";
	   dbConn = Vault.getDbConnection();
	   pStat = dbConn.prepareStatement(SQL_QUERY);
	   pStat.setInt(1,nodeId);
	   pStat.setString(2,inventCategory.toLowerCase());
	   resSet = pStat.executeQuery();
	   Inventory[] inventories = rsToInventory(resSet);
	   resSet.close();
	   pStat.close();
	   if(inventories.length<=0) 
	   	  return null;
	   return inventories[0];
	}

	public Inventory[] getInventoryOnNode(int nodeId)throws SQLException{
	   Connection dbConn = null;
	   PreparedStatement pStat = null;
	   ResultSet resSet = null;
	   final String SQL_QUERY = "select * from inventory where nodeid=?";
	   dbConn = Vault.getDbConnection();
	   pStat = dbConn.prepareStatement(SQL_QUERY);
	   pStat.setInt(1,nodeId);
	   resSet = pStat.executeQuery();
	   Inventory[] inventories = rsToInventory(resSet);
	   resSet.close();
	   pStat.close();
	   return inventories;
	}
	 
    public int getCountInventories(String inventoryCategory)throws SQLException{
	  int countRecords=0;
	  Connection dbConn = null;
	  PreparedStatement pStat = null;
	  ResultSet resSet = null;
	  final String SQL_QUERY = "select count(*) from inventory where name=?" ;
	  dbConn = Vault.getDbConnection();
	  pStat = dbConn.prepareStatement(SQL_QUERY);
	  pStat.setString(1,inventoryCategory);
	  resSet = pStat.executeQuery();
	  while(resSet.next()){
		   countRecords=resSet.getInt(1);
	  }
	  resSet.close();
	  pStat.close();
	  return countRecords;
	}
	 
	 public int getCountInventories(int nodeId,  String inventoryCategory)throws SQLException{
	   int countRecords=0;
	   Connection dbConn = null;
	   PreparedStatement pStat = null;
	   ResultSet resSet = null;
	   final String SQL_QUERY = "select count(*) from inventory where nodeid=? and name=?" ;
	   dbConn = Vault.getDbConnection();
	   pStat = dbConn.prepareStatement(SQL_QUERY);
	   pStat.setInt(1,nodeId);
	   pStat.setString(2,inventoryCategory);
	   resSet = pStat.executeQuery();
	   while(resSet.next()){
	   		countRecords=resSet.getInt(1);
	   }
	   resSet.close();
	   pStat.close();
	   return countRecords;
	 }
	 
	 
	 public Inventory[] getSimilarInventories(String filePath, String inventoryCategory)throws SQLException{
	 	inventoryCategory = inventoryCategory.toLowerCase();
	 	Connection dbConn = null;
		Statement stat = null;
		ResultSet resSet = null;
		String SQL_QUERY =
			"SELECT * from inventory where LOWER(name) like '"+inventoryCategory+"' and pathtofile<>'"+filePath+"' order by name, lastpolltime DESC";
		dbConn = Vault.getDbConnection();
		stat = dbConn.createStatement();
		resSet = stat.executeQuery(SQL_QUERY);
		Inventory[] inventories = rsToInventory(resSet);
		resSet.close();
		stat.close();
		return inventories;
		
	 }
	 
	 public Inventory[] getSimilarInventories(String inventoryCategory)throws SQLException{
	 	inventoryCategory = inventoryCategory.toLowerCase();
	 	Connection dbConn = null;
		Statement stat = null;
		ResultSet resSet = null;
		String SQL_QUERY =
			"SELECT * from inventory where LOWER(name) like '"+inventoryCategory+"' order by lastpolltime DESC";
		dbConn = Vault.getDbConnection();
		stat = dbConn.createStatement();
		resSet = stat.executeQuery(SQL_QUERY);
		Inventory[] inventories = rsToInventory(resSet);
		resSet.close();
		stat.close();
		return inventories;
		
	 }
	 
     public Inventory[] getInventories(
     	String nodeLabel, 
     	String ipAddr, 
     	String name, 
     	String status, 
     	int lastpolltimeint) throws SQLException {
        
        nodeLabel = nodeLabel.toLowerCase();
        name = name.toLowerCase();
		Connection dbConn = null;
		Statement stat = null;
		ResultSet resSet = null;
		String SQL_QUERY =
			"SELECT * from inventory where pathtofile like '%%'";
		
		if (nodeLabel != null && !nodeLabel.equals("") )
			SQL_QUERY += " and nodeid in (select nodeid from node where LOWER(nodelabel) like '%" + nodeLabel + "%')";
		if (ipAddr != null  && !ipAddr.equals("") ){
			ipAddr = ipAddr.replace('*','%');
			SQL_QUERY += (" and nodeid in (select nodeid from ipinterface where ipaddr like '%" + ipAddr + "%')");
		}
		if (!name.equals("0") )
			SQL_QUERY += (" and lower(name)='" + name + "'");
		if (!status.equals("Y") )
				SQL_QUERY += (" and status='" + status + "'");
		if (lastpolltimeint != 0) {
			if (lastpolltimeint==1)
				SQL_QUERY += " and lastpolltime>=(select now()-interval'1 hour')";
			else if (lastpolltimeint==2)
				SQL_QUERY += " and lastpolltime>=(select now()-interval'4 hours')";
			else if (lastpolltimeint==3)
				SQL_QUERY += " and lastpolltime>=(select now()-interval'8 hours')";
			else if (lastpolltimeint==4)
				SQL_QUERY += " and lastpolltime>=(select now()-interval'12 hours')";
			else if (lastpolltimeint==5)
				SQL_QUERY += " and lastpolltime>=(select now()-interval'24 hours')";
			else if (lastpolltimeint==6)
				SQL_QUERY += " and lastpolltime>=(select now()-interval'168 hours')";
			else if (lastpolltimeint==7)
				SQL_QUERY += " and lastpolltime>=(select now()-interval'744 hours')";
		}

		SQL_QUERY += " order by inventory.nodeid, name,  lastpolltime DESC";
		dbConn = Vault.getDbConnection();
		stat = dbConn.createStatement();
		resSet = stat.executeQuery(SQL_QUERY);
		Inventory[] inventories = rsToInventory(resSet);
		resSet.close();
		stat.close();
		return inventories;
     }


private Inventory[] rsToInventory(ResultSet rs)
	throws SQLException {
	Inventory[] records = null;
	Vector vector = new Vector();

	
	while (rs.next()) {
		Inventory invent = new Inventory();
		int nodeId=rs.getInt("nodeid");
		invent.setNodeID(nodeId);
		invent.setNodeLabel(NetworkElementFactory.getNodeLabel(nodeId));  
		invent.setCreateTime(rs.getTimestamp("createtime"));
		java.util.Date lastPollTime =  new java.util.Date((rs.getTimestamp("lastpolltime")).getTime());  
		invent.setLastPollTime(rs.getTimestamp("lastpolltime"));
		invent.setName(rs.getString("name"));
		invent.setStatus(rs.getString("status"));
		invent.setPathToFile(rs.getString("pathtofile"));
		vector.add(invent);
	}

	records = new Inventory[vector.size()];
	for (int i = 0; i < records.length; i++) {
		records[i] = (Inventory)vector.elementAt(i);
	}

	return records;
}
     
/*	public Inventory[] searchOnInventoryByNode(int nodeid){
	}
*/
}
