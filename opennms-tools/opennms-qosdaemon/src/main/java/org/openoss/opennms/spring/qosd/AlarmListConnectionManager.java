/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.openoss.opennms.spring.qosd;

import java.util.Hashtable;
import java.util.Properties;

import javax.oss.fm.monitor.AlarmKey;
import javax.oss.fm.monitor.AlarmValue;

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
	public abstract void send(Hashtable<AlarmKey,AlarmValue> alarmList);

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
	 * @return a javax$oss$fm$monitor$AlarmValue object.
	 */
	public abstract javax.oss.fm.monitor.AlarmValue makeAlarmValue();

	/**
	 * Makes a new alarm value object pre-populated with internal objects
	 * which have been made from a local invarient specification.
	 * NOTE THIS IS A PATCH to proxy for JVTAlarmMonitorSession
	 *
	 * @return a javax$oss$fm$monitor$AlarmValue object.
	 */
	public abstract javax.oss.fm.monitor.AlarmValue makeAlarmValueFromSpec();

	/**
	 * Used by jmx mbean QoSD to pass in Spring Application context
	 *
	 * @param m_context - application context for this bean to use
	 */
	public abstract void setApplicationContext(ClassPathXmlApplicationContext m_context);

}
