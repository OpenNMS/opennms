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
package org.opennms.netmgt.config;

import java.util.*;
import java.util.Date;
import java.io.*;
import java.sql.*;

import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.netmgt.*;
import org.opennms.netmgt.config.assetLocation.*;
import org.opennms.netmgt.config.assetLocation.Header;

import org.opennms.core.resource.Vault;
import org.opennms.web.asset.*;

public class AssetLocationFactory {

	/**
	 * Singleton instance
	 */
	private static AssetLocationFactory instance;

	/**
	 * Object containing all Buildings and Room objects parsed from the xml file
	 */
	protected static AssetLocations m_assetLocations;

	/**
	 * Input stream for the general Asset Location configuration xml
	*/
	protected static InputStream configIn;

	/**
	 * Boolean indicating if the init() method has been called
	 */
	private static boolean initialized = false;

	/**
	 * The Asset Location Configuration File
	 */
	private static File m_assetLocationConfFile;

	/**
	 * A long Date Representing Asset Location Configuration File Last Modified Date
	*/
	private static long m_lastModified;

	/**
	 * Object Containig Header parsed from xml file
	 */
	private static Header oldHeader;

	private AssetLocationFactory() {
	}

	static synchronized public AssetLocationFactory getInstance() {
		if (!initialized)
			return null;

		if (instance == null) {
			instance = new AssetLocationFactory();
		}

		return instance;
	}

/**
 * 
 * @throws IOException
 * @throws FileNotFoundException
 * @throws MarshalException
 * @throws ValidationException
 * @throws ClassNotFoundException
 */

	public static synchronized void init()
		throws
			IOException,
			FileNotFoundException,
			MarshalException,
			ValidationException,
			ClassNotFoundException {
		if (!initialized) {
			reload();
			initialized = true;
		}
	}

	public static synchronized void reload()
		throws IOException, MarshalException, ValidationException {
		m_assetLocationConfFile =
			ExtendedConfigFileConstants.getFile(
			        ExtendedConfigFileConstants.ASSETLOCATION_CONF_FILE_NAME);
		InputStream configIn = new FileInputStream(m_assetLocationConfFile);
		m_lastModified = m_assetLocationConfFile.lastModified();

		m_assetLocations =
			(AssetLocations) Unmarshaller.unmarshal(
				AssetLocations.class,
				new InputStreamReader(configIn));
		oldHeader = m_assetLocations.getHeader();

	}

	/**
	 * 
	 * @return Hash Table containing  Object Building as function of Building Name
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */

	public Map getBuildings()
		throws IOException, MarshalException, ValidationException {

		updateFromFile();

		Map newMap = new HashMap();

		Building build[] = m_assetLocations.getBuilding();
		for (int i = 0; i < build.length; i++) {
			newMap.put(build[i].getName(), build[i]);
		}

		return newMap;
	}
	/**
	 * 
	 * @param String Building name
	 * @return Building Object
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */
	public Building getBuilding(String name)
		throws IOException, MarshalException, ValidationException {

		updateFromFile();

		return (Building) getBuildings().get(name);
	}

	/**
	 * 
	 * @param building
	 * @return int The integer identifier of the Building Object in m_assetLocation 
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */
	public int getBuildingInt(String building)
		throws IOException, MarshalException, ValidationException {

		updateFromFile();
		int i = 0;
		Building build[] = m_assetLocations.getBuilding();
		for (i = 0; i < build.length; i++) {
			if (build[i].getName().equals(building)) {
				break;
			}
		}
		if (i == build.length)
/*			return (int) - 1;
		return (int) i;*/
		    return -1;
		return i;

	}

	/**
	 * 
	 * @param newBuild
	 * @return Hash Table containing Building's Room Object as function of RoomId
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */
	public Map getRooms(Building newBuild)
		throws IOException, MarshalException, ValidationException {

		updateFromFile();

		Map newMap = new HashMap();
		Room room[] = newBuild.getRoom();

		for (int i = 0; i < room.length; i++) {
			newMap.put(room[i].getRoomID(), room[i]);
		}

		return newMap;
	}

	/**
	 * 
	 * @param building
	 * @param room
	 * @return Object Room
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */
	public Room getRoom(String building, String room)
		throws IOException, MarshalException, ValidationException {

		updateFromFile();
		Building Build = getBuilding(building);
		return (Room) getRooms(Build).get(room);
	}

