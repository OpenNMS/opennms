// This file is part of the OpenNMS(R) QoSD OSS/J interface.
//
// Copyright (C) 2006-2007 Craig Gallen, 
//                         University of Southampton,
//                         School of Electronics and Computer Science
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// See: http://www.fsf.org/copyleft/lesser.html
//

package org.openoss.opennms.spring.qosd;

import java.util.Hashtable;
import java.util.Properties;

import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * <p>AlarmListConnectionManager interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface AlarmListConnectionManager {

	//states for status
	/** Constant <code>CONNECTED=0</code> */
	public static final int CONNECTED = 0;

	/** Constant <code>DISCONNECTED=1</code> */
	public static final int DISCONNECTED = 1;

	/** Constant <code>STOP=2</code> */
	public static final int STOP = 2;

	//states for send_status
	/** Constant <code>SEND=0</code> */
	public static final int SEND = 0;

	/** Constant <code>SENT=1</code> */
	public static final int SENT = 1;

	/** Constant <code>REBUILD=2</code> */
	public static final int REBUILD = 2;

	/**
	 * this method resets the alarm list in the AlarmMonitorBean and causes an alarmlist
	 * rebuilt event to be sent.
	 *
	 * @param _rebuilt_message message to include in the NotifyAlarmListRebuiltEvent
	 */
	public abstract void reset_list(String _rebuilt_message);

	/**
	 * Sends the AlarmList to the AlarmMonitorBean
	 *
	 * @param alarmList a {@link java.util.Hashtable} object.
	 */
	public abstract void send(Hashtable alarmList);

	/**
	 * Starts the ConnectionManagerThread
	 *
	 * @throws java.lang.IllegalStateException if any.
	 */
	public abstract void run() throws IllegalStateException;

	/**
	 * Initialises the ConnectionManagerThread
	 *
	 * @param props  The PropertiesLoader method which reads the Qosd.properties file
	 * @param env    the returned properties for setting up the connections to the AlarmMonitorBean
	 */
	public abstract void init(PropertiesLoader props, Properties env);

	/**
	 * Stops the ConnectionManagerThread
	 */
	/* Thread.stop() is unsafe so ending run method by changing
	 * the status variable that tells the run method to return
	 * and end execution.
	 */
	public abstract void kill();

	/**
	 * returns the threads current status
	 * states for status:
	 * CONNECTED = 0;
	 * DISCONNECTED = 1;
	 * STOP = 2;
	 *
	 * @return a int.
	 */
	public abstract int getStatus();
	
	/**
	 * Causes the thread supporting the connection Manager to start
	 */
	public void start();
	
	/**
	 * Makes a new empty alarm value object
	 * NOTE THIS IS A PATCH to proxy for JVTAlarmMonitorSession.makeAlarmValue()
	 *
	 * @return a {@link javax.oss.fm.monitor.AlarmValue} object.
	 */
	public abstract javax.oss.fm.monitor.AlarmValue makeAlarmValue();

	/**
	 * Makes a new alarm value object pre-populated with internal objects
	 * which have been made from a local invarient specification.
	 * NOTE THIS IS A PATCH to proxy for JVTAlarmMonitorSession
	 *
	 * @return a {@link javax.oss.fm.monitor.AlarmValue} object.
	 */
	public abstract javax.oss.fm.monitor.AlarmValue makeAlarmValueFromSpec();

	/**
	 * Used by jmx mbean QoSD to pass in Spring Application context
	 *
	 * @param m_context - application context for this bean to use
	 */
	public abstract void setapplicationcontext(ClassPathXmlApplicationContext m_context);

}
