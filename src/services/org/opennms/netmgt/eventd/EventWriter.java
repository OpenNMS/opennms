//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.eventd;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Types;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.util.*;

import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.MarshalException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.eventd.db.*;
import org.opennms.netmgt.eventd.datablock.*;

// castor generated classes
import org.opennms.netmgt.xml.event.*;

/**
 * <P>EventWriter loads the information in each 'Event' into the database</P>
 *
 * <P>While loading mutiple values of the same element into a single DB column,
 * the mutiple values are delimited by MULTIPLE_VAL_DELIM</P>
 *
 * <P>When an element and its attribute are loaded into a single DB column,
 * the value and the attribute are separated by a DB_ATTRIB_DELIM</P>
 *
 * <P>When using delimiters to append values, if the values already have the
 * delimiter, the delimiter in the value is escaped as in URLs</P>
 *
 * <P>Values for the '<parms>' block are loaded with each parm name and
 * parm value delimited with the NAME_VAL_DELIM</P>
 *
 * @see org.opennms.netmgt.eventd.db.Constants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.eventd.db.Constants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.eventd.db.Constants#NAME_VAL_DELIM
 *
 * @author 	<A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj</A>
 * @author	<A HREF="http://www.opennms.org">OpenNMS.org</A>
 */
final class EventWriter 
{
	//
	// Field sizes in the events table
	//
	private static final int EVENT_UEI_FIELD_SIZE		=  256;
	private static final int EVENT_HOST_FIELD_SIZE		=  256;
	private static final int EVENT_INTERFACE_FIELD_SIZE	=   16;
	private static final int EVENT_DPNAME_FIELD_SIZE	=   12;
	private static final int EVENT_SNMPHOST_FIELD_SIZE	=  256;
	private static final int EVENT_SNMP_FIELD_SIZE		=  256;
	private static final int EVENT_DESCR_FIELD_SIZE		= 4000;
	private static final int EVENT_LOGGRP_FIELD_SIZE	=   32;
	private static final int EVENT_LOGMSG_FIELD_SIZE	=  256;
	private static final int EVENT_PATHOUTAGE_FIELD_SIZE	= 1024;
	private static final int EVENT_CORRELATION_FIELD_SIZE	= 1024;
	private static final int EVENT_OPERINSTRUCT_FIELD_SIZE	= 1024;
	private static final int EVENT_AUTOACTION_FIELD_SIZE	=  256;
	private static final int EVENT_OPERACTION_FIELD_SIZE	=  256;
	private static final int EVENT_OPERACTION_MENU_FIELD_SIZE=  64;
	private static final int EVENT_NOTIFICATION_FIELD_SIZE	=  128;
	private static final int EVENT_TTICKET_FIELD_SIZE	=  128;
	private static final int EVENT_FORWARD_FIELD_SIZE	=  256;
	private static final int EVENT_MOUSEOVERTEXT_FIELD_SIZE	=   64;
	private static final int EVENT_ACKUSER_FIELD_SIZE	=  256;
	private static final int EVENT_SOURCE_FIELD_SIZE	=  128;

	/**
	 * The character to put in if the log or display is to be set
	 * to yes
	 */
	private static char 		MSG_YES = 'Y';

	/**
	 * The character to put in if the log or display is to be set
	 * to no
	 */
	private static char 		MSG_NO = 'N';

	/**
	 * The database connection
	 */
	private Connection		m_dbConn;

	/**
	 * SQL statement to get service id for a service name
	 */
	private PreparedStatement		m_getSvcIdStmt;

	/**
	 * SQL statement to get hostname for an ip from the ipinterface table
	 */
	private PreparedStatement		m_getHostNameStmt;

	/**
	 * SQL statement to get next event id from sequence
	 */
	private PreparedStatement		m_getNextEventIdStmt;

	/**
	 * SQL statement to get insert an event into the db
	 */
	private PreparedStatement		m_eventInsStmt;

	/**
	 * Sets the statement up for a String value.
	 *
	 * @param stmt	The statement to add the value to.
	 * @param ndx	The ndx for the value.
	 * @param value The value to add to the statement.
	 *
	 * @exception java.sql.SQLException Thrown if there is an error
	 * 	adding the value to the statement.
	 */
	private void set(PreparedStatement stmt, int ndx, String value)
		throws SQLException
	{
		if(value == null || value.length() == 0)
		{
			stmt.setNull(ndx, Types.VARCHAR);
		}
		else
		{
			stmt.setString(ndx, value);
		}
	}
	