	/**
	 * 
	 * @param building
	 * @param room
	 * @return int The integer identifier of the Room Object in m_assetLocation 
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */
	public int getRoomInt(String building, String room)
		throws IOException, MarshalException, ValidationException {

		updateFromFile();
		int i = 0;
		Room lroom[] =
			m_assetLocations.getBuilding(getBuildingInt(building)).getRoom();
		for (i = 0; i < lroom.length; i++) {
			if (lroom[i].getRoomID().equals(room)) {
				break;
			}
		}
		if (i == lroom.length)
/*			return (int) - 1;
		return (int) i;*/
		    return -1;
		return i;
	}

	public Map getAssetNode(String building, String room) throws SQLException {

		AssetModel model;

		model = new AssetModel();

		Map nodeMap = new HashMap();
		String column = "Room";
		AssetModel.MatchingAsset[] assets =
			AssetModel.searchAssets(column, room);
		if (assets.length > 0) {
			for (int i = 0; i < assets.length; i++) {
				Asset asset = model.getAsset(assets[i].nodeId);

				if (asset.getBuilding().equals(building)
					|| building.equals(""))
					nodeMap.put(String.valueOf(asset.getNodeId()), asset);
			}
		} else if ( room.equals("") ) {
			Asset[] assetarray = model.getAllAssets();
			for (int i = 0; i < assetarray.length; i++) {
				if (assetarray[i].getBuilding().equals(building)
					|| building.equals(""))
					nodeMap.put(
						String.valueOf(assetarray[i].getNodeId()),
						assetarray[i]);

			}
		}
		return nodeMap;
	}
	/**
	 * removeBuilding remove a Building from Asset Location xml Configuration File
	 * also remove asset info from db table assets
	 * @param building
	 * @param userLastModified
	 * @throws MarshalException
	 * @throws SQLException
	 * @throws ValidationException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public synchronized void removeBuilding(
		String building,
		String userLastModified)
		throws
			MarshalException,
			SQLException,
			ValidationException,
			IOException,
			ClassNotFoundException {

		//m_assetLocations.removeBuilding(getBuildingInt(building));
	    m_assetLocations.removeBuilding(getBuilding(building));

		if (building == null || userLastModified == null) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}

		Connection conn = Vault.getDbConnection();

		try {
			PreparedStatement stmt =
				conn.prepareStatement(
					"UPDATE ASSETS SET address1=?,address2=?,city=?,state=?,zip=?,building=?,room=?,floor=?,userLastModified=?,lastModifiedDate=? WHERE building=?");
			stmt.setString(1, "");
			stmt.setString(2, "");
			stmt.setString(3, "");
			stmt.setString(4, "");
			stmt.setString(5, "");
			stmt.setString(6, "");
			stmt.setString(7, "");
			stmt.setString(8, "");
			stmt.setString(9, userLastModified);
			stmt.setTimestamp(10, (new Timestamp(System.currentTimeMillis())));
			stmt.setString(11, building);

			stmt.execute();
			stmt.close();
		} finally {
			Vault.releaseDbConnection(conn);
		}
		saveCurrent();
	}

	/**
	 * addBuilding add a new Building to Asset Location xml configuration file
	 * @param build
	 * @throws MarshalException
	 * @throws ValidationException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */

	public synchronized void addBuilding(Building build)
		throws
			MarshalException,
			ValidationException,
			IOException,
			ClassNotFoundException {
		while (getBuildingInt(build.getName()) != -1) {
			//m_assetLocations.removeBuilding(getBuildingInt(build.getName()));
		    m_assetLocations.removeBuilding(getBuilding(build.getName()));
		}

		m_assetLocations.addBuilding(build);
		saveCurrent();
	}

