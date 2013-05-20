/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

 package org.openoss.opennms.spring.qosd.ejb;

import java.util.Hashtable;
import java.util.Properties;

import javax.oss.fm.monitor.AlarmKey;
import javax.oss.fm.monitor.AlarmValue;

import org.openoss.opennms.spring.qosd.AlarmListConnectionManager;
import org.openoss.opennms.spring.qosd.PropertiesLoader;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * This class provides an implimentation of a AlarmListConnectionManager which
 * connects to an external AlarmMonitor bean in a J2ee container ( i.e.
 * in Jboss ). It proxys the calls to the AlarmListJ2eeConnectionManagerThread which
 * coes the actiual connection. This allows Spring wiring to be used to
 * select this or nother class as the AlarmListConnectionManager in QosD
 *
 * @author ranger
 * @version $Id: $
 */
public class AlarmListConnectionManagerJ2eeImpl implements AlarmListConnectionManager {

	AlarmListJ2eeConnectionManagerThread cmt;
	
	/**
	 * <p>Constructor for AlarmListConnectionManagerJ2eeImpl.</p>
	 */
	public AlarmListConnectionManagerJ2eeImpl() {
		cmt = new AlarmListJ2eeConnectionManagerThread();
	}

	/**
	 * <p>getStatus</p>
	 *
	 * @return a int.
	 */
        @Override
	public int getStatus() {
		return cmt.getStatus();
	}

	/** {@inheritDoc} */
        @Override
	public void init(PropertiesLoader props, Properties env) {
		cmt.init(props, env);
	}

	/**
	 * <p>kill</p>
	 */
        @Override
	public void kill() {
		cmt.kill();
	}

	/** {@inheritDoc} */
        @Override
	public void reset_list(String _rebuilt_message) {
		cmt.reset_list(_rebuilt_message);
	}

	/**
	 * <p>run</p>
	 *
	 * @throws java.lang.IllegalStateException if any.
	 */
        @Override
	public void run() throws IllegalStateException {
		cmt.run();
	}

	/** {@inheritDoc} */
        @Override
	public void send(Hashtable<AlarmKey,AlarmValue> alarmList) {
		cmt.send(alarmList);

	}
	
	/**
	 * Causes the thread supporting the connection Manager to start
	 */
        @Override
	public void start(){
		cmt.start();
	}
	
	/**
	 * Makes a new empty alarm value object
	 * NOTE THIS IS A PATCH to proxy for JVTAlarmMonitorSession.makeAlarmValue()
	 *
	 * @return a javax$oss$fm$monitor$AlarmValue object.
	 */
        @Override
	public  javax.oss.fm.monitor.AlarmValue makeAlarmValue(){
		return cmt.makeAlarmValue();
		
	}

	/**
	 * Makes a new alarm value object pre-populated with internal objects
	 * which have been made from a local invarient specification.
	 * NOTE THIS IS A PATCH to proxy for JVTAlarmMonitorSession
	 *
	 * @return a javax$oss$fm$monitor$AlarmValue object.
	 */
        @Override
	public javax.oss.fm.monitor.AlarmValue makeAlarmValueFromSpec(){
		return cmt.makeAlarmValueFromSpec();
	}
	
	// SPRING DAO SETTERS - NOT USED IN THIS VERSION

	/**
	 * {@inheritDoc}
	 *
	 * Used by jmx mbean QoSD to pass in Spring Application context
	 */
        @Override
	public  void setApplicationContext(ClassPathXmlApplicationContext m_context){
		cmt.setApplicationContext(m_context);
	}

}