	/**
	 * Sets the statement up for an integer type. If the 
	 * integer type is less than zero, then it is set to
	 * null!
	 *
	 * @param stmt	The statement to add the value to.
	 * @param ndx	The ndx for the value.
	 * @param value The value to add to the statement.
	 *
	 * @exception java.sql.SQLException Thrown if there is an error
	 * 	adding the value to the statement.
	 */
	private void set(PreparedStatement stmt, int ndx, int value)
		throws SQLException
	{
		if(value < 0)
		{
			stmt.setNull(ndx, Types.INTEGER);
		}
		else
		{
			stmt.setInt(ndx, value);
		}
	}

	/**
	 * Sets the statement up for a timestamp type.
	 *
	 * @param stmt	The statement to add the value to.
	 * @param ndx	The ndx for the value.
	 * @param value The value to add to the statement.
	 *
	 * @exception java.sql.SQLException Thrown if there is an error
	 * 	adding the value to the statement.
	 */
	private void set(PreparedStatement stmt, int ndx, Timestamp value)
		throws SQLException
	{
		if(value == null)
		{
			stmt.setNull(ndx, Types.TIMESTAMP);
		}
		else
		{
			stmt.setTimestamp(ndx, value);
		}
	}

	/**
	 * Sets the statement up for a character value.
	 *
	 * @param stmt	The statement to add the value to.
	 * @param ndx	The ndx for the value.
	 * @param value The value to add to the statement.
	 *
	 * @exception java.sql.SQLException Thrown if there is an error
	 * 	adding the value to the statement.
	 */
	private void set(PreparedStatement stmt, int ndx, char value)
		throws SQLException
	{
		stmt.setString(ndx, String.valueOf(value));
	}

	/**
	 * <P>This method is used to convert the service name into
	 * a service id. It first looks up the information from a
	 * service map of Eventd and if no match is found, by performing
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
	 * @see EventdConstants#SQL_DB_SVCNAME_TO_SVCID
	 *
	 */
	private int getServiceID(String name)
		throws SQLException
	{
		//
		// Check the name to make sure that it is not null
		//
		if(name == null)
			throw new NullPointerException("The service name was null");

		// ask persistd
		//
		int id = Eventd.getInstance().getServiceID(name);
		if (id != -1)
			return id;
		
		//
		// talk to the database and get the identifer
		//
		m_getSvcIdStmt.setString(1,name);
		ResultSet rset = m_getSvcIdStmt.executeQuery();
		if (rset.next())
		{
			id = rset.getInt(1);
		}

		// close result set
		rset.close();

		// inform persistd about the new find
		//
		if (id != -1)
			Eventd.getInstance().addServiceMapping(name, id);

		//
		// return the id to the caller
		//
		return id;
	}

	/**
	 * <P>This method is used to convert the event host into
	 * a hostname id by performing a lookup in the database. If
	 * the conversion is successful then the corresponding hosname
	 * will be returned to the caller.</P>
	 *
	 * @param name		The event host
	 *
	 * @return The hostname
	 *
	 * @exception java.sql.SQLException Thrown if there is an error accessing
	 * 	the stored data or the SQL text is malformed.
	 *
	 * @see EventdConstants#SQL_DB_HOSTIP_TO_HOSTNAME
	 *
	 */
	private String getHostName(String hostip)
		throws SQLException
	{
		
		//
		// talk to the database and get the identifer
		//
		String hostname = hostip;

		m_getHostNameStmt.setString(1,hostip);
		ResultSet rset = m_getHostNameStmt.executeQuery();
		if(rset.next())
		{
			hostname = rset.getString(1);
		}

		// close and free the result set
		//
		rset.close(); 
		
		// hostname can be null - if it is, return the ip
		//
		if (hostname == null)
			hostname = hostip;

		//
		// return the hostname to the caller
		//
		return hostname;
	}

