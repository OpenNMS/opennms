//
// Copyright (C) 2002-2003 Sortova Consulting Group, Inc.  All rights reserved.
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

import org.opennms.netmgt.config.OutageManagerConfigFactory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
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
 * the nodeid/ipaddr/serviceid, the outage is cleared. If not, the event is placed
 * in the event cache in case a race condition has occurred that puts the "up"
 * event in before the "down" event.</p>
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
 * @author 	<A HREF="mailto:jamesz@blast.com">James Zuo</A>
 * @author 	<A HREF="mailto:tarus@opennms.org">Tarus</A>
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
         * Convert event time into timestamp
         */
        private java.sql.Timestamp convertEventTimeIntoTimestamp(String eventTime)
        {
                java.sql.Timestamp timestamp = null;
                try
                {
                        java.util.Date date = EventConstants.parseToDate(eventTime);
                        timestamp = new java.sql.Timestamp(date.getTime());
                }
                catch(ParseException e)
                {
                        ThreadCategory.getInstance(OutageWriter.class).warn("Failed to convert event time " + eventTime + " to timestamp.", e);

                        timestamp = new java.sql.Timestamp((new java.util.Date()).getTime());
                }
                return timestamp;
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
         * This method checks the outage table and determines if an open outage
         * entry exists for the specified node id.
         *
         * @throws SQLException if database error encountered.
         */
        private boolean openOutageExists(Connection dbConn, long nodeId)
                throws SQLException
        {
                return openOutageExists(dbConn, nodeId, null, -1);
        }

        /**
         * This method checks the outage table and determines if an open outage
         * entry exists for the specified node/ip pair.
         *
         * @throws SQLException if database error encountered.
         */
        private boolean openOutageExists(Connection dbConn, long nodeId, String ipAddr)
                throws SQLException
        {
                return openOutageExists(dbConn, nodeId, ipAddr, -1);
        }
        /**
         * This method checks the outage table and determines if an open outage
         * entry exists for the specified node/ip/service tuple.
         *
         * @throws SQLException if database error encountered.
         */
        private boolean openOutageExists(Connection dbConn, long nodeId, String ipAddr, long serviceId)
                throws SQLException
        {
                int numOpenOutages = -1;

                // Prepare SQL statement used to see if there is already an
                // 'open' record for the node/ip/svc combination
                PreparedStatement openStmt = null;
                if (ipAddr != null && serviceId > 0)
                {
                        // have nodeid/ipAddr/serviceid tuple
                        openStmt = dbConn.prepareStatement(OutageConstants.DB_OPEN_RECORD);
                        openStmt.setLong  (1, nodeId);
                        openStmt.setString(2, ipAddr);
                        openStmt.setLong  (3, serviceId);
                }
                else if (ipAddr != null)
                {
                        // have nodeid/ipAddr pair
                        openStmt = dbConn.prepareStatement(OutageConstants.DB_OPEN_RECORD_2);
                        openStmt.setLong  (1, nodeId);
                        openStmt.setString(2, ipAddr);
                }
                else
                {
                        // have nodeid
                        openStmt = dbConn.prepareStatement(OutageConstants.DB_OPEN_RECORD_3);
                        openStmt.setLong  (1, nodeId);
                }

                ResultSet rs = openStmt.executeQuery();
                if(rs.next())
                {
                        numOpenOutages = rs.getInt(1);
                }

                // close result set
                rs.close();

                // close statement
                openStmt.close();

                if (numOpenOutages > 0)
                        return true;
                else
                        return false;
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


		// check that there is no 'open' entry already
		Connection dbConn = null;

		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();
                        // check that there is no 'open' entry already
                        if (openOutageExists(dbConn, nodeID, ipAddr, serviceID))
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
                                // Check the OutageCache to see if an event exists.
                                OutageEventEntry regainedEvent = OutageEventCache.getInstance().findCacheMatch(eventID,
                                                                                                nodeID,
                                                                                                ipAddr,
                                                                                                serviceID,
                                                                                                eventTime,
                                                                                                OutageEventEntry.EVENT_TYPE_LOST_SERVICE);

                                PreparedStatement newOutageWriter = null;
                                if (regainedEvent == null)
                                {
                                        // Prepare statement to insert a new outage table entry
                                        if (log.isDebugEnabled())
                                                log.debug("handleNodeLostService: creating new outage entry...");
                                        newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_NEW_OUTAGE);
                                        newOutageWriter.setLong  (1, outageID);
                                        newOutageWriter.setLong  (2, eventID);
                                        newOutageWriter.setLong  (3, nodeID);
                                        newOutageWriter.setString(4, ipAddr);
                                        newOutageWriter.setLong  (5, serviceID);
                                        newOutageWriter.setTimestamp(6, convertEventTimeIntoTimestamp(eventTime));
                                }
                                else
                                {
                                        // Matching regained service event in the cache, so create new
                                        // outage entry with both lost and regained time.

                                        // Prepare statement to insert a closed outage table entry
                                        if (log.isDebugEnabled())
                                                log.debug("handleNodeLostService: creating closed outage entry...");
                                        newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_CACHE_HIT);
                                        newOutageWriter.setLong  (1, outageID);
                                        newOutageWriter.setLong  (2, eventID);
                                        newOutageWriter.setLong  (3, nodeID);
                                        newOutageWriter.setString(4, ipAddr);
                                        newOutageWriter.setLong  (5, serviceID);
                                        newOutageWriter.setTimestamp(6, convertEventTimeIntoTimestamp(eventTime));
                                        newOutageWriter.setLong  (7, regainedEvent.getEventId());
                                        newOutageWriter.setTimestamp(8, convertEventTimeIntoTimestamp(regainedEvent.getEventTime()));
                                }

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

                        // Check the OutageCache to see if an event exists.

                        OutageEventEntry regainedEvent = OutageEventCache.getInstance().findCacheMatch(eventID,
                                                                                        nodeID,
                                                                                        ipAddr,
                                                                                        -1,
                                                                                        eventTime,
                                                                                        OutageEventEntry.EVENT_TYPE_INTERFACE_DOWN);
                        
                        if (regainedEvent == null || openOutageExists(dbConn, nodeID, ipAddr))
                        {
                                // No matching regained service event in the cache, so open new
                                // outage entries in the outage table.

                                // Prepare statement to insert a new outage table entry
                                newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_NEW_OUTAGE);

                                if (log.isDebugEnabled())
                                        log.debug("handleInterfaceDown: creating new outage entries...");

                                activeSvcsStmt.setLong  (1, nodeID);
                                activeSvcsStmt.setString(2, ipAddr);
                                ResultSet activeSvcsRS = activeSvcsStmt.executeQuery();
                                while(activeSvcsRS.next())
                                {
                                        long serviceID = activeSvcsRS.getLong(1);

                                        if (openOutageExists(dbConn, nodeID, ipAddr, serviceID))
                                        {
                                                if (log.isDebugEnabled())
                                                        log.debug("handleInterfaceDown: " + nodeID + "/" + ipAddr + "/" + serviceID + " already down");
                                        }
                                        else
                                        {
                                                long outageID = -1;
                                                ResultSet seqRS = getNextOutageIdStmt.executeQuery();
                                                if (seqRS.next())
                                                {
                                                        outageID = seqRS.getLong(1);
                                                }
                                                seqRS.close();

                                                newOutageWriter.setLong  (1, outageID);
                                                newOutageWriter.setLong  (2, eventID);
                                                newOutageWriter.setLong  (3, nodeID);
                                                newOutageWriter.setString(4, ipAddr);
                                                newOutageWriter.setLong  (5, serviceID);
                                                newOutageWriter.setTimestamp(6, convertEventTimeIntoTimestamp(eventTime));

                                                // execute update
                                                newOutageWriter.executeUpdate();

                                                if (log.isDebugEnabled())
                                                        log.debug("handleInterfaceDown: Recording new outage for " + nodeID + "/" + ipAddr + "/" + serviceID);
                                        }
                                }
                                // close result set
                                activeSvcsRS.close();
                        }
                        else if (regainedEvent != null)
                        {
                                // Matching regained service event in the cache

                                // Prepare statement to insert a closed outage table entry
                                if (log.isDebugEnabled())
                                        log.debug("handleInterfaceDown: creating closed outage entries...");

                                newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_CACHE_HIT);

                                // Get all active services for the nodeid/ip
                                activeSvcsStmt.setLong  (1, nodeID);
                                activeSvcsStmt.setString(2, ipAddr);
                                ResultSet activeSvcsRS = activeSvcsStmt.executeQuery();
                                while(activeSvcsRS.next())
                                {
                                        long serviceID = activeSvcsRS.getLong(1);

                                        long outageID = -1;
                                        ResultSet seqRS = getNextOutageIdStmt.executeQuery();
                                        if (seqRS.next())
                                        {
                                                outageID = seqRS.getLong(1);
                                        }
                                        seqRS.close();

                                        newOutageWriter.setLong  (1, outageID);
                                        newOutageWriter.setLong  (2, eventID);
                                        newOutageWriter.setLong  (3, nodeID);
                                        newOutageWriter.setString(4, ipAddr);
                                        newOutageWriter.setLong  (5, serviceID);
                                        newOutageWriter.setTimestamp(6, convertEventTimeIntoTimestamp(eventTime));
                                        newOutageWriter.setLong  (7, regainedEvent.getEventId());
                                        newOutageWriter.setTimestamp(8, convertEventTimeIntoTimestamp(regainedEvent.getEventTime()));

                                        // execute insert
                                        newOutageWriter.executeUpdate();

                                        if (log.isDebugEnabled())
                                                log.debug("handleInterfaceDown: Recording closed outage for " + nodeID + "/" + ipAddr + "/" + serviceID);
                                }
                        }

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

                        // Check the OutageCache to see if an event exists.

                        OutageEventEntry regainedEvent = OutageEventCache.getInstance().findCacheMatch(eventID,
                                                                                        nodeID,
                                                                                        null,
                                                                                        -1,
                                                                                        eventTime,
                                                                                        OutageEventEntry.EVENT_TYPE_NODE_DOWN);

                        if (regainedEvent == null || openOutageExists(dbConn, nodeID))
                        {
                                // No matching regained service event in the cache

                                // Prepare statement to insert a new outage table entry
                                newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_NEW_OUTAGE);

                                if (log.isDebugEnabled())
                                        log.debug("handleNodeDown: creating new outage entries...");

                                // Get all active services for the nodeid
                                activeSvcsStmt.setLong  (1, nodeID);
                                ResultSet activeSvcsRS = activeSvcsStmt.executeQuery();
                                while(activeSvcsRS.next())
                                {
                                        String ipAddr = activeSvcsRS.getString(1);
                                        long serviceID = activeSvcsRS.getLong(2);

                                        if (openOutageExists(dbConn, nodeID, ipAddr, serviceID))
                                        {
                                                if (log.isDebugEnabled())
                                                        log.debug("handleNodeDown: " + nodeID + "/" + ipAddr + "/" + serviceID + " already down");
                                        }
                                        else
                                        {
                                                long outageID = -1;

                                                ResultSet seqRS = getNextOutageIdStmt.executeQuery();
                                                if (seqRS.next())
                                                {
                                                        outageID = seqRS.getLong(1);
                                                }
                                                seqRS.close();

                                                // set parms
                                                newOutageWriter.setLong  (1, outageID);
                                                newOutageWriter.setLong  (2, eventID);
                                                newOutageWriter.setLong  (3, nodeID);
                                                newOutageWriter.setString(4, ipAddr);
                                                newOutageWriter.setLong  (5, serviceID);
                                                newOutageWriter.setTimestamp(6, convertEventTimeIntoTimestamp(eventTime));

                                                // execute update
                                                newOutageWriter.executeUpdate();

                                                if (log.isDebugEnabled())
                                                        log.debug("handleNodeDown: Recording outage for " + nodeID + "/" + ipAddr + "/" + serviceID);
                                        }

                                }
                                // close result set
                                activeSvcsRS.close();
                        } else if (regainedEvent != null)
                        {
                                // Matching regained service event in the cache.

                                // Prepare statement to insert a closed outage table entry
                                if (log.isDebugEnabled())
                                        log.debug("handleNodeDown: creating closed outage entries...");

                                newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_CACHE_HIT);

                                // Get all active services for the nodeid
                                activeSvcsStmt.setLong  (1, nodeID);
                                ResultSet activeSvcsRS = activeSvcsStmt.executeQuery();
                                while(activeSvcsRS.next())
                                {
                                        String ipAddr = activeSvcsRS.getString(1);
                                        long serviceID = activeSvcsRS.getLong(2);

                                        // Execute the statement to get the next outage id from the sequence
                                        //
                                        long outageID = -1;
                                        ResultSet seqRS = getNextOutageIdStmt.executeQuery();
                                        if (seqRS.next())
                                        {
                                                outageID = seqRS.getLong(1);
                                        }
                                        seqRS.close();

                                        newOutageWriter.setLong  (1, outageID);
                                        newOutageWriter.setLong  (2, eventID);
                                        newOutageWriter.setLong  (3, nodeID);
                                        newOutageWriter.setString(4, ipAddr);
                                        newOutageWriter.setLong  (5, serviceID);
                                        newOutageWriter.setTimestamp(6, convertEventTimeIntoTimestamp(eventTime));
                                        newOutageWriter.setLong  (7, regainedEvent.getEventId());
                                        newOutageWriter.setTimestamp(8, convertEventTimeIntoTimestamp(regainedEvent.getEventTime()));

                                        // execute insert
                                        newOutageWriter.executeUpdate();

                                        if (log.isDebugEnabled())
                                                log.debug("handleNodeDown: Recording closed outage for " + nodeID + "/" + ipAddr + "/" + serviceID);
                                }
                        }

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

                        int count = 0;

                        if (openOutageExists(dbConn, nodeID))
                        {

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

                                // Prepare SQL statement used to update the 'regained time' for
                                // all open outage entries for the nodeid
                                PreparedStatement outageUpdater = dbConn.prepareStatement(OutageConstants.DB_UPDATE_OUTAGES_FOR_NODE);
                                outageUpdater.setLong  (1, eventID);
                                outageUpdater.setTimestamp(2, convertEventTimeIntoTimestamp(eventTime));
                                outageUpdater.setLong  (3, nodeID);
                                count = outageUpdater.executeUpdate();

                                // close statement
                                outageUpdater.close();
                        }
                        else
                        {
                                // Outage table does not have an open record.
                                log.warn("\'" + EventConstants.NODE_UP_EVENT_UEI + "\' for " + nodeID + " no open record, so adding to cache.");

                                // Store the event in the event cache
                                OutageEventCache.getInstance().add(new OutageEventEntry(eventID,
                                                                                nodeID,
                                                                                null,
                                                                                -1,
                                                                                eventTime,
                                                                                OutageEventEntry.EVENT_TYPE_NODE_UP));
                        }

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

                        if (openOutageExists(dbConn, nodeID, ipAddr))
                        {
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

                                // Prepare SQL statement used to update the 'regained time' for
                                // all open outage entries for the nodeid/ipaddr
                                PreparedStatement outageUpdater = dbConn.prepareStatement(OutageConstants.DB_UPDATE_OUTAGES_FOR_INTERFACE);
                                outageUpdater.setLong  (1, eventID);
                                outageUpdater.setTimestamp(2, convertEventTimeIntoTimestamp(eventTime));
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
                                                log.debug("handleInterfaceUp: interfaceUp closed " +  count + " outages for nodeid/ip " + nodeID + "/" + ipAddr + " in DB");
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
                        else
                        {
                                // Outage table does not have an open record.
                                log.warn("\'" + EventConstants.INTERFACE_UP_EVENT_UEI + "\' for " + nodeID + "/" + ipAddr + " ignored, adding to event cache.");

                                // Store the event in the event cache
                                OutageEventCache.getInstance().add(new OutageEventEntry(eventID,
                                                                                nodeID,
                                                                                ipAddr,
                                                                                -1,
                                                                                eventTime,
                                                                                OutageEventEntry.EVENT_TYPE_INTERFACE_UP));
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

                        if (openOutageExists(dbConn, nodeID, ipAddr, serviceID))
                        {
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

                                // Prepare SQL statement used to update the 'regained time' in an open entry
                                PreparedStatement outageUpdater = dbConn.prepareStatement(OutageConstants.DB_UPDATE_OUTAGE_FOR_SERVICE);
                                outageUpdater.setLong  (1, eventID);
                                outageUpdater.setTimestamp(2, convertEventTimeIntoTimestamp(eventTime));
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
                                                log.debug("nodeRegainedService: closed outage for nodeid/ip/service " + nodeID + "/" + ipAddr + "/" + serviceID + " in DB");
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
                        else
                        {
                                // Outage table does not have an open record.
                                log.warn("\'" + EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI + "\' for " + nodeID + "/" + ipAddr + "/" + serviceID + " does not have open record, adding to cache.");

                                // Store the event in the event cache
                                OutageEventCache.getInstance().add(new OutageEventEntry(eventID,
                                                                                nodeID,
                                                                                ipAddr,
                                                                                serviceID,
                                                                                eventTime,
                                                                                OutageEventEntry.EVENT_TYPE_REGAINED_SERVICE));
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
			log.warn(EventConstants.DELETE_SERVICE_EVENT_UEI 
                                + " ignored - info incomplete - eventID/nodeid/ip/svc: " 
                                + eventID + "/" + nodeID + "/" + ipAddr + "/" + serviceName);
			return;
		}
		
		boolean generateServiceDeletedEvent = false;
		boolean generateDeleteInterfaceEvent = false;
		boolean generateDeleteNodeEvent = false;
		boolean generateForceNodeRescanEvent= false;

		boolean bRollback = false;
		
		Connection dbConn = null;
                PreparedStatement stmt = null;
                ResultSet rs = null;
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
	
                        if (OutageManagerConfigFactory.getInstance().deletePropagation())
                        {
                                // First, verify if there are other services exist on the node/interface pair.
                                stmt = dbConn.prepareStatement(OutageConstants.DB_COUNT_REMAIN_SERVICES_ON_INTERFACE);
                                stmt.setLong(1, nodeID);
                                stmt.setString(2, ipAddr);
                                stmt.setLong(3, serviceID);
                        
                                rs = stmt.executeQuery();

                                int otherServices = 0;
                                if (rs.next())
                                        otherServices = rs.getInt(1);
                        
                                rs.close();
                                stmt.close();
                        
                                if (otherServices < 1)
                                {
                                        // The service to delete is the only service on the node/interface pair.
                                        // verify if the interface is the only one on the node
                                        stmt = dbConn.prepareStatement(OutageConstants.DB_COUNT_REMAIN_INTERFACES_ON_NODE);
                                        stmt.setLong(1, nodeID);
                                        stmt.setString(2, ipAddr);

                                        rs = stmt.executeQuery();
                                        int otherInterfaces = 0;

                                        if (rs.next())
                                                otherInterfaces = rs.getInt(1);
                                
                                        rs.close();
                                        stmt.close();
                                
                                        if (otherInterfaces < 1)
                                        {
                                                // the interface is the only one on the node. Delete the node.
		                                generateDeleteNodeEvent = true;
			                        if (log.isDebugEnabled())
				                        log.debug("handleDeleteService: Will generate delete node event: " + nodeID);
                                        }
                                        else
                                        {
                                                // there are other interfaces on the node. Just delete the 
                                                //interface.
		                                generateDeleteInterfaceEvent = true;
			                        if (log.isDebugEnabled())
				                        log.debug("handleDeleteService: Will generate delete interface "
                                                                + "event for node/interface: " + nodeID + "/" + ipAddr);
                                        }
                                }
                        }
                        
                        if (!generateDeleteNodeEvent && !generateDeleteInterfaceEvent)
                        {
			        if (log.isDebugEnabled())
			                log.debug("handleDeleteService: start deleting nodeid/interface/service: " 
                                        + nodeID + "/" + ipAddr + "/" + serviceName);
                                deleteService(dbConn, nodeID, ipAddr, serviceName, serviceID);
			
                                // If service is SNMP, make sure appropriate adjustments or
			        // deletions occur for SNMP
			        if (serviceName.equals(SNMP_SVC))
			        {
			                if (isSnmpPrimaryInterface(dbConn, nodeID, ipAddr))
		                        {	
				                // Since a deleteService was received for SNMP, and the interface
                                                // host this SNMP serviceis the primary SNMP interface of a node,
                                                // a forceRescan should be performed for the node
				                generateForceNodeRescanEvent = true;
                                        }
				        // Delete all entries in the 'snmpInterface' table for this node
				        stmt = dbConn.prepareStatement(OutageConstants.DB_DELETE_SNMP_INTERFACE);
				        stmt.setLong(1, nodeID);
				        stmt.executeUpdate();
			
			                stmt.close();
				        if (log.isDebugEnabled())
				                log.debug("handleDeleteService: deleted interface entries from 'snmpinterface' table");		
                                }
		                generateServiceDeletedEvent = true;
                        }
			//
			// commit the work
			//
			dbConn.commit();

			if (log.isDebugEnabled())
				log.debug("Commited changes for deleteService (node/ip/service): " 
                                        + nodeID + "/" + ipAddr + "/" + serviceID);

		} 
                catch(SQLException sqlE)
		{
			log.warn("Database service deletion failed for " 
                                + nodeID + "/" + ipAddr + "/" + serviceID, sqlE);
			bRollback = true;
		}
		catch(Throwable t)
		{
			log.warn("Run into unexpected exception.");
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
			catch (SQLException sqlE)
			{
				log.warn("SQL exception during rollback, reason", sqlE);
			}

			if (log.isDebugEnabled())
				log.debug("rolled back changes for (node/ip/service): " 
                                        + nodeID + "/" + ipAddr + "/" + serviceID);

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
		
		// Form now events generated will have this date - will need to be
		// the date set if the ifservices table ever gets a service deletion time field
		java.util.Date deleteDate = new java.util.Date();
		
                // 
		// generate events to notify of modifications made to the database
		//
                try 
                {
                        EventIpcManagerFactory eFactory = EventIpcManagerFactory.getInstance();
		        
                        if (generateServiceDeletedEvent)
		        {
				// Generate event notifying of service deletion
				eFactory.getManager().sendNow(createEvent(EventConstants.SERVICE_DELETED_EVENT_UEI, 
						deleteDate,
						nodeID,
						ipAddr, 
						serviceName));
                                                
			        if (log.isDebugEnabled())
				        log.debug("Sent service deleted event to eventd for:"
                                                + nodeID + "/" + ipAddr + "/" + serviceName);
			}
                        
                        if (generateDeleteNodeEvent)
		        {
				// Generate event notifying of service deletion
				eFactory.getManager().sendNow(createEvent(EventConstants.DELETE_NODE_EVENT_UEI, 
						deleteDate,
						nodeID,
						null, 
						null));
			
                                if (log.isDebugEnabled())
				        log.debug("Sent delete node event to eventd for: " + nodeID);
			}
                        if (generateDeleteInterfaceEvent)
		        {
				// Generate event notifying of service deletion
				eFactory.getManager().sendNow(createEvent(EventConstants.DELETE_INTERFACE_EVENT_UEI, 
						deleteDate,
						nodeID,
						ipAddr, 
						null));
			
                                if (log.isDebugEnabled())
				        log.debug("Sent delete interface event to eventd for: "
                                                + nodeID + "/" + ipAddr);
			}
                        
                        if (generateForceNodeRescanEvent)
		        {
				// Generate event notifying of service deletion
				eFactory.getManager().sendNow(createEvent(EventConstants.FORCE_RESCAN_EVENT_UEI, 
						deleteDate,
						nodeID,
						ipAddr, 
						serviceName));
			
                                if (log.isDebugEnabled())
				        log.debug("Sent force rescan event to eventd on node:" + nodeID);
			}
		}
		catch(Throwable t)
		{
			log.error("Failed to send new event(s) to eventd", t);
		}
	}
        
	/**
	 * <p>Delete the service and associated info from the database.</p>
         *
         * @param dbConn        JDBC database connection
	 * @param nodeID	NodeID associated with the interface
	 * @param ipAddr	IP Address associated with the interface
	 * @param serviceName 	Name of the service to delete
	 * @param serviceID	ID of the service to delete
	 * 
	 */
	private void deleteService(Connection dbConn, long nodeID, String ipAddr, String serviceName, 
                long serviceID) throws SQLException
	{
		Category log = ThreadCategory.getInstance(OutageWriter.class);

                PreparedStatement stmt = null;
	
                // There are other services exist on the node/interface pair. Just delete
                // the node/interface/service tuple.
                        
                // Deleting all the userNotified info associated with the nodeid/interface/service tuple
                stmt = dbConn.prepareStatement(OutageConstants.DB_DELETE_USERSNOTIFIED_ON_SERVICE);
                stmt.setLong(1, nodeID);
                stmt.setString(2, ipAddr);
                stmt.setLong(3, serviceID);
                stmt.executeUpdate();

                stmt.close();
		if (log.isDebugEnabled())
		        log.debug("handleDeleteService: deleted all userNotified info on: " 
                                + nodeID + "/" + ipAddr + "/" + serviceName);

                // Deleting all the notifications assocaited with the nodeid/interface/service tuple
                stmt = dbConn.prepareStatement(OutageConstants.DB_DELETE_NOTIFICATIONS_ON_SERVICE);
                stmt.setLong(1, nodeID);
                stmt.setString(2, ipAddr);
                stmt.setLong(3, serviceID);
                stmt.executeUpdate();

                stmt.close();
		if (log.isDebugEnabled())
		        log.debug("handleDeleteService: deleted all notifications on: " 
                                + nodeID + "/" + ipAddr + "/" + serviceName);

                // Deleting all the outages assocaited with the nodeid/interface/service tuple
                stmt = dbConn.prepareStatement(OutageConstants.DB_DELETE_OUTAGES_ON_SERVICE);
                stmt.setLong(1, nodeID);
                stmt.setString(2, ipAddr);
                stmt.setLong(3, serviceID);
                stmt.executeUpdate();

                stmt.close();
	        if (log.isDebugEnabled())
	                log.debug("handleDeleteService: deleted all outages on: " 
                                + nodeID + "/" + ipAddr + "/" + serviceName);

                // Deleting all the events assocaited with the nodeid/interface/service tuple
                stmt = dbConn.prepareStatement(OutageConstants.DB_DELETE_EVENTS_ON_SERVICE);
                stmt.setLong(1, nodeID);
                stmt.setString(2, ipAddr);
                stmt.setLong(3, serviceID);
                stmt.executeUpdate();

                stmt.close();
		if (log.isDebugEnabled())
		        log.debug("handleDeleteService: deleted all events on: " 
                                + nodeID + "/" + ipAddr + "/" + serviceName);

                // Deleting the service
                stmt = dbConn.prepareStatement(OutageConstants.DB_DELETE_SERVICE);
                stmt.setLong(1, nodeID);
                stmt.setString(2, ipAddr);
                stmt.setLong(3, serviceID);
                stmt.executeUpdate();

                stmt.close();
		if (log.isDebugEnabled())
		        log.debug("handleDeleteService: deleted the service based on: " 
                                + nodeID + "/" + ipAddr + "/" + serviceName);
			
	}

	/**
	 * <p>Verify the specified interface is primary interface.</p>
	 *
         * @param dbConn        JDBC database connection
	 * @param nodeID	NodeID associated with the interface
	 * @param ipAddr	IP Address associated with the interface
	 * 
	 */
	private boolean isSnmpPrimaryInterface(Connection dbConn, long nodeID, String ipAddr)
	{
		Category log = ThreadCategory.getInstance(OutageWriter.class);
		
		if (log.isDebugEnabled())
			log.debug("isSnmpPrimaryInterface: verify if the interface: " + ipAddr 
                                + " is the primary interface on node: " + nodeID);
                        
                char isPrimary = 'N';
                PreparedStatement stmt = null;
		try 
                {
                        stmt = dbConn.prepareStatement(OutageConstants.DB_QUERY_PRIMARY_INTERFACE);
		        stmt.setLong(1, nodeID);
                        stmt.setString(2, ipAddr);

                        ResultSet rs = stmt.executeQuery();

                        if (rs.next())
                        {
                                isPrimary = rs.getString(1).charAt(0);
		                if (log.isDebugEnabled())
			                log.debug("isSnmpPrimaryInterface: Interface " + ipAddr 
                                                + " is " + isPrimary + " on node: " + nodeID);
                        }
                        rs.close();
                        stmt.close();
		}
                catch (SQLException e)
                {
		        log.warn("isSnmpPrimaryInterface: get SQL Exception: ", e);
                }
                finally
                {
                        try
                        {
                                if (stmt != null)
                                        stmt.close();
                        }
                        catch (SQLException sqlE) {}
                }
                return isPrimary == 'P';
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