	/**
	 * replaceBuilding replace Building Info in Asset Location xml Configuration File
	 * also repalce asset info in db table assets  
	 * @param oldBuildName
	 * @param newBuild
	 * @param userLastModified
	 * @throws MarshalException
	 * @throws SQLException
	 * @throws ValidationException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public synchronized void replaceBuilding(
		String oldBuildName,
		Building newBuild,
		String userLastModified)
		throws
			MarshalException,
			SQLException,
			ValidationException,
			IOException,
			ClassNotFoundException {

		Building oldBuild = getBuilding(oldBuildName);

		if (oldBuild != null)
			while (getBuildingInt(oldBuildName) != -1) {
				//m_assetLocations.removeBuilding(getBuildingInt(oldBuildName));
			    m_assetLocations.removeBuilding(getBuilding(oldBuildName));
			}

		m_assetLocations.addBuilding(newBuild);

		if (newBuild == null
			|| oldBuildName == null
			|| userLastModified == null) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}

		Connection conn = Vault.getDbConnection();

		try {
			PreparedStatement stmt =
				conn.prepareStatement(
					"UPDATE ASSETS SET address1=?,address2=?,city=?,state=?,zip=?,building=?,userLastModified=?,lastModifiedDate=? WHERE building=?");
			stmt.setString(1, newBuild.getAddress1());
			stmt.setString(2, newBuild.getAddress2());
			stmt.setString(3, newBuild.getCity());
			stmt.setString(4, newBuild.getState());
			stmt.setString(5, newBuild.getZIP());
			stmt.setString(6, newBuild.getName());
			stmt.setString(7, userLastModified);
			stmt.setTimestamp(8, (new Timestamp(System.currentTimeMillis())));
			stmt.setString(9, oldBuildName);

			stmt.execute();
			stmt.close();
		} finally {
			Vault.releaseDbConnection(conn);
		}

		saveCurrent();
	}

	/**
	 * 
	 * @param building
	 * @param room
	 * @param userLastModified
	 * @throws MarshalException
	 * @throws SQLException
	 * @throws ValidationException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public synchronized void removeRoom(
		String building,
		String room,
		String userLastModified)
		throws
			MarshalException,
			SQLException,
			ValidationException,
			IOException,
			ClassNotFoundException {

		//m_assetLocations.getBuilding(getBuildingInt(building)).removeRoom(
		//	getRoomInt(building, room));
		m_assetLocations.getBuilding(getBuildingInt(building)).removeRoom(
			getRoom(building, room));

		if (building == null || room == null || userLastModified == null) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}

		Connection conn = Vault.getDbConnection();

		try {
			PreparedStatement stmt =
				conn.prepareStatement(
					"UPDATE ASSETS SET floor=?,room=?,userLastModified=?,lastModifiedDate=? WHERE building=? AND room=?");
			stmt.setString(1, "");
			stmt.setString(2, "");
			stmt.setString(3, userLastModified);
			stmt.setTimestamp(4, (new Timestamp(System.currentTimeMillis())));
			stmt.setString(5, building);
			stmt.setString(6, room);

			stmt.execute();
			stmt.close();
		} finally {
			Vault.releaseDbConnection(conn);
		}
		saveCurrent();
	}

	/**
	 * 
	 * @param room
	 * @param building
	 * @throws MarshalException
	 * @throws ValidationException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public synchronized void addRoom(Room room, String building)
		throws
			MarshalException,
			ValidationException,
			IOException,
			ClassNotFoundException {
		while (getRoomInt(building, room.getRoomID()) != -1) {
			//m_assetLocations.getBuilding(getBuildingInt(building)).removeRoom(
			//	getRoomInt(building, room.getRoomID()));
			m_assetLocations.getBuilding(getBuildingInt(building)).removeRoom(
				getRoom(building, room.getRoomID()));
		}

		m_assetLocations.getBuilding(getBuildingInt(building)).addRoom(room);
		saveCurrent();
	}

	/**
	 * 
	 * @param oldRoomName
	 * @param room
	 * @param building
	 * @param userLastModified
	 * @throws MarshalException
	 * @throws SQLException
	 * @throws ValidationException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */

	public synchronized void replaceRoom(
		String oldRoomName,
		Room room,
		String building,
		String userLastModified)
		throws
			MarshalException,
			SQLException,
			ValidationException,
			IOException,
			ClassNotFoundException {

		Room oldRoom = getRoom(building, oldRoomName);

		if (oldRoom != null)
			while (getRoomInt(building, oldRoomName) != -1) {
				/*m_assetLocations.getBuilding(
					getBuildingInt(building)).removeRoom(
					getRoomInt(building, oldRoomName));*/
				m_assetLocations.getBuilding(
				getBuildingInt(building)).removeRoom(
				getRoom(building, oldRoomName));
			}

		m_assetLocations.getBuilding(getBuildingInt(building)).addRoom(room);

		if (room == null
			|| oldRoomName == null
			|| building == null
			|| userLastModified == null) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}

		Connection conn = Vault.getDbConnection();

