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
// Modifications:
//
// 2003 Oct 21: Fixed typo in variable name.
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.discovery;

import java.lang.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.fiber.PausableFiber;

import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.DiscoveryConfigFactory;

import org.opennms.netmgt.eventd.EventIpcManagerFactory;

// castor classes generated from the discovery-configuration.xsd
import org.opennms.netmgt.config.discovery.*;

/**
 * This class is the main interface to the OpenNMS discovery service.
 * The class implements the <em>singleton</em> design pattern, in that there
 * is only one instance in any given virtual machine.  The service delays
 * the reading of configuration information until the service is started.
 *
 * @author <a href="mailto:weave@opennms.org">Brian Weaver</a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org</a>
 *
 */
public final class Discovery
	implements PausableFiber
{
	/**
	 * The log4j category used to log messages.
	 */
	private static final String 	LOG4J_CATEGORY	= "OpenNMS.Discovery";
	
	/**
	 * The string indicating the start of the comments in a
	 * line containing the IP address in a file URL
	 */
	private final static String COMMENT_STR		= " #";

	/**
	 * This character at the start of a line indicates a comment line in a URL file
	 */
	private final static char COMMENT_CHAR		= '#';

	/**
	 * The singular instance of the discovery service.
	 */
	private static final Discovery	m_singleton = new Discovery();

	/**
	 * The IP Generator queue
	 */
	private IPGenerator		m_generator;

	/**
	 * The fiber that generates and sends suspect events
	 */
	private SuspectEventGenerator	m_eventWriter;
	
	/**
	 * The class instance used to recieve new events from
	 * for the system.
	 */
	private BroadcastEventProcessor	m_eventReader;

	/**
	 * The ICMP Poller Manager class. This manager iterates
	 * over the address range, using the IPGenerator, checking
	 * for available systems.
	 */
	private PingManager		m_manager;

	/**
	 * The current status of this fiber
	 */
	private int			m_status;

	/**
	 * Constructs a new discovery instance.
	 */
	private Discovery()
	{
		m_generator = null;
		m_eventWriter = null;
		m_eventReader = null;
		m_manager = null;
		m_status      = START_PENDING;
	}

	/**
	 * <pre>The file URL is read and a 'specific IP' is added for each entry
	 * in this file. Each line in the URL file can be one of -
	 * <IP><space>#<comments>
	 * or
	 * <IP>
	 * or
	 * #<comments>
	 *
	 * Lines starting with a '#' are ignored and so are characters after
	 * a '<space>#' in a line.</pre>
	 *
	 * @param specifics	the list to add to
	 * @param url		the URL file 
	 * @param timeout	the timeout for all entries in this URL
	 * @param retries	the retries for all entries in this URL
	 */
	private boolean addToSpecificsFromURL(List specifics, String url, long timeout, int retries)
	{
		boolean bRet = true;

		try
		{
			// open the file indicated by the url
			URL fileURL = new URL(url);
			
			File file = new File(fileURL.getFile());
		
			//check to see if the file exists
			if(file.exists())
			{
				BufferedReader buffer = new BufferedReader(new FileReader(file));
			
				String ipLine = null;
				String specIP =null;
		
				// get each line of the file and turn it into a specific range
				while( (ipLine = buffer.readLine()) != null )
				{
					ipLine = ipLine.trim();
					if (ipLine.length() == 0 || ipLine.charAt(0) == COMMENT_CHAR)
					{
						// blank line or skip comment
						continue;
					}

					// check for comments after IP
					int comIndex = ipLine.indexOf(COMMENT_STR);
					if (comIndex == -1)
					{
						specIP = ipLine;
					}
					else 
					{
						specIP = ipLine.substring(0, comIndex);
						ipLine = ipLine.trim();
					}

					try
					{
						specifics.add(new IPPollAddress(specIP, timeout, retries));
					}
					catch(UnknownHostException e)
					{
						ThreadCategory.getInstance().warn("Unknown host \'" + specIP + "\' read from URL \'" + url.toString() + "\': address ignored");
					}
					
					specIP = null;
				}
			
				buffer.close();
			}
			else
			{
				// log something
				ThreadCategory.getInstance().warn("URL does not exist: " + url.toString());
				bRet = true;
			}
		}
		catch(MalformedURLException e)
		{
			ThreadCategory.getInstance().error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
			bRet = false;
		}
		catch(FileNotFoundException e)
		{
			ThreadCategory.getInstance().error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
			bRet = false;
		}
		catch(IOException e)
		{
			ThreadCategory.getInstance().error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
			bRet = false;
		}
		
		
		return bRet;
	}


	public synchronized void init()
	{
		if(m_manager != null)
			throw new IllegalStateException("The discovery service is already running");

		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log = ThreadCategory.getInstance();

		// Initialize the Database configuration factory and verify
		// that we can get a database connection.
		//
		java.sql.Connection ctest = null;
		try
		{
			DatabaseConnectionFactory.reload();
			ctest = DatabaseConnectionFactory.getInstance().getConnection();
		}
		catch (IOException ie)
		{
			log.fatal("IOException getting database connection", ie);
			throw new UndeclaredThrowableException(ie);
		}
		catch (MarshalException me)
		{
			log.fatal("Marshall Exception getting database connection", me);
			throw new UndeclaredThrowableException(me);
		}
		catch (ValidationException ve)
		{
			log.fatal("Validation Exception getting database connection", ve);
			throw new UndeclaredThrowableException(ve);
		}
		catch (SQLException sqlE)
		{
			log.fatal("SQL Exception getting database connection", sqlE);
			throw new UndeclaredThrowableException(sqlE);
		}
		catch (ClassNotFoundException cnfE)
		{
			log.fatal("Class Not Found Exception getting database connection", cnfE);
			throw new UndeclaredThrowableException(cnfE);
		}
		finally
		{
			try
			{
				if(ctest != null)
					ctest.close();
			}
			catch(Exception e) { }
		}
		
		// Initialize discovery configuration factory
		DiscoveryConfigFactory dFactory = null;
		try
		{
			DiscoveryConfigFactory.reload();
			dFactory = DiscoveryConfigFactory.getInstance();
		}
		catch(MarshalException ex)
		{
			ThreadCategory.getInstance().error("Failed to load discovery configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(ValidationException ex)
		{
			ThreadCategory.getInstance().error("Failed to load discovery configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(IOException ex)
		{
			ThreadCategory.getInstance().error("Failed to load discovery configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		
		// Get the discovery configuration from the factory.
		//
		DiscoveryConfiguration cfg = dFactory.getConfiguration();
		
		//
		// build the lists
		//
		List specifics = new LinkedList();
		List includes = new LinkedList();

		Enumeration e = cfg.enumerateSpecific();
		while(e.hasMoreElements())
		{
			Specific s = (Specific)e.nextElement();

			long timeout = 800L;
			if(s.hasTimeout())
				timeout = s.getTimeout();
			else if(cfg.hasTimeout())
				timeout = cfg.getTimeout();

			int retries = 3;
			if(s.hasRetries())
				retries = s.getRetries();
			else if(cfg.hasRetries())
				retries = cfg.getRetries();

			try
			{
				specifics.add(new IPPollAddress(s.getContent(), timeout, retries));
			}
			catch(UnknownHostException uhE)
			{
				ThreadCategory.getInstance().warn("Failed to convert address " + s.getContent(), uhE);
			}
		}

		e = cfg.enumerateIncludeRange();
		while(e.hasMoreElements())
		{
			IncludeRange ir = (IncludeRange)e.nextElement();

			long timeout = 800L;
			if(ir.hasTimeout())
				timeout = ir.getTimeout();
			else if(cfg.hasTimeout())
				timeout = cfg.getTimeout();

			int retries = 3;
			if(ir.hasRetries())
				retries = ir.getRetries();
			else if(cfg.hasRetries())
				retries = cfg.getRetries();

			try
			{
				includes.add(new IPPollRange(ir.getBegin(), ir.getEnd(), timeout, retries));
			}
			catch(UnknownHostException uhE)
			{
				ThreadCategory.getInstance().warn("Failed to convert address range ("+ir.getBegin()+", "+ir.getEnd()+")", uhE);
			}
		}

		// add addresses from the URL specified to specifics
		//
		e = cfg.enumerateIncludeUrl();
		while(e.hasMoreElements())
		{
			IncludeUrl url = (IncludeUrl)e.nextElement();

			long timeout = 800L;
			if(url.hasTimeout())
				timeout = url.getTimeout();
			else if(cfg.hasTimeout())
				timeout = cfg.getTimeout();

			int retries = 3;
			if(url.hasRetries())
				retries = url.getRetries();
			else if(cfg.hasRetries())
				retries = cfg.getRetries();

			addToSpecificsFromURL(specifics, url.getContent(), timeout, retries);
		}

		// Setup the exclusion range.
		//
		DiscoveredIPMgr.setExclusionList(cfg.getExcludeRange());

		// Setup the specifics list.
		//
		DiscoveredIPMgr.setSpecificsList(specifics); 
		
		// Build a generator
		m_generator = new IPGenerator(specifics,
					      includes,
					      cfg.getInitialSleepTime(),
					      cfg.getRestartSleepTime());

		// initialize the EventIpcManagerFactory
		EventIpcManagerFactory.init();

		// A queue for responses
		//
		FifoQueue responsive = new FifoQueueImpl();

		try
		{
			m_eventWriter = new SuspectEventGenerator(responsive, cfg.getRestartSleepTime());
		}
		catch(Exception ex)
		{
			ThreadCategory.getInstance().error("Failed to create event writer", ex);
			throw new UndeclaredThrowableException(ex);
		}

		try
		{
			m_eventReader = new BroadcastEventProcessor();
		}
		catch(Exception ex)
		{
			try
			{
				m_eventWriter.stop();
			}
			catch(Exception exx) { }

			ThreadCategory.getInstance().error("Failed to create event reader", ex);
			throw new UndeclaredThrowableException(ex);
		}

		try
		{
			m_manager = new PingManager(m_generator,
						    responsive,
						    (short)0xbeef,
						    cfg.getThreads(),
						    cfg.getPacketsPerSecond());

		}
		catch(Exception ex)
		{
			ThreadCategory.getInstance().error("Failed to create ping manager", ex);
			throw new UndeclaredThrowableException(ex);
		}
	}

	/**
	 * <p>This method is used to start the discovery process. When called
	 * the discovery configuration file is parsed and the internal state
	 * for discovery is setup. If the discovery process has already started
	 * then an exception is generated.</p>
	 *
	 * <p>The discovery process may be restarted if, and only if, is has first
	 * been stopped.</p>
	 *
	 * @throws java.lang.IllegalStateException Thrown if the service is already running.
	 * @throws java.lang.reflect.UndeclaredThrowableException Thrown if an
	 * 	error occurs that is not recoverable.
	 *
	 */
	public synchronized void start()
	{
		m_status = STARTING;

		ThreadCategory.setPrefix(LOG4J_CATEGORY);

		try
		{
			m_eventWriter.start();
		}
		catch(Exception ex)
		{
			ThreadCategory.getInstance().error("Failed to create event writer", ex);
			throw new UndeclaredThrowableException(ex);
		}

		try
		{
			m_manager.start();
		}
		catch(Exception ex)
		{
			try
			{
				m_eventWriter.stop();
			}
			catch(Exception exx) { }

			ThreadCategory.getInstance().error("Failed to create ping manager", ex);
			throw new UndeclaredThrowableException(ex);
		}


		m_status = RUNNING;
	}

	/**
	 * Stops the current discovery process. After a 
	 * successful call this method the discovery process
	 * may be restared.
	 *
	 */
	public synchronized void stop()
	{
		m_status      = STOP_PENDING;

		try
		{
			if(m_eventReader != null)
			{
				m_eventReader.close();
			}
		}
		catch(Exception e) { }

		try
		{
			if(m_eventWriter != null)
			{
				m_eventWriter.stop();
			}
		}
		catch(Exception e) { }

		m_eventWriter = null;
		m_eventReader = null;
		m_generator   = null;

		try
		{
			if(m_manager != null)
				m_manager.stop();
		}
		catch(Exception e) { }
		m_manager = null;
		m_status = STOPPED;
	}

	/**
	 * Returns the current status of the discovery process.
	 */
	public synchronized int getStatus()
	{
		return m_status;
	}

	/**
	 * Returns the name of this fiber.
	 */
	public String getName()
	{
		return "OpenNMS.Discovery";
	}

	/**
	 * Pauses the discovery process if its currently running
	 */
	public synchronized void pause()
	{
		if(m_status != RUNNING)
			return;

		m_status = PAUSE_PENDING;

		m_manager.pause();
		m_status = PAUSED;
	}

	/**
	 * Resumes the discovery process if its currently paused
	 */
	public synchronized void resume()
	{
		if(m_status != PAUSED)
			return;

		m_status = RESUME_PENDING;
		m_manager.resume();
		m_status = RUNNING;
	}

	/**
	 * Returns the singulare instance of the 
	 * discovery process
	 */
	public static Discovery getInstance()
	{
		return m_singleton;
	}
}
