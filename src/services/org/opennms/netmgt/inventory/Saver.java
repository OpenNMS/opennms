/*
 * Created on 30-ago-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.opennms.netmgt.inventory;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import java.text.SimpleDateFormat;
import java.net.InetAddress;
import org.opennms.netmgt.config.*;
import org.apache.log4j.*;
import java.io.IOException;
import org.exolab.castor.xml.MarshalException;
import java.sql.*;
import java.io.*;
import java.util.*;
import org.opennms.netmgt.config.inventory.parser.*;
import org.opennms.netmgt.config.inventory.InventoryConfiguration;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.asset.Asset;
/**
 * @author antonio
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Saver {
	private int nodeId;
	private String ipAddress="";
	private String saveMessage="";
	private static final String SELECT_NODEID_BY_INTERFACE =	"SELECT nodeId FROM ipInterface WHERE ipAddr = ?";
	private static final String COUNT_NODEID_CONFIGURATION_NAME = "SELECT count(*) FROM inventory WHERE nodeID=? AND name=?";
	private static final String UPDATE_CONFIGURATION_TO_STATUS_N =	"UPDATE inventory SET  status='N' WHERE nodeID =? AND name=? and status<>'N'";
	private static final String UPDATE_LASTPOLLTIME_PATHTOFILE =	"UPDATE inventory SET  lastpolltime=? , pathtofile=? WHERE nodeID =? AND name=? and status='A'";
	private static final String INSERT_IN_CONFIGURATION = "INSERT INTO inventory (nodeID,  createTime , lastpolltime, name, pathtofile, status) VALUES (? , ? , ?, ? ,?,'A')";
	private static final String COUNT_NODEID_IN_ASSET = "SELECT count(*) FROM assets WHERE nodeID=?";
	private static final String SELECT_PATHTOFILE = "SELECT pathtofile from inventory where nodeid=? and name=? and status=?";
	private Map parameters;
	private Map asset = new HashMap();
	private List newPathList = new ArrayList();
	private Map newItemMap = new HashMap();
	
	public Saver(NetworkInterface iFace, Map parameters){

		this.parameters=parameters;
		ipAddress = ((InetAddress) iFace.getAddress()).getHostAddress();
		Category log = ThreadCategory.getInstance(getClass());
		java.sql.Connection dbConn = null;
		try {
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();
		} catch (SQLException s) {
			log.error("Unable to connect to DB");
			}
		ResultSet rs = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_NODEID_BY_INTERFACE);
			stmt.setString(1, ipAddress);
			rs = stmt.executeQuery();
	
			// retrieve nodeid the ip address is associated and update the file repository path
			while (rs.next()) {
				nodeId = rs.getInt(1);
			}
			dbConn.close();
		}catch(SQLException s){
			log.error("Unable to retrieve nodeid from db.");
		}
	}
		
	private String getDataItemValue(List itemTreeList){
		Item item = (Item) newItemMap.get(itemTreeList);
		if(item!=null)
		   return item.getDataitem();
		else return "";
	}
		
	private void visit(Item currItem, List path, List list, Map itemMap){
				List tmpPathList=new ArrayList(path);
				tmpPathList.add(currItem.getName());
				if(currItem.getItemCount()==0){
						itemMap.put(tmpPathList,currItem);
						list.add(tmpPathList);
						String assetField = currItem.getAssetField();
						if(assetField!=null){
							asset.put(assetField,getDataItemValue(tmpPathList));
						}
				}else{
			
					Enumeration enumItem = currItem.enumerateItem();
					while(enumItem.hasMoreElements()){
						visit((Item)enumItem.nextElement(),tmpPathList, list, itemMap);
					}
				}
			}
	
			/*
			 * @param newInvent
			 * 
			 */
	private void init(String newInventory)throws MarshalException, ValidationException {
		Category log = ThreadCategory.getInstance(getClass());
		log.debug(newInventory);
		org.opennms.netmgt.config.inventory.parser.Inventory newInvent = (org.opennms.netmgt.config.inventory.parser.Inventory) Unmarshaller.unmarshal(org.opennms.netmgt.config.inventory.parser.Inventory.class,new StringReader(newInventory));
		List newPathListTmp = new ArrayList();
		newPathListTmp.add(newInvent.getName());
		if(newInvent.getItemCount()>0){
				Enumeration enumItem = newInvent.enumerateItem();
			while(enumItem.hasMoreElements()){
				visit((Item)enumItem.nextElement(),newPathListTmp, newPathList, newItemMap); 
			}
		}
		//System.out.println(newPathListTmp.toString());
		//System.out.println(newPathList.toString());
		}
		
		
	private void InsertIntoAssets(Connection dbConn)throws SQLException{
			Category log = ThreadCategory.getInstance(getClass());
			String  category = "'"+Asset.UNSPECIFIED_CATEGORY+"'";
			
			int nodeid = this.nodeId;
			long time = System.currentTimeMillis();
			Timestamp lastmodifieddate = new Timestamp(time);
		 
			Iterator dbColumnIter = asset.keySet().iterator();
			boolean userLastModFound=false, categoryFound=false;
			String strFields = "nodeid, lastmodifieddate";
			String strValues = ""+nodeid+", ?";
			String insertQuery = "INSERT INTO assets ";
			int counter = 0;
			while(dbColumnIter.hasNext()){
				counter++;	
				String currDbColumn = (String) dbColumnIter.next();
				if(currDbColumn.equals("category"))
					categoryFound=true;
				if(currDbColumn.equals("userlastmodified"))
					userLastModFound=true;
				String dataItem = (String) asset.get(currDbColumn);
				strFields+=","+currDbColumn;
				strValues+=",'"+dataItem+"'";
				}
			if(categoryFound==false){
				strFields+=",category";
				strValues+=",'"+Asset.UNSPECIFIED_CATEGORY+"'";			
			}
			if(userLastModFound==false){
				strFields+=",userlastmodified";
				strValues+=",'admin'";			
			}
			insertQuery= insertQuery+"("+strFields+") VALUES ("+strValues+")";	
			if(counter>0){
				PreparedStatement stmt = dbConn.prepareStatement(insertQuery);
				stmt.setTimestamp(1, lastmodifieddate);
				stmt.execute();
			}
			return;
		}



		public int save(String newInventory, String inventoryType, int compareResult,  boolean renameCorruptedFile) {
			if(newInventory==null || newInventory.equals(""))
				return InventoryMonitor.CONFIGURATION_NOT_SAVED;
			Category log = ThreadCategory.getInstance(getClass());
			try{
				log.debug(newInventory);
				init(newInventory);
			}catch(ValidationException ve){
				log.error("Unable to parse new Inventory.");
				log.error(ve);
				return InventoryMonitor.CONFIGURATION_NOT_SAVED;
			}catch(MarshalException me){
				log.error("Unable to parse new Inventory.");
				log.error(me);
				return InventoryMonitor.CONFIGURATION_NOT_SAVED;
			}
			InventoryConfiguration iConfig =
				InventoryConfigFactory.getInstance().getConfiguration();
			
			
			String directory_repository = iConfig.getFileRepository();
			String path = (String) parameters.get("path");
			if(path==null){
				log.error("Parameter 'path' not found.");
				saveMessage = "Unable to save "+inventoryType+" configuration.<br>";
				return InventoryMonitor.CONFIGURATION_NOT_SAVED;
			}
			java.sql.Connection dbConn = null;
			try {
				dbConn = DatabaseConnectionFactory.getInstance().getConnection();
				dbConn.setAutoCommit(false);
			} catch (SQLException s) {
				log.error("Unable to connect to DB");
				saveMessage =
					"Unable to save "+inventoryType+" configuration.<br>";
				return InventoryMonitor.CONFIGURATION_NOT_SAVED;
			} catch (Exception s) {
				log.error("Unable to connect to DB");
				saveMessage =
					"Unable to save "+inventoryType+" configuration.<br>";
				return InventoryMonitor.CONFIGURATION_NOT_SAVED;
			}
			String nodeDirectory_repository="";
			String file_repository="";
			String oldPathToFile="";
			String newPathToFile="";
			boolean renameFile=false;

			ResultSet rs = null;
			try {
				PreparedStatement stmt = dbConn.prepareStatement(SELECT_NODEID_BY_INTERFACE);
				stmt.setString(1, ipAddress);
				rs = stmt.executeQuery();

				// retrieve nodeid the ip address is associated and update the file repository path
				while (rs.next()) {
					nodeId = rs.getInt(1);
				}
				if (directory_repository.endsWith("/") == false
					&& directory_repository.endsWith(File.separator) == false) {
					directory_repository += File.separator;
				}
				nodeDirectory_repository = directory_repository + nodeId;

			} catch (SQLException s) {
				log.error("Unable to read from DB");
				saveMessage =
					"Unable to save "+inventoryType+" configuration.<br>";
				return InventoryMonitor.CONFIGURATION_NOT_SAVED;
			} 
			
			rs = null;
			try {
				PreparedStatement stmt = dbConn.prepareStatement(SELECT_PATHTOFILE);
				stmt.setInt(1, nodeId);
				stmt.setString(2,inventoryType);
				stmt.setString(3,"A");
				rs = stmt.executeQuery();

				// retrieve nodeid the ip address is associated and update the file repository path
				while (rs.next()) {
					oldPathToFile = rs.getString(1);
				}

				//String oldDirRep = nodeDirectory_repository;
				String newDirRep = nodeDirectory_repository;

				if (path.startsWith("/") == false
					&& path.startsWith(File.separator) == false) {
					newDirRep += File.separator;
				}
				long time = System.currentTimeMillis();
				Timestamp currTime = new Timestamp(time);
				java.util.Date currTimeDate =  new java.util.Date(currTime.getTime());
				SimpleDateFormat ObjectformatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				newPathToFile = newDirRep + path+"["+ObjectformatDate.format(currTimeDate)+"]";
			} catch (SQLException s) {
				log.error("Unable to read from DB");
				saveMessage =
					"Unable to save "+inventoryType+" configuration.<br>";
				return InventoryMonitor.CONFIGURATION_NOT_SAVED;
			} 
	
			int returnValue = InventoryMonitor.CONFIGURATION_SAVED;
			
			rs = null;
			int found = -1;
			try {
				PreparedStatement stmt = dbConn.prepareStatement(COUNT_NODEID_CONFIGURATION_NAME);
				stmt.setInt(1, nodeId);
				stmt.setString(2, inventoryType);
				rs = stmt.executeQuery();

				while (rs.next()) {
					found = rs.getInt(1);
				}
			} catch (SQLException s) {
				log.error("Unable to read from DB");
				log.error(s);
				saveMessage = "Unable to save "+inventoryType+" configuration.<br>";
				return InventoryMonitor.CONFIGURATION_NOT_SAVED;
			}
			long time = System.currentTimeMillis();
			Timestamp currentTime = new Timestamp(time);

			// if this isn't the first configuration inventory for this node 
			if (found > 0) {
				if(compareResult==InventoryMonitor.CONFIGURATION_CHANGED || compareResult==InventoryMonitor.FIRST_ACTIVE_CONFIGURATION_DOWNLOAD ){
					// prepare and execute the UPDATE
					try {
						log.debug("FOUND=" + found + " row/s in configuration, UPDATE it");
						PreparedStatement stmt = dbConn.prepareStatement(UPDATE_CONFIGURATION_TO_STATUS_N);
						stmt.setInt(1, nodeId);
						stmt.setString(2, inventoryType);
						stmt.executeUpdate();
					} catch (SQLException s) {
						log.error("Unable to update DB");
						saveMessage = "Unable to save "+inventoryType+" configuration.<br>";
						return InventoryMonitor.CONFIGURATION_NOT_SAVED;
					}
	
					// prepare and execute the INSERT
					try {
	
						PreparedStatement stmt = dbConn.prepareStatement(INSERT_IN_CONFIGURATION);
						stmt.setInt(1, nodeId);
						stmt.setTimestamp(2, currentTime);
						stmt.setTimestamp(3, currentTime);
						stmt.setString(4, inventoryType);
						stmt.setString(5, newPathToFile);
						stmt.execute();
					} catch (SQLException s) {
						log.error("Unable to insert in DB");
						log.error(s);
						saveMessage = "Unable to save "+inventoryType+" configuration.<br>";
						return InventoryMonitor.CONFIGURATION_NOT_SAVED;
					}
			
				}else{
					try {
						log.debug("FOUND=" + found + " row/s in configuration, UPDATE it");
						PreparedStatement stmt =
							dbConn.prepareStatement(UPDATE_LASTPOLLTIME_PATHTOFILE);
						stmt.setTimestamp(1,currentTime);
						stmt.setString(2, newPathToFile);
						stmt.setInt(3, nodeId);
						stmt.setString(4, inventoryType);
						stmt.executeUpdate();
						renameFile=true;
						
					} catch (SQLException s) {
						log.error("Unable to update DB");
						log.error(s);
						saveMessage = "Unable to save "+inventoryType+" configuration.<br>";
						return InventoryMonitor.CONFIGURATION_NOT_SAVED;
					}
				}
				try{
					dbConn.commit();
				}catch(SQLException s){
					log.error("Unable to commit to DB");
					return InventoryMonitor.CONFIGURATION_NOT_SAVED;
				}
			}else{
			// prepare and execute the INSERT
			try {
					
				PreparedStatement stmt =
					dbConn.prepareStatement(INSERT_IN_CONFIGURATION);
				stmt.setInt(1, nodeId);
				stmt.setTimestamp(2, currentTime);
				stmt.setTimestamp(3, currentTime);
				stmt.setString(4, inventoryType);
				stmt.setString(5, newPathToFile);
				stmt.execute();
			} catch (SQLException s) {
				log.error("Unable to insert in DB");
				log.error(s);
				saveMessage = "Unable to save "+inventoryType+" configuration.<br>";
				return InventoryMonitor.CONFIGURATION_NOT_SAVED;
			}
		}
		

			rs = null;
			found = -1;
			try {
				PreparedStatement stmt =
					dbConn.prepareStatement(COUNT_NODEID_IN_ASSET);
				stmt.setInt(1, nodeId);
				rs = stmt.executeQuery();
				while (rs.next()) {
					found = rs.getInt(1);
				}
			} catch (SQLException s) {
				log.error("Unable to write into DB");
				log.error(s);
				saveMessage = "Unable to save "+inventoryType+" configuration.<br>";
				return InventoryMonitor.CONFIGURATION_NOT_SAVED;
			}
			time = System.currentTimeMillis();
			currentTime = new Timestamp(time);
			
			// if this isn't the first asset inventory for this node 
			if (found > 0) {
				// prepare and execute the UPDATE in the asset table
				try {
					log.debug("Found " + found + " rows in table assets: UPDATE it");
					Iterator dbColumnIter = asset.keySet().iterator();
					String queryParam = "";
					int counter = 0;
					while(dbColumnIter.hasNext()){
						counter++;
						String currDbColumn = (String) dbColumnIter.next();
						String dataItem = (String) asset.get(currDbColumn);
						queryParam += currDbColumn+"='"+dataItem+"',";
						}
					if(counter>0){
							String updateAssets = "UPDATE assets SET  "+queryParam+" lastmodifieddate=? WHERE nodeID =?";
							PreparedStatement stmt = dbConn.prepareStatement(updateAssets);
							stmt.setTimestamp(1, currentTime);
							stmt.setInt(2, nodeId);
							log.debug("UPDATEQUERY"+updateAssets);
							stmt.executeUpdate();
						}
					} catch (SQLException s) {
							log.error("Unable to update DB" + s);
							saveMessage = "Unable to save "+inventoryType+" configuration.<br>";
							return InventoryMonitor.CONFIGURATION_NOT_SAVED;
					 }
					
			} else { //else
					// prepare and execute the INSERT in asset table
					try {
						log.debug("row not found. INSERT into assets");
						InsertIntoAssets(dbConn);
					} catch (SQLException s) {
						log.error("Unable to insert in DB");
						log.error(s);
						saveMessage = "Unable to save "+inventoryType+" configuration.<br>";
						return InventoryMonitor.CONFIGURATION_NOT_SAVED;
					}
			}

			try{
				dbConn.commit();
				}catch(SQLException sqle){
					log.error("Unable to save into DB");
					saveMessage = "Unable to save "+inventoryType+" configuration.<br>";
					return InventoryMonitor.CONFIGURATION_NOT_SAVED;
				}
			
			 //	writes on file
			 try {
				 boolean exists = (new File(directory_repository)).exists();
				 if (!exists) {
					 log.warn(
						 "file-repository '"
							 + directory_repository
							 + "' not found: trying to create it.");
					 // Create directory
					 boolean success = (new File(directory_repository)).mkdir();
					 if (!success) {
						 log.error("Directory creation failed");
						 try{
						 	dbConn.rollback();
						 }catch(SQLException s){
						 	log.error("Unable to rollback DB");
						 }
						 return InventoryMonitor.CONFIGURATION_NOT_SAVED;
					 }
					 log.warn(
						 "file-repository '" + directory_repository + "' created.");

				 }

				 exists = (new File(nodeDirectory_repository)).exists();
				 if (!exists) {
					 boolean success = (new File(nodeDirectory_repository)).mkdir();
					 if (!success) {
						 log.error(
							 "Node Directory '"
								 + nodeDirectory_repository
								 + "' creation failed.");
						 saveMessage =
							 "Unable to save "+inventoryType+" configuration.<br>";
						try{
						   dbConn.rollback();
						}catch(SQLException s){
						   log.error("Unable to rollback DB");
						}
						return InventoryMonitor.CONFIGURATION_NOT_SAVED;
					 }

				 }

				 exists = (new File(oldPathToFile)).exists();
				 log.debug(oldPathToFile+" exists="+exists);
				 if (!exists) {
					 if (renameCorruptedFile) {
						 log.warn(
							 "renameCorruptedFile=true and old configuration doesn't exist.");
					 }
					 returnValue = InventoryMonitor.FIRST_ACTIVE_CONFIGURATION_DOWNLOAD;
				 }

				 if (renameCorruptedFile) {
					 File f = new File(oldPathToFile);
					 String oldConfigurationFileDestination = oldPathToFile + "_Corrupted";
					 File dest = new File(oldConfigurationFileDestination);
					 dest.createNewFile();
					 f.renameTo(dest);
					 FileWriter fileout = new FileWriter(newPathToFile);
					 BufferedWriter filebufwri = new BufferedWriter(fileout);
					 PrintWriter printout = new PrintWriter(filebufwri);
					 printout.println(newInventory);
					 printout.close();
				 }else{
					 if(renameFile){
						File f = new File(oldPathToFile);
						File dest = new File(newPathToFile);
						dest.createNewFile();
						f.renameTo(dest);
					 }else{
						FileWriter fileout = new FileWriter(newPathToFile);
						BufferedWriter filebufwri = new BufferedWriter(fileout);
						PrintWriter printout = new PrintWriter(filebufwri);
						printout.println(newInventory);
						printout.close();
					 }
				 }
				
			 } catch (IOException ioex) {
				 try{
				    dbConn.rollback();
				 }catch(SQLException s){
				    log.error("Unable to rollback DB");
				 }
				 log.error("Failed writing to file '" + newPathToFile + "'.");
				 saveMessage = "Unable to save "+inventoryType+" configuration.<br>";
				 return InventoryMonitor.CONFIGURATION_NOT_SAVED;
			 }finally{
				 	try{
					   dbConn.close();
					}catch(SQLException s){
					   log.error("Unable to close connection to DB");
					}
			 	}
			saveMessage = "inventory "+inventoryType+" success.<br>";
			log.debug(""+returnValue); 
			return returnValue;
		}


		public String getSaveMessage() {
			return saveMessage;
		}

}