	/**
	 * Insert values into the EVENTS table 
	 *
	 * @exception java.sql.SQLException Thrown if there is an error adding
	 *	the event to the database.
	 * @exception java.lang.NullPointerException Thrown if a required resource cannot
	 * 	be found in the properties file.
	 */
	private void add(Header eventHeader, Event event)
		throws SQLException
	{
		int eventID = -1;

		Category log = ThreadCategory.getInstance(EventWriter.class);

		// events next id from sequence
		//

		// Execute the statementto get the next event id
		//
		ResultSet rs = m_getNextEventIdStmt.executeQuery();
		rs.next();
		eventID = rs.getInt(1);
		rs.close();
		rs = null;

		if(log.isDebugEnabled())
			log.debug("EventWriter: DBID: " + eventID);

		synchronized(event)
		{
			event.setDbid(eventID);
		}
	
		//
		// Set up the sql information now
		//

		// eventID
		m_eventInsStmt.setInt(1, eventID);

		// eventUEI
		m_eventInsStmt.setString(2, Constants.format(event.getUei(), EVENT_UEI_FIELD_SIZE));
		
		// nodeID 
		int nodeid = (int)event.getNodeid();
		set(m_eventInsStmt, 3, event.hasNodeid() ? nodeid : -1);

		// eventTime
		java.sql.Timestamp eventTime = null;
		try
		{
			java.util.Date date = EventConstants.parseToDate(event.getTime());
			eventTime = new java.sql.Timestamp(date.getTime());
			m_eventInsStmt.setTimestamp(4, eventTime);
		}
		catch(java.text.ParseException pe)
		{
			log.warn("Failed to convert time " + event.getTime() + " to java.sql.Timestamp, Setting current time instead", pe);

			eventTime = new java.sql.Timestamp((new java.util.Date()).getTime());
			m_eventInsStmt.setTimestamp(4, eventTime);
		}

		//
		// Resolve the event host to a hostname using
		// the ipinterface table
		//
		String hostname = event.getHost();
		if(hostname != null)
		{
			try
			{
				hostname = getHostName(hostname);
			}
			catch(SQLException sqlE)
			{
				// hostname can be null - so do nothing
				// use the IP
				hostname = event.getHost();
			}
		}

		// eventHost
		set(m_eventInsStmt, 5, Constants.format(hostname, EVENT_HOST_FIELD_SIZE));

		// ipAddr
		set(m_eventInsStmt, 6, Constants.format(event.getInterface(), EVENT_INTERFACE_FIELD_SIZE));

		// eventDpName
		m_eventInsStmt.setString(7, (eventHeader != null)
						? Constants.format(eventHeader.getDpName(), EVENT_DPNAME_FIELD_SIZE)
						: "undefined");

		// eventSnmpHost
		set(m_eventInsStmt, 8, Constants.format(event.getSnmphost(), EVENT_SNMPHOST_FIELD_SIZE));

		//
		// convert the service name to a service id
		//
		int svcId = -1;
		if(event.getService() != null)
		{
			try
			{
				svcId = getServiceID(event.getService());
			}
			catch(SQLException sqlE)
			{
				log.warn("EventWriter.add: Error converting service name \""
						+ event.getService()
						+ "\" to an integer identifier, storing -1", sqlE);
			}
		}

		// service identifier
		set(m_eventInsStmt, 9, svcId);
		

		// eventSnmp
		if(event.getSnmp() != null)
			m_eventInsStmt.setString(10, SnmpInfo.format(event.getSnmp(), EVENT_SNMP_FIELD_SIZE));
		else
			m_eventInsStmt.setNull(10, Types.VARCHAR);

		// eventParms
		set(m_eventInsStmt, 11, (event.getParms() != null)
					? Parameter.format(event.getParms())
					: null);

		// eventCreateTime
		java.sql.Timestamp eventCreateTime = new java.sql.Timestamp((new java.util.Date()).getTime());
		m_eventInsStmt.setTimestamp(12, eventCreateTime);

		// eventDescr
		set(m_eventInsStmt, 13, Constants.format(event.getDescr(), EVENT_DESCR_FIELD_SIZE));

		// eventLoggroup
		set(m_eventInsStmt, 14, (event.getLoggroupCount() > 0)
					? Constants.format(event.getLoggroup(), EVENT_LOGGRP_FIELD_SIZE) 
					: null);

		// eventLogMsg
		// eventLog 
		// eventDisplay
		if (event.getLogmsg() != null)
		{
			// set log message
			set(m_eventInsStmt, 15, Constants.format(event.getLogmsg().getContent(), EVENT_LOGMSG_FIELD_SIZE));
			String logdest = event.getLogmsg().getDest();
			// if 'logndisplay' set both log and display
			// column to yes
			if (logdest.equals("logndisplay"))
			{
				set(m_eventInsStmt, 16, MSG_YES);
				set(m_eventInsStmt, 17, MSG_YES);
			}
			// if 'logonly' set log column to true
			else if (logdest.equals("logonly"))
			{
				set(m_eventInsStmt, 16, MSG_YES);
				set(m_eventInsStmt, 17, MSG_NO);
			}
			// if 'displayonly' set display column to true
			else if (logdest.equals("displayonly"))
			{
				set(m_eventInsStmt, 16, MSG_NO);
				set(m_eventInsStmt, 17, MSG_YES);
			}
			// if 'suppress' set both log and display to false
			else if (logdest.equals("suppress"))
			{
				set(m_eventInsStmt, 16, MSG_NO);
				set(m_eventInsStmt, 17, MSG_NO);
			}
		}
		else
		{
			m_eventInsStmt.setNull(15, Types.VARCHAR);

			// If this is an event that had no match in the event conf
			// mark it as to be logged and displayed so that there
			// are no events that slip through the system
			// without the user knowing about them

			set(m_eventInsStmt, 16, MSG_YES);
			set(m_eventInsStmt, 17, MSG_YES);
		}


		// eventSeverity
		set(m_eventInsStmt, 18, Constants.getSeverity(event.getSeverity()));

		// eventPathOutage
		set(m_eventInsStmt, 19, (event.getPathoutage() != null)
					? Constants.format(event.getPathoutage(), EVENT_PATHOUTAGE_FIELD_SIZE)
					: null);

		// eventCorrelation
		set(m_eventInsStmt, 20, (event.getCorrelation() != null)
					? org.opennms.netmgt.eventd.db.Correlation.format(event.getCorrelation(), EVENT_CORRELATION_FIELD_SIZE)
					: null);


		// eventSuppressedCount
		m_eventInsStmt.setNull(21, Types.INTEGER);

		// eventOperInstruct
		set(m_eventInsStmt, 22, Constants.format(event.getOperinstruct(), EVENT_OPERINSTRUCT_FIELD_SIZE));

		// eventAutoAction
		set(m_eventInsStmt, 23, (event.getAutoactionCount() > 0)
					? AutoAction.format(event.getAutoaction(), EVENT_AUTOACTION_FIELD_SIZE)
					: null);

		// eventOperAction / eventOperActionMenuText
		if(event.getOperactionCount() > 0)
		{		
			List a = new ArrayList();
			List b = new ArrayList();

			Enumeration enum = event.enumerateOperaction();
			while(enum.hasMoreElements())
			{
				Operaction eoa = (Operaction)enum.nextElement();
				a.add(eoa);
				b.add(eoa.getMenutext());
			}

			set(m_eventInsStmt, 24, OperatorAction.format(a, EVENT_OPERACTION_FIELD_SIZE));
			set(m_eventInsStmt, 25, Constants.format(b, EVENT_OPERACTION_MENU_FIELD_SIZE));
		}
		else
		{
			m_eventInsStmt.setNull(24, Types.VARCHAR);
			m_eventInsStmt.setNull(25, Types.VARCHAR);
		}
		
		// eventNotification, this column no longer needed
		m_eventInsStmt.setNull(26, Types.VARCHAR);
		

		// eventTroubleTicket / eventTroubleTicket state
		if (event.getTticket() != null)
		{
			set(m_eventInsStmt, 27, Constants.format(event.getTticket().getContent(), EVENT_TTICKET_FIELD_SIZE));
			int ttstate = 0;
			if (event.getTticket().getState().equals("on"))
				ttstate = 1;

			set(m_eventInsStmt, 28, ttstate);
		}
		else
		{
			m_eventInsStmt.setNull(27, Types.VARCHAR);
			m_eventInsStmt.setNull(28, Types.INTEGER);
		}


		// eventForward
		set(m_eventInsStmt, 29, (event.getForwardCount() > 0)
					? org.opennms.netmgt.eventd.db.Forward.format(event.getForward(), EVENT_FORWARD_FIELD_SIZE)
					: null);

		// event mouseOverText
		set(m_eventInsStmt, 30, Constants.format(event.getMouseovertext(), EVENT_MOUSEOVERTEXT_FIELD_SIZE));

		// eventAckUser
		if (event.getAutoacknowledge() != null && event.getAutoacknowledge().getState().equals("on"))
		{

			set(m_eventInsStmt, 31, Constants.format(event.getAutoacknowledge().getContent(), EVENT_ACKUSER_FIELD_SIZE));

			// eventAckTime - if autoacknowledge is present,
			// set time to event create time
			set(m_eventInsStmt, 32, eventCreateTime);
		}
		else
		{
			m_eventInsStmt.setNull(31, Types.INTEGER);
			m_eventInsStmt.setNull(32, Types.TIMESTAMP);
		}

		// eventSource
		set(m_eventInsStmt, 33, Constants.format(event.getSource(), EVENT_SOURCE_FIELD_SIZE));
		
		// execute
		m_eventInsStmt.executeUpdate();

		if(log.isDebugEnabled())
			log.debug("SUCCESSFULLY added " + event.getUei() + " related  data into the EVENTS table");
	}

