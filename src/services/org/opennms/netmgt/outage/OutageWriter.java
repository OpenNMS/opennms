//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//
//
// Tab Size = 8
//

package org.opennms.netmgt.outage;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.ParseException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.MarshalException;

import org.opennms.netmgt.config.OutageManagerConfigFactory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;

// castor generated classes
import org.opennms.netmgt.xml.event.*;

/**
 * <p>When a 'nodeLostService' is received, it is made sure that there is no
 * 'open' outage record in the 'outages' table for this nodeid/ipaddr/serviceid
 * - i.e that there is not already a record for this n/i/s where the 'lostService'
 * time is known and the 'regainedService' time is NULL - if there is, the
 * current 'lostService' event is ignored else a new outage is created</p>
 *
 * <p>The 'interfaceDown' is similar to the 'nodeLostService' except that it acts
 * relevant to a nodeid/ipaddr combination and a 'nodeDown' acts on a nodeid</p>
 *
 * <p>When a 'nodeRegainedService' is received and there is an 'open' outage for
 * the nodeid/ipaddr/serviceid, the outage is cleared</p>
 *
 * <p>The 'interfaceUp' is similar to the 'nodeRegainedService' except that it acts
 * relevant to a nodeid/ipaddr combination and a 'nodeUp' acts on a nodeid</p>
 *
 * <p>When a 'deleteService' is received, the appropriate entry is marked for
 * deletion is the 'ifservices' table - if this entry is the only entry for
 * a node/ip combination, the corresponding entry in the 'ipinterface' table
 * is marked for deletion and this is then cascaded to the node table
 * All deletions are followed by an appropriate event(serviceDeleted or
 * interfaceDeleted or..) being generated and sent to eventd</p>
 *
 * <p> When an 'interfaceReparented' event is received, 'outages' table entries
 * associated with the old nodeid/interface pairing are changed so that those outage
 * entries will be associated with the new nodeid/interface pairing.</p>
 *
 * <p>The nodeLostService, interfaceDown, nodeDown, nodeUp, interfaceUp,
 * nodeRegainedService, deleteService events update the svcLostEventID and the 
 * svcRegainedEventID fields as approppriate. The interfaceReparented event has
 * no impact on these eventid reference fields</p>
 *
 * @author 	<A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj</A>
 * @author 	<A HREF="mailto:mike@opennms.org">Mike Davidson</A>
 * @author	<A HREF="http://www.opennms.org">OpenNMS.org</A>
 */
public final class OutageWriter implements Runnable
{
	private static final String	SNMP_SVC	= "SNMP";
	private static final String	SNMPV2_SVC	= "SNMPv2";

	/**
	 * The event from which data is to be read
	 */
	private Event			m_event;

	// Control whether or not an event is generated following 
	// database modifications to notify other OpenNMS processes
	private boolean			m_generateNodeDeletedEvent;
	
	/**
	 * A class to hold SNMP/SNMPv2 entries for a node from the ifservices table
	 * A list of this class is maintained on SNMP delete so as to be able to
	 * generate a series of serviceDeleted for all entries marked as 'D'
	 */
	private static class IfSvcSnmpEntry
	{
		private long		m_nodeID;
		private String		m_ipAddr;
		private String		m_svcName;

		IfSvcSnmpEntry(long nodeid, String ip, String sname)
		{
			m_nodeID = nodeid;
			m_ipAddr = ip;
			m_svcName = sname;
		}

		long getNodeID()
		{
			return m_nodeID;
		}

		String getIP()
		{
			return m_ipAddr;
		}

		String getService()
		{
			return m_svcName;
		}
	}

	/**
	 * <P>This method is used to convert the service name into
	 * a service id. It first looks up the information from a
	 * service map in OutagesManager and if no match is found, by performing
	 * a lookup in the database. If the conversion is successful then the
	 * corresponding integer identifier will be returned to the caller.</P>
	 *
	 * @param name		The name of the service
	 *
	 * @return The integer identifier for the service name.
	 *
	 * @exception java.sql.SQLException Thrown if there is an error accessing
	 * 	the stored data or the SQL text is malformed. This will also
	 * 	be thrown if the result cannot be obtained.
	 *
	 * @see org.opennms.netmgt.outage.OutageConstants.DB_GET_SVC_ID
	 */
	private long getServiceID(String name)
		throws SQLException
	{
		//
		// Check the name to make sure that it is not null
		//
		if(name == null)
			throw new NullPointerException("The service name was null");

		// ask OutageManager
		//
		long id = OutageManager.getInstance().getServiceID(name);
		if (id != -1)
			return id;

		//
		// talk to the database and get the identifer
		//
		Connection dbConn = null;
		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();

	 		// SQL statement to get service id for a servicename from the service table
			PreparedStatement serviceStmt  = dbConn.prepareStatement(OutageConstants.DB_GET_SVC_ID);

			serviceStmt.setString(1, name);
			ResultSet rset = serviceStmt.executeQuery();
			if (rset.next())
			{
				id = rset.getLong(1);
			}

			// close result set
			rset.close();

			// close statement
			if (serviceStmt != null)
				serviceStmt.close();
		}
		finally
		{
			try
			{
				if(dbConn != null)
					dbConn.close();
			}
			catch(SQLException e)
			{
				ThreadCategory.getInstance(getClass()).warn("Exception closing JDBC connection", e);
			}
		}

		// Record the new find
		//
		if (id != -1)
			OutageManager.getInstance().addServiceMapping(name, id);