		try {
			PreparedStatement stmt =
				conn.prepareStatement(
					"UPDATE ASSETS SET floor=?,room=?,userLastModified=?,lastModifiedDate=? WHERE building=? AND room=?");
			stmt.setString(1, room.getFloor());
			stmt.setString(2, room.getRoomID());
			stmt.setString(3, userLastModified);
			stmt.setTimestamp(4, (new Timestamp(System.currentTimeMillis())));
			stmt.setString(5, building);
			stmt.setString(6, oldRoomName);

			stmt.execute();
			stmt.close();
		} finally {
			Vault.releaseDbConnection(conn);
		}
		saveCurrent();
	}

	/**
	 * 
	 * @param asset
	 * @throws SQLException
	 */

	public void createAsset(Asset asset) throws SQLException {
		if (asset == null) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}

		Connection conn = Vault.getDbConnection();

		try {
			PreparedStatement stmt =
				conn.prepareStatement(
					"INSERT INTO ASSETS (nodeID,category,rack,slot,port,address1,address2,city,state,zip,building,floor,room,circuitid,userLastModified,lastModifiedDate) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			stmt.setInt(1, asset.getNodeId());
			stmt.setString(2, asset.getCategory());
			stmt.setString(3, asset.getRack());
			stmt.setString(4, asset.getSlot());
			stmt.setString(5, asset.getPort());
			stmt.setString(6, asset.getAddress1());
			stmt.setString(7, asset.getAddress2());
			stmt.setString(8, asset.getCity());
			stmt.setString(9, asset.getState());
			stmt.setString(10, asset.getZip());
			stmt.setString(11, asset.getBuilding());
			stmt.setString(12, asset.getFloor());
			stmt.setString(13, asset.getRoom());
			stmt.setString(14, asset.getCircuitId());
			stmt.setString(15, asset.getUserLastModified());
			stmt.setTimestamp(
				16,
				(new Timestamp(asset.getLastModifiedDate().getTime())));

			stmt.execute();
			stmt.close();
		} finally {
			Vault.releaseDbConnection(conn);
		}
	}

	/**
	 * 
	 * @param asset
	 * @throws SQLException
	 */
	public void modifyAsset(Asset asset) throws SQLException {
		if (asset == null) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}

		Connection conn = Vault.getDbConnection();

		try {
			PreparedStatement stmt =
				conn.prepareStatement(
					"UPDATE ASSETS SET category=?,rack=?,slot=?,port=?,address1=?,address2=?,city=?,state=?,zip=?,building=?,floor=?,room=?,circuitId=?,userLastModified=?,lastModifiedDate=? WHERE nodeid=?");
			stmt.setString(1, asset.getCategory());
			stmt.setString(2, asset.getRack());
			stmt.setString(3, asset.getSlot());
			stmt.setString(4, asset.getPort());
			stmt.setString(5, asset.getAddress1());
			stmt.setString(6, asset.getAddress2());
			stmt.setString(7, asset.getCity());
			stmt.setString(8, asset.getState());
			stmt.setString(9, asset.getZip());
			stmt.setString(10, asset.getBuilding());
			stmt.setString(11, asset.getFloor());
			stmt.setString(12, asset.getRoom());
			stmt.setString(13, asset.getCircuitId());
			stmt.setString(14, asset.getUserLastModified());
			stmt.setTimestamp(
				15,
				(new Timestamp(asset.getLastModifiedDate().getTime())));
			stmt.setInt(16, asset.getNodeId());

			stmt.execute();
			stmt.close();
		} finally {
			Vault.releaseDbConnection(conn);
		}
	}

	public synchronized void saveCurrent()
		throws
			MarshalException,
			ValidationException,
			IOException,
			ClassNotFoundException {

		m_assetLocations.setHeader(rebuildHeader());

		//marshall to a string first, then write the string to the file. This way the original config
		//isn't lost if the xml from the marshall is hosed.
		StringWriter stringWriter = new StringWriter();
		Marshaller.marshal(m_assetLocations, stringWriter);
		if (stringWriter.toString() != null) {
			FileWriter fileWriter = new FileWriter(m_assetLocationConfFile);
			fileWriter.write(stringWriter.toString());
			fileWriter.flush();
			fileWriter.close();
		}

		reload();
	}

	private Header rebuildHeader() {
		Header header = oldHeader;

		header.setCreated(ExtendedEventConstants.formatToString(new Date()));

		return header;
	}

	private static void updateFromFile()
		throws IOException, MarshalException, ValidationException {
		if (m_lastModified != m_assetLocationConfFile.lastModified()) {
			reload();
		}
	}
}