	/**
	 * Constructor
	 */
	public EventWriter(String getNextEventIdStr)
			throws SQLException
	{
		// Get a database connection
		//
		m_dbConn = null;
		try
		{
			DatabaseConnectionFactory.init();
			m_dbConn = DatabaseConnectionFactory.getInstance().getConnection();
		}
		catch (MarshalException me)
		{
			ThreadCategory.getInstance(EventWriter.class).fatal("Marshall Exception getting database connection", me);
			throw new UndeclaredThrowableException(me);
		}
		catch (ValidationException ve)
		{
			ThreadCategory.getInstance(EventWriter.class).fatal("Validation Exception getting database connection", ve);
			throw new UndeclaredThrowableException(ve);
		}
		catch (ClassNotFoundException cnfE)
		{
			ThreadCategory.getInstance(EventWriter.class).fatal("Driver Class Not Found Exception getting database connection", cnfE);
			throw new UndeclaredThrowableException(cnfE);
		}
		catch (IOException ioE)
		{
			ThreadCategory.getInstance(EventWriter.class).fatal("IO Exception getting database connection", ioE);
			throw new UndeclaredThrowableException(ioE);
		}

		//
		// prepare the SQL statement
		//
		m_getSvcIdStmt    = m_dbConn.prepareStatement(EventdConstants.SQL_DB_SVCNAME_TO_SVCID);
		m_getHostNameStmt = m_dbConn.prepareStatement(EventdConstants.SQL_DB_HOSTIP_TO_HOSTNAME);
		m_getNextEventIdStmt  = m_dbConn.prepareStatement(getNextEventIdStr);
		m_eventInsStmt        = m_dbConn.prepareStatement(EventdConstants.SQL_DB_INS_EVENT);
		// set the database for rollback support
		//
		try
		{
			m_dbConn.setAutoCommit(false);
		}
		catch(SQLException se)
		{
			ThreadCategory.getInstance(EventWriter.class).warn("Unable to set auto commit mode");
		}
	}