		//
		// return the id to the caller
		//
		return id;
	}

	/**
	 * <p>Record the 'nodeLostService' event in the outages table - create
	 * a new outage entry if the service is not already down</p>
	 */
	private void handleNodeLostService(long eventID, long nodeID, String ipAddr, long serviceID, String eventTime)
	{
		Category log = ThreadCategory.getInstance(OutageWriter.class);

		if(eventID == -1 || nodeID == -1 || ipAddr == null || serviceID == -1)
		{
			log.warn(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI + " ignored - info incomplete - eventid/nodeid/ip/svc: " + eventID + "/" + nodeID + "/" + ipAddr + "/" + serviceID);
			return;
		}

		int numOpenRecs = -1;

		// check that there is no 'open' entry already
		Connection dbConn = null;
		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();


	 		// Prepare SQL statement used to see if there is already an
			// 'open' record for the node/ip/svc combination
			PreparedStatement openStmt = dbConn.prepareStatement(OutageConstants.DB_OPEN_RECORD);
			openStmt.setLong  (1, nodeID);
			openStmt.setString(2, ipAddr);
			openStmt.setLong  (3, serviceID);
			
			ResultSet rs = openStmt.executeQuery();
			if(rs.next())
			{
				numOpenRecs = rs.getInt(1);
			}

			// close result set
			rs.close();

			// close statement
			openStmt.close();

			if (numOpenRecs > 0)
			{
				log.warn("\'" + EventConstants.NODE_LOST_SERVICE_EVENT_UEI + "\' for " + nodeID + "/" + ipAddr + "/" + serviceID + " ignored - table already  has an open record ");

			}
			else
			{
	 			// Prepare SQL statement to get the next outage id from the db sequence
				PreparedStatement getNextOutageIdStmt  = dbConn.prepareStatement(OutageManagerConfigFactory.getInstance().getGetNextOutageID());

				long outageID = -1;

				// Execute the statement to get the next outage id from the sequence
				//
				ResultSet seqRS = getNextOutageIdStmt.executeQuery();
				if (seqRS.next())
				{
					outageID = seqRS.getLong(1);
				}
				seqRS.close();

				// Set the database commit mode
				try
				{
					dbConn.setAutoCommit(false);
				}
				catch (SQLException sqle)
				{
					log.error("Unable to change database AutoCommit to FALSE", sqle);
					return;
				}
			
				// get timestamp
				java.sql.Timestamp eventTimeTS = null;
				try
				{
					java.util.Date date = EventConstants.parseToDate(eventTime);
					eventTimeTS = new java.sql.Timestamp(date.getTime());
				}
				catch(ParseException pe)
				{
					log.warn("Failed to convert time " + eventTime + " to java.sql.Timestamp, Setting current time instead", pe);

					eventTimeTS = new java.sql.Timestamp((new java.util.Date()).getTime());
				}

				// Prepare statement to insert a new outage table entry
				PreparedStatement newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_NEW_OUTAGE);
				newOutageWriter.setLong  (1, outageID);
				newOutageWriter.setLong  (2, eventID);
				newOutageWriter.setLong  (3, nodeID);
				newOutageWriter.setString(4, ipAddr);
				newOutageWriter.setLong  (5, serviceID);
				newOutageWriter.setTimestamp(6, eventTimeTS);

				// execute
				newOutageWriter.executeUpdate();

				// close statement
				newOutageWriter.close();

				// commit work
				try
				{
					dbConn.commit();

					if (log.isDebugEnabled())
						log.debug("nodeLostService : " + nodeID + "/" + ipAddr + "/" + serviceID + " recorded in DB");
				}
				catch(SQLException se)
				{
					log.warn("Rolling back transaction, nodeLostService could not be recorded  for nodeid/ipAddr/service: " + nodeID + "/" + ipAddr + "/" + serviceID, se);


					try 
					{
						dbConn.rollback();
					}
					catch (SQLException sqle)
					{
						log.warn("SQL exception during rollback, reason", sqle);
					}

				}

			}
		}
		catch (SQLException sqle)
		{
			log.warn("SQL exception while handling \'nodeLostService\'", sqle);
		}
		finally
		{
			// close database connection
			try
			{
				if(dbConn != null)
					dbConn.close();
			}
			catch(SQLException e)
			{
				log.warn("Exception closing JDBC connection", e);
			}
		}

	}

	/**
	 * <p>Record the 'interfaceDown' event in the outages table - create
	 * a new outage entry for each active service of the nodeid/ip if
	 * service not already down</p>
	 */
	private void handleInterfaceDown(long eventID, long nodeID, String ipAddr, String eventTime)
	{
		Category log = ThreadCategory.getInstance(OutageWriter.class);

		if(eventID == -1 || nodeID == -1 || ipAddr == null)
		{
			log.warn(EventConstants.INTERFACE_DOWN_EVENT_UEI + " ignored - info incomplete - eventid/nodeid/ip: " + eventID + "/" + nodeID + "/" + ipAddr);
			return;
		}


		Connection dbConn = null;
		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();

			// Set the database commit mode
			try
			{
				dbConn.setAutoCommit(false);
			}
			catch (SQLException sqle)
			{
				log.error("Unable to change database AutoCommit to FALSE", sqle);
				return;
			}

	 		// Prepare SQL statement used to get active services for the nodeid/ip
			PreparedStatement activeSvcsStmt = dbConn.prepareStatement(OutageConstants.DB_GET_ACTIVE_SERVICES_FOR_INTERFACE);

	 		// Prepare SQL statement used to see if there is already an
			// 'open' record for the node/ip/svc combination
			PreparedStatement openStmt = dbConn.prepareStatement(OutageConstants.DB_OPEN_RECORD);
			// Prepare statement to insert a new outage table entry
			PreparedStatement newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_NEW_OUTAGE);

	 		// Prepare SQL statement to get the next outage id from the db sequence
			PreparedStatement getNextOutageIdStmt  = dbConn.prepareStatement(OutageManagerConfigFactory.getInstance().getGetNextOutageID());

			// Get all active services for the nodeid/ip
			activeSvcsStmt.setLong  (1, nodeID);
			activeSvcsStmt.setString(2, ipAddr);
			ResultSet activeSvcsRS = activeSvcsStmt.executeQuery();
			while(activeSvcsRS.next())
			{
				long serviceID = activeSvcsRS.getLong(1);

				int numOpenRecs = -1;

				// Check if this service is already down
				openStmt.setLong  (1, nodeID);
				openStmt.setString(2, ipAddr);
				openStmt.setLong  (3, serviceID);
				
				ResultSet openOutageRS = openStmt.executeQuery();
				if(openOutageRS.next())
				{
					numOpenRecs = openOutageRS.getInt(1);
				}
				// close result set
				openOutageRS.close();

				if (numOpenRecs > 0)
				{
					if (log.isDebugEnabled())
						log.debug(nodeID + "/" + ipAddr + "/" + serviceID + " already down");
				}
				else
				{

					long outageID = -1;

					// Execute the statement to get the next outage id from the sequence
					//
					ResultSet seqRS = getNextOutageIdStmt.executeQuery();
					if (seqRS.next())
					{
						outageID = seqRS.getLong(1);
					}
					seqRS.close();

					// get timestamp
					java.sql.Timestamp eventTimeTS = null;
					try
					{
						java.util.Date date = EventConstants.parseToDate(eventTime);
						eventTimeTS = new java.sql.Timestamp(date.getTime());
					}
					catch(ParseException pe)
					{
						log.warn("Failed to convert time " + eventTime + " to java.sql.Timestamp, Setting current time instead", pe);

						eventTimeTS = new java.sql.Timestamp((new java.util.Date()).getTime());
					}

					newOutageWriter.setLong  (1, outageID);
					newOutageWriter.setLong  (2, eventID);
					newOutageWriter.setLong  (3, nodeID);
					newOutageWriter.setString(4, ipAddr);
					newOutageWriter.setLong  (5, serviceID);
					newOutageWriter.setTimestamp(6, eventTimeTS);

					// execute update
					newOutageWriter.executeUpdate();

					if (log.isDebugEnabled())
						log.debug("Recording outage for " + nodeID + "/" + ipAddr + "/" + serviceID);
				}

			}
			// close result set
			activeSvcsRS.close();

			// commit work
			try
			{
				dbConn.commit();

				if (log.isDebugEnabled())
					log.debug("Outage recorded for all active services for " + nodeID + "/" + ipAddr);
			}
			catch(SQLException se)
			{
				log.warn("Rolling back transaction, interfaceDown could not be recorded  for nodeid/ipAddr: " + nodeID + "/" + ipAddr, se);


				try 
				{
					dbConn.rollback();
				}
				catch (SQLException sqle)
				{
					log.warn("SQL exception during rollback, reason", sqle);
				}

			}

			// close statements
			activeSvcsStmt.close();
			openStmt.close();
			newOutageWriter.close();
		}
		catch (SQLException sqle)
		{
			log.warn("SQL exception while handling \'interfaceDown\'", sqle);
		}
		finally
		{
			try
			{
				if(dbConn != null)
					dbConn.close();
			}
			catch(SQLException e)
			{
				log.warn("Exception closing JDBC connection", e);
			}
		}
	}

	/**
	 * <p>Record the 'nodeDown' event in the outages table - create a new
	 * outage entry for each active service of the nodeid if service is
	 * not already down</p>
	 */
	private void handleNodeDown(long eventID, long nodeID, String eventTime)
	{
		Category log = ThreadCategory.getInstance(OutageWriter.class);

		if(eventID == -1 || nodeID == -1)
		{
			log.warn(EventConstants.NODE_DOWN_EVENT_UEI + " ignored - info incomplete - eventid/nodeid: " + eventID + "/" + nodeID);
			return;
		}


		Connection dbConn = null;
		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();

			// Set the database commit mode
			try
			{
				dbConn.setAutoCommit(false);
			}
			catch (SQLException sqle)
			{
				log.error("Unable to change database AutoCommit to FALSE", sqle);
				return;
			}

	 		// Prepare SQL statement used to get active services for the nodeid
			PreparedStatement activeSvcsStmt = dbConn.prepareStatement(OutageConstants.DB_GET_ACTIVE_SERVICES_FOR_NODE);

	 		// Prepare SQL statement used to see if there is already an
			// 'open' record for the node/ip/svc combination
			PreparedStatement openStmt = dbConn.prepareStatement(OutageConstants.DB_OPEN_RECORD);
			// Prepare statement to insert a new outage table entry
			PreparedStatement newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_NEW_OUTAGE);

	 		// Prepare SQL statement to get the next outage id from the db sequence
			PreparedStatement getNextOutageIdStmt  = dbConn.prepareStatement(OutageManagerConfigFactory.getInstance().getGetNextOutageID());

			// Get all active services for the nodeid
			activeSvcsStmt.setLong  (1, nodeID);
			ResultSet activeSvcsRS = activeSvcsStmt.executeQuery();
			while(activeSvcsRS.next())
			{
				String ipAddr = activeSvcsRS.getString(1);
				long serviceID = activeSvcsRS.getLong(2);

				int numOpenRecs = -1;

				// Check if this service is already down
				openStmt.setLong  (1, nodeID);
				openStmt.setString(2, ipAddr);
				openStmt.setLong  (3, serviceID);
				
				ResultSet openOutageRS = openStmt.executeQuery();
				if(openOutageRS.next())
				{
					numOpenRecs = openOutageRS.getInt(1);
				}
				// close result set
				openOutageRS.close();

				if (numOpenRecs > 0)
				{
					if (log.isDebugEnabled())
						log.debug(nodeID + "/" + ipAddr + "/" + serviceID + " already down");
				}
				else
				{
					long outageID = -1;

					// Execute the statement to get the next outage id from the sequence
					//
					ResultSet seqRS = getNextOutageIdStmt.executeQuery();
					if (seqRS.next())
					{
						outageID = seqRS.getLong(1);
					}
					seqRS.close();

					// get timestamp
					java.sql.Timestamp eventTimeTS = null;
					try
					{
						java.util.Date date = EventConstants.parseToDate(eventTime);
						eventTimeTS = new java.sql.Timestamp(date.getTime());
					}
					catch(ParseException pe)
					{
						log.warn("Failed to convert time " + eventTime + " to java.sql.Timestamp, Setting current time instead", pe);

						eventTimeTS = new java.sql.Timestamp((new java.util.Date()).getTime());
					}

					// set parms
					newOutageWriter.setLong  (1, outageID);
					newOutageWriter.setLong  (2, eventID);
					newOutageWriter.setLong  (3, nodeID);
					newOutageWriter.setString(4, ipAddr);
					newOutageWriter.setLong  (5, serviceID);
					newOutageWriter.setTimestamp(6, eventTimeTS);

					// execute update
					newOutageWriter.executeUpdate();

					if (log.isDebugEnabled())
						log.debug("Recording outage for " + nodeID + "/" + ipAddr + "/" + serviceID);
				}

			}
			// close result set
			activeSvcsRS.close();

			// commit work
			try
			{
				dbConn.commit();

				if (log.isDebugEnabled())
					log.debug("Outage recorded for all active services for " + nodeID);
			}
			catch(SQLException se)
			{
				log.warn("Rolling back transaction, nodeDown could not be recorded  for nodeId: " + nodeID, se);

				try 
				{
					dbConn.rollback();
				}
				catch (SQLException sqle)
				{
					log.warn("SQL exception during rollback, reason", sqle);
				}

			}

			// close statements
			activeSvcsStmt.close();
			openStmt.close();
			newOutageWriter.close();
		}
		catch (SQLException sqle)
		{
			log.warn("SQL exception while handling \'nodeDown\'", sqle);
		}
		finally
		{
			try
			{
				if(dbConn != null)
					dbConn.close();
			}
			catch(SQLException e)
			{
				log.warn("Exception closing JDBC connection", e);
			}
		}
	}

	/**
	 * <p>Record the 'nodeUp' event in the outages table - close all open
	 * outage entries for the nodeid in the outages table</p>
	 */
	private void handleNodeUp(long eventID, long nodeID, String eventTime)
	{
		Category log = ThreadCategory.getInstance(OutageWriter.class);

		if(eventID == -1 || nodeID == -1 )
		{
			log.warn(EventConstants.NODE_UP_EVENT_UEI + " ignored - info incomplete - eventid/nodeid: " + eventID + "/" + nodeID);
			return;
		}

		Connection dbConn = null;
		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();

			// Set the database commit mode
			try
			{
				dbConn.setAutoCommit(false);
			}
			catch (SQLException sqle)
			{
				log.error("Unable to change database AutoCommit to FALSE", sqle);
				return;
			}

			// get timestamp
			java.sql.Timestamp eventTimeTS = null;
			try
			{
				java.util.Date date = EventConstants.parseToDate(eventTime);
				eventTimeTS = new java.sql.Timestamp(date.getTime());
			}
			catch(ParseException pe)
			{
				log.warn("Failed to convert time " + eventTime + " to java.sql.Timestamp, Setting current time instead", pe);

				eventTimeTS = new java.sql.Timestamp((new java.util.Date()).getTime());
			}

	 		// Prepare SQL statement used to update the 'regained time' for
			// all open outage entries for the nodeid
			PreparedStatement outageUpdater = dbConn.prepareStatement(OutageConstants.DB_UPDATE_OUTAGES_FOR_NODE);
			outageUpdater.setLong  (1, eventID); 
			outageUpdater.setTimestamp(2, eventTimeTS); 
			outageUpdater.setLong  (3, nodeID);
			int count = outageUpdater.executeUpdate();

			// close statement
			outageUpdater.close();

			// commit work
			try
			{
				dbConn.commit();

				if (log.isDebugEnabled())
					log.debug("nodeUp closed " +  count + " outages for nodeid " + nodeID + " in DB");
			}
			catch(SQLException se)
			{
				log.warn("Rolling back transaction, nodeUp could not be recorded  for nodeId: " + nodeID, se);

				try 
				{
					dbConn.rollback();
				}
				catch (SQLException sqle)
				{
					log.warn("SQL exception during rollback, reason", sqle);
				}

			}
		
		}
		catch(SQLException se)
		{
			log.warn("SQL exception while handling \'nodeRegainedService\'", se);
		}
		finally
		{
			try
			{
				if(dbConn != null)
					dbConn.close();
			}
			catch(SQLException e)
			{
				log.warn("Exception closing JDBC connection", e);
			}
		}
	}

	/**
	 * <p>Record the 'interfaceUp' event in the outages table - close all open
	 * outage entries for the nodeid/ip in the outages table</p>
	 */
	private void handleInterfaceUp(long eventID, long nodeID, String ipAddr, String eventTime)
	{
		Category log = ThreadCategory.getInstance(OutageWriter.class);

		if(eventID == -1 || nodeID == -1 || ipAddr == null)
		{
			log.warn(EventConstants.INTERFACE_UP_EVENT_UEI + " ignored - info incomplete - eventid/nodeid/ipAddr: " + eventID + "/" + nodeID + "/" + ipAddr);
			return;
		}

		Connection dbConn = null;
		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();

			// Set the database commit mode
			try
			{
				dbConn.setAutoCommit(false);
			}
			catch (SQLException sqle)
			{
				log.error("Unable to change database AutoCommit to FALSE", sqle);
				return;
			}

			// get timestamp
			java.sql.Timestamp eventTimeTS = null;
			try
			{
				java.util.Date date = EventConstants.parseToDate(eventTime);
				eventTimeTS = new java.sql.Timestamp(date.getTime());
			}
			catch(ParseException pe)
			{
				log.warn("Failed to convert time " + eventTime + " to java.sql.Timestamp, Setting current time instead", pe);

				eventTimeTS = new java.sql.Timestamp((new java.util.Date()).getTime());
			}

	 		// Prepare SQL statement used to update the 'regained time' for
			// all open outage entries for the nodeid/ipaddr
			PreparedStatement outageUpdater = dbConn.prepareStatement(OutageConstants.DB_UPDATE_OUTAGES_FOR_INTERFACE);
			outageUpdater.setLong  (1, eventID); 
			outageUpdater.setTimestamp(2, eventTimeTS); 
			outageUpdater.setLong  (3, nodeID);
			outageUpdater.setString(4, ipAddr);
			int count = outageUpdater.executeUpdate();

			// close statement
			outageUpdater.close();

			// commit work
			try
			{
				dbConn.commit();

				if (log.isDebugEnabled())
					log.debug("interfaceUp closed " +  count + " outages for nodeid/ip " + nodeID + "/" + ipAddr + " in DB");
			}
			catch(SQLException se)
			{
				log.warn("Rolling back transaction, interfaceUp could not be recorded for nodeId/ipaddr: " + nodeID + "/" + ipAddr, se);

				try 
				{
					dbConn.rollback();
				}
				catch (SQLException sqle)
				{
					log.warn("SQL exception during rollback, reason: ", sqle);
				}

			}
		
		}
		catch(SQLException se)
		{
			log.warn("SQL exception while handling \'interfaceUp\'", se);
		}
		finally
		{
			try
			{
				if(dbConn != null)
					dbConn.close();
			}
			catch(SQLException e)
			{
				log.warn("Exception closing JDBC connection", e);
			}
		}
	}

	/**
	 * <p>Record the 'nodeRegainedService' event in the outages table - close
	 * the outage entry in the table if the service is currently down</p>
	 */
	private void handleNodeRegainedService(long eventID, long nodeID, String ipAddr, long serviceID, String eventTime)
	{
		Category log = ThreadCategory.getInstance(OutageWriter.class);

		if(eventID == -1 || nodeID == -1 || ipAddr == null || serviceID == -1)
		{
			log.warn(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI + " ignored - info incomplete - eventid/nodeid/ip/svc: " + eventID + "/" + nodeID + "/" + ipAddr + "/" + serviceID);
			return;
		}

		Connection dbConn = null;
		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();

			// Set the database commit mode
			try
			{
				dbConn.setAutoCommit(false);
			}
			catch (SQLException sqle)
			{
				log.error("Unable to change database AutoCommit to FALSE", sqle);
				return;
			}

			// get timestamp
			java.sql.Timestamp eventTimeTS = null;
			try
			{
				java.util.Date date = EventConstants.parseToDate(eventTime);
				eventTimeTS = new java.sql.Timestamp(date.getTime());
			}
			catch(ParseException pe)
			{
				log.warn("Failed to convert time " + eventTime + " to java.sql.Timestamp, Setting current time instead", pe);

				eventTimeTS = new java.sql.Timestamp((new java.util.Date()).getTime());
			}

	 		// Prepare SQL statement used to update the 'regained time' in an open entry
			PreparedStatement outageUpdater = dbConn.prepareStatement(OutageConstants.DB_UPDATE_OUTAGE_FOR_SERVICE);
			outageUpdater.setLong  (1, eventID); 
			outageUpdater.setTimestamp(2, eventTimeTS); 
			outageUpdater.setLong  (3, nodeID);
			outageUpdater.setString(4, ipAddr);
			outageUpdater.setLong  (5, serviceID);
			outageUpdater.executeUpdate();

			// close statement
			outageUpdater.close();

			// commit work
			try
			{
				dbConn.commit();

				if (log.isDebugEnabled())
					log.debug("nodeRegainedService closed outage for nodeid/ip/service " + nodeID + "/" + ipAddr + "/" + serviceID + " in DB");
			}
			catch(SQLException se)
			{
				log.warn("Rolling back transaction, nodeRegainedService could not be recorded  for nodeId/ipAddr/service: " + nodeID + "/" + ipAddr + "/" + serviceID, se);

				try 
				{
					dbConn.rollback();
				}
				catch (SQLException sqle)
				{
					log.warn("SQL exception during rollback, reason", sqle);
				}

			}
		
		}
		catch(SQLException se)
		{
			log.warn("SQL exception while handling \'nodeRegainedService\'", se);
		}
		finally
		{
			try
			{
				if(dbConn != null)
					dbConn.close();
			}
			catch(SQLException e)
			{
				log.warn("Exception closing JDBC connection", e);
			}
		}
	}

	/**
	 * <p>Record the 'interfaceReparented' event in the outages table. 
	 * Change'outages' table entries associated with the old nodeid/interface
	 * pairing so that those outage entries will be associated with
	 * the new nodeid/interface pairing.</p>
	 *
	 * <p><strong>Note:</strong>This event has no impact on the event id reference
	 * fields</p>
	 */
	private void handleInterfaceReparented(String ipAddr,Parms eventParms)
	{
		Category log = ThreadCategory.getInstance(OutageWriter.class);

		if (log.isDebugEnabled())
			log.debug("interfaceReparented event received...");

		if(ipAddr == null || eventParms == null)
		{
			log.warn(EventConstants.INTERFACE_REPARENTED_EVENT_UEI + " ignored - info incomplete - ip/parms: " + ipAddr + "/" + eventParms);
			return;
		}

		long oldNodeId = -1;
		long newNodeId = -1;
		
		String parmName = null;
		Value parmValue = null;
		String parmContent = null;

		Enumeration parmEnum = eventParms.enumerateParm();
		while(parmEnum.hasMoreElements())
		{
			Parm parm = (Parm)parmEnum.nextElement();
			parmName  = parm.getParmName();
			parmValue = parm.getValue();
			if (parmValue == null)
				continue;
			else 
				parmContent = parmValue.getContent();

			// old nodeid
			if (parmName.equals(EventConstants.PARM_OLD_NODEID))
			{
				try
				{
					oldNodeId = Integer.valueOf(parmContent).intValue();
				}
				catch (NumberFormatException nfe)
				{
					log.warn("Parameter " +  EventConstants.PARM_OLD_NODEID + " cannot be non-numeric");
					oldNodeId = -1;
				}

			}

			// new nodeid
			else if (parmName.equals(EventConstants.PARM_NEW_NODEID))
			{
				try
				{
					newNodeId = Integer.valueOf(parmContent).intValue();
				}
				catch (NumberFormatException nfe)
				{
					log.warn("Parameter " +  EventConstants.PARM_NEW_NODEID + " cannot be non-numeric");
					newNodeId = -1;
				}
			}
		}
		
		if (newNodeId == -1 || oldNodeId == -1)
		{
			log.warn("Unable to process 'interfaceReparented' event, invalid event parm.");
			return;
		}
			
		Connection dbConn = null;
		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();

			// Set the database commit mode
			try
			{
				dbConn.setAutoCommit(false);
			}
			catch (SQLException sqle)
			{
				log.error("Unable to change database AutoCommit to FALSE", sqle);
				return;
			}


			// Issue SQL update to change the 'outages' table entries
			// associated with the old nodeid/interface pairing
			// so that those outage entries will be associated with
			// the new nodeid/interface pairing.

	 		// Prepare SQL statement used to reparent outage table entries -
	 		// used when a 'interfaceReparented' event is received
			PreparedStatement reparentOutagesStmt = dbConn.prepareStatement(OutageConstants.DB_REPARENT_OUTAGES);
			reparentOutagesStmt.setLong   (1, newNodeId);
			reparentOutagesStmt.setLong   (2, oldNodeId);
			reparentOutagesStmt.setString (3, ipAddr);
			int count = reparentOutagesStmt.executeUpdate();

			// commit work
			try
			{
				dbConn.commit();

				if(log.isDebugEnabled()) 
					log.debug("Reparented " + count + " outages - ip: " + ipAddr + " reparented from " + oldNodeId + " to " + newNodeId);
			}
			catch(SQLException se)
			{
				log.warn("Rolling back transaction, reparent outages failed for newNodeId/ipAddr: " + newNodeId + "/" + ipAddr);

				try 
				{
					dbConn.rollback();
				}
				catch (SQLException sqle)
				{
					log.warn("SQL exception during rollback, reason", sqle);
				}

			}

			// close statement
			reparentOutagesStmt.close();

		}
		catch(SQLException se)
		{
			log.warn("SQL exception while handling \'interfaceReparented\'", se);
		}
		finally
		{
			try
			{
				if(dbConn != null)
					dbConn.close();
			}
			catch(SQLException e)
			{
				log.warn("Exception closing JDBC connection", e);
			}
		}
	}

	/**
	 * <p>Delete the service - mark the corresponding 'ifServices' table entry
	 * as deleted. If this is the last active service on its interface, mark
	 * interface as deleted, cascade to the node level - mark node as deleted
	 * if interface just deleted was the last managed interface on the node.</p>
	 *
	 * <p>If service deleted is 'SNMP', clear out snmp info in the 'ipinterface'
	 * and 'node' tables, clear entries in the 'snmpInterface' table, 
	 * and generate a 'forceRescan' on the node</p>
	 *
	 * <p>Generate serviceDeleted, interfaceDeleted, nodeDeleted, forceRescan events
	 * as appropriate</p>
	 */
	private void handleDeleteService(long eventID, long nodeID, String ipAddr, String serviceName, long serviceID)
	{
		Category log = ThreadCategory.getInstance(OutageWriter.class);

		if(eventID == -1 || nodeID == -1 || ipAddr == null || serviceName == null)
		{
			log.warn(EventConstants.DELETE_SERVICE_EVENT_UEI + " ignored - info incomplete - eventID/nodeid/ip/svc: " + eventID + "/" + nodeID + "/" + ipAddr + "/" + serviceName);
			return;
		}
		
		// Form now events generated will have this date - will need to be
		// the date set if the ifservices table ever gets a service deletion time field
		java.util.Date svcDeleteDate = new java.util.Date();

		boolean generateServiceDeletedEvent = false;
		boolean generateInterfaceDeletedEvent = false;
		boolean generateForceNodeRescanEvent= false;

	 	// List of 'IfSvcSnmpEntry's
		List ifSvcSnmpEntries = null;

		boolean bRollback = false;
		
		Connection dbConn = null;
		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();

			// Set the database commit mode
			try
			{
				dbConn.setAutoCommit(false);
			}
			catch (SQLException sqle)
			{
				log.error("Unable to change database AutoCommit to FALSE", sqle);
				return;
			}
		
			m_generateNodeDeletedEvent = false;

			if (log.isDebugEnabled())
				log.debug("handleDeleteService: nodeID/ip/svcName/svcID - " + nodeID + "/" + ipAddr + "/" + serviceName + "/" + serviceID);

			if (log.isDebugEnabled())
				log.debug("handleDeleteService: Closing any outstanding outage for " + nodeID + "/" + ipAddr + "/" + serviceID);

			// Close any outstanding outages, if present, for this combination

			// Prepare SQL statement used to update the 'regained time' in an open entry
			PreparedStatement outageUpdater = dbConn.prepareStatement(OutageConstants.DB_UPDATE_OUTAGE_FOR_SERVICE);

			outageUpdater.setLong  (1, eventID); 
			outageUpdater.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
			outageUpdater.setLong  (3, nodeID);
			outageUpdater.setString(4, ipAddr);
			outageUpdater.setLong  (5, serviceID);
			int count = outageUpdater.executeUpdate();

			// close statement
			outageUpdater.close();


			if (log.isDebugEnabled())
				log.debug("handleDeleteService: " + count + " outstanding outage(s) closed for " + nodeID + "/" + ipAddr + "/" + serviceID);

			// If service is SNMP, make sure appropriate adjustments or
			// deletions occur for SNMP
			if (serviceName.equals(SNMP_SVC))
			{
				long snmpv2id = getServiceID(SNMPV2_SVC);

				ifSvcSnmpEntries = new ArrayList();

				if (log.isDebugEnabled())
					log.debug("handleDeleteService: service was SNMP, getting all SNMP/SNMPV2 entries for the node(to account for aliases/secondary)");
			
				
	 			// Prepare SQL statement used to get all currently active
				// SNMP and SNMPv2 entries for a node from the ifServices table 
				PreparedStatement getSnmpServiceStmt = dbConn.prepareStatement(OutageConstants.DB_GET_SNMP_SERVICE);
				getSnmpServiceStmt.setLong  (1, nodeID);
				getSnmpServiceStmt.setLong  (2, serviceID);
				getSnmpServiceStmt.setLong  (3, snmpv2id);

				ResultSet svcRS = getSnmpServiceStmt.executeQuery();
				while(svcRS.next())
				{
					String ip = svcRS.getString(1);
					long sid = svcRS.getLong(2);

					// create new IfSvcSnmpEntry
					IfSvcSnmpEntry entry = null;
					if (sid == serviceID)
					{
						entry = new IfSvcSnmpEntry(nodeID, ip, SNMP_SVC);

						if (log.isDebugEnabled())
							log.debug("handleDeleteService: IfSvcSnmpEntry for: " + nodeID + "/" + ip + "/" + SNMP_SVC);
					}
					else if (sid == snmpv2id)
					{
						entry = new IfSvcSnmpEntry(nodeID, ip, SNMPV2_SVC);

						if (log.isDebugEnabled())
							log.debug("handleDeleteService: IfSvcSnmpEntry for: " + nodeID + "/" + ip + "/" + SNMPV2_SVC);
					}

					ifSvcSnmpEntries.add(entry);

				}
				
				// close result set and statement
				svcRS.close();
				getSnmpServiceStmt.close();

				if (log.isDebugEnabled())
					log.debug("handleDeleteService: service was SNMP, deleting all SNMP/SNMPV2 entries for the node(to account for aliases/secondary)");

				// Remove the all SNMP service entries if present

	 			// Prepare SQL statement used to flag all SNMP and SNMPv2 entries
				// for a node as deleted in the ifServices table 
				PreparedStatement deleteSnmpServiceStmt = dbConn.prepareStatement(OutageConstants.DB_DELETE_SNMP_SERVICE);
				deleteSnmpServiceStmt.setLong  (1, nodeID);
				deleteSnmpServiceStmt.setLong  (2, serviceID);
				deleteSnmpServiceStmt.setLong  (3, snmpv2id);

				count = deleteSnmpServiceStmt.executeUpdate();

				// close statement
				deleteSnmpServiceStmt.close();

				if (log.isDebugEnabled())
					log.debug("handleDeleteService: " + count + " SNMP/SNMPv2 entries deleted for node " + nodeID);
			
				// make appropriate changes in the node, ipinterface table and
				// generate a forceRescan if necessary
				makeSNMPChanges(dbConn, nodeID, ipAddr);
			
				// Since a deleteService was received for SNMP, this has to be the
				// primary SNMP interface, generate a forceRescan for the node
				// mark all the ipinterface entries appropriately
				generateForceNodeRescanEvent = true;

				// Delete all entries in the 'snmpInterface' table for this node

				// Prepare SQL statement used to delete all entries for a node
				// from the snmpInterface table
				PreparedStatement deleteSnmpInterfaceStmt = dbConn.prepareStatement(OutageConstants.DB_DELETE_SNMP_INTERFACE);
				deleteSnmpInterfaceStmt.setLong(1, nodeID);
			
				if (log.isDebugEnabled())
					log.debug("handleDeleteService: deleting interface entries from 'snmpinterface' table");			

				count = deleteSnmpInterfaceStmt.executeUpdate();
			
				// close statement
				deleteSnmpInterfaceStmt.close();

				if (log.isDebugEnabled())
					log.debug("handleDeleteService: snmp interface entries deleted: " + count);

				/* ---------------------- Begin RRD file deletion ------------------------
			     * No longer deleting RRD files...will keep them around and handle any issues
				 * with presentation in the user interface.
				 
				// get RRD directory
				String rrdRepository = null;
				try
				{
					DataCollectionConfigFactory.reload();
					rrdRepository = DataCollectionConfigFactory.getInstance().getRrdRepository();
				}
				catch (Exception e)
				{
					log.warn("Unable to get rrd repository", e);
				}

				if (log.isDebugEnabled())
					log.debug("handleDeleteService: rrdRepository: " + rrdRepository);

				if (rrdRepository != null)
				{
					// Append <nodeId> to RRD repository directory to gain 
					// access to the node's RRD files.
					String nodeRRDPath = rrdRepository + File.separator + String.valueOf(nodeID);
					try
					{
						File fd = new File(nodeRRDPath);

						File[] rrdFiles = fd.listFiles();
						if (rrdFiles != null)
						{
							// Delete all the node's RRD files
							for (int i=0; i < rrdFiles.length; i++)
							{
								File tmp = rrdFiles[i];
								String tmpName = tmp.getName();
								if (log.isDebugEnabled())
									log.debug("handleDeleteService: rrdFileName: " + tmpName);

								if (tmpName != null && tmpName.endsWith(".rrd"))
								{
									boolean pass = tmp.delete();

									if (log.isDebugEnabled())
										log.debug("handleDeleteService: rrdFileName: " + tmpName + " removed?: " + pass);
								}
							}
						}

					}
					catch (Exception ioe)
					{
						log.warn("Unable to delete rrd files for nodeid/interface: " + nodeID + "/" + ipAddr, ioe);
					}
				}
				else
				{
					log.info("No RRD file deleted as RRD repository not found");
				} 
				--------------------------- End RRD File Deletion --------------------*/
			}
			else
			{
				// Remove the service entry

	 			// Prepare SQL statement used to flag an entry from the ifServices
				// table as deleted based on a node/interface/service tuple
				PreparedStatement deleteServiceStmt   = dbConn.prepareStatement(OutageConstants.DB_DELETE_SERVICE);
				deleteServiceStmt.setLong  (1, nodeID);
				deleteServiceStmt.setString(2, ipAddr);
				deleteServiceStmt.setLong  (3, serviceID);
				
				if (log.isDebugEnabled())
					log.debug("handleDeleteService: deleting service entry");			
				deleteServiceStmt.executeUpdate();

				if (log.isDebugEnabled())
					log.debug("handleDeleteService: service entry deleted");
			
				// close statement
				deleteServiceStmt.close();
			}
			
			generateServiceDeletedEvent = true;
			
			// Are there any remaining service entries for this nodeID/ipAddr pair?
			// If not then we can delete the interface as well.

			// Prepare SQL statement used to determine if there are any remaining
	 		//  entries in the 'ifservices' table for a specific nodeID/ipAddr pair
	 		//  following the deletion of a service entry.
			PreparedStatement getServiceListCount = dbConn.prepareStatement(OutageConstants.DB_GET_SERVICE_COUNT);
			getServiceListCount.setLong(1, nodeID);
			getServiceListCount.setString(2, ipAddr);
						
			ResultSet rs = getServiceListCount.executeQuery();
			
			// Retrieve row count from query
			int rowCount = -1;
			while (rs.next())
			{
				rowCount = rs.getInt(1);
				if (log.isDebugEnabled())
					log.debug("handleDeleteService: remaining entries in ifservice table: " + rowCount);
			}
			rs.close();
			
			// Check rowCount...
			if (rowCount == 0)
			{
				// No service entries remain for this nodeID/ipAddr pair, 
				// so delete the interface
				if (log.isDebugEnabled())
					log.debug("handleDeleteService: looks like we just removed the last service entry for nodeID/ipAddr: " + nodeID + "/" + ipAddr);

				cleanupInterface(dbConn, nodeID, ipAddr);
				generateInterfaceDeletedEvent = true;
			}
	
			//
			// commit the work
			//
			if (log.isDebugEnabled())
				log.debug("Commiting work to the database");						
			dbConn.commit();

			if (log.isDebugEnabled())
				log.debug("Commited changes for deleteService (node/ip/service): " + nodeID + "/" + ipAddr + "/" + serviceID);

		}
		catch(SQLException se)
		{
			log.warn("Database service deletion failed for " + nodeID + "/" + ipAddr + "/" + serviceID, se);
			bRollback = true;
		}
		catch(RuntimeException rtE)
		{
			String name = rtE.getClass().getName();
			name = name.substring(name.lastIndexOf('.')+1);
			log.warn("A RuntimeException of type (" + name + ") was generated during service removal.");
			log.debug(rtE.getLocalizedMessage(), rtE);
			bRollback = true;
		}

		//
		// rollback if necessary
		//
		if(bRollback)
		{
			if (log.isDebugEnabled())
				log.debug("rolling back transaction");

			try
			{
				dbConn.rollback();
			}
			catch (SQLException sqle)
			{
				log.warn("SQL exception during rollback, reason", sqle);
			}

			if (log.isDebugEnabled())
				log.debug("rolled back changes for (node/ip/service): " + nodeID + "/" + ipAddr + "/" + serviceID);

			return;
		}


		// close database connection
		try
		{
			if(dbConn != null)
				dbConn.close();
		}
		catch(SQLException e)
		{
			log.warn("Exception closing JDBC connection", e);
		}
		
		// 
		// generate events to notify of modifications made to the database
		//
		Events events = new Events();

		if (generateServiceDeletedEvent)
		{
			// Generate events notifying of service deletion for all SNMP/SNMPv2 entries for the node
			if (serviceName.equals(SNMP_SVC) && ifSvcSnmpEntries != null)
			{
				Iterator iter = ifSvcSnmpEntries.iterator();
				while(iter.hasNext())
				{
					IfSvcSnmpEntry entry = (IfSvcSnmpEntry)iter.next();

					events.addEvent(createEvent(EventConstants.SERVICE_DELETED_EVENT_UEI, 
							svcDeleteDate,
							entry.getNodeID(),
							entry.getIP(), 
							entry.getService()));
				}

				ifSvcSnmpEntries.clear();
				ifSvcSnmpEntries = null;
			}
			else
			{
				// Generate event notifying of service deletion
				events.addEvent(createEvent(EventConstants.SERVICE_DELETED_EVENT_UEI, 
						svcDeleteDate,
						nodeID,
						ipAddr, 
						serviceName));
			}
		}
	  
		if (generateInterfaceDeletedEvent)
		{
			// Generate event notifying of interface deletion
			events.addEvent(createEvent(EventConstants.INTERFACE_DELETED_EVENT_UEI, 
					svcDeleteDate,
					nodeID,
					ipAddr, 
					null));
		}
				
		if (m_generateNodeDeletedEvent)
		{
			// Generate event notifying of node deletion
			events.addEvent(createEvent(EventConstants.NODE_DELETED_EVENT_UEI, 
					svcDeleteDate,
					nodeID,
					null, 
					null));
		}

		if (!m_generateNodeDeletedEvent && generateForceNodeRescanEvent)
		{
			// Generate event forcing node rescan
			events.addEvent(createEvent(EventConstants.FORCE_RESCAN_EVENT_UEI,
					svcDeleteDate,
					nodeID,
					null, 
					null));
		}

		if (events.getEventCount() <= 0)
		{
			// nothing to send
			return;
		}

		// Serialize and send the events
		//
		Log eventLog = new Log();
		eventLog.setEvents(events);

		try
		{
			// Send to Eventd 
			EventIpcManagerFactory.getInstance().getManager().sendNow(eventLog);

			if (log.isDebugEnabled())
				log.debug("Sent deletion events to eventd");
		}
		catch(Throwable t)
		{
			log.error("Failed to send new event(s) to eventd", t);
		}
	}

	/**
	 * <p>This method is called if there are no longer any "Active" services
	 * associated with a particular interface...they've all be flagged as
	 * "Deleted".  This method will flag the interface as deleted by
	 * setting the 'isManaged' field of the ipinterface table to 'D' for
	 * "Deleted".</p>
	 *
	 * @param dbConn		the database connection
	 * @param nodeID		NodeID associated with the interface
	 * @param ipAddr		IP Address associated with the interface
	 * 
	 * @throws SQLException	If there is a problem modifying the database.
	 */
	private void cleanupInterface(Connection dbConn, long nodeID, String ipAddr)
		throws SQLException
	{
		Category log = ThreadCategory.getInstance(OutageWriter.class);

		if (log.isDebugEnabled())
			log.debug("cleanupInterface: nodeID/ipAddr - " + nodeID + "/" + ipAddr);

		// Delete the interface entry from the 'ipInterface' table

		// Prepare SQL statement used to flag an entry from the ifServices table
		// as deleted based on a node/interface/service tuple
		PreparedStatement deleteInterfaceStmt = dbConn.prepareStatement(OutageConstants.DB_DELETE_INTERFACE);
		deleteInterfaceStmt.setLong(1, nodeID);
		deleteInterfaceStmt.setString(2, ipAddr);

		if (log.isDebugEnabled())
			log.debug("cleanupInterface: deleting interface entry from 'ipinterface' table");

		deleteInterfaceStmt.executeUpdate();

		// close the statement
		deleteInterfaceStmt.close();

		if (log.isDebugEnabled())
			log.debug("cleanupInterface: interface entry deleted");
		
		// Does the 'ipInterface' table contain any other MANAGED interfaces for
		// this node.  If not, then we can delete any UNMANAGED interfaces and
		// then delete the node.

		// Prepare SQL statement used to determine if there are any remaining
	 	// entries in the 'ipInterface' table for a specific nodeID
	 	// following the deletion of an interface entry.
		PreparedStatement getInterfaceListStmt = dbConn.prepareStatement(OutageConstants.DB_GET_INTERFACE_LIST);
		getInterfaceListStmt.setLong(1, nodeID);

		ResultSet rs = getInterfaceListStmt.executeQuery();
		
		// loop through the result set and determine if there are any remaining
		// managed interfaces for the node in question.
		boolean foundManagedInterface = false;
		boolean deleteNode = false;
		
		int ifCount = 0;  // Keep track of number of remaining interfaces
		while (rs.next())
		{
			ifCount++;
			String isManagedStr = rs.getString(1);
			if (log.isDebugEnabled())
				log.debug("cleanupInterface: interface entry " + ifCount + " - isManaged: " + isManagedStr);

			if (isManagedStr.equals("M"))
			{
				foundManagedInterface = true;
				break;
			}
		}

		// close result set and statement
		rs.close();
		getInterfaceListStmt.close();
		
		if (log.isDebugEnabled())
			log.debug("cleanupInterface: foundManagedInterface? " + foundManagedInterface);

		if (!foundManagedInterface)
		{
			deleteNode = true;

			if (log.isDebugEnabled())
				log.debug("cleanupInterface: No more managed interfaces found for node: " + nodeID);

			// If any unmanaged interface entries remain, delete them all
			if (ifCount > 0)
			{
				// Delete all 'ipinterface' table entries for this node
				// ???? DO WE NEED TO GENERATE A DELETION EVENT FOR THESE UNMANAGED INTERFACES????
				// Prepare statement used to flag all interface entries from the
	 			// 'ipInterface' table as deleted for a specific node
				PreparedStatement deleteAllInterfacesStmt = dbConn.prepareStatement(OutageConstants.DB_DELETE_ALL_INTERFACES);
				deleteAllInterfacesStmt.setLong(1, nodeID);
			
				if (log.isDebugEnabled())
					log.debug("cleanupInterface: deleting all remaining interfaces for nodeID: " + nodeID);			

				int count = deleteAllInterfacesStmt.executeUpdate();

				// close statement
				deleteAllInterfacesStmt.close();

				if (log.isDebugEnabled())
					log.debug("cleanupInterface: all remaining " + count + " interfaces deleted");
				
				// Must delete any remaining entries in the 'snmpInterface' table as well.

				// Prepare SQL statement used to delete all entries for a node
				// from the snmpInterface table
				PreparedStatement deleteSnmpInterfaceStmt = dbConn.prepareStatement(OutageConstants.DB_DELETE_SNMP_INTERFACE);
				deleteSnmpInterfaceStmt.setLong(1, nodeID);
			
				if (log.isDebugEnabled())
					log.debug("cleanupInterface: deleting all remaining SNMP interfaces for nodeID: " + nodeID);			
				count = deleteSnmpInterfaceStmt.executeUpdate();

				// close statement
				deleteSnmpInterfaceStmt.close();


				if (log.isDebugEnabled())
					log.debug("cleanupInterface: all remaining " + count + " SNMP interfaces deleted");
			}
			 
		}
		
		// If necessary delete the node entry from the 'node' table
		if (deleteNode)
			cleanupNode(dbConn, nodeID);
	}
	
	/**
	 * <p>This method is called if there are no longer any managed interfaces
	 * associated with a particular node...they've all be flagged as
	 * "Deleted".  This method will flag the node as deleted by
	 * setting the 'nodeType' field of the node table to 'D' for
	 * "Deleted".</p>
	 *
	 * @param nodeID		NodeID associated with the interface
	 * 
	 * @throws SQLException	If there is a problem modifying the database.
	 */
	private void cleanupNode(Connection dbConn, long nodeID)
		throws SQLException
	{
		Category log = ThreadCategory.getInstance(OutageWriter.class);

		// Prepare SQL statement used to flag an entry from the node table
		// as deleted based on a node identifier
		PreparedStatement deleteNodeStmt = dbConn.prepareStatement(OutageConstants.DB_DELETE_NODE);
		deleteNodeStmt.setLong(1, nodeID);

		if (log.isDebugEnabled())
			log.debug("cleanupNode: deleting entry from node table w/ nodeID: " + nodeID);

		deleteNodeStmt.executeUpdate();

		// close statement
		deleteNodeStmt.close();

		if (log.isDebugEnabled())
			log.debug("cleanupNode: node deleted");

		m_generateNodeDeletedEvent = true;
	}

	/**
	 * <p>This method is called when the service jsut deleted was SNMP - so that
	 * appropriate changes can be effected in the database tables to mark this
	 * deletion.</p>
	 *
	 * @param nodeID		NodeID associated with the interface
	 * @param ipAddr		IP Address associated with the interface
	 * 
	 * @throws SQLException	If there is a problem modifying the database.
	 */
	private void makeSNMPChanges(Connection dbConn, long nodeID, String ipAddr)
		throws SQLException
	{
		Category log = ThreadCategory.getInstance(OutageWriter.class);
		
		if (log.isDebugEnabled())
			log.debug("makeSNMPChanges: clearing snmp info entry from 'node' table");			
		// See if this was the primary SNMP interface for the node,
		// clear snmp data from the node table

	 	// Prepare SQL statement used to clear snmp info for a nodeid in the 'node' table
		PreparedStatement clearNodeSnmpInfoStmt = dbConn.prepareStatement(OutageConstants.DB_CLEAR_NODE_SNMP_INFO);
		clearNodeSnmpInfoStmt.setLong(1, nodeID);

		clearNodeSnmpInfoStmt.executeUpdate();

		// close statement
		clearNodeSnmpInfoStmt.close();

		if (log.isDebugEnabled())
			log.debug("makeSNMPChanges: cleared snmp info entry from 'node' table");			
		if (log.isDebugEnabled())
				log.debug("makeSNMPChanges: clearing snmp info entry from 'ipinterface' table");			

		// clear snmp data from ipinterface table

		// Prepare SQL statement used to clear snmp info for a nodeid/ip
		// from the 'ipinterface' table
		PreparedStatement clearInterfaceSnmpInfoStmt = dbConn.prepareStatement(OutageConstants.DB_CLEAR_INTERFACE_SNMP_INFO);
		clearInterfaceSnmpInfoStmt.setLong(1, nodeID);
			
		clearInterfaceSnmpInfoStmt.executeUpdate();

		// close statement
		clearInterfaceSnmpInfoStmt.close();

		if (log.isDebugEnabled())
			log.debug("makeSNMPChanges: cleared snmp info entry from 'ipinterface' table");			
	}

	/**
	 * <p>This method creates an event for the passed parameters</p>
	 *
	 * @param uei		Event to generate and send
	 * @param eventDate	Time to be set for the event
	 * @param nodeID	Node identifier associated with this event
	 * @param ipAddr	Interface address associated with this event
	 * @param serviceName	Service name associated with this event
	 */
	private Event createEvent(String uei, java.util.Date eventDate, long nodeID, String ipAddr, String serviceName)
	{
		// build event to send
		Event newEvent = new Event();
		
		newEvent.setUei(uei);
		newEvent.setSource("OutageManager");
		
		// Convert integer nodeID to String
		newEvent.setNodeid(nodeID);
		
		if (ipAddr != null)
			newEvent.setInterface(ipAddr);
			
		if (serviceName != null)
			newEvent.setService(serviceName);

		newEvent.setTime(EventConstants.formatToString(eventDate));

		return newEvent;
	}

	/**
	 * <p>Read the event UEI, nodeid, interface and service - depending
	 * on the UEI, read event parms, if necessary, and process as appropriate</p>
	 */
	private void processEvent()
	{
		Category log = ThreadCategory.getInstance(OutageWriter.class);

		if (m_event == null)
		{
			if(log.isDebugEnabled())
				log.debug("Event is null, nothing to process");
			return;
		}

		if (log.isDebugEnabled())
			log.debug("About to process event: " + m_event.getUei());

		//
		// Check to make sure the event has a uei
		//
		String uei = m_event.getUei();
		if(uei == null)
		{
			// should only get registered events
			if (log.isDebugEnabled())
				log.debug("Event received with null UEI, ignoring event");
			return;
		}
		
		// get eventid
		long eventID = -1;
		if (m_event.hasDbid())
			eventID = m_event.getDbid();

		// convert the node id
		long nodeID = -1;
		if (m_event.hasNodeid())
			nodeID = m_event.getNodeid();

		String ipAddr    = m_event.getInterface();
		String service   = m_event.getService();
		String eventTime = m_event.getTime();
		
		if (log.isDebugEnabled())
			log.debug("processEvent: Event\nuei\t\t" + uei +
					"\neventid\t\t" + eventID +
					"\nnodeid\t\t" + nodeID +
					"\nipaddr\t\t" + ipAddr +
					"\nservice\t\t" + service +
					"\neventtime\t" + (eventTime != null ? eventTime : "<null>"));


		// get service id for the service name
		long serviceID = -1;
		if (service != null)
		{
			try
			{
				serviceID = getServiceID(service);
			}
			catch(SQLException sqlE)
			{
				log.warn("Error converting service name \"" 
						+ service
						+ "\" to an integer identifier, storing -1", sqlE);
			}
		}

		//
		// Check for any of the following UEIs:
		//
		//	nodeLostService 
		//	interfaceDown 
		//	nodeDown 
		//	nodeUp 
		//	interfaceUp 
		//	nodeRegainedService 
		//	deleteService
		// 	interfaceReparented
		//
		if(uei.equals(EventConstants.NODE_LOST_SERVICE_EVENT_UEI))
		{
			handleNodeLostService(eventID, nodeID, ipAddr, serviceID, eventTime);
		}
		else if(uei.equals(EventConstants.INTERFACE_DOWN_EVENT_UEI))
		{
			handleInterfaceDown(eventID, nodeID, ipAddr, eventTime);
		}
		else if(uei.equals(EventConstants.NODE_DOWN_EVENT_UEI))
		{
			handleNodeDown(eventID, nodeID, eventTime);
		}
		else if(uei.equals(EventConstants.NODE_UP_EVENT_UEI))
		{
			handleNodeUp(eventID, nodeID, eventTime);
		}
		else if(uei.equals(EventConstants.INTERFACE_UP_EVENT_UEI))
		{
			handleInterfaceUp(eventID, nodeID, ipAddr, eventTime);
		}
		else if(uei.equals(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI))
		{
			handleNodeRegainedService(eventID, nodeID, ipAddr, serviceID, eventTime);
		}
		else if(uei.equals(EventConstants.DELETE_SERVICE_EVENT_UEI))
		{
			handleDeleteService(eventID, nodeID, ipAddr, service, serviceID);
		}
		else if(uei.equals(EventConstants.INTERFACE_REPARENTED_EVENT_UEI))
		{
			handleInterfaceReparented(ipAddr, m_event.getParms());
		}
	}

 	/**
	 * The constructor
	 *
	 * @param id	the id for this fiber
	 * @param q 	the queue to which incoming messages are addded
	 */
	public OutageWriter(Event event)
	{
		m_event = event;
	}

	/**
	 * <p>Process the event depending on the UEI</p>
	 */
	public void run()
	{
		try
		{
			processEvent();
		}
		catch(Throwable t)
		{
			Category log = ThreadCategory.getInstance(OutageWriter.class);
			log.warn("Unexpected exception processing event", t);
		}
	}
}