	/**
	 * Close all the prepared statements
	 */
	public void close()
	{
		try
		{
			m_getSvcIdStmt.close();
			m_getHostNameStmt.close();
			m_getNextEventIdStmt.close();
			m_eventInsStmt.close();
			m_dbConn.close();
		}
		catch (SQLException sqle)
		{
			ThreadCategory.getInstance(EventWriter.class).warn("SQLException while closing prepared statements/database connection", sqle);
		}
	}

	/**
	 * The method that inserts the event into the database
	 * 
	 * @param eventHeader	the event header
	 * @param event		the actual event to be inserted
	 */
	public void persistEvent(Header eventHeader, Event event)
		throws SQLException
	{
		if(event != null)
		{
			Category log = ThreadCategory.getInstance(EventWriter.class);
			
			// Check value of <logmsg> attribute 'dest', if set to
			// "donotpersist" then simply return, the uei is not to be 
			// persisted to the database
			String logdest = event.getLogmsg().getDest();
			if (logdest.equals("donotpersist"))
			{
				log.debug("EventWriter: uei '" + event.getUei() + "' marked as 'doNotPersist'.");
				return;
			}
			else
			{
				if(log.isDebugEnabled())
				{
					log.debug("EventWriter dbRun for : " + event.getUei()
						  + " nodeid: " + event.getNodeid() + " ipaddr: "
						  + event.getInterface() + " serviceid: " + event.getService());
				}
			}
			
			try
			{
				add(eventHeader, event);

				// commit
				m_dbConn.commit();
			}
			catch(SQLException e)
			{
				log.warn("Error inserting event into the datastore", e);
				try
				{
					m_dbConn.rollback();
				}
				catch(Exception e2)
				{
					log.warn("Rollback of transaction failed!", e2);
				}

				throw e;
			}

			if(log.isDebugEnabled())
				log.debug("EventWriter finished for : " + event.getUei());
		}
	}
}

